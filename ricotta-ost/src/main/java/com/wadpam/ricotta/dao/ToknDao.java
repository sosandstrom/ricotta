package com.wadpam.ricotta.dao;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Tokn;

/**
 * Business Methods interface for entity Tokn.
 */
public interface ToknDao extends GeneratedToknDao {

    List<Tokn> findByBranchName(Key branchKey, String tokenName);

}
