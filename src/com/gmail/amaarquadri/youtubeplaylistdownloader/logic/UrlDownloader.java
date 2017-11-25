package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import java.io.*;
import java.net.URL;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by Amaar on 2017-06-14.
 * Class used to facilitate downloading and saving Files.
 */
public class UrlDownloader {
    private final Data data;

    private final MessageAcceptor messageAcceptor;

    private final Thread mainThread;

    private final ArrayList<Song> skipSongs;

    private boolean cancel = false;


    /**
     * Creates and starts a new UrlDownloader.
     */
    public UrlDownloader(Data data, MessageAcceptor messageAcceptor) {
        this.data = data;
        if (messageAcceptor == null) this.messageAcceptor = MessageAcceptor.EMPTY;
        else this.messageAcceptor = MessageAcceptor.addAcceptCondition(messageAcceptor, () -> !cancel);
        skipSongs = new ArrayList<>();
        cancel = false;

        mainThread = new Thread(() -> {
            forLoop:
            for (Song song : data.getSongs()) {
                while (song.getDownloadUrl() == null) {
                    synchronized (skipSongs) {
                        if (skipSongs.remove(song)) continue forLoop;
                    }
                    try {
                        Thread.sleep(100); //TODO: configure sleep millis
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                }
                if (cancel) return;
                process(song);
            }
            this.messageAcceptor.acceptLast("Downloads Finished.");
        });
        mainThread.start();
    }

    public void skip(Song song) {
        synchronized (skipSongs) {
            skipSongs.add(song);
        }
    }

    public void cancel() {
        cancel = true;
        mainThread.interrupt();
    }

    /**
     * Processes a single download Request.
     * This method blocks until the download is finished.
     *
     */
    private void process(Song song) {
        String finalFilePath = data.getDownloadDirectory() + "\\" + song.getArtistName() + " - " +
                song.getTitle() + ".mp3";
        int i = 0;
        while (new File(finalFilePath).exists()) {
            i++;
            finalFilePath = finalFilePath.substring(0, finalFilePath.length() - 4) + " (" + i + ").mp3";
        }
        String tempFilePath = finalFilePath.substring(0, finalFilePath.length() - 4) + " (temp).mp3";
        while (new File(tempFilePath).exists()) {
            tempFilePath = tempFilePath.substring(0, tempFilePath.length() - 5) + "_).mp3";
        }
        try (ReadableByteChannel inChannel = Channels.newChannel(new URL(song.getDownloadUrl()).openStream());
             FileChannel outChannel = new FileOutputStream(tempFilePath).getChannel()) {
            if (cancel) return;
            outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);
        }
        catch (ClosedByInterruptException e) {
            File outputFile = new File(tempFilePath);
            if (outputFile.exists()) if (!outputFile.delete()) Utils.showErrorMessage("The file at \"" +
                    tempFilePath + "\" was in the middle of being downloaded when you cancelled, " +
                    "and could not be deleted. Thus, it is unlikely to function normally.", false);
            return;
        }
        catch (IOException ignore) {
            File outputFile = new File(tempFilePath);
            if (outputFile.exists()) if (!outputFile.delete()) Utils.showErrorMessage("An error occurred while " +
                    "downloading the file at \"" + tempFilePath + "\", and it could not be deleted. " +
                    "Thus it will not function normally.", false);
            return;
        }
        messageAcceptor.accept("\"" + song.getTitle() + "\" download finished successfully.");

        //edit metadata asynchronously
        final String finalFilePath_ = finalFilePath;
        final String tempFilePath_ = tempFilePath;
        new Thread(() -> {
            try {
                Mp3File mp3File = new Mp3File(tempFilePath_);
                if (mp3File.hasId3v2Tag()) {
                    ID3v2 tag = mp3File.getId3v2Tag();
                    tag.setTitle(song.getTitle());
                    tag.setArtist(song.getArtistName());
                    if (data.isFromSingleArtist() && !data.getAlbumName().isEmpty())
                        tag.setAlbumArtist(song.getArtistName());
                    tag.setUrl(song.getYouTubeUrl());
                    if (data.isAddTrackNumbers()) tag.setTrack(String.valueOf(data.getSongs().indexOf(song) + 1));
                    tag.setComment("Downloaded by YouTube Playlist Downloader - by Amaar Quadri");
                    mp3File.setId3v2Tag(tag);
                    mp3File.save(finalFilePath_);
                }
                else if (mp3File.hasId3v1Tag()) {
                    Utils.log("ID3V2 tag unavailable for :" + song.toString());
                    ID3v1 tag = mp3File.getId3v1Tag();
                    tag.setTitle(song.getTitle());
                    tag.setArtist(song.getArtistName());
                    if (data.isAddTrackNumbers()) tag.setTrack(String.valueOf(data.getSongs().indexOf(song) + 1));
                    tag.setComment("Downloaded using YouTube Playlist Downloader - by Amaar Quadri");
                    mp3File.setId3v1Tag(tag);
                    mp3File.save(finalFilePath_);
                }
                else throw new UnsupportedOperationException("MP3File doesn't have an Id3v1 or Id3v2 tag.");
            }
            catch (Exception e) {
                if (!new File(tempFilePath_).renameTo(new File(finalFilePath_)))
                    Utils.showErrorMessage("Couldn't edit MP3 file's metadata or rename the temporary file.", false);
                else Utils.showErrorMessage("Couldn't edit MP3 file's metadata.", false);
                Utils.logThrowable(e);
                return;
            }

            try {
                Files.delete(Paths.get(tempFilePath_));
            } catch (IOException e) {
                Utils.showErrorMessage("Could not delete the temporary file \"" + tempFilePath_ + "\"", false);
            }
        }).start();
    }
}
