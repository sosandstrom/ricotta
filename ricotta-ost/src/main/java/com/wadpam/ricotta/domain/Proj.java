package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;

@Entity
public class Proj extends AEDPrimaryKeyEntity {
    private static final long serialVersionUID = 8370138695054738061L;

    @Id
    String                    name;

    String                    owner;

    @Override
    public String toString() {
        return "Proj{" + getKeyString() + ',' + name + ',' + owner + '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public Object getSimpleKey() {
        return name;
    }
}
