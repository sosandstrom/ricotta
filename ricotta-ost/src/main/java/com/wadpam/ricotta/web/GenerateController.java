package com.wadpam.ricotta.web;

import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@Controller
public class GenerateController extends AbstractDaoController {
    static final Logger LOG = LoggerFactory.getLogger(GenerateController.class);

    @RequestMapping(value = "/proj/{projName}/branch/{branchName}/lang/{langCode}/templ/{templName}/index.html", method = RequestMethod.GET)
    public String getTempl(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        return renderTemplBySubset(request, response);
    }

    @RequestMapping(value = "/proj/{projName}/branch/{branchName}/lang/{langCode}/templ/{templName}/subset/{subsetName}/index.html", method = RequestMethod.GET)
    public String getTemplSubset(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        return renderTemplBySubset(request, response);
    }

    @RequestMapping(value = "/projects/{projName}/languages/{langCode}/templates/{templName}/index.html", method = RequestMethod.GET)
    public String getMall(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        return renderTemplBySubset(request, response);
    }

    @RequestMapping(value = "/projects/{projName}/languages/{langCode}/templates/{templName}/artifacts/{subsetName}/index.html", method = RequestMethod.GET)
    public String getMallArtifact(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        return renderTemplBySubset(request, response);
    }

    protected String renderTemplBySubset(HttpServletRequest request, HttpServletResponse response)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        final Key templKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_TEMPLKEY);
        final Key branchKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_BRANCHKEY);
        final Key subsetKey = (Key) request.getAttribute(ProjectHandlerInterceptor.KEY_SUBSETKEY);
        LOG.debug("rendering templ " + templKey.getName());
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());

        final String langCode = (String) request.getAttribute(ProjectHandlerInterceptor.KEY_LANGCODE);
        final ProjLang projLang = projLangDao.findByPrimaryKey(branchKey, langCode);
        final Collection<TransModel> trans = uberDao.loadTrans(branchKey, subsetKey, projLang, null);
        model.put("translations", trans);

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

}
