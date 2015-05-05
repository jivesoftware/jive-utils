/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jivesoftware.os.jive.utils.http.client;

import java.io.InputStream;

/**
 *
 * @author jonathan
 */
public class HttpStreamResponse extends AbstractHttpResponse {

    private String fileName;
    InputStream inputStream;
    private String contentType;
    private Long fileSize;

    public HttpStreamResponse(int statusCode, String statusReasonPhrase, InputStream inputStream) {
        super(statusCode, statusReasonPhrase);
        this.inputStream = inputStream;
    }

    public HttpStreamResponse(int statusCode, String statusReasonPhrase, InputStream inputStream, String fileName, String contentType, Long fileSize) {
        super(statusCode, statusReasonPhrase);
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }
}
