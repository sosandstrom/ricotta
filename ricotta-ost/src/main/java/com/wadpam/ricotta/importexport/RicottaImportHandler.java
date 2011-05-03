package com.wadpam.ricotta.importexport;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.dao.UberDao;

public class RicottaImportHandler extends DefaultHandler {
    public static final String     BRANCH   = "branch";
    public static final String     LANGUAGE = "language";
    public static final String     PROJ     = "project";

    static final Logger            LOG      = LoggerFactory.getLogger(RicottaImportHandler.class);

    private final UberDao          uberDao;

    private final Map<String, Key> keys     = new HashMap<String, Key>();

    private Object                 proj     = null, lang = null, branch = null, projLang = null;

    public RicottaImportHandler(UberDao uberDao) {
        this.uberDao = uberDao;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes a) throws SAXException {

        final String code = a.getValue("code");
        final String name = a.getValue("name");
        final String description = a.getValue("description");
        final String owner = a.getValue("owner");

        LOG.info("<{} name={}>", qName, name);

        if (LANGUAGE.equals(qName)) {
            if (null == branch) {
                lang = uberDao.createLang(code, name);
            }
            else {

            }
        }
        else if (PROJ.equals(qName)) {
            proj = uberDao.createProj(name, owner);
        }
        else if (BRANCH.equals(qName)) {
            branch = uberDao.createBranch(proj, name, description);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (LANGUAGE.equals(qName)) {
            if (null == branch) {
                lang = null;
            }
            else {
                projLang = null;
            }
        }
        else if (PROJ.equals(qName)) {
            proj = null;
        }
        else if (BRANCH.equals(qName)) {
            branch = null;
        }
    }
}
