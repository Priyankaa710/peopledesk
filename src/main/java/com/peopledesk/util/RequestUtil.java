package com.peopledesk.util;

/** Small helpers for reading request parameters safely. */
public final class RequestUtil {

    private RequestUtil() {
    }

    /** Converts text such as "12" to a positive int. Returns -1 for missing, invalid or non-positive values. */
    public static int parseId(String text) {
        if (text == null) {
            return -1;
        }
        try {
            int id = Integer.parseInt(text.trim());
            return id > 0 ? id : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
