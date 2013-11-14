/*
 * Created: 10/2/12 by brad.jordan
 * Copyright (C) 1999-2012 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JvmMetricsContext implements MetricsContext {

    private static final String JVM_UID = UUID.randomUUID().toString();
    private static final String JVM_HOME = new File("." + File.separator).getAbsolutePath();
    private static final String JVM_IP_ADDRS = scanForIpAddrsStr();
    private static final String JVM_HOSTNAME = lookupHostName();

    private static String lookupHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "ERROR:" + e.getMessage();
        }
    }
    private static final Map<String, String> report;

    static {
        report = new HashMap<>();
        report.put("jvmUid", JVM_UID);
        report.put("jvmIpAddr", JVM_IP_ADDRS);
        report.put("jvmHome", JVM_HOME);
        report.put("jvmHost", JVM_HOSTNAME);
    }

    @Override
    public Map<String, String> report() {
        return report;
    }

    private static String scanForIpAddrsStr() {
        try {
            StringBuilder sb = new StringBuilder();
            Object[] toArray = scanForIpAddrs().toArray();
            for (int i = 0; i < toArray.length; i++) {
                if (i > 0) {
                    sb.append(':');
                }
                sb.append(toArray[i]);

            }
            return sb.toString();
        } catch (SocketException e) {
            return "unknown";
        }
    }

    private static List<String> scanForIpAddrs() throws SocketException {
        ArrayList<String> ipAddrs = new ArrayList<>();

        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            StringBuffer nicAndMaxAndIp = new StringBuffer("nic=").append(ni.getDisplayName()).append(",");
            byte[] hardwareAddress = ni.getHardwareAddress();
            if (hardwareAddress != null) {
                nicAndMaxAndIp.append("mac=");
                convertToHex(hardwareAddress, ":", nicAndMaxAndIp);
                nicAndMaxAndIp.append(",");
            }
            Enumeration<InetAddress> ina = ni.getInetAddresses();
            boolean addedInetAddress = false;
            while (ina != null && ina.hasMoreElements()) {
                InetAddress a = ina.nextElement();
                if (a.isAnyLocalAddress() || a.isLoopbackAddress()) {
                    continue;
                }
                if (addedInetAddress) {
                    nicAndMaxAndIp.append(",");
                } else {
                    addedInetAddress = true;
                }
                nicAndMaxAndIp.append("ip=").append(a.getHostAddress());
            }
            if (addedInetAddress) {
                ipAddrs.add(nicAndMaxAndIp.toString());
            }
        }
        return ipAddrs;
    }

    //todo move out to a util class somewhere.
    private static StringBuffer convertToHex(byte[] data, String sep, StringBuffer sb) {
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                sb.append(sep);
            }
            int low = data[i] & 0xF;
            int high = (data[i] >> 8) & 0xF;
            sb.append(Character.forDigit(high, 16));
            sb.append(Character.forDigit(low, 16));
        }
        return sb;
    }
}
