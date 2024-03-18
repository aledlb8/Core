package dev.aledlb.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {
    private final int resourceId;
    private final String currentVersion;

    public UpdateChecker(int resourceId, String currentVersion) {
        this.resourceId = resourceId;
        this.currentVersion = currentVersion;
    }

    public boolean isUpToDate() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "UpdateChecker");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String latestVersion = reader.readLine();
            reader.close();
            return !latestVersion.equals(currentVersion);
        } catch (Exception e) {
            return false;
        }
    }
}
