package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Amaar on 2017-08-23.
 */
public class Data implements Serializable {
    private static final long serialVersionUID = 2;

    private boolean isFromSingleArtist;
    private String artistName;
    private String albumName;
    private String playlistUrl;
    private String downloadDirectory;
    private final ArrayList<Song> songs;
    private boolean addTrackNumbers;

    public Data(String artistName, String albumName, String playlistUrl, String downloadDirectory) {
        if (artistName == null || albumName == null || playlistUrl == null || downloadDirectory == null)
            throw new IllegalArgumentException("Arguments cannot be null");
        isFromSingleArtist = true;
        this.artistName = artistName;
        this.albumName = albumName;
        this.playlistUrl = playlistUrl;
        this.downloadDirectory = downloadDirectory;
        songs = new ArrayList<>();
        addTrackNumbers = true;
    }

    public Data(String playlistUrl, String downloadDirectory) {
        if (playlistUrl == null || downloadDirectory == null)
            throw new IllegalArgumentException("Arguments cannot be null");
        isFromSingleArtist = false;
        artistName = null;
        albumName = null;
        this.playlistUrl = playlistUrl;
        this.downloadDirectory = downloadDirectory;
        songs = new ArrayList<>();
        addTrackNumbers = false;
    }

    /**
     * @return True if this Data was made from SingleArtistController
     * and false if it was made from MultipleArtistController.
     */
    public boolean isFromSingleArtist() {
        return isFromSingleArtist;
    }

    /**
     * Guaranteed to return null if isFromSingleArtist returns false, and non-null otherwise.
     *
     * @return The artist's names.
     */
    public String getArtistName() {
        return artistName;
    }

    /**
     * Guaranteed to return null if isFromSingleArtist returns false, and non-null otherwise.
     *
     * @return The album's name.
     */
    public String getAlbumName() {
        return albumName;
    }

    /**
     * @return The YouTube playlist's URL.
     */
    public String getPlaylistUrl() {
        return playlistUrl;
    }

    /**
     * @return The path of the directory in which to download files.
     */
    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public boolean isAddTrackNumbers() {
        return addTrackNumbers;
    }

    public void setFromSingleArtist(boolean fromSingleArtist) {
        isFromSingleArtist = fromSingleArtist;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setPlaylistUrl(String playlistUrl) {
        this.playlistUrl = playlistUrl;
    }

    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }

    public void setAddTrackNumbers(boolean addTrackNumbers) {
        this.addTrackNumbers = addTrackNumbers;
    }
}
