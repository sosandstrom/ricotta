package com.wadpam.ricotta.web;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.model.TransModel;
import com.wadpam.ricotta.velocity.Encoder;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value="{projName}/branch/{branchName}/lang/{langCode}")
public class GenerateController extends AbstractDaoController {
    static final Logger LOG = LoggerFactory.getLogger(GenerateController.class);
    
    public void init() throws Exception {

        ExtendedProperties configuration = new ExtendedProperties();
        LOG.info("Initializing Velocity");

        final Properties p = new Properties();
        p.setProperty("resource.loader", "dao");

        p.setProperty("dao.resource.loader.description", "Ricotta DAO Resource Loader");
        p.setProperty("dao.resource.loader.class", "com.wadpam.ricotta.velocity.DaoResourceLoader");

        Velocity.init(p);
    }    

    @RequestMapping(value = {"templ/{templName}", "templ/{templName}/index.html"}, method = RequestMethod.GET)
    public String getTempl(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        return renderTemplBySubset(request, response);
    }

    @RequestMapping(value = {"templ/{templName}/subset/{subsetName}","templ/{templName}/subset/{subsetName}/index.html"}, method = RequestMethod.GET)
    public String getTemplSubset(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        return renderTemplBySubset(request, response);
    }
    
    /**
     * File-Upload your template to merge with Ricotta data model.
     * @param request
     * @param response
     * @param template the file input field should be named 'template'
     * @return null
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws Exception 
     */
    @RequestMapping(value = {"templ/my"}, method = RequestMethod.POST)
    public String postTempl(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("template") String templateText) 
            throws IOException, ResourceNotFoundException, ParseErrorException, Exception {
        
            Template templ = new Template();
            templ.setData(templateText);
            VelocityContext model = buildModel(request);

            renderTemplate(templ, model, response);
        return null;
    }
    
    protected VelocityContext buildModel(HttpServletRequest request) {
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());
        final Key projKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_PROJKEY);
        model.put(ProjectHandlerInterceptor.KEY_PROJKEY, projKey);
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        model.put(ProjectHandlerInterceptor.KEY_BRANCHKEY, branchKey);
        final Key templKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_TEMPLKEY);
        model.put(ProjectHandlerInterceptor.KEY_TEMPLKEY, templKey);
        final Key subsetKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_SUBSETKEY);
        model.put(ProjectHandlerInterceptor.KEY_SUBSETKEY, subsetKey);
        LOG.debug("rendering templ " + templKey.getName());

        final String langCode = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
        final ProjLang projLang = projLangDao.findByPrimaryKey(branchKey, langCode);
        final Collection<TransModel> trans = uberDao.loadTrans(branchKey, subsetKey, projLang, null);
        model.put("translations", trans);
        
        // meta data:
        model.put("language", projLang);
        model.put("project", projKey);
        model.put("template", templKey);
        // legacy
        model.put("mall", templKey);
        
        return model;
    }

    protected String renderTemplBySubset(HttpServletRequest request, HttpServletResponse response)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        final VelocityContext model = buildModel(request);
        final Key templKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_TEMPLKEY);

        renderTemplate(templKey.getName(), model, response);
        return null;
    }

    protected static void renderTemplate(String templName, VelocityContext model, HttpServletResponse response)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        renderTemplate(templName, model, response, "text/plain; charset=UTF-8");
    }

    protected static void renderTemplate(String templName, VelocityContext model, HttpServletResponse response, String contentType)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(contentType); // mall.getMimeType());
        final PrintWriter writer = response.getWriter();
        Template template = Velocity.getTemplate(templName);
        template.merge(model, writer);
        writer.close();
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected static void renderTemplate(Template template, VelocityContext model, 
            HttpServletResponse response)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8");
        final PrintWriter writer = response.getWriter();
        template.merge(model, writer);
        writer.close();
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
