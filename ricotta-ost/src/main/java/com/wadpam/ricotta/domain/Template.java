package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;
import net.sf.mardao.api.domain.PrimaryKeyEntity;

@Entity
public class Template extends AEDPrimaryKeyEntity implements PrimaryKeyEntity {
    private static final long serialVersionUID = 7906382709967388339L;

    @Id
    String                    name;

    String                    description;

    String                    body;

    String                    mimeType;

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + name + ',' + description + '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Object getSimpleKey() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}
