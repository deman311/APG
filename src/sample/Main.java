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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends Application {

    final String VERSION = "1.5.2";           // EDIT THE VERSION HERE
    Random rand = new Random();
    boolean isListening = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("PRNT GENERATOR v" + VERSION);
        stage.setResizable(true);

        Clipboard clp = Clipboard.getSystemClipboard();

        // Title related
        ImageView title = new ImageView(new Image(getClass().getResource("/Program Images/APG Title.png").toExternalForm()));
        Label subtitle = new Label((char) 169 + "Dmitry Gribovsky --- Version " + VERSION);
        subtitle.setFont(Font.font("Serif", FontWeight.THIN, 15));
        Label banCount = new Label("Banned Images: " + BanScanner.BANNED_COUNT);
        BanScanner.loadBanned(); // load banned images to an array in static class
        banCount.setTextFill(Color.RED);
        CheckBox mute = new CheckBox("Mute APG sounds");
        mute.setPadding(new Insets(20));

        // Embed Reader
        Button btn_embed = new Button("Decoder");
        Label em_title = new Label("Drag a photo on this window to decode.");
        em_title.setFont(Font.font("Serif", FontWeight.BOLD, 14));
        TextField code = new TextField();
        code.setMaxWidth(80);
        code.setAlignment(Pos.CENTER);
        code.setEditable(false);
        VBox vbox_em = new VBox(em_title, code);
        vbox_em.setAlignment(Pos.CENTER);
        vbox_em.setSpacing(20);
        Scene em_scene = new Scene(vbox_em, 300, 100);
        em_scene.setOnDragOver(ae -> {
            ae.acceptTransferModes(TransferMode.COPY);
        });
        em_scene.setOnDragDropped(de -> {
            boolean success = false;
            if (de.getDragboard().hasFiles()) {
                success = true;
                String url = de.getDragboard().getFiles().get(0).getAbsolutePath();
                if (url.contains(".png")) {
                    code.clear();
                    try {
                        code.setText(IMGhandler.GetEmbed(url));
                    } catch (IOException e) {
                        e.printStackTrace();
                        code.setText("ERROR");
                    }
                }
                de.setDropCompleted(success);
                de.consume();
            }
        });
        Stage em_stage = new Stage();
        em_stage.setTitle("Decoder");
        vbox_em.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        em_stage.setScene(em_scene);
        em_stage.setResizable(false);

        btn_embed.setOnAction(ae -> {
            code.clear();
            if (em_stage.isShowing())
                em_stage.hide();
            else
                em_stage.show();
        });
        // --------------------------------------------------------embed reader end

        CheckBox translateVRbtn = new CheckBox("Translate to VR");
        HBox checksHbox = new HBox(mute, translateVRbtn);
        checksHbox.setAlignment(Pos.CENTER);

        VBox vboxTitle = new VBox(title, subtitle, banCount, checksHbox, btn_embed);
        vboxTitle.setAlignment(Pos.TOP_CENTER);

        Button gib = new Button("GIVE ME 5");
        gib.setPrefWidth(300);
        gib.setPrefHeight(50);
        Background gibBG = new Background(new BackgroundFill(Color.BLACK, new CornerRadii(20), Insets.EMPTY));
        gib.setBackground(gibBG);
        gib.setTextFill(Color.GOLD);
        gib.setFont(Font.font("Aerial", FontWeight.EXTRA_BOLD, 20));
        gib.setOnMouseEntered(ae -> {
            gib.setBackground(new Background(new BackgroundFill(Color.GOLD, new CornerRadii(20), Insets.EMPTY)));
            gib.setTextFill(Color.BLACK);
        });
        gib.setOnMouseExited(ae -> {
            gib.setBackground(gibBG);
            gib.setTextFill(Color.GOLD);
        });
        gib.setFocusTraversable(false); // fix the scroll jumping to focus on the button after it was clicked.
        Button gibBottom = new Button("GIVE ME MORE");
        gibBottom.setPrefWidth(300);
        gibBottom.setPrefHeight(50);
        Background gibBotBG = new Background(new BackgroundFill(Color.BLACK, new CornerRadii(20), Insets.EMPTY));
        gibBottom.setBackground(gibBotBG);
        gibBottom.setTextFill(Color.GOLD);
        gibBottom.setFont(Font.font("Aerial", FontWeight.EXTRA_BOLD, 20));
        gibBottom.setOnMouseEntered(ae -> {
            gibBottom.setBackground(new Background(new BackgroundFill(Color.GOLD, new CornerRadii(20), Insets.EMPTY)));
            gibBottom.setTextFill(Color.BLACK);
        });
        gibBottom.setOnMouseExited(ae -> {
            gibBottom.setBackground(gibBotBG);
            gibBottom.setTextFill(Color.GOLD);
        });
        gibBottom.setFocusTraversable(false);

        // Slider section
        Slider slider = new Slider(10, 100, 10);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setPrefWidth(400);
        Label sliderValue = new Label("Amount to Generate: 10");
        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (Math.abs(oldValue.intValue() - newValue.intValue()) > 0)
                sliderValue.setText("Amount to Generate: " + newValue.intValue());
        });
        Button sliderbtn = new Button("SHOWER ME CONTENT");
        sliderbtn.setFocusTraversable(false);
        HBox hboxSlider = new HBox(sliderValue, slider, sliderbtn);
        hboxSlider.setSpacing(15);
        hboxSlider.setAlignment(Pos.CENTER);
        AtomicBoolean isRangedSearch = new AtomicBoolean(false);

        // Kill everything related on exit
        stage.setOnCloseRequest(ae -> {
            Platform.exit();
            System.exit(0);
        });

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

        //POOL BANNED IMAGES
