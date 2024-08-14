package com.brov3r.whitelistip;

import com.avrix.utils.PlayerUtils;
import org.json.JSONObject;
import zombie.core.raknet.UdpConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for handling IP-related operations such as checking if an IP is within a range,
 * validating IP addresses, and obtaining the country code for an IP using an external API.
 */
public class IpUtils {
    /**
     * Asynchronously checks if the given IP address matches any specified rules in the configuration
     * and punishes the player accordingly if the conditions are met.
     *
     * @param targetIP   The IP address to be checked.
     * @param connection The UdpConnection object associated with the player.
     */
    public static void checkIP(String targetIP, UdpConnection connection) {
        CompletableFuture.runAsync(() -> {
            // Check the configuration for the necessary data
            if (Main.getConfig().getInt("blockType") == 0) return;

            boolean isWhiteList = Main.getConfig().getBoolean("isWhiteList");
            boolean isShouldBlock = false;
            String ip = targetIP.trim();

            // IP Validity Check
            if (!isValidIp(ip)) return;

            // Get lists of countries and IP addresses from the configuration
            List<String> countryList = Main.getConfig().getStringList("countryCodeList");
            List<String> ipList = Main.getConfig().getStringList("ipList");

            String ipCountry = null;

            if (Main.getConfig().getString("api") != null && !Main.getConfig().getString("api").isEmpty()){
                String api = Main.getConfig().getString("api").trim();

                ipCountry = getIpCountryCode(ip, api);

                // Get the country code for an IP address using the API
                if (ipCountry != null && !ipCountry.isEmpty()){
                    // Checking a country against a list of countries
                    boolean countryMatches = false;
                    for (String country : countryList) {
                        if (country.equalsIgnoreCase(ipCountry.trim())) {
                            countryMatches = true;
                            break;
                        }
                    }

                    // If the list is white and the country does not match, we punish the player
                    if (isWhiteList && !countryMatches) isShouldBlock = true;

                    // If the list is black and the country match, we punish the player
                    if (!isWhiteList && countryMatches) isShouldBlock = true;
                }
            }

            boolean ipMatches = false;

            // IP Address List Checker
            for (String ipInList : ipList) {
                ipInList = ipInList.trim();
                if (isRangeFormat(ipInList)) {
                    if (ipInRange(ip, ipInList)) {
                        ipMatches = true;
                        break;
                    }
                } else if (ipInList.equalsIgnoreCase(ip)) {
                    ipMatches = true;
                    break;
                }
            }

            // If the IP address matches and the list is black, we penalize the player
            if (ipMatches && !isWhiteList) isShouldBlock = true;

            // If the IP address does not match and the list is white, we punish the player
            if (!ipMatches && isWhiteList) isShouldBlock = true;

            if (isShouldBlock) {
                System.out.println("[?] WhiteListIP - Blocking connection: IP = " + ip + ", Country = " + (ipCountry != null ? ipCountry : "Unknown"));
                punishPlayer(connection);
            }
        });
    }

    /**
     * Retrieves the country code for a given IP address using an external API.
     *
     * @param ip  The IP address for which the country code is to be retrieved.
     * @param api The API key required to access the IPHub API.
     * @return The country code corresponding to the IP address, or null if retrieval fails.
     */
    public static String getIpCountryCode(String ip, String api) {
        String apiUrl = "http://v2.api.iphub.info/ip/" + ip;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("X-Key", api);

            int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            return jsonObject.getString("countryCode");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if a given string is in a valid IP range format (e.g., "192.168.0.1 - 192.168.0.255").
     *
     * @param range The string to check.
     * @return true if the string is a valid IP range format, false otherwise.
     */
    public static boolean isRangeFormat(String range) {
        String[] parts = range.split(" - ");

        if (parts.length != 2) return false;

        return !parts[0].isEmpty() && !parts[1].isEmpty();
    }

    /**
     * Applies punishment to the player based on the configuration settings.
     *
     * @param connection The UdpConnection object associated with the player.
     */
    private static void punishPlayer(UdpConnection connection) {
        int punishType = Main.getConfig().getInt("blockType");
        String reason = Main.getConfig().getString("blockReason");
        if (punishType == 0) return;

        if (punishType == 1) {
            PlayerUtils.kickPlayer(connection, reason);
            return;
        }

        if (punishType == 2) {
            PlayerUtils.banPlayer(connection, reason, false, false);
        }
    }

    /**
     * Checks if the given IP address falls within the specified IP range.
     *
     * @param ip    The IP address to check.
     * @param range The IP range in which to check the IP address.
     * @return true if the IP address falls within the range, false otherwise.
     */
    public static boolean ipInRange(String ip, String range) {
        if (!isValidIp(ip) || !isValidRange(range)) {
            return true;
        }

        String[] parts = range.split(" - ");
        String startIP = parts[0];
        String endIP = parts[1];

        long ipNum = ipToLong(ip);
        long startIPNum = ipToLong(startIP);
        long endIPNum = ipToLong(endIP);

        if (startIPNum > endIPNum) {
            long temp = startIPNum;
            startIPNum = endIPNum;
            endIPNum = temp;
        }

        return ipNum >= startIPNum && ipNum <= endIPNum;
    }

    /**
     * Validates if the provided IP address is in the correct format and within the allowable range.
     *
     * @param ip The IP address to validate.
     * @return true if the IP address is valid, false otherwise.
     */
    public static boolean isValidIp(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.length() > 1 && part.startsWith("0")) {
                return false;
            }
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates if the provided IP range is in the correct format.
     *
     * @param range The IP range to validate.
     * @return true if the IP range is valid, false otherwise.
     */
    public static boolean isValidRange(String range) {
        String[] parts = range.split(" - ");
        if (parts.length != 2) {
            return false;
        }
        return isValidIp(parts[0]) && isValidIp(parts[1]);
    }

    /**
     * Converts an IP address to its numeric representation.
     *
     * @param ipAddress The IP address to convert.
     * @return The numeric representation of the IP address.
     */
    private static long ipToLong(String ipAddress) {
        String[] ipParts = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result <<= 8;
            result |= Integer.parseInt(ipParts[i]);
        }
        return result;
    }
}