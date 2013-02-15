/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.web;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.wadpam.ricotta.dao.ProjDao;
import com.wadpam.ricotta.dao.UberDaoBean;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Lang;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjUser;
import com.wadpam.ricotta.domain.Role;
import com.wadpam.ricotta.domain.Subset;
import com.wadpam.ricotta.domain.Template;
import com.wadpam.ricotta.model.v10.Blob10;
import com.wadpam.ricotta.model.v10.Me10;
import com.wadpam.ricotta.model.v10.Proj10;
import com.wadpam.ricotta.model.v10.Tokn10;
import com.wadpam.ricotta.velocity.Encoder;

/**
 * 
 * @author f94os
 */
@Controller
public class RestController {

    static final Logger            LOG              = LoggerFactory.getLogger(RestController.class);

    private UberDaoBean            uberDao;

    protected ProjDao              projDao;

    private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @RequestMapping(value = "blob/v10/{projName}", method = RequestMethod.GET)
    public ResponseEntity<Blob10> getUploadUrl(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String projName) {
        final String uploadUrl = blobstoreService.createUploadUrl(String.format("/api/blob/v10/%s", projName));
        Blob10 blob = new Blob10();
        blob.setUploadUrl(uploadUrl);
        return new ResponseEntity<Blob10>(blob, HttpStatus.OK);
    }

