/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */package com.jivesoftware.os.jive.utils.shell.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 */
public class Untar {

    public static List<File> unTar(boolean verbose, final File outputDir, final File inputFile, boolean deleteOriginal)
            throws FileNotFoundException, IOException, ArchiveException {

        if (verbose) {
            System.out.println(String.format("untaring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
        }

        final List<File> untaredFiles = new LinkedList<>();
        final InputStream is = new FileInputStream(inputFile);
        final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        TarArchiveEntry entry = null;
        while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            entryName = entryName.substring(entryName.indexOf("/") + 1);
            final File outputFile = new File(outputDir, entryName);
            if (entry.isDirectory()) {
                if (verbose) {
                    System.out.println(String.format("Attempting to write output directory %s.", getRelativePath(outputDir, outputFile)));
                }
                if (!outputFile.exists()) {
                    if (verbose) {
                        System.out.println(String.format("Attempting to create output directory %s.", getRelativePath(outputDir, outputFile)));
                    }
                    if (!outputFile.mkdirs()) {
                        throw new IllegalStateException(String.format("Couldn't create directory %s.", getRelativePath(outputDir, outputFile)));
                    }
                }
            } else {
                try {
                    if (verbose) {
                        System.out.println(String.format("Creating output file %s.", getRelativePath(outputDir, outputFile)));
                    }
                    outputFile.mkdirs();
                    final OutputStream outputFileStream = new FileOutputStream(outputFile);
                    IOUtils.copy(debInputStream, outputFileStream);
                    outputFileStream.close();

                    if (getRelativePath(outputDir, outputFile).contains("bin/") || outputFile.getName().endsWith(".sh")) { // Hack!
                        if (verbose) {
                            System.out.println(String.format("chmod +x file %s.", getRelativePath(outputDir, outputFile)));
                        }
                        outputFile.setExecutable(true);
                    }

                } catch (Exception x) {
                    System.err.println("failed to untar " + getRelativePath(outputDir, outputFile) + " " + x);
                }
            }
            untaredFiles.add(outputFile);
        }
        debInputStream.close();

        if (deleteOriginal) {
            FileUtils.forceDelete(inputFile);
            if (verbose) {
                System.out.println(String.format("deleted original file %s.", inputFile.getAbsolutePath()));
            }
        }
        return untaredFiles;
    }

    public static String getRelativePath(File _root, File _file) {
        String home = _root.getAbsolutePath();
        String path = _file.getAbsolutePath();
        if (!path.startsWith(home)) {
            return null;
        }
        int l = home.length() + 1;
        if (path.length() > l) {
            return path.substring(l);
        } else {
            return path;
        }
    }
}