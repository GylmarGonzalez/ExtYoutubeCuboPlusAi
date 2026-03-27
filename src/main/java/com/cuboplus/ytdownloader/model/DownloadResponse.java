package com.cuboplus.ytdownloader.model;

public class DownloadResponse {
    private boolean success;
    private String message;
    private String filename;
    private String downloadUrl;

    public DownloadResponse(boolean success, String message, String filename, String downloadUrl) {
        this.success = success;
        this.message = message;
        this.filename = filename;
        this.downloadUrl = downloadUrl;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getFilename() { return filename; }
    public String getDownloadUrl() { return downloadUrl; }
}
