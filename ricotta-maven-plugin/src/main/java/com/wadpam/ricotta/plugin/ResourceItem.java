package com.wadpam.ricotta.plugin;

public class ResourceItem {
    /**
     * @parameter expression="${download.languageCode}" default-value="en"
     */
    private String languageCode;

    /**
     * @parameter expression="${download.templateName}" default-value="properties_java"
     */
    private String templateName;

    private String artifactName;

    /**
     * @parameter expression="${download.filePath}" required
     */
    private String filePath;
    
    /**
     * @parameter expression="${download.encoding}" default-value="UTF-8"
     */
    private String encoding;

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactName() {
        return artifactName;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        if (null == encoding) {
            encoding = "UTF-8";
        }
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
