package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.ProjectUser;

/**
 * Implementation of Business Methods related to entity ProjectUser.
 */
public class ProjectUserDaoBean extends GeneratedProjectUserDaoImpl implements ProjectUserDao {

    @Override
    public ProjectUser persist(Key projectKey, String email) {
        ProjectUser pu = findByProjectUser(projectKey, email);
        if (null == pu) {
            pu = new ProjectUser();
            pu.setProject(projectKey);
            pu.setUser(email);
            persist(pu);
        }
        return pu;
    }

}
