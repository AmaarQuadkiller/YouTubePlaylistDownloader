package com.gmail.amaarquadri.youtubeplaylistdownloader.logic;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Amaar on 2017-08-23.
 */
public class DriverUtils {
    private DriverUtils() {
        throw new AssertionError("Cannot instantiate DriverUtils.");
    }

    /**
     * The File where the driver's executable File is stored.
     */
    private static final File DRIVER_EXECUTABLE_FILE = new File("driver.exe");

    private static RemoteWebDriver driver;
    private static final Object driverLock = new Object();
    private static final Object threadLock = new Object();

    /**
     * Asynchronously loads the driver into DRIVER_EXECUTABLE_FILE, if it does not already exist.
     * Also, calls createDriver.
     */
    public static void initDriver() {
        if (DRIVER_EXECUTABLE_FILE.exists()) {
            new Thread(DriverUtils::createDriver).start();
            return;
        }

        new Thread(() -> {
            InputStream input;
            try {
                input = DriverTask.class.getResource(
                        "/com/gmail/amaarquadri/youtubeplaylistdownloader/driver.exe").openStream();
            }
            catch (IOException e) {
                Main.setDriverError(e, "There is a problem with the contents of this application, making it unusable. " +
                        "Please contact the developer for help.");
                return;
            }

            FileOutputStream output;
            try {
                output = new FileOutputStream(DRIVER_EXECUTABLE_FILE);
            }
            catch (FileNotFoundException e) {
                Main.setDriverError(e, "This application is in a location where it cannot create files. " +
                        "Please move it to another location and try again.");
                return;
            }

            try {
                IOUtils.copy(input, output);
            }
            catch (IOException e) {
                Main.setDriverError(e, "A fatal error occurred. Please try again. " +
                        "If this error persists, please contact the developer for help.");
                return;
            }

            try {
                input.close();
            } catch (IOException ignore) {}
            try {
                output.close();
            } catch (IOException ignore) {}

            try {
                Files.setAttribute(FileSystems.getDefault().getPath(DRIVER_EXECUTABLE_FILE.getAbsolutePath()),
                        "dos:hidden", true);
            } catch (IOException ignore) {}

            createDriver();
        }).start();
    }

    private static void createDriver() {
        try {
            synchronized (driverLock) {
                driver = new PhantomJSDriver(new PhantomJSDriverService.Builder()
                        .usingPhantomJSExecutable(DRIVER_EXECUTABLE_FILE).withLogFile(null).build(),
                        DesiredCapabilities.phantomjs());
            }
        }
        catch (Exception e) {//includes UnreachableBrowserException
            Main.setDriverError(e, "An error occurred.");
            if (!DRIVER_EXECUTABLE_FILE.delete())
                Utils.log("Couldn't delete \"" + DRIVER_EXECUTABLE_FILE.getAbsolutePath() + "\".");
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> driver.quit()));
    }

    public static boolean isDriverReady() {
        synchronized (driverLock) {
            return driver != null;
        }
    }

    /**
     * Provides a single Object lock for synchronization of DriverTasks methods.
     * This allows a set of DriverTasks methods to be called together as a single atomic operation.
     *
     * @return An Object lock for synchronization of DriverTasks methods.
     */
    public static Object getLock() {
        return threadLock;
    }

    public static void get(String url) {
        driver.get(url);
    }

    public static String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public static WebElement findWebElement(String webElementDescription) throws NoSuchElementException {
        return driver.findElement(getBy(webElementDescription));
    }

    public static List<WebElement> findWebElements(String webElementDescription) {
        return driver.findElements(getBy(webElementDescription));
    }

    private static By getBy(String webElementDescription) {
        switch (webElementDescription) {
            case "urlBox": return By.name("url");
            case "loadMoreButton": return By.xpath(
                    "html/body/div/div/div/div/div/div/div/div/div/div/ul/li/div/button");
            case "songTitleLink": return By.xpath(
                    "html/body/div/div/div/div/div/div/div/div/div/div/ul/li/div/table/tbody[@id='pl-load-more-destination']/tr/td/a");
            case "editArtistButton": return By.xpath(
                    "/html/body/div/div/div/form/div/div/label/span[@id='input_artist']/a");
            case "editArtistTextBox": return By.xpath(
                    "/html/body/div/div/div/form/div/div/label/span[@id='input_artist']/input");
            case "editTitleButton": return By.xpath(
                    "/html/body/div/div/div/form/div/div/label/span[@id='input_title']/a");
            case "editTitleTextBox": return By.xpath(
                    "/html/body/div/div/div/form/div/div/label/span[@id='input_title']/input");
            case "advancedTagsButton": return By.xpath(
                    "/html/body/div/div/div/form/div/a[@id='advancedtags_btn']");
            case "editAlbumTextBox": return By.xpath(
                    "/html/body/div/div/div/form/div/div/div/input[@id='inputAlbum']");
            case "continueButton": return By.xpath(
                    "/html/body/div/div/div/form/div/div/button[@type='submit']");
            case "downloadButton": return By.xpath(
                    "/html/body/div/div/div/div/div/a");
            default: throw new IllegalArgumentException();
        }
    }
}
