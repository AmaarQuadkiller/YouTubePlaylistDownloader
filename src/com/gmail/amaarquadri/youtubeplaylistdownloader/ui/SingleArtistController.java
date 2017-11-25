package com.gmail.amaarquadri.youtubeplaylistdownloader.ui;

import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Data;
import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Utils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Amaar on 2016-04-17.
 */
public final class SingleArtistController extends PrimaryScreenController {
    public static void start(Stage stage, Data data) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource(
                "/com/gmail/amaarquadri/youtubeplaylistdownloader/fxml/single_artist.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("YouTube Playlist Downloader - Single Artist");
        stage.show();
        ((SingleArtistController) loader.getController()).init(data);
    }

    @FXML private TextField artistNameTextField;
    @FXML private TextField albumNameTextField;

    @Override
    protected void init(Data data) {
        super.init(data);
        data.setFromSingleArtist(true);

        artistNameTextField.setText(data.getArtistName());
        albumNameTextField.setText(data.getAlbumName());

        artistNameTextField.textProperty().addListener((observable, oldValue, newValue) ->
                data.setArtistName(newValue));
        albumNameTextField.textProperty().addListener(((observable, oldValue, newValue) ->
                data.setAlbumName(newValue)));

        if (data.getArtistName().isEmpty()) artistNameTextField.requestFocus();
        else if (data.getAlbumName().isEmpty()) albumNameTextField.requestFocus();
        else if (!assignFocus()) artistNameTextField.requestFocus();
    }

    //FXML Method Implementations

    public void multipleArtistsViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) multipleArtists(keyEvent);
    }

    public void multipleArtists(Event event) throws IOException {
        MultipleArtistsController.start(Utils.getStage(event), data);
    }
}
