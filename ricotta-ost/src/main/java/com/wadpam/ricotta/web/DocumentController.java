/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.web;

import com.google.appengine.api.datastore.*;
import java.util.*;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author os
 */
@Controller
@RequestMapping("doc")
public class DocumentController {
    
    public static final String NAME_ID = "_id";
    
    static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);
    
    @RequestMapping(value="{kind}/{id}", method= RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> getProfile(
            @PathVariable String kind,
            @PathVariable Long id
            ) {
        
        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        final Transaction t = datastore.beginTransaction();

        try {
            Map<String, Object> props = load(datastore, t, null, kind, id);

            final HttpStatus status = null != props ? HttpStatus.OK : HttpStatus.NOT_FOUND;
            if (null == props) {
                props = Collections.EMPTY_MAP;
            }
            return new ResponseEntity<Map<String, Object>>(props, status);
        }
        finally {
            t.commitAsync();
        }
    }
    
    protected Map<String, Object> load(DatastoreService datastore, Transaction t, Key parentKey, String kind, Long id) {
        final Key key = KeyFactory.createKey(parentKey, kind, id);
        try {
            final Entity entity = datastore.get(t, key);
            
            // populate ID if root object
            final Map<String, Object> props = populate(datastore, t, entity, null == parentKey);
            
            return props;
            
        } catch (EntityNotFoundException ex) {
            return null;
        }
    }
    
    protected Object load(DatastoreService datastore, Transaction t, Key parentKey, String kind, boolean asIterable) {
        
        final Query q = new Query(kind, parentKey);

        final PreparedQuery pq = datastore.prepare(q);
        
        if (asIterable) {
            final ArrayList<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
            
            for(Entity entity : pq.asIterable()) {
                list.add(populate(datastore, t, entity));
            }
            
            return list;
        }
        else {
            // single child
            final Entity entity = pq.asSingleEntity();
            return populate(datastore, t, entity);
        }
    }
    
    protected Map<String, Object> populate(DatastoreService datastore, Transaction t, Entity entity) {
        return populate(datastore, t, entity, false);
    }
    
    protected Map<String, Object> populate(DatastoreService datastore, Transaction t, Entity entity, boolean populateID) {
        // entity properties immutable
        final Map<String, Object> props = new TreeMap<String,Object>(entity.getProperties());
        
        Object value;
        String name;
        for (Entry<String, Object> entry : props.entrySet()) {
            name = entry.getKey();
            value = entry.getValue();
            
            // single Entity inner child
            if (value instanceof Category) {
                props.put(name, load(datastore, t, entity.getKey(), name, false));
            }
            // array of inner child Entities
            else if (value instanceof Key) {
                props.put(name, load(datastore, t, entity.getKey(), name, true));
            }
        }
        
        if (populateID) {
            props.put(NAME_ID, Long.toString(entity.getKey().getId()));
        }
        
        return props;
    }
    
    @RequestMapping(value="{kind}", method= RequestMethod.POST)
    public ResponseEntity<String> createProfile(
            @PathVariable String kind,
            @RequestParam(required=false) Long id,
            @RequestBody Map<String, Object> props
            ) {
        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction t = datastore.beginTransaction();
        try {
            final Key key = persist(datastore, t, null, kind, id, props);
            return new ResponseEntity<String>(Long.toString(key.getId()), HttpStatus.OK);
        }
        finally {
            t.commit();
        }
               
    }
    
    protected Key persist(DatastoreService datastore, Transaction t, Key parentKey, String kind, Long id, Map<String, Object> props) {
        Key key = null; 
        Entity entity = null;
        
        // ID included in JSON object?
        if (null == id) {
            final Object _id = props.get(NAME_ID);
            if (null != _id) {
                if (_id instanceof String) {
                    id = Long.parseLong((String)_id);
                }
                else {
                    id = (Long)_id;
                }
            }
        }
        
        if (null != id) {
            key = KeyFactory.createKey(parentKey, kind, id);
            try {
                datastore.get(t, key);

                // if found, we have a conflict, do no more
                LOG.warn("Entity already exists for {}:{}", kind, id);
                return key;
            } catch (EntityNotFoundException ex) {
            }
            
            entity = new Entity(key);
        }
        else {
            entity = new Entity(kind, parentKey);
        }
            
        // entity does not exist, go ahead and populate:
        final Map<String,Map<String, Object>> singles = new TreeMap<String,Map<String, Object>>();
        final Map<String,List> arrays = new TreeMap<String,List>();

        Object value;
        String name;
        for (Entry<String, Object> entry : props.entrySet()) {
            name = entry.getKey();
            if (!NAME_ID.equals(name)) {
                value = entry.getValue();

                // inner Entity (non-array)
                if (value instanceof Map) {
                    singles.put(name, (Map<String, Object>) value);

                    // single entity has a Category with it's name
                    value = new Category(name);
                }
                // inner array of Entities
                else if (value instanceof List) {
                    List list = (List) value;
                    if (!list.isEmpty()) {
                        Object item = list.get(0);
                        if (item instanceof String) {
                            // don't worry
                        }
                        else if (item instanceof Integer) {
                            // don't worry
                        }
                        else if (item instanceof Long) {
                            // don't worry
                        }
                        else {
                            // arrays has parent Key as property value
                            arrays.put(name, list);
                            value = entity.getKey();
                        }
                    }
                }

                entity.setProperty(name, value);
            }
        }

        key = datastore.put(t, entity);

        // parent key has to be real before processing inner childs
        for (Entry<String, Map<String,Object>> entry : singles.entrySet()) {
                persist(datastore, t, key, entry.getKey(), null, entry.getValue());
        }

        for (Entry<String, List> entry : arrays.entrySet()) {
            for (Object item : entry.getValue()) {
                if (item instanceof Map) {
                    persist(datastore, t, key, entry.getKey(), null, (Map<String,Object>) item);
                }                
                else {
                    LOG.warn("Unsupported item type for {}: {}", entry.getKey(), item.getClass());
                }
            }
        }
        
        return key;
    }
}
