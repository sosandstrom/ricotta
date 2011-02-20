package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;
import net.sf.mardao.api.domain.PrimaryKeyEntity;

import com.google.appengine.api.datastore.Key;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
public class Language extends AEDPrimaryKeyEntity implements PrimaryKeyEntity {
    private static final long serialVersionUID = -7733471256247686317L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Key                       key;

    String                    code;

    String                    name;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public Object getSimpleKey() {
        return key;
    }

}