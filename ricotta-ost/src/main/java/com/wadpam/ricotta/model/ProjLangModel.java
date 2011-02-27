package com.wadpam.ricotta.model;

import com.wadpam.ricotta.domain.Lang;

public class ProjLangModel {
    private Lang   lang;
    private String defaultCode;

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public Lang getLang() {
        return lang;
    }

    public void setDefaultCode(String defaultName) {
        this.defaultCode = defaultName;
    }

    public String getDefaultCode() {
        return defaultCode;
    }
}
