package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * Created by Amaar on 2016-04-16.
 * This class uses Selenium to iterate over all the YouTube videos in a given playlist.
 * Each video is converted to an MP3 file using http://convert2mp3.net/en/, and downloaded.
 */
public class PlaylistLoader extends DriverTask {
    public PlaylistLoader(Data data, MessageAcceptor messageAcceptor) {
        super(data, messageAcceptor);
    }

    public void execute() {
        new Thread(() -> {
            messageAcceptor.accept("Initializing.");
            if (!waitForDriver()) return;

            messageAcceptor.accept("Collecting video data.");

            //get Songs
            try {
                synchronized (DriverUtils.getLock()) {
                    if (cancel) return;
                    DriverUtils.get(data.getPlaylistUrl());

                    //click the load more button as many times as necessary so that all videos are visible
                    while (true) {
                        if (cancel) return;
                        try {
                            WebElement loadMoreButton = DriverUtils.findWebElement("loadMoreButton");
                            loadMoreButton.click();
                        }
                        catch (NoSuchElementException ex) {
                            break;
                        }
                    }

                    if (cancel) return;
                    for (WebElement songTitleLink : DriverUtils.findWebElements("songTitleLink")) {
                        String href = songTitleLink.getAttribute("href");
                        if (href == null) {
                            //TODO: review
                            String errorMessage = "rekt";
                            Utils.log(errorMessage);
                            Utils.showErrorMessage(errorMessage, false);
                            throw new IllegalStateException(errorMessage);
                        }
                        data.getSongs().add(new Song(data.isFromSingleArtist() ?
                                data.getArtistName() : "", songTitleLink.getText(), href));
                    }
                }
            }
            catch(WebDriverException e){
                messageAcceptor.accept("Invalid playlist URL.");
                messageAcceptor.acceptLast("Aborting.");
                return;
            }

            //edit titles to remove artist name prefixes, and set them as the artist names
            for (Song song : data.getSongs()) {
                String title = song.getTitle().trim();
                if (data.isFromSingleArtist()) {
                    //noinspection ConstantConditions
                    if (title.toLowerCase().startsWith(data.getArtistName().toLowerCase() + " -"))
                        title = title.substring(data.getArtistName().length() + 2).trim();
                    song.setTitle(title);
                } else {
                    int pos = title.indexOf("-");
                    if (pos == -1) continue;
                    song.setArtistName(title.substring(0, pos).trim());
                    song.setTitle(title.substring(pos + 1).trim());
                }
            }

            if (cancel) return;
            messageAcceptor.acceptLast("Finished collecting video data.");
        }).start();
    }
}
