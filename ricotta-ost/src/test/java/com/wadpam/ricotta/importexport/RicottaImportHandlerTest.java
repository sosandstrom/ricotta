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

import org.easymock.EasyMock;
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

        final Object KEY_TEMPL_SAI = "Templ.sai";
        expect(
                uberMock.createTempl(EasyMock.eq("strings_android_inherit"),
                        EasyMock.eq("Android strings.xml with parent default translations"), (String) EasyMock.notNull()))
                .andReturn(KEY_TEMPL_SAI).once();

        final Object KEY_PROJ = "Proj.ricotta";
        expect(uberMock.createProj("ricotta", "s.o.sandstrom@gmail.com")).andReturn(KEY_PROJ).once();
        final Object KEY_USER_TEST = "User.test";
        expect(uberMock.createUser(KEY_PROJ, "test@example.com")).andReturn(KEY_USER_TEST).once();
        final Object KEY_BRANCH = "Branch.trunk";
        expect(uberMock.createBranch(KEY_PROJ, "trunk", "the trunk")).andReturn(KEY_BRANCH).once();

        final Object KEY_PROJLANG_EN = "ProjLang.en";
        expect(uberMock.createProjLang(KEY_BRANCH, "en", null, KEY_EN)).andReturn(KEY_PROJLANG_EN).once();
        final Object KEY_PROJLANG_EN_GB = "ProjLang.en_GB";
        expect(uberMock.createProjLang(KEY_BRANCH, "en_GB", KEY_EN, KEY_EN_GB)).andReturn(KEY_PROJLANG_EN_GB).once();
        final Object KEY_PROJLANG_SV = "ProjLang.sv";
        expect(uberMock.createProjLang(KEY_BRANCH, "sv", KEY_EN, KEY_SV)).andReturn(KEY_PROJLANG_SV).once();

        final Object KEY_CONTEXT_LOGIN = "Ctxt.login";
        expect(
                uberMock.createCtxt(
                        KEY_BRANCH,
                        "login",
                        "The Login Form",
                        "AMIfv97f5_9u972oyevGaylFV1EmbLg7Q7zd8VtuNhu8w5chE9Jj5W6tZwcnDebF9ORlzjUhc95c2GOj13R05rAQJZ90gME2fCA9dI-XMrVzvKfQLabaKSjTeMqK3X1-a-BYU-p8qBVS2tkoVLUr4rC-W8v1dj4GCw"))
                .andReturn(KEY_CONTEXT_LOGIN).once();

        // Project tokn
        final Object KEY_TOKN_14 = "Tokn.14";
        expect(uberMock.createTokn(KEY_BRANCH, 14L, "Project", "The Project Entity", null)).andReturn(KEY_TOKN_14).once();
        final Object KEY_TRANS_14_EN_GB = "Tokn.14.en_GB";
        expect(uberMock.createTrans(KEY_PROJLANG_EN_GB, 14L, "Project")).andReturn(KEY_TRANS_14_EN_GB).once();
        final Object KEY_TRANS_14_SV = "Tokn.14.sv";
        expect(uberMock.createTrans(KEY_PROJLANG_SV, 14L, "Projekt")).andReturn(KEY_TRANS_14_SV).once();

        // appTitle tokn
        final Object KEY_TOKN_13 = "Tokn.13";
        expect(
                uberMock.createTokn(KEY_BRANCH, 13L, "appTitle", "The Application title as displayed to the user",
                        KEY_CONTEXT_LOGIN)).andReturn(KEY_TOKN_13).once();
        final Object KEY_TRANS_13_EN = "Tokn.13.en";
        expect(uberMock.createTrans(KEY_PROJLANG_EN, 13L, "Ricotta")).andReturn(KEY_TRANS_13_EN).once();

        // ricotta-ost subset
        final Object KEY_SUBSET_OST = "Subset.ost";
        expect(uberMock.createSubset(KEY_BRANCH, "ricotta-ost", "${s.description}")).andReturn(KEY_SUBSET_OST).once();
        expect(uberMock.createSubsetTokn(KEY_SUBSET_OST, 13L)).andReturn("SubsetTokn.ost.13").once();
        expect(uberMock.createSubsetTokn(KEY_SUBSET_OST, 14L)).andReturn("SubsetTokn.ost.14").once();

        // maven-plugin subset
        final Object KEY_SUBSET_MAVEN_PLUGIN = "Subset.maven-plugin";
        expect(uberMock.createSubset(KEY_BRANCH, "ricotta-maven-plugin", "${s.description}")).andReturn(KEY_SUBSET_MAVEN_PLUGIN)
                .once();
        expect(uberMock.createSubsetTokn(KEY_SUBSET_MAVEN_PLUGIN, 13L)).andReturn("SubsetTokn.maven-plugin.13").once();

        replay(uberMock);

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        InputStream in = getClass().getResourceAsStream("/OldProject.xml");
        parser.parse(in, handler);

    }
}
