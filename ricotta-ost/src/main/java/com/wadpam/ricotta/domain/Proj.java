package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.domain.AEDStringEntity;

@Entity
public class Proj extends AEDStringEntity {
    private static final long serialVersionUID = 8370138695054738061L;

    @Id
    String                    name;

    String                    owner;

    @Override
    public String toString() {
        return "Proj{" + name + ',' + owner + '}';
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
    public String getSimpleKey() {
        return name;
    }
}
