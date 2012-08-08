package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.wadpam.ricotta.domain.Trans;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.TransformerConfigurationException;
import org.xml.sax.SAXException;

/**
 * Implementation of Business Methods related to entity Trans.
 */
public class TransDaoBean extends GeneratedTransDaoImpl implements TransDao {

    @Override
    public void xmlGenerateEntities(Writer willBeNull, Object appArg0, Iterable<Trans> cursor) throws SAXException, IOException, TransformerConfigurationException {
        
        // first, run thru all to collect projects
        final Set<Key> projects = new HashSet<Key>();
        Key projLangKey, branchKey, ancestorKey;
        for (Trans t : cursor) {
            // Trans -> ProjLang -> Branch -> Proj
            if (null != t) {
                projLangKey = t.getParentKey();
                if (null != projLangKey) {
                    branchKey = projLangKey.getParent();
                    if (null != branchKey) {
                        ancestorKey = branchKey.getParent();
                        if (null != ancestorKey) {
                            projects.add(ancestorKey);
                        }
                        else {
                            LOG.warn("Orphan Branch: {}", branchKey);
                        }
                    }
                    else {
                        LOG.warn("Orphan ProjLang: {}", projLangKey);
                    }
                }
                else {
//                    LOG.warn("Orphan Trans: {}", t);
                }
            }
        }
        
        final DatastoreService datastore = getDatastoreService();
        for (Key projKey : projects) {
            LOG.debug("         Entities for Trans proj={}", projKey.getName());
            Query projTransQuery = new Query(getTableName());
            projTransQuery.setAncestor(projKey);
            PreparedQuery pq = datastore.prepare(projTransQuery);
            QueryResultIterable projTransCursor = new CursorIterable(asQueryResultIterable(pq));
            super.xmlGenerateEntities(null, appArg0, projTransCursor);
        }
        
    }

    

}
