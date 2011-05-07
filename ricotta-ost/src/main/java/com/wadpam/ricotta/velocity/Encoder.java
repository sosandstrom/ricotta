package com.wadpam.ricotta.velocity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;

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
        if (null == s) {
            return null;
        }
        final String tmp = s.replaceAll("\\&", "&amp;");
        final String returnValue = tmp.replaceAll("\\&amp;amp;", "&amp;");
        return returnValue;
    }

    public String android(String s) {
        if (null == s) {
            return null;
        }
        final String amp = amp(s);
        final String quot = amp.replaceAll("\\\"", "&quot;");
        final String apostrophy = quot.replace("'", "\\'");
        final String lt = apostrophy.replace("<", "&lt;");
        final String gt = lt.replace(">", "&gt;");
        final String br = gt.replace("\n", "\\n");
        return br;
    }

    public String firstToUpper(String s) {
        if (null == s) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        if (1 <= s.length()) {
            sb.append(s.substring(0, 1).toUpperCase());
        }
        if (2 <= s.length()) {
            sb.append(s.substring(1));
        }
        return sb.toString();
    }

    public String xml(String s) {
        return StringEscapeUtils.escapeXml(s);
    }
}