//        Button temp = new Button("Pool Banned");
//        temp.setOnAction(ae ->{
//            File pDir = new File("Bano");
//            Pooler pt = new Pooler(3);
//            for(File f : pDir.listFiles()) {
//                Image img = pt.Pool(new Image(f.toURI().toString())); // pooled
//                BufferedImage bf = SwingFXUtils.fromFXImage(img, null);
//                try {
//                    ImageIO.write(bf, "PNG", f);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        // Sounds
        Media coinInsert = new Media(getClass().getResource("/Sounds/coin_insert.mp3").toExternalForm());
        Media cashInSound = new Media(getClass().getResource("/Sounds/bingo.mp3").toExternalForm());
        //Soundboard related
        Media ah = new Media(getClass().getResource("/Sounds/animemoan.mp3").toExternalForm());
        Media pinkfluffy = new Media(getClass().getResource("/Sounds/pinkfluffy.mp3").toExternalForm());
        Media bananaphone = new Media(getClass().getResource("/Sounds/bananaphone.mp3").toExternalForm());
        Media fart = new Media(getClass().getResource("/Sounds/fart.mp3").toExternalForm());
        Media dicks = new Media(getClass().getResource("/Sounds/dicks.mp3").toExternalForm());
        Media hotaliensex = new Media(getClass().getResource("/Sounds/hotaliensex.mp3").toExternalForm());
        Media rampage = new Media(getClass().getResource("/Sounds/rampage.mp3").toExternalForm());
        Media slap = new Media(getClass().getResource("/Sounds/slap.mp3").toExternalForm());
        Media yee2 = new Media(getClass().getResource("/Sounds/yee2.mp3").toExternalForm());
        Media yesyesyes = new Media(getClass().getResource("/Sounds/yesyesyes.mp3").toExternalForm());
        Media burp1 = new Media(getClass().getResource("/Sounds/burp1.mp3").toExternalForm());
        Media ass1 = new Media(getClass().getResource("/Sounds/ass1.mp3").toExternalForm());

        // Create the Soundboard
        Button btnAH = new Button("AH"), btnPF = new Button("PinkFluffy"), btnBP = new Button("BananaPhone"), btnFRT = new Button("Fart"), btnDKS = new Button("Dicks");
        Button btnHAS = new Button("HotAlienSex"), btnRAM = new Button("Rampage"), btnSLP = new Button("Slap"), btnYEE = new Button("Yee");
        Button btnYES = new Button("YesYesYes"), btnBRP = new Button("Burp"), btnASS = new Button("Ass");
        Label titleSounds = new Label("SoundBoard");
        titleSounds.setFont(Font.font("Serif", FontWeight.EXTRA_BOLD, 20));
        // Create 3 rows of sounds
        HBox hSounds1 = new HBox(btnAH, btnPF, btnBP, btnFRT);
        hSounds1.setAlignment(Pos.TOP_CENTER);
        HBox hSounds2 = new HBox(btnDKS, btnHAS, btnRAM, btnSLP);
        hSounds2.setAlignment(Pos.TOP_CENTER);
        HBox hSounds3 = new HBox(btnYEE, btnYES, btnBRP, btnASS);
        hSounds3.setAlignment(Pos.TOP_CENTER);
        CheckBox alwaysonCB = new CheckBox("Always on top");
        VBox vSoundsButtons = new VBox(hSounds1, hSounds2, hSounds3), vSoundsTitle = new VBox(titleSounds, alwaysonCB), vSoundsMain = new VBox(vSoundsTitle, vSoundsButtons);
        vSoundsTitle.setAlignment(Pos.TOP_CENTER);
        vSoundsButtons.setAlignment(Pos.CENTER);
        vSoundsMain.setSpacing(30);
        vSoundsMain.setBackground(new Background((new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(30), new Insets(-10)))));
        // Set button actions
        setBtnAction(hSounds1, ah, btnAH);
        setBtnAction(hSounds1, pinkfluffy, btnPF);
        setBtnAction(hSounds1, bananaphone, btnBP);
        setBtnAction(hSounds1, fart, btnFRT);
        setBtnAction(hSounds2, dicks, btnDKS);
        setBtnAction(hSounds2, hotaliensex, btnHAS);
        setBtnAction(hSounds2, rampage, btnRAM);
        setBtnAction(hSounds2, slap, btnSLP);
        setBtnAction(hSounds3, yee2, btnYEE);
        setBtnAction(hSounds3, burp1, btnBRP);
        setBtnAction(hSounds3, ass1, btnASS);
        setBtnAction(hSounds3, yesyesyes, btnYES);

        // MAIN SCENE
        VBox mainBox;
        mainBox = new VBox(vboxTitle, gib, hboxSlider);
        ScrollPane mainPane = new ScrollPane(mainBox);
        Scene scene = new Scene(mainPane, 1280, 720);
        title.setFitWidth(scene.getWidth() / 1.2);
        title.setFitHeight(title.getFitHeight() / 1.2);
        mainBox.setSpacing(30);
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainPane.setFitToWidth(true);
        mainPane.setPadding(new Insets(10));
        Background bg = new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(bg);
        mainBox.setBackground(bg);
        mainPane.setStyle("-fx-background-color: lightblue; -fx-border-insets: 10; -fx-border-radius: 20; -fx-border-width: 2; -fx-border-color: white;");
        stage.setScene(scene);
        stage.show();

        // SOUNDBOARD Window
        Scene sbScene = new Scene(vSoundsMain, 450, 450);
        Stage sbStage = new Stage();
        sbStage.setResizable(false);
        sbStage.setX(stage.getX() + stage.getWidth());
        sbStage.setY(stage.getY() + stage.getHeight() / 5);
        sbStage.setTitle("Soundboard");
        sbStage.setScene(sbScene);
        sbStage.show();
        alwaysonCB.setOnAction(ae -> {
            sbStage.setAlwaysOnTop(alwaysonCB.isSelected());
        });

