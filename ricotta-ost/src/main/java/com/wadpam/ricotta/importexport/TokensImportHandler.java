package com.wadpam.ricotta.importexport;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.ricotta.dao.UberDao;
import com.wadpam.ricotta.domain.Branch;
import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.Proj;
import com.wadpam.ricotta.domain.ProjLang;

public class TokensImportHandler extends DefaultHandler {
    public static final String        BRANCH    = "branch";
    public static final String        LANGUAGE  = "language";
    public static final String        PROJ      = "project";
    public static final String        CTXT      = "context";
    public static final String        TOKN      = "token";
    public static final String        TOKENS    = "tokens";
    public static final String        TRANS     = "translation";

    static final Logger               LOG       = LoggerFactory.getLogger(TokensImportHandler.class);

    private final StringBuffer        cdata     = new StringBuffer();

    private final UberDao             uberDao;
    private final String              projName;
    private final String              branchName;
    private final String updatedBy;
    private boolean                   active    = false;

    private final Map<String, Object> langs     = new HashMap<String, Object>();
    private final Map<String, Object> ctxts     = new HashMap<String, Object>();
    private final Map<String, Object> projLangs = new HashMap<String, Object>();

    private Object                    proj      = null, lang = null, branch = null, tokn = null, subset = null,
            projLangKey = null;

    private Long                      toknId    = null;
    private String                    templName = null, templDescription = null;

    public TokensImportHandler(UberDao uberDao, String projName, String branchName,
            String updatedBy) {
        this.uberDao = uberDao;
        this.projName = projName;
        this.branchName = branchName;
        this.updatedBy = updatedBy;
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
        final String email = a.getValue("email");

        if (active) {
            LOG.info("<{} name={}>", qName, name);
        }

        if (!active && LANGUAGE.equals(qName)) {
            if (null == branch) {
                // lang = uberDao.createLang(code, name);
                // langs.put(code, lang);
            }
            else {
                Object projLang = KeyFactory.createKey((Key) branch, ProjLang.class.getSimpleName(), code);
                projLangs.put(code, projLang);
            }
        }
        else if (!active && PROJ.equals(qName)) {
            if (projName.equals(name)) {
                proj = KeyFactory.createKey(Proj.class.getSimpleName(), name);
            }
        }
        else if (!active && BRANCH.equals(qName)) {
            if (null != proj && branchName.equals(name)) {
                branch = KeyFactory.createKey((Key) proj, Branch.class.getSimpleName(), name);
            }
        }
        else if (!active && TOKENS.equals(qName)) {
            active = (null != proj && null != branch);
        }
        else if (!active && CTXT.equals(qName)) {
            Object ctxt = KeyFactory.createKey((Key) branch, Ctxt.class.getSimpleName(), name);
            ctxts.put(name, ctxt);
        }
        else if (active && TOKN.equals(qName)) {
            toknId = Long.parseLong(id);
            if (null == subset) {
                Object ctxtKey = (null != context) ? ctxts.get(context) : null;
                tokn = uberDao.createTokn(branch, toknId, name, description, ctxtKey);
            }
        }
        else if (active && TRANS.equals(qName)) {
            projLangKey = projLangs.get(code);
            // entity is created in endElement, as cdata is required
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
        else if (TRANS.equals(qName)) {
            uberDao.createTrans(projLangKey, toknId, cdata.toString(), updatedBy);
        }
        else if (TOKENS.equals(qName)) {
            active = false;
        }

        cdata.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (active) {
            String s = new String(ch, start, length);
            cdata.append(s);
            if (0 < cdata.length()) {
                LOG.info("appended to {}", cdata.toString());
            }
        }
    }

}
