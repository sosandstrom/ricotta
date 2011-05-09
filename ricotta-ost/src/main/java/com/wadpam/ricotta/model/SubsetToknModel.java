package com.wadpam.ricotta.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Key;
import com.wadpam.ricotta.domain.Subset;
import com.wadpam.ricotta.domain.SubsetTokn;
import com.wadpam.ricotta.domain.Tokn;

public class SubsetToknModel extends Tokn {
    static final Logger            LOG     = LoggerFactory.getLogger(SubsetToknModel.class);

    private final Tokn             wrapped;
    private final List<SubsetTokn> subsets = new ArrayList<SubsetTokn>();

    public SubsetToknModel(Tokn wrapped, List<Subset> subsets) {
        this.wrapped = wrapped;
        for(Subset s : subsets) {
            SubsetTokn st = new SubsetTokn();
            st.setSubset((Key) s.getPrimaryKey());
            st.setTokn(wrapped.getId());
            this.subsets.add(st);
        }
    }

    public List<SubsetTokn> getSubsets() {
        return subsets;
    }

    public Long getToknId() {
        return wrapped.getId();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Override
    public String getKeyString() {
        return wrapped.getKeyString();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public Key getViewContext() {
        return wrapped.getViewContext();
    }
}
