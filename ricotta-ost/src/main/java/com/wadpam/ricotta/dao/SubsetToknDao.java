package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import java.util.List;

/**
 * Business Methods interface for entity SubsetTokn.
 * This interface is generated by mardao, but edited by developers.
 * It is not overwritten by the generator once it exists.
 *
 * Generated on 2011-09-07T19:29:36.183+0700.
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
public interface SubsetToknDao extends GeneratedSubsetToknDao<Key, Key> {

	// TODO: declare your Business Methods here

    List<Key> findKeysByBranchKeyTokenId(Key branchKey, Long tokenId);
}
