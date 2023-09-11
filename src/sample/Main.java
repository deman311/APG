package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends Application {
    public static final String VERSION = "1.7.2";     // EDIT THE VERSION HERE

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        var embedReader = new EmbedReader(getHostServices());

        // Welcome Message
        JFrame frame = new JFrame();
            // Read from the changelog.
        Scanner fr = new Scanner(getClass().getResourceAsStream("/Changelog.txt"));
        StringBuilder changeLog = new StringBuilder();
        while (fr.hasNextLine())
            changeLog.append(fr.nextLine() + "\n");
        JOptionPane.showMessageDialog(frame, "© Dmitry Gribovsky\n" +
                        "[ Changelog v" + VERSION + " ]\n\n" + changeLog,
                "Welcome to APG v" + VERSION, JOptionPane.INFORMATION_MESSAGE);

        var mainScreen = new MainScreen(stage, embedReader.getEmbedBtn());
        new Soundboard(stage); // create the soundboard
        var searchHandler = new SearchHandler(mainScreen, getHostServices());
        mainScreen.SetEventHandler(searchHandler);
    }
}
