package com.gmail.amaarquadri.youtubeplaylistdownloader.ui;

import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Data;
import com.gmail.amaarquadri.youtubeplaylistdownloader.logic.Utils;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Amaar on 2017-06-13.
 */
public final class MultipleArtistsController extends PrimaryScreenController {
    public static void start(Stage stage, Data data) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource(
                "/com/gmail/amaarquadri/youtubeplaylistdownloader/fxml/multiple_artists.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("YouTube Playlist Downloader - Multiple Artists");
        stage.show();
        ((MultipleArtistsController) loader.getController()).init(data);
    }

    @Override
    protected void init(Data data) {
        super.init(data);
        data.setFromSingleArtist(false);
        if (!assignFocus()) pasteFromClipboardButton.requestFocus();
    }

    //FXML Method Implementations

    public void singleArtistViaEnter(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) singleArtist(keyEvent);
    }

    public void singleArtist(Event event) throws IOException {
        SingleArtistController.start(Utils.getStage(event), data);
    }
}
