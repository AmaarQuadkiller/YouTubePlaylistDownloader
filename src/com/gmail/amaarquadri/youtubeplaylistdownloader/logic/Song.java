package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import java.io.Serializable;

/**
 * Created by Amaar on 2017-08-23.
 * This class holds data about a song that is to be downloaded, including the artist's name, title, and YouTube URL.
 */
public class Song implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * The name of the artist of this Song.
     */
    private String artistName;

    /**
     * This Song's title.
     */
    private String title;

    /**
     * This Song's youTubeUrl.
     */
    private final String youTubeUrl;

    /**
     * The link from which to download this Song.
     */
    private String downloadUrl;

    /**
     * Creates a new Song Object.
     *
     * @param artistName The name of the artist of the Song.
     * @param title The Song's title.
     * @param youTubeUrl The Song's YouTube URL.
     */
    public Song(String artistName, String title, String youTubeUrl) {
        this.artistName = artistName;
        this.title = title;
        this.youTubeUrl = youTubeUrl;
        downloadUrl = null;
    }

    /**
     * @return The name of the artist of this Song.
     */
    public String getArtistName() {
        return artistName;
    }

    /**
     * Sets the name of the artist of this Song.
     *
     * @param artistName The name of the artist of this Song.
     */
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    /**
     * @return This Song's title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets this Song's title.
     *
     * @param title This Song's title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return This Song's YouTube URL.
     */
    public String getYouTubeUrl() {
        return youTubeUrl;
    }

    /**
     * @return The link from which to download this Song.
     */
    public synchronized String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * @param downloadUrl The link from which to download this Song.
     */
    public synchronized void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return artistName + " - " + title + " (" + youTubeUrl + ")";
    }
}
