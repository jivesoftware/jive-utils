package com.jivesoftware.os.jive.utils.http.client;

public class NoClientAvailableHttpClient implements HttpClient {

    @Override
    public HttpResponse get(String path) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the get call.", new byte[0]);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postJson call.", new byte[0]);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    @Override
    public HttpResponse get(String path, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }
}
