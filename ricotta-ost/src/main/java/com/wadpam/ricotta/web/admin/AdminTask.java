/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.web.admin;

import java.util.Map;

/**
 *
 * @author os
 */
public interface AdminTask {
    Object processTask(String taskName, Map parameterMap);
}