    @RequestMapping(value = "blob/v10/{projName}", method = RequestMethod.POST)
    public ResponseEntity<Blob10> uploadCallback(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String projName) {
        final Map<String, List<BlobKey>> blobMap = blobstoreService.getUploads(request);
        final Blob10 blob = new Blob10();

        if (1 == blobMap.size()) {
            for(List<BlobKey> keys : blobMap.values()) {
                if (1 == keys.size()) {
                    blob.setBlobKey(keys.get(0).getKeyString());

                    String accessUrl = String.format("%s://%s/api/blob/v10/%s?key=%s", request.getScheme(),
                            request.getHeader("Host"), projName, blob.getBlobKey());

                    blob.setAccessUrl(accessUrl);
                }
            }
        }
        else {
            return new ResponseEntity<Blob10>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Blob10>(blob, HttpStatus.OK);
    }

    @RequestMapping(value = "blob/v10/{projName}", method = RequestMethod.GET, params = {"key"})
    public void getBlob(HttpServletRequest request, HttpServletResponse response, @RequestParam String key,
            @PathVariable String projName) throws IOException {
        blobstoreService.serve(new BlobKey(key), response);
    }

    @RequestMapping(value = "project/v10/{projName}/context", method = RequestMethod.POST)
    public ResponseEntity<Object> createContext(@PathVariable String projName, @RequestParam String name,
            @RequestParam String description, @RequestParam String blobKey) {
        Object ctx = uberDao.createContext(projName, ProjectHandlerInterceptor.NAME_TRUNK, name, description, blobKey);
        return new ResponseEntity<Object>(ctx, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projName}/context", method = RequestMethod.DELETE)
    public ResponseEntity deleteContext(@PathVariable String projName, @RequestParam String keyString) {
        uberDao.deleteContext(keyString);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projName}/subset", method = RequestMethod.DELETE)
    public ResponseEntity deleteSubset(@PathVariable String projName, @RequestParam String keyString) {
        uberDao.deleteSubset(keyString);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value = "lang/v10", method = RequestMethod.POST)
    public ResponseEntity<Object> addLanguage(@RequestParam String langCode, @RequestParam String name) {
        Object langKey = uberDao.createLang(langCode, name);
        return new ResponseEntity(langKey, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projName}/projLang", method = RequestMethod.POST)
    public ResponseEntity<Object> addProjLang(@PathVariable String projName, @RequestParam String langCode,
            @RequestParam String defaultLang) {
        Object projLangKey = uberDao.addProjLang(projName, ProjectHandlerInterceptor.NAME_TRUNK, langCode, defaultLang);
        return new ResponseEntity(projLangKey, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10", method = RequestMethod.POST)
    public ResponseEntity<List<Proj10>> createProject(Principal principal, @RequestParam String name,
            @RequestParam String defaultLang) {
        String username = "Googlebot";
        if (null != principal) {
            username = principal.getName();
        }

        try {
            Object projKey = uberDao.createProj(name, username);

            // create a trunk branch
            Object branch = uberDao.createBranch(projKey, ProjectHandlerInterceptor.NAME_TRUNK, "The main branch");

            // create default language
            Object projLangKey = uberDao.addProjLang(name, ProjectHandlerInterceptor.NAME_TRUNK, defaultLang, null);
        }
        catch (IllegalArgumentException alreadyExists) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<List<Proj10>>(uberDao.getProjects(username), HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/token", method = RequestMethod.POST, params = {"name", "description"})
    public ResponseEntity<Tokn10> createToken(@PathVariable String projectName, @RequestParam String name,
            @RequestParam String description, @RequestParam String context) {
        final Tokn10 body = uberDao.createToken(projectName, ProjectHandlerInterceptor.NAME_TRUNK, name, description, context);
        if (null == body) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(body, HttpStatus.OK);
    }

    @RequestMapping(value = "lang/v10", method = RequestMethod.GET)
    public ResponseEntity<Collection<Lang>> getLanguages() {
        final List<Lang> body = uberDao.getLang();
        return new ResponseEntity<Collection<Lang>>(body, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10", method = RequestMethod.GET)
    public ResponseEntity<List<Proj10>> getProjects(Principal principal) {
        String username = "Googlebot";
        if (null != principal) {
            username = principal.getName();
        }
        return new ResponseEntity<List<Proj10>>(uberDao.getProjects(username), HttpStatus.OK);
    }

    @RequestMapping(value = "role/v10", method = RequestMethod.GET)
    public ResponseEntity<List<Role>> getRoles() {
        return new ResponseEntity<List<Role>>(uberDao.getRoles(), HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{name}/token", method = RequestMethod.GET)
    public ResponseEntity<Proj10> getTokens(Principal principal, @PathVariable String name) {
        String username = "Googlebot";
        if (null != principal) {
            username = principal.getName();
        }
        final Proj10 body = uberDao.getTokens(username, name, ProjectHandlerInterceptor.NAME_TRUNK);
        return new ResponseEntity(body, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/subsets", method = RequestMethod.GET)
    public ResponseEntity<List<Subset>> getSubset(@PathVariable String projectName) {
        List<Subset> body = uberDao.getSubset(projectName, ProjectHandlerInterceptor.NAME_TRUNK);
        return new ResponseEntity(body, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/subsets", method = RequestMethod.POST)
    public ResponseEntity<Object> createSubset(@PathVariable String projectName, @RequestParam String name,
            @RequestParam String description) {
        Object subsetKey = uberDao.createSubset(projectName, ProjectHandlerInterceptor.NAME_TRUNK, name, description);
        return new ResponseEntity(subsetKey, HttpStatus.OK);
    }

    @RequestMapping(value = "me/v10", method = RequestMethod.GET)
    public ResponseEntity<Me10> me(Principal principal, @RequestParam(value = "path", defaultValue = "/index.html") String path) {
        final Me10 me = new Me10();
        LOG.debug("path={}, principal is {}", path, principal);
        if (null == principal) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-Logout-URL", UserServiceFactory.getUserService().createLoginURL(path));
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }
        me.setUrl(UserServiceFactory.getUserService().createLogoutURL(path));
        me.setEmail(principal.getName());
        return new ResponseEntity<Me10>(me, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/user", method = RequestMethod.POST)
    public ResponseEntity<Object> createUser(@PathVariable String projectName, @RequestParam Long role, @RequestParam String email) {
        Object roleKey = null;
        try {
            roleKey = uberDao.createUser(projectName, email, role);
        }
        catch (IllegalArgumentException alreadyExists) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<Object>(roleKey, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/user/{keyString}", method = RequestMethod.POST)
    public ResponseEntity<ProjUser> updateUser(@PathVariable String keyString, @RequestParam Long role) {
        final ProjUser body = uberDao.updateUser(keyString, role);
        if (null == body) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(body, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/token/{tokenId}", method = RequestMethod.POST, params = {"name",
            "description"})
    public ResponseEntity<Tokn10> updateToken(@PathVariable String projectName, @PathVariable Long tokenId,
            @RequestParam String name, @RequestParam String description, @RequestParam String context,
            @RequestParam String subsets, @RequestParam(value = "separator", defaultValue = ",") String separator) {
        final String[] subs = subsets.isEmpty() ? null : subsets.split(separator);
        final Tokn10 body = uberDao.updateToken(projectName, ProjectHandlerInterceptor.NAME_TRUNK, tokenId, name, description,
                context, subs);
        if (null == body) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(body, HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/token/{tokenId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteToken(@PathVariable String projectName, @PathVariable Long tokenId) {
        uberDao.deleteToken(ProjectHandlerInterceptor.NAME_TRUNK, projectName, tokenId);

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/token/{tokenId}", method = RequestMethod.POST, params = {"langCode"})
    public ResponseEntity<Tokn10> updateTranslation(@PathVariable String projectName, @PathVariable Long tokenId,
            @RequestParam String langCode, @RequestParam(value = "value", required = false) String value) {

        final List<String> body = uberDao
                .updateTrans(projectName, ProjectHandlerInterceptor.NAME_TRUNK, tokenId, langCode, value);
        if (null == body) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projectName}/token/{tokenId}", method = RequestMethod.POST, params = {"langCode[]",
            "value[]"})
    public ResponseEntity<Tokn10> updateTranslation(@RequestParam(value = "langCode[]") String[] langCode,
            @RequestParam(value = "value[]") String[] value, @PathVariable String projectName, @PathVariable Long tokenId) {
        for(int i = 0; i < langCode.length; i++) {
            updateTranslation(projectName, tokenId, langCode[i], value[i]);
        }
        return new ResponseEntity<Tokn10>(HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projName}/token/{tokenId}", method = RequestMethod.GET)
    public ResponseEntity<Tokn10> getToken(@PathVariable String projName, @PathVariable Long tokenId) {
        final Tokn10 tokn10 = uberDao.getToken10(projName, ProjectHandlerInterceptor.NAME_TRUNK, tokenId);

        return new ResponseEntity<Tokn10>(tokn10, HttpStatus.OK);
    }

    @RequestMapping(value = "template/v10", method = RequestMethod.GET)
    public ResponseEntity<List<Template>> getTemplates() {
        return new ResponseEntity<List<Template>>(uberDao.getTemplate(), HttpStatus.OK);
    }

    @RequestMapping(value = "template/v10", method = RequestMethod.POST)
    public ResponseEntity<Object> addTemplate(@RequestParam String name, @RequestParam String description,
            @RequestParam String body) {
        Object tmplKey = uberDao.createTempl(name, description, body);
        return new ResponseEntity(tmplKey, HttpStatus.OK);
    }

    @RequestMapping(value = "template/v10/{templateName}", method = RequestMethod.POST)
    public ResponseEntity<Template> updateTemplate(@PathVariable String templateName, @RequestParam String description,
            @RequestParam String body) {
        Template tpl = uberDao.updateTemplate(templateName, description, body);
        if (null == tpl) {
            return new ResponseEntity<Template>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Template>(tpl, HttpStatus.OK);
    }

    public void setUberDao(UberDaoBean uberDao) {
        this.uberDao = uberDao;
    }

    @RequestMapping(value = "project/v10/{projName}/export", method = RequestMethod.GET)
    public void export(HttpServletRequest request, HttpServletResponse response, @PathVariable String projName)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());
        final Proj proj = uberDao.getProj(projName);
        model.put("p", proj);
        model.put("uberDao", uberDao);

        GenerateController.renderTemplate("ricotta-export-proj", model, response, "text/xml; charset=UTF-8");

    }

    @RequestMapping(value = "project/v10/{projName}/delete", method = RequestMethod.POST)
    public ResponseEntity<String> deleteProject(@PathVariable String projName) {
        final Proj pro = uberDao.getProj(projName);
        if (null == pro) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        uberDao.deleteProj(projName);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projName}/user", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteProjUser(@PathVariable String projName, @RequestParam String email) {
        uberDao.deleteProjectUser(projName, email);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "project/v10/{projName}/lang", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteProjectLang(@PathVariable String projName, @RequestParam String langCode) {
        uberDao.deleteProjLanguage(ProjectHandlerInterceptor.NAME_TRUNK, projName, langCode);
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
