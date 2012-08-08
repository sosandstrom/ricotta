package com.wadpam.ricotta.web;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author os
 */
public class StringToKeyConverter implements Converter<String, Key> {

    @Override
    public Key convert(String source) {
        return KeyFactory.stringToKey(source);
    }

}
