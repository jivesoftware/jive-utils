/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package com.jivesoftware.os.server.http.jetty.jersey.endpoints.base;

import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;

class LocateStringResource {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final String jarFileName;
    private final String resourceName;

    LocateStringResource(String jarFileName, String resourceName) {
        this.jarFileName = jarFileName;
        this.resourceName = resourceName;
    }

    String getStringResource() {
        Map<String, Long> htSizes = new HashMap<>();
        try {
            try (final ZipFile zf = new ZipFile(jarFileName)) {
                Enumeration<? extends ZipEntry> e = zf.entries();
                while (e.hasMoreElements()) {
                    ZipEntry ze = e.nextElement();
                    if (!ze.getName().equals(resourceName)) {
                        continue;
                    }
                    htSizes.put(ze.getName(), ze.getSize());
                }
            }
            // extract resources and put them into the hashtable.
            FileInputStream fis = new FileInputStream(jarFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            try (final ZipInputStream zis = new ZipInputStream(bis)) {
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    if (!ze.getName().equals(resourceName)) {
                        continue;
                    }
                    if (ze.isDirectory()) {
                        continue;
                    }
                    int size = (int) ze.getSize();
                    // -1 means unknown size.
                    if (size == -1) {
                        size = htSizes.get(ze.getName()).intValue();
                    }
                    byte[] b = new byte[size];
                    int rb = 0;
                    int chunk = 0;
                    while ((size - rb) > 0) {
                        chunk = zis.read(b, rb, size - rb);
                        if (chunk == -1) {
                            break;
                        }
                        rb += chunk;
                    }
                    InputStream inputStream = new ByteArrayInputStream(b);
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(inputStream, writer, "UTF-8");
                    return writer.toString();
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to locate " + resourceName, e);
        }
        return null;
    }

}
