package com.wadpam.ricotta.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.logging.Log;

public class Downloader {

    private static Log LOG;

    protected static void warn(String s) {
        if (null != LOG) {
            LOG.warn(s);
        }
    }

    protected static void info(String s) {
        if (null != LOG) {
            LOG.info(s);
        }
    }

    public static void download(String projectName, String languageCode, String templateName, File destination)
            throws ClientProtocolException, IOException {
        String url = "http://ricotta-ost.appspot.com/projects/" + projectName + "/languages/" + languageCode + "/templates/"
                + templateName + '/';
        info("Download URL " + url);
        HttpGet method = new HttpGet(url);

        // TODO: add authentication

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(method);

        final StatusLine status = response.getStatusLine();
        if (status.getStatusCode() == 200) {
            InputStream is = response.getEntity().getContent();
            File folder = destination.getParentFile();
            if (false == folder.exists()) {
                folder.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(destination);

            byte buf[] = new byte[1024];
            int count;
            while (0 < (count = is.read(buf))) {
                fos.write(buf, 0, count);
            }

            fos.close();
            is.close();
            info("Downloaded " + destination.getAbsolutePath());
        }
        else {
            warn("HTTP " + status.getStatusCode() + " " + status.getReasonPhrase());
        }

        method.abort();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    public static void setLog(Log log) {
        Downloader.LOG = log;
    }

}
