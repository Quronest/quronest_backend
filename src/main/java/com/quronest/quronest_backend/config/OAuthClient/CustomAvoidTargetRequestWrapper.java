package com.quronest.quronest_backend.config.OAuthClient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Servlet request wrapper that hides/removes the "targetUrlParameter" parameter from the wrapped request.
 *
 * <p>This wrapper is useful in contexts (for example OAuth or redirect handling) where a request parameter
 * named "targetUrlParameter" should not be exposed or used by downstream code. It overrides parameter accessors
 * and the query string accessor to behave as if the parameter does not exist.</p>
 *
 * Behavior:
 * - getParameter(name): returns null when name equals "targetUrlParameter".
 * - getParameterMap(): returns a shallow copy of the original parameter map without the "targetUrlParameter" entry.
 * - getParameterValues(name): returns null when name equals "targetUrlParameter".
 * - getQueryString(): returns the original query string with any "targetUrlParameter=..." fragment removed.
 *
 * Thread-safety: this wrapper forwards to the underlying HttpServletRequest and returns copies when mutating
 * the parameter map, so it does not modify the original request state.
 */
public class CustomAvoidTargetRequestWrapper extends HttpServletRequestWrapper {


    /**
     * Create a new wrapper around the given request.
     *
     * @param request the original HttpServletRequest to wrap
     */
    public CustomAvoidTargetRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * Return the parameter value for the given name, or null when the name is "targetUrlParameter".
     *
     * @param name parameter name
     * @return the parameter value or null for "targetUrlParameter"
     */
    @Override
    public String getParameter(String name) {
        if ("targetUrlParameter".equals(name)) {
            return null;
        }
        return super.getParameter(name);
    }

    /**
     * Return a parameter map that excludes the "targetUrlParameter" entry.
     *
     * @return a copy of the original parameter map without "targetUrlParameter"
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalMap = super.getParameterMap();
        Map<String, String[]> filteredMap = new HashMap<>(originalMap.size());

        for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
            if (!"targetUrlParameter".equals(entry.getKey())) {
                filteredMap.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredMap;
    }

    /**
     * Return parameter values for the given name, or null when the name is "targetUrlParameter".
     *
     * @param name parameter name
     * @return parameter values or null for "targetUrlParameter"
     */
    @Override
    public String[] getParameterValues(String name) {
        if ("targetUrlParameter".equals(name)) {
            return null;
        }
        return super.getParameterValues(name);
    }

    /**
     * Return the query string with any "targetUrlParameter=..." fragment removed.
     *
     * @return filtered query string or null if none
     */
    @Override
    public String getQueryString() {
        String originalQueryString = super.getQueryString();
        if (originalQueryString != null && originalQueryString.contains("targetUrlParameter")) {
            originalQueryString = originalQueryString.replaceAll("targetUrlParameter=[^&]*&?", "");
            originalQueryString = originalQueryString.replaceAll("&$", "");
        }
        return originalQueryString;
    }

}
