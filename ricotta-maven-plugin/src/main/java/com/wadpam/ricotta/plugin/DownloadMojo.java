package com.wadpam.ricotta.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.wadpam.ricotta.command.Downloader;

/**
 * @goal download
 * @author os
 * 
 */
public class DownloadMojo extends AbstractMojo {

    /**
     * @parameter expression="${download.projectName}" default-value="${project.artifactId}"
     */
    private String       projectName;

    /**
     * @parameter expression="${download.version}"
     */
    private String       version;
    //
    // /**
    // * @parameter expression="${download.templateName}" default-value="properties_java"
    // */
    // private String templateName;

    /**
     * @parameter expression="${download.destination}" default-value="${project.build.directory}/generated-resources"
     */
    private File         destination;

    /**
     * @parameter expression="${download.resourceItems}" required"
     */
    private ResourceItem resourceItems[];

    /**
     * @parameter expression="${download.baseUrl}" default-value="http://ricotta-ost.appspot.com/"
     */
    private String       baseUrl;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting download of ricotta language files...");
        Downloader.setLog(getLog());

        for(ResourceItem item : resourceItems) {
            File output = new File(destination, item.getFilePath());
            try {
                Downloader.download(baseUrl, projectName, version, item.getLanguageCode(), item.getTemplateName(),
                        item.getArtifactName(), output);
            }
            catch (ClientProtocolException e) {
                throw new MojoExecutionException("Error downloading language file: " + item.getLanguageCode(), e);
            }
            catch (IOException e) {
                throw new MojoExecutionException("Error downloading language file: " + item.getLanguageCode(), e);
            }
        }
        getLog().info("Done downloading ricotta language files.");
    }

}
