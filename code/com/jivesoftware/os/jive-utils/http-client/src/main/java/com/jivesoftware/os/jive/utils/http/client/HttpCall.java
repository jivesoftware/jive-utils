package com.jivesoftware.os.jive.utils.http.client;

interface HttpCall {

    HttpResponse call(HttpClient client) throws HttpClientException;

}
