/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.model.v10;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author os
 */
public class Tokn10 {
    private Long id;
    private String name;
    private String description;
    private String context;
    private final Map<String, String> trans = new HashMap<String, String>();
    private final Set<String> subsets = new TreeSet<String>();

    public Tokn10(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Map<String, String> getTrans() {
        return trans;
    }

    public Set<String> getSubsets() {
        return subsets;
    }
    
    
}
