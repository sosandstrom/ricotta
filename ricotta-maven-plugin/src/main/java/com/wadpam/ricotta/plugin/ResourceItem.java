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

}
