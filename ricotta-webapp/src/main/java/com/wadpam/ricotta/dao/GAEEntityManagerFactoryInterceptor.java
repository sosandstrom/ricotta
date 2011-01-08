package com.wadpam.ricotta.dao;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class GAEEntityManagerFactoryInterceptor extends HandlerInterceptorAdapter implements EntityManagerFactory {

    static final Logger                      LOG           = LoggerFactory.getLogger(GAEEntityManagerFactoryInterceptor.class);

    private final EntityManagerFactory       entityManagerFactory;

    private final ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();

    public GAEEntityManagerFactoryInterceptor(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public EntityManager createEntityManager() {
        return createEntityManager(null);
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        final EntityManager em = entityManager.get();
        LOG.debug("createEntityManager " + em);
        return em;
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }

    @Override
    public boolean isOpen() {
        return null != entityManagerFactory && entityManagerFactory.isOpen();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (null != entityManager.get()) {
            throw new Exception("Entity manager already exists");
        }
        entityManager.set(entityManagerFactory.createEntityManager());
        LOG.debug("Created Entity Manager " + entityManager.get());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        final EntityManager em = entityManager.get();
        if (null == em) {
            throw new Exception("No Entity manager to close");
        }
        LOG.debug("Closing Entity Manager " + em);

        em.close();

        entityManager.remove();
    }
}
