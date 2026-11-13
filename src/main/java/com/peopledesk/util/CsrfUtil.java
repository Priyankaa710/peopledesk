package com.peopledesk.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Basic CSRF (Cross-Site Request Forgery) protection.
 *
 * Idea: every session gets a random secret token. Every HTML form contains that token in a
 * hidden field. For each POST request the AuthenticationFilter checks that the token sent by
 * the form equals the token stored in the session. A malicious website cannot know the token,
 * so it cannot make your browser submit a valid form.
 */
public final class CsrfUtil {

    public static final String PARAM_NAME = "csrfToken";
    private static final String SESSION_KEY = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    /** Returns the session's token, creating it on first use. */
    public static String getToken(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_KEY, token);
        }
        return token;
    }

    /** The hidden input that every POST form must contain. */
    public static String hiddenField(HttpSession session) {
        return "<input type=\"hidden\" name=\"" + PARAM_NAME + "\" value=\""
                + HtmlUtil.escape(getToken(session)) + "\">";
    }

    /** True only if the request carries the same token that is stored in the session. */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(SESSION_KEY);
        String actual = request.getParameter(PARAM_NAME);
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
