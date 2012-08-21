/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wadpam.ricotta.web.admin;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TransientFailureException;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sf.mardao.api.dao.AEDDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author os
 */
@Controller
@RequestMapping("_admin/task")
public class AdminTaskController {
    
    public static final String PATH_ADMIN_TASK = "/api/_admin/task/%s";
    
    static final Logger LOG = LoggerFactory.getLogger(AdminTaskController.class);
    
    private AdminTask adminTaskBean;
    
    @RequestMapping(value="{taskName}", method= RequestMethod.GET)
    public ResponseEntity<Object> enqueueTask(
            HttpServletRequest request,
            @PathVariable String taskName,
            @RequestParam(defaultValue="false") boolean retry) {
        final Queue queue = QueueFactory.getDefaultQueue();
        final String path = String.format(PATH_ADMIN_TASK, taskName);
        final TaskOptions options = TaskOptions.Builder.withUrl(path);
        
        Set<Entry> entrySet = request.getParameterMap().entrySet();
        for (Entry entry : entrySet) {
            String values[] = (String[]) entry.getValue();
            for (String value : values) {
                options.param(entry.getKey().toString(), value);
            }
        }
        
        addOptionsToQueueWithRetry(queue, options, retry);
        
        LOG.info("Added queue task for {}", path);
        
        return new ResponseEntity<Object>(HttpStatus.OK);
    }
    
    @RequestMapping(value="{taskName}", method= RequestMethod.POST)
    public ResponseEntity<Object> processTask(HttpServletRequest request,
            @PathVariable final String taskName) {
        
        // for serializing BlobKeys:
        AEDDaoImpl.setBlobKeyFormat(request.getProtocol() + "://" +
                request.getHeader("Host") + "/screenshot?blobKey=%s");
        
        LOG.info("Processing task for {}...", taskName);

        final Object body = adminTaskBean.processTask(taskName, request.getParameterMap());
        LOG.info("Processed task for {}: {}", taskName, body);
        
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    public static final int        SHORT_TIME           = 2000;
    public static final int        LONG_TIME            = 5000;
    public static boolean addOptionsToQueueWithRetry(Queue queue, TaskOptions options,
            boolean retry) {
        boolean successful = true;
        try {
            queue.add(options);
        }
        catch (TransientFailureException tfe) {
            LOG.warn("Failed to add task to queue. This is first retry");
            if (retry) {
                try {
                    try {
                        Thread.sleep(SHORT_TIME);
                    }
                    catch (InterruptedException e) {
                        // Leave it at this
                    }
                    queue.add(options);
                }
                catch (TransientFailureException tfe2) {
                    LOG.warn("Failed to add task to queue. This is second retry");
                    try {
                        try {
                            Thread.sleep(LONG_TIME);
                        }
                        catch (InterruptedException e) {
                            // Leave it at this
                        }
                        queue.add(options);
                    }
                    catch (TransientFailureException tfe3) {
                        successful = false;
                        LOG.error("Failed to add task to queue with exception {} ", tfe3);
                    }
                }
            }
        }
        catch (IllegalStateException e) {
            LOG.info("Queue, {}, is not found in the application configuration. Default queue is being used.",
                    queue.getQueueName());
            queue = QueueFactory.getDefaultQueue();
            addOptionsToQueueWithRetry(queue, options, retry);
        }
        return successful;
    }

    public void setAdminTaskBean(AdminTask adminTaskBean) {
        this.adminTaskBean = adminTaskBean;
    }
    
}
