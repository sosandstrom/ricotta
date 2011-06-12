package com.wadpam.ricotta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;
import net.sf.mardao.api.domain.PrimaryKeyEntity;

@Entity
public class Role extends AEDPrimaryKeyEntity implements PrimaryKeyEntity {
    private static final long serialVersionUID = -7733471256247686317L;

    public static final long  GRANT_ALL        = 0x1000;
    public static final long  GRANT_VIEW       = 0x01;
    public static final long  GRANT_TRANS      = 0x02;
    public static final long  GRANT_TOKN       = 0x04;
    public static final long  GRANT_MANAGE     = 0x08;
    public static final long  GRANT_DESTROY    = 0x10;

    /** VIEWER has no rights granted */
    public static final long  ROLE_VIEWER      = GRANT_VIEW;
    /** TRANSLATOR can add and edit translations */
    public static final long  ROLE_TRANSLATOR  = ROLE_VIEWER | GRANT_TRANS;
    /** DEVELOPER can also add and edit tokens */
    public static final long  ROLE_DEVELOPER   = ROLE_TRANSLATOR | GRANT_TOKN | GRANT_MANAGE;
    /** OWNER has full access to the project */
    public static final long  ROLE_OWNER       = ROLE_DEVELOPER | GRANT_DESTROY;
    /** ADMIN can do everything */
    public static final long  ROLE_ADMIN       = ROLE_OWNER | GRANT_ALL;

    @Id
    Long                      grants;

    String                    name;

    @Override
    public Object getSimpleKey() {
        return grants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final Long getGrants() {
        return grants;
    }

    public final void setGrants(Long grants) {
        this.grants = grants;
    }

}