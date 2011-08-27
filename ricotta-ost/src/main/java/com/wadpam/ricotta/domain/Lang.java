package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.domain.AEDStringEntity;

@Entity
public class Lang extends AEDStringEntity {
    private static final long serialVersionUID = -7733471256247686317L;

    @Id
    String                    code;

    String                    name;

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
    public String getSimpleKey() {
        return code;
    }

}