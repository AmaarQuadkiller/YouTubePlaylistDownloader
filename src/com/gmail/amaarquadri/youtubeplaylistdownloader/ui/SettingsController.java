package com.gmail.amaarquadri.youtubeplaylistdownloader.ui;

import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Cache;
import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Data;
import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Utils;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Created by Amaar on 2017-08-17.
 */
public final class SettingsController {
    public static void start(Stage stage, Data data) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource(
                "/com/gmail/amaarquadri/youtubeplaylistdownloader/fxml/settings.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("YouTube Playlist Downloader - Settings");
        stage.show();
        ((SettingsController) loader.getController()).init(data);
    }

    //Controller Instance Members

    @FXML private TextField defaultPlaylistUrlTextField;
    @FXML private TextField defaultDownloadDirectoryTextField;
    @FXML private CheckBox autoLoadCheckBox;
    private Data data;

    private void init(Data data) {
        this.data = data;
        defaultPlaylistUrlTextField.setText(Cache.getDefaultPlaylistUrl());
        defaultDownloadDirectoryTextField.setText(Cache.getDefaultDownloadDirectory());
        autoLoadCheckBox.setSelected(Cache.isAutoLoad());

        if (defaultPlaylistUrlTextField.getText().isEmpty() || defaultDownloadDirectoryTextField.getText().isEmpty())
            autoLoadCheckBox.setDisable(true);
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (autoLoadCheckBox.isDisabled()) {
                if (!defaultPlaylistUrlTextField.getText().isEmpty() &&
                        !defaultDownloadDirectoryTextField.getText().isEmpty())
                    autoLoadCheckBox.setDisable(false);
            }
            else if (newValue.isEmpty()) {
                autoLoadCheckBox.setSelected(false);
                autoLoadCheckBox.setDisable(true);
            }
        };
        defaultPlaylistUrlTextField.textProperty().addListener(listener);
        defaultDownloadDirectoryTextField.textProperty().addListener(listener);
    }

    //FXML Method Implementations

    public void goBackViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) goBack(keyEvent);
    }

    public void goBack(Event event) throws IOException {
        if (data.isFromSingleArtist()) SingleArtistController.start(Utils.getStage(event), data);
        else MultipleArtistsController.start(Utils.getStage(event), data);
    }

    public void pasteFromClipboardViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) pasteFromClipboard();
    }

    public void pasteFromClipboard() {
        String clipboardText = Clipboard.getSystemClipboard().getString();
        if (clipboardText != null && !clipboardText.isEmpty()) defaultPlaylistUrlTextField.setText(clipboardText);
        else Utils.showErrorMessage("Nothing on the Clipboard to paste.", false);
    }

    public void pickDownloadDirectoryViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) pickDownloadDirectory(keyEvent);
    }

    public void pickDownloadDirectory(Event event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Pick the Default Directory for Downloads");
        chooser.setInitialDirectory(Utils.getDirectory(defaultDownloadDirectoryTextField.getText()));
        File file = chooser.showDialog(Utils.getStage(event));
        if (file != null) defaultDownloadDirectoryTextField.setText(file.getAbsolutePath());
    }

    public void saveViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) save();
    }

    public void save() {
        String defaultDownloadDirectory = defaultDownloadDirectoryTextField.getText();
        if (!defaultDownloadDirectory.isEmpty())
            if (!PrimaryScreenController.verifyDirectory(new File(defaultDownloadDirectory))) return;

        String defaultPlaylistUrl = defaultPlaylistUrlTextField.getText();
        if (Cache.setCache(defaultPlaylistUrl, defaultDownloadDirectory, autoLoadCheckBox.isSelected())) {
            if (data.getPlaylistUrl().isEmpty()) data.setPlaylistUrl(defaultPlaylistUrl);
            if (data.getDownloadDirectory().isEmpty() ||
                    data.getDownloadDirectory().equals(Utils.getDefaultDownloadDirectoryPath()))
                data.setDownloadDirectory(defaultDownloadDirectory);
        }
        else Utils.showErrorMessage("Failed to save to cache file. " +
                "Try moving the app to another location if this error persists.", false);
    }
}
