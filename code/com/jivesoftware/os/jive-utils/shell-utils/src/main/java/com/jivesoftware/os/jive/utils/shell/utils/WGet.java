/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.shell.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */
public class WGet {

    public static boolean wget(String theUrl, File file) {

        URLConnection con;
        try {
            URL gotoUrl = new URL(theUrl);
            con = gotoUrl.openConnection();
            con.connect();
            String type = con.getContentType();

            if (type != null) {
                System.out.println("wgetting " + theUrl);

                byte[] buffer = new byte[4 * 1024];
                int read;

                FileOutputStream os = new FileOutputStream(file);
                InputStream in = con.getInputStream();

                while ((read = in.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                }

                os.close();
                in.close();
                System.out.println("got " + file + " size " + read + " bytes");
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            System.err.println("failed to wget " + theUrl + " " + e);
            return false;
        }

    }
}
