package com.wadpam.ricotta.velocity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import com.wadpam.ricotta.dao.TemplateDao;
import com.wadpam.ricotta.domain.Template;

public class DaoResourceLoader extends ResourceLoader {

    private static TemplateDao templateDao = null;

    @Override
    public long getLastModified(Resource resource) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public InputStream getResourceStream(String source) throws ResourceNotFoundException {
        byte buf[] = null;

        Template templ = templateDao.findByPrimaryKey(source);
        if (null != templ) {
            buf = templ.getBody().getBytes();
        }
        else {
            throw new ResourceNotFoundException("No such template " + source);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        return is;
    }

    @Override
    public void init(ExtendedProperties configuration) {
    }

    @Override
    public boolean isSourceModified(Resource arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setTemplateDao(TemplateDao templateDao) {
        DaoResourceLoader.templateDao = templateDao;
    }
}
