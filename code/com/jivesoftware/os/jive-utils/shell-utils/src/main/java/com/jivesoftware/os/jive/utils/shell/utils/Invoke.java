/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.jive.utils.shell.utils;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

/**
 *
 */
public class Invoke {

    public static int invoke(File home, String[] command, final InputStream writeToProcess, final ConcurrentLinkedQueue<String> response) throws Exception {
        Executor executor = new DefaultExecutor();
        executor.setExitValue(0);
        if (home != null) {
            executor.setWorkingDirectory(home);
        }

        //give all the processes 120s to return that they started successfully
        ExecuteWatchdog watchdog = new ExecuteWatchdog(120000);
        executor.setWatchdog(watchdog);

        LogOutputStream outputStream = new LogOutputStream(20000) {
            @Override
            protected void processLine(final String line, final int level) {
                response.add(line);
            }
        };

        LogOutputStream errorStream = new LogOutputStream(40000) {
            @Override
            protected void processLine(final String line, final int level) {
                response.add(line);
            }
        };

        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream, writeToProcess);
        executor.setStreamHandler(pumpStreamHandler);
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());


        CommandLine commandLine = new CommandLine(command[0]);
        for (int i = 1; i < command.length; i++) {
            commandLine.addArgument(command[i]);
        }
        try {
            //executor.execute(commandLine, handler);
            return executor.execute(commandLine);
            //handler.waitFor(20000);
        } catch (Exception x) {
            x.printStackTrace();
            return 1;
        }
    }
}
