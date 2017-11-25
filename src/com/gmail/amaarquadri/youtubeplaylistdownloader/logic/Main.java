package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import com.gmail.amaarquadri.youtubeplaylistdownloader.ui.LoadingScreenController;
import com.gmail.amaarquadri.youtubeplaylistdownloader.ui.MultipleArtistsController;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.util.Set;

/**
 * Created by Amaar on 2016-04-18.
 * The entry point for the Application.
 * TODO: cancel after entering url, work around permission pop up, make FXML method implementations private
 * TODO (bugs): leaving temp files, no doing anything, cant edit cache
 */
public class Main extends Application {
    /**
     * Object used as a lock for synchronizing between setDriverError and start.
     */
    private static final Object lock = new Object();

    /**
     * String to hold the error message associated with any error that occurs while initializing the driver.
     * Will be null if no error occurs.
     */
    private static String driverError = null;

    /**
     * Boolean indicating whether or not the synchronized block in the start method has run.
     */
    private static boolean hasStarted = false;


    /**
     * Entry point for the Application.
     */
    public static void main(String[] args) {
        /*Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            //Although this exception is not necessarily (or likely to be) a driver error,
            //the definition of setDriverError is perfectly suited to arbitrary unhandled Exceptions as well
            setDriverError(e, "An unknown fatal error occurred.");
        });
        DriverUtils.initDriver();
        launch(args);*/
        dumbShit();
    }

    public static void setDriverError(Throwable e, String driverError) {
        synchronized (lock) {
            Main.driverError = driverError;
            if (hasStarted) Utils.showErrorMessage(driverError, true);
        }
        Utils.logThrowable(e);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        synchronized (lock) {
            hasStarted = true;
            if (driverError != null) {
                Utils.showErrorMessage(driverError, true);
                return;
            }
        }

        Data cacheData = Cache.getDefaultData();
        if (verifyCache(cacheData)) LoadingScreenController.start(primaryStage, cacheData, false);
        else MultipleArtistsController.start(primaryStage, cacheData);
        //MetadataEditorController.start(primaryStage, (Data) new ObjectInputStream(new FileInputStream(new File("default.data"))).readObject());
    }

    //returns whether or not this data should be autoLoaded
    private boolean verifyCache(Data cacheData) {
        if (cacheData.getDownloadDirectory().isEmpty()) {
            if (Cache.isAutoLoad()) Cache.setCache(cacheData.getPlaylistUrl(), "", false);
            return false;
        }
        File directoryFile = new File(cacheData.getDownloadDirectory());
        try {
            if (!directoryFile.exists()) {
                if (new Alert(Alert.AlertType.ERROR, "The default download directory no longer exists. " +
                        "Do you want to create it and continue?", ButtonType.YES, ButtonType.NO).showAndWait()
                        .filter(buttonType -> buttonType == ButtonType.YES).isPresent()) {
                    if (!directoryFile.mkdirs()) {
                        Utils.showErrorMessage("An error occurred while creating the specified directory.", false);
                        cacheData.setDownloadDirectory("");
                        Cache.setCache(cacheData.getPlaylistUrl(), "", false);
                        return false;
                    }
                    //TODO: show toast telling user directory was created successfully
                }
                else {
                    cacheData.setDownloadDirectory("");
                    Cache.setCache(cacheData.getPlaylistUrl(), "", false);
                    return false;
                }
            }

            if (!directoryFile.isDirectory()) {
                Utils.showErrorMessage("The default directory in the cache is no longer valid.", false);
                cacheData.setDownloadDirectory("");
                Cache.setCache(cacheData.getPlaylistUrl(), "", false);
                return false;
            }

            if (!directoryFile.canWrite())
                throw new SecurityException("Creating SecurityException for control flow.");
        }
        catch (SecurityException e) {
            Utils.showErrorMessage("This app no longer has permission to create files in the default download " +
                    "directory. Please choose another location.", false);
            cacheData.setDownloadDirectory("");
            Cache.setCache(cacheData.getPlaylistUrl(), "", false);
            return false;
        }
        if (!Cache.isAutoLoad()) return false;
        if (cacheData.getPlaylistUrl().isEmpty()) {
            //TODO: show toast saying autoLoad could not be done
            Cache.setCache("", cacheData.getDownloadDirectory(), false);
            return false;
        }
        return true;
    }

    public static void dumbShit() {
        System.setProperty("webdriver.chrome.driver",
                "C:\\Users\\Amaar\\Documents\\ProgrammingProjects\\Libraries\\Selenium\\chromedriver.exe");
        ChromeDriver driver = new ChromeDriver();
        for (int i = 0;; i++) {
            try {
                driver.get("http://www.anonvote.com/poll/u586987y");
                WebElement element = driver.findElement(By.xpath(
                        "html/body/div/form/div/div/div/input[@id='cb4']"));
                element.click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                WebElement submit = driver.findElement(By.xpath("html/body/div/form/input[@name='submit']"));
                submit.click();
                Set<Cookie> cookies = driver.manage().getCookies();
                for (Cookie cookie : cookies) if (cookie.getDomain().equals("www.anonvote.com")) {
                    driver.manage().deleteCookie(cookie);
                    System.out.println("Noooo my cookies");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(i);
        }
    }
}
