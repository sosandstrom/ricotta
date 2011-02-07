package com.wadpam.ricotta.web;

import javax.servlet.http.HttpServletRequest;

import com.wadpam.ricotta.domain.Project;

public class LinkBuilder {
    private final HttpServletRequest request;

    public LinkBuilder(HttpServletRequest request) {
        this.request = request;
    }

    public Builder add(String filename) {
        return new Builder().add(filename);
    }

    public Builder add(Project project) {
        return new Builder().add(project);
    }

    class Builder {
        final StringBuffer link = new StringBuffer("<a href=\"");

        public String url(String text) {
            link.append('/');
            // version to add?
            final String v = request.getParameter(ProjectHandlerInterceptor.KEY_VERSION);
            if (null != v) {
                link.append("?version=");
                link.append(v);
            }
            link.append("\">");
            link.append(text);
            link.append("</a>");
            return link.toString();
        }

        public String head(String text) {
            link.append('/');
            link.append("\">");
            link.append(text);
            link.append("</a>");
            return link.toString();
        }

        public Builder add(String filename) {
            link.append('/');
            link.append(filename);
            return this;
        }

        public Builder add(Project project) {
            link.append("/projects/");
            link.append(project.getName());
            return this;
        }
    }
}
