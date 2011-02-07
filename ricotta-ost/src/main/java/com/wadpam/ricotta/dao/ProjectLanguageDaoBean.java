package com.wadpam.ricotta.dao;

import java.util.List;

import net.sf.mardao.api.dao.FilterEqual;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.ProjectLanguage;

/**
 * Implementation of Business Methods related to entity ProjectLanguage.
 */
public class ProjectLanguageDaoBean extends GeneratedProjectLanguageDaoImpl implements ProjectLanguageDao {

    // @Override
    // public ProjectLanguage persist(Key parentKey, Key projectKey, Key languageKey) {
    // ProjectLanguage pl = findByLanguageProject(languageKey, projectKey);
    // if (null == pl) {
    // pl = new ProjectLanguage();
    // pl.setLanguage(languageKey);
    // pl.setProject(projectKey);
    // pl.setParent(parentKey);
    //
    // persist(pl);
    // }
    // return pl;
    // }

    @Override
    public ProjectLanguage findDefault(Key projectKey, Key versionKey) {
        return findBy(new FilterEqual(COLUMN_NAME_PROJECT, projectKey), new FilterEqual(COLUMN_NAME_VERSION, versionKey),
                new FilterEqual(COLUMN_NAME_PARENT, null));
    }

    @Override
    public List<ProjectLanguage> findByProjectParentVersion(Key project, Key parent, Key version) {
        return findBy(null, false, -1, 0, new FilterEqual(COLUMN_NAME_PROJECT, project), new FilterEqual(COLUMN_NAME_VERSION,
                version), new FilterEqual(COLUMN_NAME_PARENT, parent));
    }

    @Override
    public List<ProjectLanguage> findByProjectVersion(Key project, Key version) {
        return findBy(null, false, -1, 0, new FilterEqual(COLUMN_NAME_PROJECT, project), new FilterEqual(COLUMN_NAME_VERSION,
                version));
    }

}
