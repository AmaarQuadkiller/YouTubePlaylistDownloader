package com.gmail.amaarquadri.youtubeplaylistdownloader.ui;

import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.*;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Amaar on 2016-05-18.
 * TODO: reimplement message area text saving after SongNameEditor
 */
public final class LoadingScreenController {
    public static void start(Stage stage, Data data, boolean startDownloads) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource(
                "/com/gmail/amaarquadri/youtubeplaylistdownloader/fxml/loading_screen.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("YouTube Playlist Downloader");
        stage.show();
        ((LoadingScreenController) loader.getController()).init(data, stage, startDownloads);
    }

    @FXML private TextArea messageTextArea;
    @FXML private Button downloadMoreButton;
    private Data data;
    private DriverTask task;

    private void init(Data data, Stage stage, boolean startDownloads) {
        this.data = data;
        if (startDownloads) task = new PlaylistDownloader(data, new MessageAcceptor() {
            @Override
            public void accept(String message) {
                writeMessage(message);
            }

            @Override
            public void acceptLast(String message) {
                writeMessage(message);
                downloadMoreButton.setDisable(false);
            }
        });
        else task = new PlaylistLoader(data, new MessageAcceptor() {
            @Override
            public void accept(String message) {
                writeMessage(message);
            }

            @Override
            public void acceptLast(String message) {
                writeMessage(message);
                Platform.runLater(() -> {
                    try {
                        MetadataEditorController.start(stage, data);
                    } catch (IOException e) {
                        Utils.showErrorMessage("Fatal Error!", true);
                        Utils.logThrowable(e);
                    }
                });
            }
        });
        task.execute();
    }

    private void writeMessage(String message) {
        String newText = messageTextArea.getText();
        if (newText.isEmpty()) newText = message;
        else newText += "\n" + message;
        messageTextArea.setText(newText);
        messageTextArea.setScrollTop(Double.MAX_VALUE);
    }

    //FXML Method Implementations

    public void openDownloadFolderViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) openDownloadFolder();
    }

    public void openDownloadFolder() throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(new File(data.getDownloadDirectory()));
                return;
            }
        }
        Utils.showErrorMessage("That operation is not supported on your system.", false);
    }

    public void downloadMoreViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) downloadMore(keyEvent);
    }

    public void downloadMore(Event event) throws IOException {
        //TODO: verify reasonable default values for Data
        if (data.isFromSingleArtist()) SingleArtistController.start(Utils.getStage(event), Cache.getDefaultData());
        else MultipleArtistsController.start(Utils.getStage(event), Cache.getDefaultData());
    }

    public void cancelViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) cancel(keyEvent);
    }

    public void cancel(Event event) throws IOException {
        writeMessage("Cancelling.");
        if (task != null) task.cancel();
        if (data.isFromSingleArtist()) SingleArtistController.start(Utils.getStage(event), data);
        else MultipleArtistsController.start(Utils.getStage(event), data);
    }
}
