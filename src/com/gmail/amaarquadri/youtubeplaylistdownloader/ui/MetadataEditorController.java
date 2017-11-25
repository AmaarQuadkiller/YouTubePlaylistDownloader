package com.gmail.amaarquadri.youtubeplaylistdownloader.ui;

import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Data;
import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Song;
import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Utils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Amaar on 2017-01-29.
 * TODO what if playlist is empty
 */
public final class MetadataEditorController {
    public static void start(Stage stage, Data data) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource(
                "/com/gmail/amaarquadri/youtubeplaylistdownloader/fxml/metadata_editor.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("YouTube Playlist Downloader - Edit Metadata");
        stage.show();
        ((MetadataEditorController) loader.getController()).init(data);
    }

    @FXML private ListView<HBox> titlesListView;
    @FXML private CheckBox addTrackNumbersCheckBox;
    private Data data;
    private ObservableList<HBox> rows;
    private ArrayList<Song> songs;
    private ArrayList<Song> deletedSongs;

    private void init(Data data) {
        this.data = data;
        //prevent rows from being highlighted
        titlesListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                Platform.runLater(() -> titlesListView.getSelectionModel().select(-1)));
        rows = titlesListView.getItems();
        songs = data.getSongs();
        deletedSongs = new ArrayList<>();
        //TODO: create label in titlesListView that says Artist: and Title: and Up: and Down: as necessary
        double largestIndexLabelWidth = -1;

        for (int i = 0 ; i < songs.size(); i++) {
            Song song = songs.get(i);

            HBox row = new HBox();
            row.setSpacing(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setFocusTraversable(false);

            Label indexLabel = new Label(String.valueOf(i + 1));
            row.getChildren().add(indexLabel);

            if (indexLabel.getPrefWidth() > largestIndexLabelWidth)
                largestIndexLabelWidth = indexLabel.getPrefWidth();

            if (!this.data.isFromSingleArtist()) {
                TextField artistNameTextField = new TextField(song.getArtistName());
                HBox.setHgrow(artistNameTextField, Priority.ALWAYS);
                artistNameTextField.textProperty().addListener((observable, oldValue, newValue) ->
                        song.setArtistName(newValue));
                row.getChildren().add(artistNameTextField);
            }

            TextField titleTextField = new TextField(song.getTitle());
            HBox.setHgrow(titleTextField, Priority.ALWAYS);
            titleTextField.textProperty().addListener(((observable, oldValue, newValue) ->
                    song.setTitle(newValue)));
            row.getChildren().add(titleTextField);

            Button upButton = new Button("⬆");//↑
            upButton.setOnMouseClicked(event -> {
                int currentIndex = songs.indexOf(song);
                if (currentIndex == 0) return;
                rows.add(currentIndex - 1, rows.remove(currentIndex));
                songs.add(currentIndex - 1, songs.remove(currentIndex));
                indexLabel.setText(String.valueOf(currentIndex));
                ((Label) rows.get(currentIndex).getChildren().get(0)).setText(String.valueOf(currentIndex + 1));
            });
            upButton.setFocusTraversable(false);
            row.getChildren().add(upButton);

            Button downButton =  new Button("⬇");//↓
            downButton.setOnMouseClicked(event -> {
                int currentIndex = songs.indexOf(song);
                if (currentIndex == songs.size() - 1) return;
                rows.add(currentIndex + 1, rows.remove(currentIndex));
                songs.add(currentIndex + 1, songs.remove(currentIndex));
                indexLabel.setText(String.valueOf(currentIndex + 2));
                ((Label) rows.get(currentIndex).getChildren().get(0)).setText(String.valueOf(currentIndex + 1));
            });
            downButton.setFocusTraversable(false);
            row.getChildren().add(downButton);

            Button xButton = new Button("X");
            xButton.setOnMouseClicked(event -> {
                int currentIndex = songs.indexOf(song);
                rows.remove(currentIndex);
                songs.remove(currentIndex);
                for (int j = currentIndex; j < songs.size(); j++)
                    ((Label) rows.get(j).getChildren().get(0)).setText(String.valueOf(j + 1));

                if (deletedSongs.isEmpty()) {
                    HBox deletedSongsRow = new HBox(new Label("Deleted Songs (will not be downloaded):"));
                    deletedSongsRow.setAlignment(Pos.CENTER);
                    rows.add(deletedSongsRow);
                }
                deletedSongs.add(song);

                ObservableList<Node> children = row.getChildren();
                children.remove(0);
                children.remove(children.size() - 1);
                children.remove(children.size() - 1);
                children.remove(children.size() - 1);

                Button checkButton = new Button("\u2713");//\u2714
                checkButton.setOnMouseClicked(event2 -> {
                    rows.remove(row);
                    deletedSongs.remove(song);
                    if (deletedSongs.isEmpty()) rows.remove(rows.size() - 1);

                    children.remove(children.size() - 1);
                    indexLabel.setText(String.valueOf(songs.size() + 1));
                    children.add(0, indexLabel);
                    children.add(upButton);
                    children.add(downButton);
                    children.add(xButton);
                    rows.add(songs.size(), row);
                    songs.add(song);
                });
                checkButton.setFocusTraversable(false);
                children.add(checkButton);

                rows.add(row);
            });
            xButton.setFocusTraversable(false);
            row.getChildren().add(xButton);

            rows.add(row);
        }

        for (int i = 0; i < songs.size(); i++)
            ((Label) rows.get(i).getChildren().get(0)).setPrefWidth(largestIndexLabelWidth);

        addTrackNumbersCheckBox.setSelected(data.isFromSingleArtist());
        addTrackNumbersCheckBox.selectedProperty().addListener(((observable, oldValue, newValue) ->
                data.setAddTrackNumbers(newValue)));
    }

