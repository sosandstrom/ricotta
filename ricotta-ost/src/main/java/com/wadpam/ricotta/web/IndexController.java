package com.wadpam.ricotta.web;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.wadpam.ricotta.importexport.RicottaImportHandler;
import com.wadpam.ricotta.importexport.TokensImportHandler;
import com.wadpam.ricotta.velocity.Encoder;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by Ola on Nov 12, 2010
 */
@Controller
public class IndexController extends AbstractDaoController {

    static final Logger            LOG              = LoggerFactory.getLogger(IndexController.class);

    private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String getIndex(Model model) {
        model.addAttribute("loginURL", UserServiceFactory.getUserService().createLoginURL("/classic/index.html"));
        return "index";
    }

    @RequestMapping(value = "/commercial_license.html", method = RequestMethod.GET)
    public String getLicense(Model model) {
        return "commercial_license";
    }

    @RequestMapping(value = "/logout.html", method = RequestMethod.GET)
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        String logoutUrl = UserServiceFactory.getUserService().createLogoutURL("/classic/loggedout.html");
        response.sendRedirect(logoutUrl);
    }

    @RequestMapping(value = "/loggedout.html", method = RequestMethod.GET)
    public String loggedout() {
        return "loggedout";
    }

    @RequestMapping(value = "/export.xml", method = RequestMethod.GET)
    public String exportAll(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());

        model.put("uberDao", uberDao);

        GenerateController.renderTemplate("ricotta-export-all", model, response, "text/xml; charset=UTF-8");
        return null;
    }

    @RequestMapping(value = "/exportOld.xml", method = RequestMethod.GET)
    public String exportOld(HttpServletRequest request, HttpServletResponse response) throws ResourceNotFoundException,
            ParseErrorException, Exception {
        final VelocityContext model = new VelocityContext();
        model.put("encoder", new Encoder());

        model.put("uberDao", uberDao);

        GenerateController.renderTemplate("ricotta-export-old", model, response, "text/xml; charset=UTF-8");
        return null;
    }

    @RequestMapping(value = "/import-XML.html", method = RequestMethod.GET)
    public String uploadImportXML(Model model) {

        // create upload URL for Blob
        model.addAttribute("action", blobstoreService.createUploadUrl("/import-XML.html"));

        return "uploadXML";
    }

    @RequestMapping(value = "/import-blobKey.html", method = RequestMethod.GET)
    public String importBlob() {
        return "importBlobKey";
    }

    @RequestMapping(value = "/import-blobKey.html", method = RequestMethod.POST)
    public String uploadedImportBlob(HttpServletRequest request) throws IOException, ParserConfigurationException, SAXException {
        LOG.debug("upload retry GET");
        final String blobKeyString = request.getParameter("blobKeyString");
        BlobKey blobKey = new BlobKey(blobKeyString);
        return uploadedImport(blobKey);
    }

    @RequestMapping(value = "/import-XML.html", method = RequestMethod.POST)
    public String uploadedImportXML(HttpServletRequest request) throws IOException, ParserConfigurationException, SAXException {
        LOG.debug("upload callback POST");
        Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(request);
        BlobKey blobKey = blobs.get("ricottaXML");
        return uploadedImport(blobKey);
    }

    protected String uploadedImport(BlobKey blobKey) {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withUrl("/blobWorker.html").param("blobKey", blobKey.getKeyString()));
        return "redirect:/index.html";
    }

    @RequestMapping(value = "/blobWorker.html", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void blobWorker(@RequestParam(value = "blobKey") String blobKey) throws IOException, ParserConfigurationException,
            SAXException {
        BlobKey key = new BlobKey(blobKey);
        InputStream in = new BlobstoreInputStream(key);
        importXML(blobKey, in);
    }

    protected void importXML(String blobKey, InputStream in) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        DefaultHandler dh = new RicottaImportHandler(blobKey, uberDao);
        parser.parse(in, dh);
    }

    @RequestMapping(value = "/tokensWorker.html", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void tokensWorker(@RequestParam(value = "blobKey") String blobKey, @RequestParam(value = "proj") String projName,
            @RequestParam(value = "branch") String branchName) throws IOException, ParserConfigurationException, SAXException {
        BlobKey key = new BlobKey(blobKey);
        InputStream in = new BlobstoreInputStream(key);
        importXML(in, projName, branchName);
    }

    protected void importXML(InputStream in, String projName, String branchName) throws ParserConfigurationException,
            SAXException, IOException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        final String updatedBy = null != user ? user.getEmail() : "[ANONYMOUS]";

        DefaultHandler dh = new TokensImportHandler(uberDao, projName, branchName, updatedBy);
        parser.parse(in, dh);
    }

}
