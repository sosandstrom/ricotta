package com.wadpam.ricotta.model;

import com.wadpam.ricotta.domain.Language;

public class ProjectLanguageModel {
    private Language language;
    private String   parentName;

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Language getLanguage() {
        return language;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentName() {
        return parentName;
    }
}
