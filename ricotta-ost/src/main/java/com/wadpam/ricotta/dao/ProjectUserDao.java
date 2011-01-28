package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.ProjectUser;

/**
 * Business Methods interface for entity ProjectUser.
 */
public interface ProjectUserDao extends GeneratedProjectUserDao {

    ProjectUser persist(Key key, String string);

}
