package com.peopledesk.util;

/** Small helper that makes user-provided text safe to place inside HTML. */
public final class HtmlUtil {

    private HtmlUtil() {
    }

    /**
     * Replaces the special HTML characters so that text typed by a user is shown as text
     * and never runs as HTML or JavaScript. This is the defence against XSS.
     * Example: &lt;script&gt; becomes the harmless text "&amp;lt;script&amp;gt;".
     */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
}
