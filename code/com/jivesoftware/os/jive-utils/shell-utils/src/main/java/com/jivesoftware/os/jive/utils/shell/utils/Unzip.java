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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;

/**
 *
 */
public class Unzip {

    public static File unGzip(boolean verbose,
            File outputDir,
            String outName,
            File inputFile,
            boolean deleteOriginal) throws FileNotFoundException, IOException {
        String inFilePath = inputFile.getAbsolutePath();
        if (verbose) {
            System.out.println("unzipping " + inFilePath);
        }
        GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inFilePath));

        File outFile = new File(outputDir, outName);
        outFile.mkdirs();
        String outFilePath = outFile.getAbsolutePath();
        OutputStream out = new FileOutputStream(outFilePath);

        byte[] buf = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        gzipInputStream.close();
        out.close();

        if (deleteOriginal) {
            FileUtils.forceDelete(inputFile);
            if (verbose) {
                System.out.println(String.format("deleted original file %s.", inputFile.getAbsolutePath()));
            }
        }
        if (verbose) {
            System.out.println("unzipped " + inFilePath);
        }
        return new File(outFilePath);
    }
}