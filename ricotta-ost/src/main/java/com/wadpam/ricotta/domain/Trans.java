package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
public class Trans extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = -5659004527175071885L;
    @Parent(kind = "ProjLang")
    Key                       projLang;

    @Id
    Long                      token;

    String                    local;

    @Override
    public String toString() {
        return "Translation{" + local + ", tokenKey=" + token + ", langKey=" + projLang + '}';
    }

    @Override
    public Object getSimpleKey() {
        return token;
    }

    @Override
    public Object getParentKey() {
        return projLang;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Long getToken() {
        return token;
    }

    public void setToken(Long token) {
        this.token = token;
    }

    public Key getProjLang() {
        return projLang;
    }

    public void setProjLang(Key projLang) {
        this.projLang = projLang;
    }

}
