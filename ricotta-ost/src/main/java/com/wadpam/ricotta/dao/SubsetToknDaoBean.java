package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Business Methods related to entity SubsetTokn.
 */
public class SubsetToknDaoBean 
	extends GeneratedSubsetToknDaoImpl
		implements SubsetToknDao 
{

	// TODO: implement your Business Methods here
    @Override
    public List<Key> findKeysByBranchKeyTokenId(Key branchKey, Long tokenId) {
        final List<Key> list = new ArrayList<Key>();
        
        final PreparedQuery pq = prepare(true, branchKey, null, null, false);
        for (Entity st : pq.asIterable()) {
            if (tokenId.equals(st.getKey().getId())) {
                list.add(st.getKey());
            }
        }
        
        return list;
    }

}
