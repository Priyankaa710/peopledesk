package com.peopledesk.util;

import javax.servlet.http.HttpSession;

/**
 * "Flash" messages: a success/error message that survives one redirect.
 * Typical use: after a successful POST we store a message, redirect to a list page (so that
 * refreshing the page does not submit the form again) and show the message once.
 */
public final class Flash {

    private static final String TYPE_KEY = "flashType";
    private static final String TEXT_KEY = "flashText";

    private Flash() {
    }

    public static void success(HttpSession session, String message) {
        session.setAttribute(TYPE_KEY, "success");
        session.setAttribute(TEXT_KEY, message);
    }

    public static void error(HttpSession session, String message) {
        session.setAttribute(TYPE_KEY, "error");
        session.setAttribute(TEXT_KEY, message);
    }

    /** Returns the alert box HTML (or an empty string) and removes the message from the session. */
    public static String consumeHtml(HttpSession session) {
        if (session == null) {
            return "";
        }
        String type = (String) session.getAttribute(TYPE_KEY);
        String text = (String) session.getAttribute(TEXT_KEY);
        session.removeAttribute(TYPE_KEY);
        session.removeAttribute(TEXT_KEY);
        if (type == null || text == null) {
            return "";
        }
        return alert(type, text);
    }

    /** Builds an alert box. type is "success", "error" or "info". The text is escaped. */
    public static String alert(String type, String text) {
        return "<div class=\"alert alert-" + type + "\" role=\"alert\">" + HtmlUtil.escape(text) + "</div>\n";
    }
}
