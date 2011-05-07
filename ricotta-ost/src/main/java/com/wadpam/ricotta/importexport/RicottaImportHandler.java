package com.wadpam.ricotta.importexport;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.wadpam.ricotta.dao.UberDao;

public class RicottaImportHandler extends DefaultHandler {
    public static final String        BRANCH    = "branch";
    public static final String        LANGUAGE  = "language";
    public static final String        PROJ      = "project";
    public static final String        CTXT      = "context";
    public static final String        TOKN      = "token";
    public static final String        SUBSET    = "subset";
    public static final String        TRANS     = "translation";
    public static final String        TEMPL     = "template";

    static final Logger               LOG       = LoggerFactory.getLogger(RicottaImportHandler.class);

    private final StringBuffer        cdata     = new StringBuffer();

    private final UberDao             uberDao;

    private final Map<String, Object> langs     = new HashMap<String, Object>();
    private final Map<String, Object> ctxts     = new HashMap<String, Object>();
    private final Map<String, Object> projLangs = new HashMap<String, Object>();

    private Object                    proj      = null, lang = null, branch = null, tokn = null, subset = null,
            projLangKey = null;

    private Long                      toknId    = null;
    private String                    templName = null, templDescription = null;

    public RicottaImportHandler(UberDao uberDao) {
        this.uberDao = uberDao;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes a) throws SAXException {

        final String code = a.getValue("code");
        final String name = a.getValue("name");
        final String description = a.getValue("description");
        final String owner = a.getValue("owner");
        final String defaultLang = a.getValue("default");
        final String blobKeyString = a.getValue("blobKey");
        final String context = a.getValue("context");
        final String id = a.getValue("id");

        LOG.info("<{} name={}>", qName, name);

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
            proj = uberDao.createProj(name, owner);
        }
        else if (BRANCH.equals(qName)) {
            branch = uberDao.createBranch(proj, name, description);
        }
        else if (CTXT.equals(qName)) {
            Object ctxt = uberDao.createCtxt(branch, name, description, blobKeyString);
            ctxts.put(name, ctxt);
        }
        else if (TOKN.equals(qName)) {
            toknId = Long.parseLong(id);
            if (null == subset) {
                Object ctxtKey = (null != context) ? ctxts.get(context) : null;
                tokn = uberDao.createTokn(branch, toknId, name, description, ctxtKey);
            }
            else {
                uberDao.createSubsetTokn(subset, toknId);
            }
        }
        else if (SUBSET.equals(qName)) {
            subset = uberDao.createSubset(branch, name, description);
        }
        else if (TRANS.equals(qName)) {
            projLangKey = projLangs.get(code);
            // entity is created in endElement, as cdata is required
        }
        else if (TEMPL.equals(qName)) {
            templName = name;
            templDescription = description;
            // template is created in endElement, as cdata is required
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
        else if (TRANS.equals(qName)) {
            uberDao.createTrans(projLangKey, toknId, cdata.toString());
        }
        else if (TEMPL.equals(qName)) {
            uberDao.createTempl(templName, templDescription, cdata.toString());
        }

        cdata.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String s = new String(ch, start, length);
        cdata.append(s.trim());
        if (0 < cdata.length()) {
            LOG.info("appended to {}", cdata.toString());
        }
    }

}
