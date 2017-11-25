package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.*;

/**
 * Created by Amaar on 2017-05-06.
 * Class containing utility methods for use throughout the Application.
 */
public class Utils {
    private Utils() {
        throw new AssertionError("Cannot instantiate Utils.");
    }

    /**
     * Returns the Stage that the given Event originated from.
     *
     * @param event The event that will be used.
     * @return The Stage that the given Event originated from.
     */
    public static Stage getStage(Event event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    /**
     * Shows an Alert dialog to the user indicating an error of some sort.
     *
     * @param message The error message to show.
     * @param catchFire Whether or not the Application should terminate once the Alert dialog is closed.
     */
    public static void showErrorMessage(String message, boolean catchFire) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        if (catchFire) alert.setOnCloseRequest(event -> Platform.exit());

        if (Platform.isFxApplicationThread()) alert.show();
        else Platform.runLater(alert::show);
    }

    /**
     * Attempts to create a File that exists and is a directory, based on a user inputted path.
     * This method first checks if the File described by the path is suitable.
     * If not, it recursively attempts to find a suitable File using that File's parent Files.
     * If no suitable File could be found with that approach, it attempts to use the user's download directory.
     * If no download directory was found, then null is returned.
     *
     * @param path The path to use.
     * @return A File that exists and is a directory, or null if no such File could be found.
     */
    public static File getDirectory(String path) {
        File file;
        //if the path isn't empty, use (and its parents) to try to find a File that exists and is a directory
        if (path != null && !path.isEmpty()) for (file = new File(path); file != null; file = file.getParentFile())
            if (file.exists() && file.isDirectory()) return file;
        //if the path was empty, or a suitable File could not be found, then try to use the user's download directory
        file = new File(getDefaultDownloadDirectoryPath());
        return file.exists() && file.isDirectory() ? file : null;
    }

    private static String defaultDownloadDirectoryPath;
    public static String getDefaultDownloadDirectoryPath() {
        if (defaultDownloadDirectoryPath != null) return defaultDownloadDirectoryPath;
        return defaultDownloadDirectoryPath = System.getProperty("user.home") + "\\Downloads";
    }

    /**
     * Log File to write error messages such as StackTraces from Throwables.
     */
    private static final File LOG_FILE = new File("YouTube Playlist Downloader Log.txt");

    /**
     * Appends the StackTrace from the given Throwable to the LOG_FILE, creating the File if necessary.
     *
     * @param throwable The Throwable to be logged.
     */
    public static void logThrowable(Throwable throwable) {throwable.printStackTrace(); //TODO: remove
        try (StringWriter writer = new StringWriter(); FileWriter fileWriter = new FileWriter(LOG_FILE, true)) {
            throwable.printStackTrace(new PrintWriter(writer));
            fileWriter.write(format(writer.toString()));
        }
        catch (IOException ignore) {}
    }

    /**
     * Appends the given message to the LOG_FILE, creating the File if necessary.
     *
     * @param message The String to be logged.
     */
    public static void log(String message) {System.err.println(message); //TODO: remove
        try (FileWriter fileWriter = new FileWriter(LOG_FILE, true)) {
            fileWriter.write(format(message));
        }
        catch (IOException ignore) {}
    }

    private static String format(String message) {
        return System.currentTimeMillis() + ":\n" + message + "\n\n";
    }

    //TODO: implement Toasts.
    /*public static void showToast(Stage stage, String message) {
        NotificationPane pane = new NotificationPane();
        pane.setShowFromTop(false);
        pane.setText(message);
        pane.show();
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}
            Platform.runLater(pane::hide);
        }).start();
    }*/
}
