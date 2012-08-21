/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.web.admin;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter which does nothing in processTask
 * @author os
 */
public class AdminTaskAdapter implements AdminTask{
    protected static final Logger LOG = LoggerFactory.getLogger(AdminTaskAdapter.class);
    
    /**
     * Override this to implement the processing of task with specified name
     * @param taskName the name of the task to process
     * @return an object to serialize into JSON for response body
     */
    @Override
    public Object processTask(String taskName, Map parameterMap) {
        LOG.warn("Unknown task: {}", taskName);
        return null;
    }
}
