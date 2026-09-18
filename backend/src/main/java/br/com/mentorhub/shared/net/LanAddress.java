package br.com.mentorhub.shared.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public final class LanAddress {

    private LanAddress() {
    }

    public static String ipv4OrLocalhost() {
        try {
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nic.isUp() || nic.isLoopback() || nic.isVirtual()) {
                    continue;
                }
                for (InetAddress address : Collections.list(nic.getInetAddresses())) {
                    if (address instanceof Inet4Address ipv4 && ipv4.isSiteLocalAddress() && !ipv4.isLoopbackAddress()) {
                        return ipv4.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // keep localhost
        }
        return "localhost";
    }

    public static String replaceLocalhost(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        if (!url.contains("localhost") && !url.contains("127.0.0.1")) {
            return url;
        }
        String lan = ipv4OrLocalhost();
        return url.replace("localhost", lan).replace("127.0.0.1", lan);
    }
}
