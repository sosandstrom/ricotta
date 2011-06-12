package com.wadpam.ricotta.importexport;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Role;

public class RicottaImportHandler extends DefaultHandler {
    public static final String        BRANCH    = "branch";
    public static final String        LANGUAGE  = "language";
    public static final String        PROJ      = "project";
    public static final String        CTXT      = "context";
    public static final String        TOKN      = "token";
    public static final String        SUBSET    = "subset";
    public static final String        TEMPL     = "template";
    public static final String        USER      = "user";
    public static final String        TOKENS    = "tokens";

    static final Logger               LOG       = LoggerFactory.getLogger(RicottaImportHandler.class);

    private final StringBuffer        cdata     = new StringBuffer();

    private final UberDao             uberDao;
    private final String              blobKey;
    private boolean                   active    = true;

    private final Map<String, Object> langs     = new HashMap<String, Object>();
    private final Map<String, Object> ctxts     = new HashMap<String, Object>();
    private final Map<String, Object> projLangs = new HashMap<String, Object>();

    private Object                    proj      = null, lang = null, branch = null, tokn = null, subset = null,
            projLangKey = null;
    private String                    projName, branchName;

    private Long                      toknId    = null;
    private String                    templName = null, templDescription = null;

    public RicottaImportHandler(String blobKey, UberDao uberDao) {
        this.uberDao = uberDao;
        this.blobKey = blobKey;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes a) throws SAXException {

        final String code = a.getValue("code");
        final String name = a.getValue("name");
        final String description = a.getValue("description");
        final String owner = a.getValue("owner");
        final String defaultLang = a.getValue("default");
        final String blobKeyString = a.getValue("blobKey");
        final String id = a.getValue("id");
        final String email = a.getValue("email");
        final String role = a.getValue("role");

        if (active) {
            LOG.debug("<{} name={}>", qName, name);
        }

        if (LANGUAGE.equals(qName)) {
            if (null == branch) {
                lang = uberDao.createLang(code, name);
                langs.put(code, lang);
            }
            else {
                Object defaultLangKey = (null != defaultLang) ? langs.get(defaultLang) : null;
                Object langKey = langs.get(code);
                Object projLang = uberDao.createProjLang(branch, code, defaultLangKey, langKey);
                projLangs.put(code, projLang);
            }
        }
        else if (PROJ.equals(qName)) {
            projName = name;
            proj = uberDao.createProj(name, owner);
        }
        else if (BRANCH.equals(qName)) {
            branchName = name;
            branch = uberDao.createBranch(proj, name, description);
        }
        else if (CTXT.equals(qName)) {
            Object ctxt = uberDao.createCtxt(branch, name, description, blobKeyString);
            ctxts.put(name, ctxt);
        }
        else if (TOKN.equals(qName)) {
            toknId = Long.parseLong(id);
            if (null == subset) {
                // done in task.
            }
            else {
                uberDao.createSubsetTokn(subset, toknId);
            }
        }
        else if (SUBSET.equals(qName)) {
            subset = uberDao.createSubset(branch, name, description);
        }
        else if (TEMPL.equals(qName)) {
            templName = name;
            templDescription = description;
            // template is created in endElement, as cdata is required
        }
        else if (USER.equals(qName)) {
            uberDao.createUser(proj, email, null != role ? Long.parseLong(role) : Role.ROLE_DEVELOPER);
        }
        else if (TOKENS.equals(qName)) {
            active = false;
            // Queue queue = QueueFactory.getDefaultQueue();
            // queue.add(url("/tokensWorker.html").param("blobKey", blobKey).param("proj", projName).param("branch", branchName));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (LANGUAGE.equals(qName)) {
            if (null == branch) {
                lang = null;
            }
            else {
            }
        }
        else if (PROJ.equals(qName)) {
            proj = null;
        }
        else if (BRANCH.equals(qName)) {
            ctxts.clear();
            projLangs.clear();
            branch = null;
        }
        else if (TOKN.equals(qName)) {
            if (null == subset) {
                tokn = null;
            }
            else {

            }
        }
        else if (SUBSET.equals(qName)) {
            subset = null;
        }
        else if (TEMPL.equals(qName)) {
            uberDao.createTempl(templName, templDescription, cdata.toString());
        }
        else if (TOKENS.equals(qName)) {
            active = true;
        }

        cdata.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (active) {
            String s = new String(ch, start, length);
            cdata.append(s.trim());
            if (0 < cdata.length()) {
                LOG.info("appended to {}", cdata.toString());
            }
        }
    }

}
