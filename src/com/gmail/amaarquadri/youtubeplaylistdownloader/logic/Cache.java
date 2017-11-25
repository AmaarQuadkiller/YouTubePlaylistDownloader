package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.List;

/**
 * Created by Amaar on 2017-08-17.
 */
public class Cache {
    private static final File CACHE_FILE = new File("cache.txt");
    private static Cache cache = getCache();
    private static Cache getCache() {
        if (!CACHE_FILE.exists()) return cache = new Cache("", Utils.getDefaultDownloadDirectoryPath(), false);
        List<String> lines;
        try {
            lines = Files.readAllLines(CACHE_FILE.toPath());
        }
        catch (InvalidPathException | SecurityException | IOException e) {
            Utils.logThrowable(e);
            return cache = new Cache("", Utils.getDefaultDownloadDirectoryPath(), false);
        }
        if (lines.size() != 3) {
            Utils.log("Malformed cache!");
            return cache = new Cache("", Utils.getDefaultDownloadDirectoryPath(), false);
        }
        return cache = new Cache(lines.get(0), lines.get(1), Boolean.parseBoolean(lines.get(2)));
    }

    private final String defaultPlaylistUrl;
    private final String defaultDownloadDirectory;
    private final boolean autoLoad;

    private Cache(String defaultPlaylistUrl, String defaultDownloadDirectory, boolean autoLoad) {
        this.defaultPlaylistUrl = defaultPlaylistUrl;
        this.defaultDownloadDirectory = defaultDownloadDirectory;
        this.autoLoad = autoLoad;
    }

    public static String getDefaultPlaylistUrl() {
        return cache.defaultPlaylistUrl;
    }

    public static String getDefaultDownloadDirectory() {
        return cache.defaultDownloadDirectory;
    }

    public static boolean isAutoLoad() {
        return cache.autoLoad;
    }

    public static Data getDefaultData() {
        return new Data("", "", getDefaultPlaylistUrl(), getDefaultDownloadDirectory());
    }

    /**
     * Writes the specified Strings to the cache File.
     *
     * @param defaultPlaylistUrl The defaultPlaylistUrl to be written to the cache File.
     * @param defaultDownloadDirectory The defaultDownloadDirectory to be written to the cache File.
     * @return Whether or not the save operation was successful.
     */
    public static boolean setCache(String defaultPlaylistUrl, String defaultDownloadDirectory, boolean autoLoad) {
        if (defaultPlaylistUrl == null || defaultDownloadDirectory == null)
            throw new IllegalArgumentException("Arguments cannot be null.");

        if (cache.defaultPlaylistUrl.equals(defaultPlaylistUrl) &&
                cache.defaultDownloadDirectory.equals(defaultDownloadDirectory) && cache.autoLoad == autoLoad)
            return true;

        try (FileWriter fileWriter = new FileWriter(CACHE_FILE, false)) {
            fileWriter.write(defaultPlaylistUrl + "\n" + defaultDownloadDirectory + "\n" + Boolean.toString(autoLoad));
        }
        catch (IOException e) {
            return false;
        }
        try {
            Files.setAttribute(FileSystems.getDefault().getPath(CACHE_FILE.getAbsolutePath()), "dos:hidden", true);
        } catch (IOException ignore) {}

        cache = new Cache(defaultPlaylistUrl, defaultDownloadDirectory, autoLoad);
        return true;
    }
}
