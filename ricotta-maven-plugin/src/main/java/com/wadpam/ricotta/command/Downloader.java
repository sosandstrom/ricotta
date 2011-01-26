package com.wadpam.ricotta.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.logging.Log;

public class Downloader {

    private static Log LOG = null;

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

    public static void download(String projectName, String languageCode, String templateName, String artifactName,
            File destination) throws ClientProtocolException, IOException {
        File folder = destination.getParentFile();
        if (false == folder.exists()) {
            folder.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(destination);
        PrintStream ps = new PrintStream(fos);
        download(projectName, languageCode, templateName, artifactName, ps);
        ps.close();
    }

    public static void download(String projectName, String languageCode, String templateName, String artifactName, PrintStream fos)
            throws ClientProtocolException, IOException {
        StringBuffer url = new StringBuffer("http://ricotta-ost.appspot.com/projects/");
        url.append(projectName);
        url.append("/languages/");
        url.append(languageCode);
        url.append("/templates/");
        url.append(templateName);
        if (null != artifactName) {
            url.append("/artifacts/");
            url.append(artifactName);
        }
        url.append('/');
        info("Download URL " + url.toString());
        HttpGet method = new HttpGet(url.toString());

        // TODO: add authentication

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(method);

        StatusLine status = response.getStatusLine();

        // warming request?
        if (status.getStatusCode() == 500) {
            warn("Retrying as HTTP " + status.getStatusCode() + " " + status.getReasonPhrase());
            method.abort();
            // retry once!
            info("Download URL " + url.toString());
            method = new HttpGet(url.toString());
            // TODO: add authentication
            response = client.execute(method);
            status = response.getStatusLine();
        }

        if (status.getStatusCode() == 200) {
            InputStream is = response.getEntity().getContent();

            byte buf[] = new byte[1024];
            int count;
            while (0 < (count = is.read(buf))) {
                fos.write(buf, 0, count);
            }

            is.close();
            info("Downloaded " + projectName + '/' + languageCode + '/' + templateName);
        }
        else {
            warn("HTTP " + status.getStatusCode() + " " + status.getReasonPhrase());
        }

        method.abort();
    }

    /**
     * @param args
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static void main(String[] args) throws ClientProtocolException, IOException {
        switch (args.length) {
            case 3:
                download(args[0], args[1], args[2], null, System.out);
                break;
            case 4:
                download(args[0], args[1], args[2], args[3], System.out);
                break;
            default:
                throw new IOException("Usage: <projectName> <languageCode> <templateName> [<artifactName>]");
        }
    }

    public static void setLog(Log log) {
        Downloader.LOG = log;
    }

}
