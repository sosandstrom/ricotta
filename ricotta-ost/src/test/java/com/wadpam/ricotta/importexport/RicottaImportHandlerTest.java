package com.wadpam.ricotta.importexport;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.wadpam.ricotta.dao.UberDao;

public class RicottaImportHandlerTest {
    UberDao              uberMock;
    RicottaImportHandler handler;

    @Before
    public void setUp() throws Exception {
        uberMock = createMock(UberDao.class);
        handler = new RicottaImportHandler(uberMock);
    }

    @After
    public void tearDown() throws Exception {
        verify(uberMock);
    }

    @Test
    public void testOldProject() throws SAXException, IOException, ParserConfigurationException {
        final Object KEY_EN = "Lang.en";
        expect(uberMock.createLang("en", "English")).andReturn(KEY_EN).once();
        final Object KEY_EN_GB = "Lang.en_GB";
        expect(uberMock.createLang("en_GB", "British English")).andReturn(KEY_EN_GB).once();
        final Object KEY_SV = "Lang.sv";
        expect(uberMock.createLang("sv", "Swedish")).andReturn(KEY_SV).once();

        final Object KEY_PROJ = "Proj.ricotta";
        expect(uberMock.createProj("ricotta", "s.o.sandstrom@gmail.com")).andReturn(KEY_PROJ).once();
        final Object KEY_BRANCH = "Branch.trunk";
        expect(uberMock.createBranch(KEY_PROJ, "trunk", "the trunk")).andReturn(KEY_BRANCH).once();

        replay(uberMock);

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        InputStream in = getClass().getResourceAsStream("/OldProject.xml");
        parser.parse(in, handler);

    }
}
