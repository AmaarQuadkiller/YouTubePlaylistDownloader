package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Amaar on 2017-08-23.
 */
public class PlaylistDownloader extends DriverTask {
    private UrlDownloader urlDownloader;

    public PlaylistDownloader(Data data, MessageAcceptor messageAcceptor) {
        super(data, messageAcceptor);
        urlDownloader = null;
    }

    @Override
    public void cancel() {
        super.cancel();
        urlDownloader.cancel();
    }

    @Override
    public void execute() {
        new Thread(() -> {
            messageAcceptor.accept("Beginning downloads.");
            if (!waitForDriver()) return;

            if (cancel) return;
            urlDownloader = new UrlDownloader(data, messageAcceptor);

            //iterate over URLs and download MP3 files
            //TODO: fix up exception handling, verify cancelling reliability
            for (Song song : data.getSongs()) {
                try {
                    synchronized (DriverUtils.getLock()) {
                        if (cancel) return;
                        DriverUtils.get("http://convert2mp3.net/en/");

                        if (cancel) return;
                        WebElement urlBox = DriverUtils.findWebElement("urlBox");
                        urlBox.sendKeys(song.getYouTubeUrl());
                        urlBox.sendKeys(Keys.ENTER);
                        while (true) {
                            if (cancel) return;
                            if (!DriverUtils.getCurrentUrl().equals("http://convert2mp3.net/en/index.php?p=convert"))
                                break;
                        }

                        if (cancel) return;
                        WebElement editArtistButton = DriverUtils.findWebElement("editArtistButton");
                        editArtistButton.click();

                        if (cancel) return;
                        WebElement editArtistTextBox = DriverUtils.findWebElement("editArtistTextBox");
                        editArtistTextBox.clear();
                        editArtistTextBox.sendKeys(song.getArtistName());

                        if (cancel) return;
                        WebElement editTitleButton = DriverUtils.findWebElement("editTitleButton");
                        editTitleButton.click();

                        if (cancel) return;
                        WebElement editTitleTextBox = DriverUtils.findWebElement("editTitleTextBox");
                        editTitleTextBox.clear();
                        editTitleTextBox.sendKeys(song.getTitle());

                        if (!data.getAlbumName().isEmpty()) {
                            if (cancel) return;
                            WebElement advancedTagsButton = DriverUtils.findWebElement("advancedTagsButton");
                            advancedTagsButton.click();

                            WebElement editAlbumTextBox = DriverUtils.findWebElement("editAlbumTextBox");
                            while (true) {
                                if (cancel) return;
                                if (editAlbumTextBox.isDisplayed()) break;
                            }
                            editAlbumTextBox.sendKeys(data.getAlbumName());
                        }

                        if (cancel) return;
                        WebElement continueButton = DriverUtils.findWebElement("continueButton");
                        continueButton.click();
                        while (true) {
                            if (cancel) return;
                            if (!DriverUtils.getCurrentUrl().startsWith("http://convert2mp3.net/en/index.php?p=tags"))
                                break;
                        }

                        if (cancel) return;
                        WebElement downloadButton = DriverUtils.findWebElement("downloadButton");
                        song.setDownloadUrl(downloadButton.getAttribute("href"));
                    }
                }
                catch (Exception e) {
                    urlDownloader.skip(song);
                    messageAcceptor.accept("Unable to download \"" + song.getTitle() + "\". Is this video available?");
                    continue;
                }
                messageAcceptor.accept("\"" + song.getTitle() + "\" download queued successfully.");
            }
            messageAcceptor.accept("All downloads queued.");
            messageAcceptor.accept("Waiting for downloads to finish.");
        }).start();
    }

    @Deprecated
    private static boolean addTrackNumber(String filePath, int trackNumber) {
        String tempFilePath = filePath.substring(0, filePath.length() - 4) + " (temp).mp3";
        while (new File(tempFilePath).exists()) {
            tempFilePath = tempFilePath.substring(0, tempFilePath.length() - 5) + "_).mp3";
        }
        try {
            Mp3File mp3File = new Mp3File(filePath);
            if (mp3File.hasId3v2Tag()) {
                ID3v2 tag = mp3File.getId3v2Tag();
                tag.setTrack(String.valueOf(trackNumber));
                tag.setComment("Downloaded by YouTube Playlist Downloader - by Amaar Quadri");
                mp3File.setId3v2Tag(tag);
                mp3File.save(tempFilePath);
            }
            else if (mp3File.hasId3v1Tag()) {
                ID3v1 tag = mp3File.getId3v1Tag();
                tag.setTrack(String.valueOf(trackNumber));
                tag.setComment("Downloaded by YouTube Playlist Downloader - by Amaar Quadri");
                mp3File.setId3v1Tag(tag);
                mp3File.save(tempFilePath);
            }
            else return false;
            Files.delete(Paths.get(filePath));
            return new File(tempFilePath).renameTo(new File(filePath));
        }
        catch (Exception e) {
            return false;
        }
    }
}
