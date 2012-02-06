package com.wadpam.ricotta.dao;

import java.util.List;

import net.sf.mardao.api.dao.Expression;
import net.sf.mardao.api.dao.FilterEqual;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.wadpam.ricotta.domain.Tokn;

/**
 * Implementation of Business Methods related to entity Tokn.
 */
public class ToknDaoBean extends GeneratedToknDaoImpl implements ToknDao {

    @Override
    public List<Tokn> findByBranchName(Key branchKey, String tokenName) {
        final Expression nameFilter = new FilterEqual(COLUMN_NAME_NAME, tokenName);
        final PreparedQuery query = prepare(false, branchKey, null, COLUMN_NAME_DESCRIPTION, true, nameFilter);
        return asIterable(query, -1, 0);
    }

    @Override
    public List<Tokn> findSortedByBranch(Key branchKey) {
        final PreparedQuery query = prepare(false, branchKey, null, COLUMN_NAME_NAME, true);
        return asIterable(query, -1, 0);
        
    }
}
