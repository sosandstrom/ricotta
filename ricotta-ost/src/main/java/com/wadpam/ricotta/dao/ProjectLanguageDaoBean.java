package com.wadpam.ricotta.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.ProjectLanguage;

/**
 * Implementation of Business Methods related to entity ProjectLanguage.
 */
public class ProjectLanguageDaoBean extends GeneratedProjectLanguageDaoImpl implements ProjectLanguageDao {

    @Override
    public ProjectLanguage persist(Key parentKey, Key projectKey, Key languageKey) {
        ProjectLanguage pl = findByLanguageProject(languageKey, projectKey);
        if (null == pl) {
            pl = new ProjectLanguage();
            pl.setLanguage(languageKey);
            pl.setProject(projectKey);
            pl.setParent(parentKey);

            persist(pl);
        }
        return pl;
    }

    @Override
    public ProjectLanguage findDefault(Key projectKey) {
        for(ProjectLanguage pl : findByProject(projectKey)) {
            if (null == pl.getParent()) {
                return pl;
            }
        }
        throw new IllegalArgumentException("No such default for project " + projectKey);
    }

}
