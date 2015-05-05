package com.jivesoftware.os.jive.utils.http.client;


import java.util.HashMap;
import java.util.Map;

public class HttpRequestParams {

    private final String path;
    private final int timeout;
    private final Map<String, String> additionalHeaders;

    private HttpRequestParams(
            String path, int timeout, Map<String, String> additionalHeaders) {
        this.path = path;
        this.timeout = timeout;
        this.additionalHeaders = additionalHeaders;
    }

    public String getPath() {
        return path;
    }

    public int getTimeout() {
        return timeout;
    }

    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    @Override
    public String toString() {
        return "HttpRequestParams{"
                + ", path=" + path
                + ", timeout=" + timeout
                + ", additionalHeaders=" + additionalHeaders
                + '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    final public static class Builder {

        private String path;
        private int timeout = -1;
        private Map<String, String> additionalHeaders = new HashMap<String, String>();

        private Builder() {
        }

        /**
         *
         * @param path everything but the leading "http/s://host:port"
         */
        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder addHeaders(Map<String, String> headers) {
            additionalHeaders.putAll(headers);
            return this;
        }

        public Builder addHeader(String key, String value) {
            additionalHeaders.put(key, value);
            return this;
        }

        public HttpRequestParams build() {
            return new HttpRequestParams(path, timeout, additionalHeaders);
        }
    }

}
