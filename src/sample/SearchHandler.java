package sample;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchHandler implements EventHandler {
    Random rand = new Random();
    boolean isListening = false;

    // Sounds
    Media coinInsert = new Media(getClass().getResource("/Sounds/coin_insert.mp3").toExternalForm());
    Media cashInSound = new Media(getClass().getResource("/Sounds/bingo.mp3").toExternalForm());
    Media monkey = new Media(getClass().getResource("/Sounds/monkey.mp3").toExternalForm());

    // Media
    Image like_img = new Image(getClass().getResource("/Program Images/like.png").toExternalForm());
    Image dislike_img = new Image(getClass().getResource("/Program Images/dislike.png").toExternalForm());
    Media pop = new Media(getClass().getResource("/Sounds/pop.wav").toExternalForm());

    VBox loadingBox;
    MainScreen ms;
    HostServices hostServices;

    public SearchHandler(MainScreen mainScreen, HostServices hostServices) {
        this.ms = mainScreen;
        this.hostServices = hostServices;

        // create loading gif
        ImageView loading_iv = new ImageView(new Image(getClass().getResource("/Program Images/loading.gif").toExternalForm()));
        loading_iv.setFitHeight(150);
        loading_iv.setFitWidth(300);
        loadingBox = new VBox(loading_iv);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setMaxSize(350,200);
        loadingBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(25), Insets.EMPTY)));
    }

    @Override
    public void handle(Event event) {
        int MAX_THREADS = 5;
        int TOTAL = (int) ms.slider.getValue();
        int RANGE;
        SyncCounter counter;

        // Delete the old photos from the directory
        File photoDir = new File(ms.TEMP_FOLDER_DIR);
        if (photoDir.listFiles() != null)
            for (File f : photoDir.listFiles())
                f.delete();

        MediaPlayer btnSound;
        // Recreate the soundtrack each time - to replay it.
        if (ms.isRangedSearch()) {
            RANGE = TOTAL;
            btnSound = new MediaPlayer(cashInSound);
            btnSound.setVolume((ms.slider.getValue() / ms.slider.getMax()) / 5); // play the volume in correlation to the RANGE
        } else {
            btnSound = new MediaPlayer(coinInsert);
            btnSound.setVolume(0.1);

            // MAKE THE MULTITHREAD RUN ONCE WITH 5
            RANGE = 5;
            TOTAL = RANGE;
        }
        counter = new SyncCounter(TOTAL);

        if (ms.translateVRbtn.isSelected())
            VR_Server.sendPicture("@RESTART@", 0, 0); // reset VR environment

        int REMAIN = TOTAL % MAX_THREADS;
        for (int i = 0; i < MAX_THREADS; i++) {
            if (TOTAL / MAX_THREADS == 0) {
                RANGE = TOTAL % MAX_THREADS;
                i = MAX_THREADS - 1; // make it finish using only 1 thread
            }
            if (TOTAL % MAX_THREADS == 0) {
                RANGE = TOTAL / MAX_THREADS;
            } else {
                if (i < MAX_THREADS - 1 && REMAIN > 0) {
                    RANGE = (int) Math.floor((float) TOTAL / MAX_THREADS) + 1; // add it to the REMAIN
                    REMAIN--;
                } else
                    RANGE = (int) Math.floor((float) TOTAL / MAX_THREADS);
            }

            int finalRANGE = RANGE, finalI = i;
            Thread t = new Thread(() -> {
                for (int k = 0; k < finalRANGE; k++) {
                    String rHexa = "", imgDir = "";
                    boolean fromServer = false;

                    if (ms.searchMethod.getValue().toString().contentEquals("Server")
                            && !((imgDir = FetchFromServer()).contentEquals("EMPTY"))) {
                        fromServer = true;
                        rHexa = imgDir;
                    }
                    else {
                        // generate random 6 ascii url
                        for (int p = 0; p < 6; p++) {
                            int j = rand.nextInt(35);
                            if (j <= 25)
                                rHexa += (char) (j + 'a');
                            else
                                rHexa += "" + (j - 26);
                        }
                    }
                    String link = "https://prnt.sc/" + rHexa;
                    Label l1 = new Label(link);
                    Label s1 = new Label();
                    l1.setFont(Font.font("Dekko", FontWeight.BOLD, 14));
                    s1.setFont(Font.font("Dekko", FontWeight.BOLD,12));
                    l1.setOnMouseClicked(ae -> {
                        ms.clp.clear();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(l1.getText().replace(" - Could not get the image!", "").substring(l1.getText().indexOf('h')));
                        ms.clp.setContent(content);
                        if (l1.getTextFill() != Color.RED) {
                            new Thread(() -> {
                                Paint lastColor = l1.getTextFill();
                                l1.setTextFill(Color.RED);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                l1.setTextFill(lastColor);
                            }).start();
                        }
                    });

                    boolean IMGerr = false;
                    String YandexSearch = "";
                    File dir = new File(ms.TEMP_FOLDER_DIR);
                    try {
                        if (!ms.delaytf.getText().isEmpty() && !fromServer)
                            while (!counter.GetPermission(Integer.parseInt(ms.delaytf.getText()))) { /* Wait for permission */ }
                        Document doc = Jsoup.connect("https://prnt.sc/" + rHexa).get();
                        String imgSRC = doc.getElementsByClass("no-click screenshot-image").first().absUrl("src");
                        YandexSearch = "https://yandex.com/images/search?rpt=imageview&url=" + imgSRC;
                        imgDir = dir.getAbsolutePath() + "/" + rHexa + ".png";

                        if (!fromServer) {
                            URL url = new URL(imgSRC);
                            URLConnection connection = url.openConnection();
                            connection.setRequestProperty("User-Agent",
                                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                            connection.connect();
                            InputStream in = connection.getInputStream();
                            dir.mkdir();
                            OutputStream out = new BufferedOutputStream(new FileOutputStream(imgDir));
                            for (int b; (b = in.read()) != -1; )
                                out.write(b);
                            out.close();
                            in.close();
                        }

                        float imsize = Files.size(new File(imgDir).toPath()) / 1024f;
                        s1.setText("Size: " + String.format("%.2f", imsize) + " kb");

                        Image IMG = new Image(new File(imgDir).toURI().toString());
                        if (IMG.getWidth() <= 3 || IMG.getHeight() <= 3)
                            throw new IOException();
                        Encoder.CreateEmbed(IMG, rHexa, imgDir);
                    } catch (NullPointerException | IOException e) {
                        l1.setText(null);
                        k--; // search for another
                        IMGerr = true;
                    } catch (Exception e) {
                        l1.setText(l1.getText() + " - " + e.getLocalizedMessage());
                        e.printStackTrace();
                        try {
                            PrintWriter elog = new PrintWriter(new File("./APG_Errorlog.txt"));
                            e.printStackTrace(elog);
                            elog.println("Image code that caused the error: " + rHexa);
                            elog.flush();
                            elog.close();
                        } catch (IOException ex) {
                        }
                        IMGerr = true;
                    }

                    if (!IMGerr) {
                        File imgPath = new File(dir.getAbsolutePath() + "/" + rHexa + ".png");
                        Image IMG = new Image(imgPath.toURI().toString());

                        int AI_value = 2;   // from server is default good
                        if (!fromServer)
                            AI_value = BanScanner.runThroughAI(IMG, l1, !ms.searchMethod.getValue().toString().contentEquals("Direct - No AI"));
                        ms.mainPane.layout();
                        if (AI_value == 0) {
                            SyncCounter.foundBanned();
                            imgPath.delete();
                            l1.setText(null);
                            k--;
                        }
                        else if (AI_value == 1) { // && mainPane.getVvalue() != mainPane.getVmax()
                            SyncCounter.filteredBad();
                            imgPath.delete();
                            l1.setText(null);
                            k--;
                        }
                        else {
  /*                              if (AI_value == 1 && mainPane.getVvalue() == mainPane.getVmax() && useAI.isSelected()) {
                                    l1.setText("WHILE YOU WAIT\n" + l1.getText());
                                    l1.setTextFill(Color.GREEN);
                                    k--;
                                }*/

                            if (ms.translateVRbtn.isSelected())
                                VR_Server.sendPicture(imgPath.getAbsolutePath(), (int) IMG.getWidth(), (int) IMG.getHeight());

                            ImageView im1 = new ImageView(IMG);

                            // Create the context menu
                            ContextMenu cm = new ContextMenu();
                            MenuItem mi1 = new MenuItem("Yandex search");
                            MenuItem mi2 = new MenuItem("Copy image");
                            MenuItem mi3 = new MenuItem("Move to 'Good' folder");
                            MenuItem mi4 = new MenuItem("Save image");
                            String finalYandexSearch = YandexSearch;
                            String finalImgDir = imgDir;
                            String finalRhexa = rHexa;

                            mi1.setOnAction(ae -> {
                                hostServices.showDocument(finalYandexSearch);
                            });
                            mi2.setOnAction(ae -> {
                                // Copy file to Clipboard
                                ClipboardContent content = new ClipboardContent();
                                ms.clp.clear();
                                content.putImage(im1.getImage());
                                ms.clp.setContent(content);
                            });
                            mi3.setOnAction(ae -> {
                                mi2.fire(); // fires the copyToClipboard action

                                // Move to Good
                                File sDir = new File(ms.TEMP_FOLDER_DIR + "Good/");
                                sDir.mkdirs();
                                File databaseFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");

                                Image resized = new Image(im1.getImage().getUrl(), 224, 224, false, false);
                                BufferedImage piped = SwingFXUtils.fromFXImage(resized, null);

                                try {
                                    ImageIO.write(piped, "png", databaseFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                cm.getItems().remove(mi3); // remove the option after saving!
                            });
                            mi4.setOnAction(ae -> {
                                File sDir = new File("./APG Saved Images/");
                                sDir.mkdir(); // validate that the dir exists
                                File f = new File(finalImgDir);
                                try {
                                    File savedFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");
                                    FileOutputStream out = new FileOutputStream(savedFile);
                                    FileInputStream in = new FileInputStream(f);
                                    out.write(in.readAllBytes());

                                    in.close();
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                cm.getItems().remove(mi4); // remove the option after saving!
                            });
                            cm.getItems().addAll(mi2, mi3, mi4, mi1);

                            im1.setOnContextMenuRequested(ae -> cm.show(im1, ae.getScreenX(), ae.getScreenY()));

                            im1.setFitWidth(im1.getImage().getWidth());
                            im1.setFitHeight(im1.getImage().getHeight());
                            while (im1.getFitWidth() > ms.scene.getWidth() - 100 || im1.getFitHeight() > ms.scene.getHeight() - 100) {
                                im1.setFitWidth(im1.getFitWidth() / 2);
                                im1.setFitHeight(im1.getFitHeight() / 2);
                            }

                            im1.setOnMouseClicked(ae -> {
                                if(ae.getButton() == MouseButton.PRIMARY)
                                    hostServices.showDocument(im1.getImage().getUrl());
                            });
                            Platform.runLater(() -> {
                                VBox imBox = new VBox(l1, s1, im1);
                                imBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(15), Insets.EMPTY)));
                                imBox.setAlignment(Pos.CENTER);

                                // create like dislike buttons
                                Button like = new Button(), dislike = new Button();
                                ImageView like_imview = new ImageView(like_img); like_imview.setFitWidth(20); like_imview.setFitHeight(20);
                                like.setGraphic(like_imview);
                                ImageView dislike_imview = new ImageView(dislike_img); dislike_imview.setFitWidth(20); dislike_imview.setFitHeight(20);
                                dislike.setGraphic(dislike_imview);
                                like.setPrefSize(20, 20);
                                dislike.setPrefSize(20, 20);
                                like.setFocusTraversable(false);
                                dislike.setFocusTraversable(false);

                                like.setOnAction(ae -> {
                                    if (!ms.mute.isSelected()) {
                                        MediaPlayer mp = new MediaPlayer(monkey);
                                        mp.setVolume(0.1);
                                        mp.play();
                                    }

                                    like.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                                    dislike.setBackground(Background.EMPTY);

                                    // check if exists in Bad first
                                    File sDir = new File(ms.TEMP_FOLDER_DIR + "Bad/");
                                    sDir.mkdirs();
                                    File tempFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");

                                    if (tempFile.exists())
                                        tempFile.delete();

                                    // Move to Good
                                    sDir = new File(ms.TEMP_FOLDER_DIR + "Good/");
                                    sDir.mkdirs();
                                    File databaseFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");

                                    Image resized = new Image(im1.getImage().getUrl(), 224, 224, false, false);
                                    BufferedImage piped = SwingFXUtils.fromFXImage(resized, null);

                                    try {
                                        ImageIO.write(piped, "png", databaseFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    like.setDisable(true);
                                    dislike.setDisable(false);
                                });

                                dislike.setOnAction(ae -> {
                                    if (!ms.mute.isSelected()) {
                                        MediaPlayer mp = new MediaPlayer(pop);
                                        mp.setVolume(0.1);
                                        mp.play();
                                    }

                                    dislike.setBackground(new Background(new BackgroundFill(Color.INDIANRED, CornerRadii.EMPTY, Insets.EMPTY)));
                                    like.setBackground(Background.EMPTY);

                                    // check if exists in Good first
                                    File sDir = new File(ms.TEMP_FOLDER_DIR + "Good/");
                                    sDir.mkdirs();
                                    File tempFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");

                                    if (tempFile.exists())
                                        tempFile.delete();

                                    // Move to Bad
                                    sDir = new File(ms.TEMP_FOLDER_DIR + "Bad/");
                                    sDir.mkdirs();
                                    File databaseFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");

                                    Image resized = new Image(im1.getImage().getUrl(), 224, 224, false, false);
                                    BufferedImage piped = SwingFXUtils.fromFXImage(resized, null);

                                    try {
                                        ImageIO.write(piped, "png", databaseFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    like.setDisable(false);
                                    dislike.setDisable(true);
                                });

                                HBox likeBox = new HBox(like, dislike);
                                likeBox.setSpacing(20);
                                likeBox.setPadding(new Insets(5));
                                likeBox.setAlignment(Pos.BOTTOM_CENTER);

                                imBox.getChildren().add(likeBox);
                                ms.mainBox.getChildren().add(imBox);

                                if (im1.getImage().getWidth() > ms.mainPane.getWidth()) {
                                    ms.mainPane.layout();
                                    ms.mainPane.setHvalue(ms.mainPane.getHmax() / 2);
                                }
                                imBox.setMaxWidth(im1.getFitWidth());

                                ms.mainBox.getChildren().get(ms.mainBox.getChildren().indexOf(loadingBox)).toFront();    // move loading to last
                                counter.Count();
                            });
                        }
                    } else {
                        if (l1.getText() != null)// if it is null then I've added another iteration.
                            Platform.runLater(() -> {
                                ms.mainBox.getChildren().addAll(l1);
                                counter.Count();
                            });
                    }
                }

                if (finalI == MAX_THREADS - 1) {
                    while (!counter.isDone()) {
                        // await for all the other threads to be done
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.err.println("[Caused by the waiting last thread]");
                        }
                    }
                    Platform.runLater(() -> {
                        ms.mainBox.getChildren().remove(loadingBox);

                        // reset the buttons
                        ms.gib.setDisable(false);
                        ms.sliderbtn.setDisable(false);
                        ms.translateVRbtn.setDisable(false);
                        ms.mute.setDisable(false);
                        //useAI.setDisable(false);
                        ms.searchMethod.setDisable(false);

                        Label bc = new Label("");
                        if (SyncCounter.getBanned() > 0) {
                            if (ms.searchMethod.getValue().toString().contentEquals("Direct - AI"))
                                bc = new Label("This search has filtered " + SyncCounter.getFiltered() +
                                        " bad images by the AI, and banned " + SyncCounter.getBanned() + ".");
                            else
                                bc = new Label("This search has banned " + SyncCounter.getBanned() + " images.");
                            bc.setTextFill(Color.WHITE);
                            bc.setFont(Font.font("Helvetica", FontWeight.SEMI_BOLD, 13));
                            if(SyncCounter.getBanned() == 1)
                                bc.setText(bc.getText().replace("images", "image"));
                        }
                        ms.mainBox.getChildren().addAll(ms.gibBottom, bc);

                        if (ms.translateVRbtn.isSelected()) {
                            VR_Server.sendPicture("@END@", 0, 0); // Close last wall
                            ListenToInput(ms.slider, ms.sliderbtn);
                        }
                    });
                }
            });
            t.setDaemon(true);
            t.start();
            System.out.println("Started Thread" + (i + 1) + " with a range of " + RANGE);
        }

        ms.mainBox.getChildren().clear();
        ms.mainBox.getChildren().addAll(ms.vboxTitle, ms.vboxBtns, ms.hboxSlider, loadingBox);

        ms.gib.setDisable(true);
        ms.sliderbtn.setDisable(true);
        ms.translateVRbtn.setDisable(true);    // disable the checkbox of VR to failsafe from bugs
        ms.mute.setDisable(true);
        //useAI.setDisable(true);
        ms.searchMethod.setDisable(true);

        if (!ms.mute.isSelected())
            btnSound.play();
    }

    private void ListenToInput(Slider slider, Button sliderbtn) {
        if (isListening)
            return;
        isListening = true;

        AtomicInteger atg = new AtomicInteger(0);
        Thread t = new Thread(() -> {
            atg.set(VR_Server.awaitInput()); // amount to generate
            Platform.runLater(() -> {
                slider.setValue(atg.get());
                sliderbtn.fire();
                isListening = false;
            });
        });

        t.setDaemon(true);
        t.start();
    }

    private String FetchFromServer() {
        String fileName = "EMPTY";
        Socket client = null;
        // try all possible 5 ports
        for (int i=0; i<5; i++) {
            try {
                client = new Socket(ms.SERVER_IP, 5000 + i);
                break;  // reach break only if successful
            } catch (IOException e) {
                // try another
            }
        }
        try {
            if (client == null)
                throw new IOException("Could not connect to any server port 5000-5009!");

            System.out.println("Client connected!");
            DataInputStream in = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            fileName = in.readUTF();

            System.out.println(fileName);
            if (fileName.contentEquals("EMPTY"))
                return "EMPTY";

            System.out.println("Client: Received Image by name: " + fileName);

            new File("./APG Temp Images/").mkdirs(); // validate directory exists
            FileOutputStream out = new FileOutputStream("./APG Temp Images/" + fileName + ".png");

            for (int b; (b = in.read()) != -1; )
                out.write(b);
            in.close();
            out.close();

            System.out.println("Client: Done accepting the image!");
            client.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fileName;
    }
}
