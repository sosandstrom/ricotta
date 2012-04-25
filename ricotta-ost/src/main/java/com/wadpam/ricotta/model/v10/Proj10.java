package com.wadpam.ricotta.model.v10;

import com.wadpam.ricotta.domain.Ctxt;
import com.wadpam.ricotta.domain.ProjLang;
import com.wadpam.ricotta.domain.ProjUser;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author os
 */
public class Proj10 {
    private String name;
    private String owner;
    
    private ProjLang defProjLang;
    private List<ProjLang> projLangs = new ArrayList<ProjLang>();
    
    private List<Ctxt> contexts = new ArrayList<Ctxt>();
    
    private List<Tokn10> tokens;
    
    private List<String> subsets;
    
    private List<ProjUser> users;

    public Proj10(String name, String owner) {
        this.name = name;
        this.owner = owner;
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

    public ProjLang getDefProjLang() {
        return defProjLang;
    }

    public void setDefProjLang(ProjLang defProjLang) {
        this.defProjLang = defProjLang;
    }

    public List<ProjLang> getProjLangs() {
        return projLangs;
    }

    public List<Ctxt> getContexts() {
        return contexts;
    }

    public void setContexts(List<Ctxt> contexts) {
        this.contexts = contexts;
    }

    public void setProjLangs(List<ProjLang> projLangs) {
        this.projLangs = projLangs;
    }

    public List<String> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<String> subsets) {
        this.subsets = subsets;
    }

    public List<Tokn10> getTokens() {
        return tokens;
    }

    public void setTokens(List<Tokn10> tokens) {
        this.tokens = tokens;
    }

    public List<ProjUser> getUsers() {
        return users;
    }

    public void setUsers(List<ProjUser> users) {
        this.users = users;
    }

    
}