    public void deleteFromStartViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) deleteFromStart();
    }

    public void deleteFromStart() {
        int titleTextFieldPos = data.isFromSingleArtist() ? 1 : 2;
        //iterate over all songs
        for (int i = 0; i < songs.size(); i++) {
            TextField titleTextField = (TextField) rows.get(i).getChildren().get(titleTextFieldPos);
            String title = titleTextField.getText();
            if (!title.isEmpty()) titleTextField.setText(title.substring(1));
        }
        //iterate over all deleted songs (skip row with just label at i = songs.size())
        for (int i = songs.size() + 1; i < rows.size(); i++) {
            //subtract 1 from titleTextFiledPos because there is no index label
            TextField titleTextField = (TextField) rows.get(i).getChildren().get(titleTextFieldPos - 1);
            String title = titleTextField.getText();
            if (!title.isEmpty()) titleTextField.setText(title.substring(1));
        }
    }

    public void deleteFromEndViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) deleteFromEnd();
    }

    public void deleteFromEnd() {
        int titleTextFieldPos = data.isFromSingleArtist() ? 1 : 2;
        //iterate over all songs
        for (int i = 0; i < songs.size(); i++) {
            TextField titleTextField = (TextField) rows.get(i).getChildren().get(titleTextFieldPos);
            String title = titleTextField.getText();
            if (!title.isEmpty()) titleTextField.setText(title.substring(0, title.length() - 1));
        }
        //iterate over all deleted songs (skip row with just label at i = songs.size())
        for (int i = songs.size() + 1; i < rows.size(); i++) {
            //subtract 1 from titleTextFiledPos because there is no index label
            TextField titleTextField = (TextField) rows.get(i).getChildren().get(titleTextFieldPos - 1);
            String title = titleTextField.getText();
            if (!title.isEmpty()) titleTextField.setText(title.substring(0, title.length() - 1));
        }
    }

    public void openYouTubePlaylistViaEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) openYouTubePlaylist();
    }

    public void openYouTubePlaylist() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI(data.getPlaylistUrl()));
                }
                catch (URISyntaxException e) {
                    Utils.showErrorMessage("The YouTube playlist URL is malformed.", false);
                }
                catch (SecurityException e) {
                    Utils.showErrorMessage(
                            "This app does not have permission to open the YouTube playlist in your browser.", false);
                }
                catch (IOException e) {
                    Utils.showErrorMessage(
                            "An error occurred while trying to open the YouTube playlist in your browser.", false);
                }
                return;
            }
        }
        Utils.showErrorMessage("That operation is not supported on your system.", false);
    }

    public void startDownloadsViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) startDownloads(keyEvent);
    }

    public void startDownloads(Event event) throws IOException {
        LoadingScreenController.start(Utils.getStage(event), data, true);
    }

    public void cancelViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) cancel(keyEvent);
    }

    public void cancel(Event event) throws IOException {
        if (data.isFromSingleArtist()) SingleArtistController.start(Utils.getStage(event), data);
        else MultipleArtistsController.start(Utils.getStage(event), data);
    }
}
