package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
public class SubsetTokn extends AEDPrimaryKeyEntity<Long> {
    private static final long serialVersionUID = -2998638040524758351L;
    @Parent(kind = "Subset")
    Key                       subset;

    @Id
    Long                      tokn;

    @Override
    public Long getSimpleKey() {
        return tokn;
    }

    @Override
    public Class<Long> getIdClass() {
        return Long.class;
    }

    @Override
    public Object getParentKey() {
        return subset;
    }

    public Key getSubset() {
        return subset;
    }

    public void setSubset(Key subset) {
        this.subset = subset;
    }

    public Long getTokn() {
        return tokn;
    }

    public void setTokn(Long tokn) {
        this.tokn = tokn;
    }

}
