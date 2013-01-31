package com.wadpam.ricotta.model.v10;

public class Blob10 {
    private String blobKey;

    private String accessUrl;

    private String uploadUrl;

    /**
     * @return the uploadUrl
     */
    public String getUploadUrl() {
        return uploadUrl;
    }

    /**
     * @param uploadUrl
     *            the uploadUrl to set
     */
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    /**
     * @return the blobKey
     */
    public String getBlobKey() {
        return blobKey;
    }

    /**
     * @param blobKey
     *            the blobKey to set
     */
    public void setBlobKey(String blobKey) {
        this.blobKey = blobKey;
    }

    /**
     * Get the url for the blob image
     * 
     * @return the accessUrl
     */
    public String getAccessUrl() {
        return accessUrl;
    }

    /**
     * @param accessUrl
     *            the accessUrl to set
     */
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }
}
