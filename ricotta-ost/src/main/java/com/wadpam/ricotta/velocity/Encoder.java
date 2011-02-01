package com.wadpam.ricotta.velocity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Encoder {
    public String urlEncode(String s) {
        String returnValue = null;

        if (null != s) {
            try {
                returnValue = URLEncoder.encode(s, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return returnValue;
    }

    public static String REGEXP_AMP = "(\\&)[^a][^m][^p][^\\;]";

    public String amp(String s) {
        final String tmp = s.replaceAll("\\&", "&amp;");
        final String returnValue = tmp.replaceAll("\\&amp;amp;", "&amp;");
        return returnValue;
    }

    public String android(String s) {
        final String amp = amp(s);
        final String quot = amp.replaceAll("\\\"", "&quot;");
        final String apostrophy = quot.replace("'", "\\'");
        final String lt = apostrophy.replace("<", "&lt;");
        final String gt = lt.replace(">", "&gt;");
        final String br = gt.replace("\n", "\\n");
        return br;
    }
}
