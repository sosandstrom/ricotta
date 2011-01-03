package com.wadpam.ricotta.web;

import java.beans.PropertyEditorSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class RicottaBindingInitializer implements WebBindingInitializer {
    static final Logger LOG = LoggerFactory.getLogger(RicottaBindingInitializer.class);

    @Override
    public void initBinder(WebDataBinder binder, WebRequest request) {
        binder.registerCustomEditor(Key.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                LOG.debug("Converting {} to Key", text);
                setValue(KeyFactory.stringToKey(text));
                LOG.debug("Converted {} to {}", text, getValue());
            }
        });
    }

}
