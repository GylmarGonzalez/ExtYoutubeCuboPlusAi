package com.cuboplus.ytdownloader.model;

public class DownloadRequest {
    private String url;
    private String format; // "mp4" o "mp3"

    public DownloadRequest() {}

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getFormat() { return format != null ? format : "mp4"; }
    public void setFormat(String format) { this.format = format; }
}
