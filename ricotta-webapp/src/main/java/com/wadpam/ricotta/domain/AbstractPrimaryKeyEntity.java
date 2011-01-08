package com.wadpam.ricotta.domain;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public abstract class AbstractPrimaryKeyEntity implements PrimaryKeyEntity {

    public String getKeyString() {
        return (null != getPrimaryKey()) ? KeyFactory.keyToString((Key) getPrimaryKey()) : null;
    }

}
