package com.gmail.amaarquadri.youtubeplaylistdownloader.ui;

import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Data;
import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Utils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;

/**
 * Created by Amaar on 2017-06-21.
 */
public class PrimaryScreenController {
    @FXML protected TextField playlistURLTextField;
    @FXML protected Button pasteFromClipboardButton;
    @FXML protected TextField downloadDirectoryTextField;
    @FXML protected Button pickDownloadDirectoryButton;
    protected Data data;

    //@CallSuper
    protected void init(Data data) {
        this.data = data;
        playlistURLTextField.setText(data.getPlaylistUrl());
        downloadDirectoryTextField.setText(data.getDownloadDirectory());

        playlistURLTextField.textProperty().addListener(((observable, oldValue, newValue) ->
                data.setPlaylistUrl(newValue)));
        downloadDirectoryTextField.textProperty().addListener(((observable, oldValue, newValue) ->
                data.setDownloadDirectory(newValue)));
    }

    /**
     * Assigns focus to the topmost empty TextField.
     * If neither TextField is empty, then neither will be assigned focus.
     *
     * @return Whether or not focus was assigned to a TextField.
     */
    protected final boolean assignFocus() {
        if (playlistURLTextField.getText().isEmpty()) pasteFromClipboardButton.requestFocus();
        else if (downloadDirectoryTextField.getText().isEmpty())
            pickDownloadDirectoryButton.requestFocus();
        else return false;
        return true;
    }


    public final void goToSettingsViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) goToSettings(keyEvent);
    }

    public final void goToSettings(Event event) throws IOException {
        SettingsController.start(Utils.getStage(event), data);
    }

    public final void pasteFromClipboardViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) pasteFromClipboard();
    }

    public final void pasteFromClipboard() {
        String clipboardText = Clipboard.getSystemClipboard().getString();
        if (clipboardText != null && !clipboardText.isEmpty()) playlistURLTextField.setText(clipboardText);
        else Utils.showErrorMessage("Nothing on the Clipboard to paste.", false);
    }

    public final void pickDownloadDirectoryViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) pickDownloadDirectory(keyEvent);
    }

    public final void pickDownloadDirectory(Event event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Pick the Directory for Downloads");
        chooser.setInitialDirectory(Utils.getDirectory(downloadDirectoryTextField.getText()));
        File file = chooser.showDialog(Utils.getStage(event));
        if (file != null) downloadDirectoryTextField.setText(file.getAbsolutePath());
    }

    public final void startDownloadsViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) startDownloads(keyEvent);
    }

    public final void startDownloads(Event event) throws IOException {
        if (playlistURLTextField.getText().isEmpty()) {
            Utils.showErrorMessage("You must enter in a URL for the YouTube playlist " +
                    "(where MP3 files will be downloaded from).", false);
            return;
        }

        final String downloadDirectory = downloadDirectoryTextField.getText();
        if (downloadDirectory.isEmpty()) {
            Utils.showErrorMessage("You must specify the download directory, " +
                    "where the MP3 files will be saved.", false);
            return;
        }

        if (!verifyDirectory(new File(downloadDirectory))) return;
        LoadingScreenController.start(Utils.getStage(event), data, false);
    }

    public static boolean verifyDirectory(File directoryFile) {
        try {
            if (!directoryFile.exists()) {
                if (new Alert(Alert.AlertType.ERROR, "The download directory you chose does not exist. " +
                        "Do you want to create it and continue?", ButtonType.YES, ButtonType.NO).showAndWait()
                        .filter(buttonType -> buttonType == ButtonType.YES).isPresent()) {
                    if (!directoryFile.mkdirs()) {
                        Utils.showErrorMessage("An error occurred while creating the specified directory." , false);
                        return false;
                    }
                    //TODO: show toast telling user directory was created successfully
                }
                else return false;
            }

            if (!directoryFile.isDirectory()) {
                Utils.showErrorMessage("You must specify a directory (folder) under \"Download Directory\", " +
                        "where the MP3 files will be saved.", false);
                return false;
            }

            if (!directoryFile.canWrite()) throw new SecurityException("Creating SecurityException for control flow.");
        }
        catch (SecurityException e) {
            Utils.showErrorMessage("This app doesn't have permission to create files in the specified download " +
                    "directory. Please choose another location.", false);
            return false;
        }
        return true;
    }
}