//        mainBox.getChildren().add(temp);

        // Icons
        Image APGicon = new Image(getClass().getResource("/Program Images/APGico.png").toExternalForm());
        Image SBicon = new Image(getClass().getResource("/Program Images/SBico.png").toExternalForm());
        stage.getIcons().add(APGicon);
        sbStage.getIcons().add(SBicon);

        // EVENT HANDLER FOR THE SEARCH BUTTONS
        EventHandler eh = event -> {
            int MAX_THREADS = 5;
            int TOTAL = (int) slider.getValue();
            int RANGE;
            SyncCounter counter;

            // Delete the old photos from the directory
            File photoDir = new File("./APG Photos/");
            if (photoDir.listFiles() != null)
                for (File f : photoDir.listFiles())
                    f.delete();

            MediaPlayer btnSound;
            // Recreate the soundtrack each time - to replay it.
            if (isRangedSearch.get()) {
                RANGE = TOTAL;
                btnSound = new MediaPlayer(cashInSound);
                btnSound.setVolume((slider.getValue() / slider.getMax()) / 3); // play the volume in correlation to the RANGE
            } else {
                btnSound = new MediaPlayer(coinInsert);
                btnSound.setVolume(0.1);

                // MAKE THE MULTITHREAD RUN ONCE WITH 5
                RANGE = 5;
                TOTAL = RANGE;
            }
            counter = new SyncCounter(TOTAL);

            if (translateVRbtn.isSelected())
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
                        RANGE = (int) Math.floor(TOTAL / MAX_THREADS) + 1; // add it to the REMAIN
                        REMAIN--;
                    } else
                        RANGE = (int) Math.floor(TOTAL / MAX_THREADS);
                }

                int finalRANGE = RANGE, finalI = i;
                Thread t = new Thread(() -> {
                    for (int k = 0; k < finalRANGE; k++) {
                        //rand.setSeed(13 * k * finalRANGE * System.currentTimeMillis()); // for more diverse randomizer
                        String rHexa = "";
                        for (int p = 0; p < 6; p++) {
                            int j = rand.nextInt(35);
                            if (j <= 25)
                                rHexa += (char) (j + 'a');
                            else
                                rHexa += "" + (j - 26);
                        }

                        String link = "https://prnt.sc/" + rHexa;
                        Label l1 = new Label(link);
                        Label s1 = new Label();
                        l1.setFont(Font.font("Dekko", 14));
                        s1.setFont(Font.font("Dekko", 12));
                        l1.setOnMouseClicked(ae -> {
                            clp.clear();
                            ClipboardContent content = new ClipboardContent();
                            content.putString(l1.getText().replace(" - Could not get the image!", ""));
                            clp.setContent(content);
                            if (l1.getTextFill() != Color.RED) {
                                new Thread(() -> {
                                    l1.setTextFill(Color.RED);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    l1.setText(l1.getText().replace(" - Copied~!", ""));
                                    l1.setTextFill(Color.BLACK);
                                }).start();
                            }
                        });

                        boolean IMGerr = false;
                        String YandexSearch = "";
                        File dir = new File("./APG Photos/");
                        String imgDir = "";
                        try {
                            Document doc = Jsoup.connect("https://prnt.sc/" + rHexa).get();
                            String imgSRC = doc.getElementsByClass("no-click screenshot-image").first().absUrl("src");
                            YandexSearch = "https://yandex.com/images/search?rpt=imageview&url=" + imgSRC;
                            URL url = new URL(imgSRC);
                            URLConnection connection = url.openConnection();
                            connection.setRequestProperty("User-Agent",
                                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                            connection.connect();
                            InputStream in = connection.getInputStream();
                            dir.mkdir();
                            imgDir = dir + "/" + rHexa + ".png";
                            OutputStream out = new BufferedOutputStream(new FileOutputStream(imgDir));
                            for (int b; (b = in.read()) != -1; )
                                out.write(b);
                            out.close();
                            in.close();

                            float imsize = new File(imgDir).length() / 1024f;
                            imsize += imsize * 0.03f;
                            s1.setText("Size: " + String.format("%.2f", imsize) + " kb");

                            Image IMG = new Image(new File(imgDir).toURI().toString());
                            if (IMG.getWidth() <= 3 || IMG.getHeight() <= 3)
                                throw new IOException();
                            IMGhandler.CreateEmbed(IMG, rHexa, imgDir);
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
                            double oldWidth, oldHeight;
                            File imgPath = new File(dir + "/" + rHexa + ".png");
                            Image IMG = new Image(imgPath.toURI().toString());

                            if (BanScanner.checkIMG(IMG, l1)) {
                                SyncCounter.foundBanned();
                                imgPath.delete();
                                l1.setText(null);
                                k--;
                            } else {
                                if (translateVRbtn.isSelected())
                                    VR_Server.sendPicture(imgPath.getAbsolutePath(), (int) IMG.getWidth(), (int) IMG.getHeight());

                                ImageView im1 = new ImageView(IMG);

                                // Create the context menu
                                ContextMenu cm = new ContextMenu();
                                MenuItem mi1 = new MenuItem("Yandex reverse image search");
                                MenuItem mi2 = new MenuItem("Copy image");
                                MenuItem mi3 = new MenuItem("Save image");
                                String finalYandexSearch = YandexSearch;
                                mi1.setOnAction(ae -> getHostServices().showDocument(finalYandexSearch));
                                mi2.setOnAction(ae -> {
                                    ClipboardContent content = new ClipboardContent();
                                    clp.clear();
                                    content.putImage(im1.getImage());
                                    clp.setContent(content);
                                });
                                String finalImgDir = imgDir;
                                mi3.setOnAction(ae -> {
                                    File sDir = new File("./APG Saved Images/");
                                    sDir.mkdir(); // validate that the dir exists
                                    File f = new File(finalImgDir);
                                    f.renameTo(new File((sDir + "/" + l1.getText() + ".png").replace("https://prnt.sc/", "")));
                                });
                                cm.getItems().addAll(mi2, mi3, mi1);

                                im1.setOnContextMenuRequested(ae -> cm.show(im1, ae.getScreenX(), ae.getScreenY()));

                                if ((im1.getImage().getHeight() > 400 && im1.getImage().getWidth() > 600)) {
                                        /*|| im1.getImage().getHeight() > scene.getHeight() - 50
                                        || im1.getImage().getWidth() > scene.getWidth() - 50) {*/
                                    oldWidth = 600;
                                    oldHeight = 400;
                                } else {
                                    oldWidth = im1.getImage().getWidth();
                                    oldHeight = im1.getImage().getHeight();
                                }
                                im1.setFitWidth(oldWidth);
                                im1.setFitHeight(oldHeight);

                                im1.setOnMouseClicked(ae -> {
                                    if (ae.getButton() == MouseButton.PRIMARY) {
                                        if (im1.getFitWidth() == scene.getWidth()) {
                                            im1.setFitWidth(oldWidth);
                                            im1.setFitHeight(oldHeight);
                                        } else {
                                            im1.setFitWidth(scene.getWidth());
                                            im1.setFitHeight(scene.getHeight());
                                        }
                                    }
                                });
                                Platform.runLater(() -> {
                                    VBox imBox = new VBox(l1, s1, im1);
                                    imBox.setAlignment(Pos.CENTER);
                                    mainBox.getChildren().add(imBox);

                                    if (im1.getImage().getWidth() > mainPane.getWidth()) {
                                        mainPane.layout();
                                        mainPane.setHvalue(mainPane.getHmax() / 2);
                                    }

                                    counter.Count();
                                });
                            }
                        } else {
                            if (l1.getText() != null)// if it is null then I've added another iteration.
                                Platform.runLater(() -> {
                                    mainBox.getChildren().addAll(l1);
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
                            // reset the buttons
                            gib.setDisable(false);
                            sliderbtn.setDisable(false);
                            translateVRbtn.setDisable(false);

                            Label bc = null;
                            if (SyncCounter.getBanned() > 0) {
                                bc = new Label("This search has filtered " + SyncCounter.getBanned() + " banned images.");
                                mainBox.getChildren().addAll(gibBottom, bc);
                            } else
                                mainBox.getChildren().add(gibBottom);

                            if (translateVRbtn.isSelected()) {
                                VR_Server.sendPicture("@END@", 0, 0); // Close last wall
                                ListenToInput(slider, sliderbtn);
                            }
                        });
                    }
                });
                t.setDaemon(true);
                t.start();
                System.out.println("Started Thread" + (i + 1) + " with a range of " + RANGE);
            }

            mainBox.getChildren().clear();
            mainBox.getChildren().addAll(vboxTitle, gib, hboxSlider);

            gib.setDisable(true);
            sliderbtn.setDisable(true);
            translateVRbtn.setDisable(true);    // disable the checkbox of VR to failsafe from bugs

            if (!mute.isSelected())
                btnSound.play();
        };
        sliderbtn.setOnAction(ae ->
        {
            isRangedSearch.compareAndSet(false, true);
            sliderbtn.setDisable(true);
            eh.handle(ae);
        });

        gib.setOnAction(ae ->
        {
            isRangedSearch.compareAndSet(true, false);
            eh.handle(ae);
        });
        gibBottom.setOnAction(eh);

        if (translateVRbtn.isSelected())
            ListenToInput(slider, sliderbtn);
    }

    private void ListenToInput(Slider slider, Button sliderbtn) {
        if (isListening)
            return;
        isListening = true;

        AtomicInteger atg = new AtomicInteger(0);
        new Thread(() -> {
            atg.set(VR_Server.awaitInput()); // amount to generate
            Platform.runLater(() -> {
                slider.setValue(atg.get());
                sliderbtn.fire();
                isListening = false;
            });
        }).start();
    }

    public void setBtnAction(HBox hbox, Media media, Button btn) {
        btn.setPrefWidth(100);
        btn.setPrefHeight(100);
        Font oldF = btn.getFont();
        btn.setOnMousePressed(ae -> {
            btn.setTextFill(Color.BLUE);
            btn.setFont(Font.font("Imperial", FontWeight.EXTRA_BOLD, 12));
        });
        btn.setOnMouseReleased(ae -> {
            btn.setTextFill(Color.BLACK);
            btn.setFont(oldF);
        });
        btn.setOnAction(ae -> {
            //hbox.setDisable(true); // can now play simultaneously.
            MediaPlayer mp = new MediaPlayer(media);
            mp.setOnEndOfMedia(() -> hbox.setDisable(false));
            if (btn.getText().compareTo("HotAlienSex") == 0)
                mp.setVolume(0.4); // the volume of HAS is a bit low, so it needs specific adjustment
            else
                mp.setVolume(0.1);
            mp.play();
        });
    }
}
