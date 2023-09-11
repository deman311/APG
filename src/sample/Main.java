package sample;

import ai.onnxruntime.OrtException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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

    final String VERSION = "1.7.2";     // EDIT THE VERSION HERE
    final String TEMP_FOLDER_DIR = "./APG Temp Images/";
    String SERVER_IP = "192.168.1.61";     // default for now

    Random rand = new Random();
    boolean isListening = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("AUTOMATIC PRNT.SC GENERATOR v" + VERSION);
        stage.setResizable(true);

        Clipboard clp = Clipboard.getSystemClipboard();

        // Title related
        ImageView title = new ImageView(new Image(getClass().getResource("/Program Images/logo.png").toExternalForm()));
        title.setFitHeight(title.getFitHeight() / 2);
        title.setFitWidth(title.getFitWidth() / 2);
        Label subtitle = new Label((char) 169 + "Dmitry Gribovsky --- Version " + VERSION);
        subtitle.setFont(Font.font("Serif", FontWeight.THIN, 15));

            // Ban related
        Label banCount = new Label("Banned Images: " + BanScanner.BANNED_COUNT);
        banCount.setTextFill(Color.WHITE);
        banCount.setFont(Font.font("Serif", FontWeight.SEMI_BOLD, 15));
        BanScanner.loadBanned(); // load banned images to an array in static class
        try {
            BanScanner.initONNX(); // init the ONNX model

        } catch (OrtException e) {
            JOptionPane.showMessageDialog(new JFrame(), "© Dmitry Gribovsky\n" +
                            "The was an error loading the onnx model. " +
                            "Please verify that you have a APG_AI.onnx file in the program directory.",
                    "ONNX ERROR", JOptionPane.ERROR_MESSAGE);
        }

        CheckBox mute = new CheckBox("Mute APG sounds");
        //CheckBox useAI = new CheckBox("Use AI Filtering");
        ChoiceBox searchMethod = new ChoiceBox(FXCollections.observableArrayList("Server", "Direct - AI", "Direct - No AI"));

        ImageView ai_logo = new ImageView(new Image(getClass().getResource("/Program Images/ai_logo.gif").toExternalForm()));
        ai_logo.setFitHeight(60);
        ai_logo.setFitWidth(80);
        ai_logo.setVisible(false);
        //useAI.setOnAction(a -> ai_logo.setVisible(!ai_logo.isVisible()));

        // create loading gif
        ImageView loading_iv = new ImageView(new Image(getClass().getResource("/Program Images/loading.gif").toExternalForm()));
        loading_iv.setFitHeight(150);
        loading_iv.setFitWidth(300);
        VBox loading = new VBox(loading_iv);
        loading.setAlignment(Pos.CENTER);
        loading.setMaxSize(350,200);
        loading.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(25), Insets.EMPTY)));

        // Like Dislike pictures buttons load.
        Image like_img = new Image(getClass().getResource("/Program Images/like.png").toExternalForm());
        Image dislike_img = new Image(getClass().getResource("/Program Images/dislike.png").toExternalForm());
        Media pop = new Media(getClass().getResource("/Sounds/pop.wav").toExternalForm());

        // Embed Reader
        Button btn_embed = BtnFactory.CreateButton("Decoder", 200, 50);
        btn_embed.setFocusTraversable(false);
        //btn_embed.setPadding(new Insets(5));
        Label em_title = new Label("Drag a photo on this window to decode.");
        em_title.setFont(Font.font("Serif", FontWeight.BOLD, 14));
        em_title.setTextFill(Color.WHITE);
        TextField code = new TextField();
        code.setMaxWidth(200);
        code.setAlignment(Pos.CENTER);
        code.setEditable(false);
        VBox vbox_em = new VBox(em_title, code);
        vbox_em.setAlignment(Pos.CENTER);
        vbox_em.setSpacing(20);
        Scene em_scene = new Scene(vbox_em, 300, 100);
        em_scene.setOnDragOver(ae -> ae.acceptTransferModes(TransferMode.COPY));
        em_scene.setOnDragDropped(de -> {
            boolean success = false;
            if (de.getDragboard().hasFiles()) {
                success = true;
                String url = de.getDragboard().getFiles().get(0).getAbsolutePath();
                if (url.contains(".png")) {
                    code.clear();
                    code.setOnMouseClicked(ae -> { /* empty action */ });
                    try {
                        code.setText("https://prnt.sc/" + Encoder.GetEmbed(url));
                        code.setOnMouseClicked(ae -> {
                            getHostServices().showDocument(code.getText());
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        code.setText("ERROR");
                    }
                }
            }
            de.setDropCompleted(success);
            de.consume();
        });
        Stage em_stage = new Stage();
        em_stage.setTitle("Decoder");
        vbox_em.setBackground(new Background(new BackgroundFill(Color.web("0x0875e2"), CornerRadii.EMPTY, Insets.EMPTY)));
        em_stage.setScene(em_scene);
        em_stage.setResizable(false);
        em_stage.setAlwaysOnTop(true);

        btn_embed.setOnAction(ae -> {
            code.clear();
            if (em_stage.isShowing())
                em_stage.hide();
            else
                em_stage.show();
        });
        // --------------------------------------------------------embed reader end

        // CHECKBOXES
        CheckBox translateVRbtn = new CheckBox("Translate to VR");
        HBox checkHB = new HBox(mute, translateVRbtn), searchHB = new HBox(new Label("Search Method: "), searchMethod, ai_logo);
        VBox choiceVB = new VBox(checkHB, searchHB);
        checkHB.setSpacing(20); checkHB.setPadding(new Insets(10)); checkHB.setAlignment(Pos.CENTER);
        searchHB.setSpacing(10); searchHB.setAlignment(Pos.CENTER);
        choiceVB.setSpacing(20); choiceVB.setPadding(new Insets(10)); choiceVB.setAlignment(Pos.CENTER);

        // DELAYBOX
        TextField delaytf = new TextField();
        delaytf.setText("300"); // recommended delay
        HBox delaybox = new HBox(new Label("Connection delay (milliseconds): "), delaytf);
        delaybox.setAlignment(Pos.CENTER);
        delaybox.setSpacing(20);
        delaybox.setPadding(new Insets(10));
        delaytf.textProperty().addListener((observable, oldval, newval) -> {
            if (!newval.matches("\\d*"))
                delaytf.setText(newval.replaceAll("[^\\d]", ""));
        });

        searchMethod.setOnAction(ae -> {    // here because it uses the delaybox
            delaytf.setDisable(searchMethod.getValue().toString().contentEquals("Server"));
            ai_logo.setVisible(!searchMethod.getValue().toString().contentEquals("Direct - No AI"));
        });
        searchMethod.setValue("Server");

        Button gib = BtnFactory.CreateButton("Give Me 5", 200, 50);
        Button gibBottom = BtnFactory.CreateButton("GIVE ME MORE", 200, 50);

        VBox vboxTitle = new VBox(title, subtitle, banCount, choiceVB, delaybox), vboxBtns = new VBox(btn_embed, gib);
        vboxBtns.setAlignment(Pos.TOP_CENTER);
        vboxTitle.setAlignment(Pos.TOP_CENTER);
        vboxBtns.setSpacing(10);

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
        Button sliderbtn = BtnFactory.CreateButton("SHOWER ME CONTENT", 200, 50);
        HBox hboxSlider = new HBox(sliderValue, slider, sliderbtn);
        hboxSlider.setSpacing(20);
        hboxSlider.setPadding(new Insets(15));
        hboxSlider.setAlignment(Pos.CENTER);
        AtomicBoolean isRangedSearch = new AtomicBoolean(false);

        // fix the scroll jumping to focus on the button after it was clicked.
        gib.setFocusTraversable(false);
        gibBottom.setFocusTraversable(false);
        sliderbtn.setFocusTraversable(false);

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

        // Sounds
        Media coinInsert = new Media(getClass().getResource("/Sounds/coin_insert.mp3").toExternalForm());
        Media cashInSound = new Media(getClass().getResource("/Sounds/bingo.mp3").toExternalForm());
        Media monkey = new Media(getClass().getResource("/Sounds/monkey.mp3").toExternalForm());
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
        Button btnAH = BtnFactory.CreateButton("AH", 100, 100), btnPF = BtnFactory.CreateButton("PinkFluffy", 100, 100),
                btnBP = BtnFactory.CreateButton("BananaPhone", 100, 100), btnFRT = BtnFactory.CreateButton("Fart", 100, 100),
                btnDKS = BtnFactory.CreateButton("Dicks", 100, 100), btnHAS = BtnFactory.CreateButton("HotAlienSex", 100, 100),
                btnRAM = BtnFactory.CreateButton("Rampage", 100, 100), btnSLP = BtnFactory.CreateButton("Slap", 100, 100),
                btnYEE = BtnFactory.CreateButton("Yee", 100, 100), btnYES = BtnFactory.CreateButton("YesYesYes", 100, 100),
                btnBRP = BtnFactory.CreateButton("Burp", 100, 100), btnASS = BtnFactory.CreateButton("Ass", 100, 100);
        Label titleSounds = new Label("SoundBoard");
        titleSounds.setTextFill(Color.WHITE);
        titleSounds.setFont(Font.font("Serif", FontWeight.EXTRA_BOLD, 20));
        // Create 3 rows of sounds
        HBox hSounds1 = new HBox(btnAH, btnPF, btnBP, btnFRT);
        hSounds1.setAlignment(Pos.TOP_CENTER);
        HBox hSounds2 = new HBox(btnDKS, btnHAS, btnRAM, btnSLP);
        hSounds2.setAlignment(Pos.TOP_CENTER);
        HBox hSounds3 = new HBox(btnYEE, btnYES, btnBRP, btnASS);
        hSounds3.setAlignment(Pos.TOP_CENTER);
        CheckBox alwaysonCB = new CheckBox("Always on top");
        VBox vSoundsButtons = new VBox(hSounds1, hSounds2, hSounds3), vSoundsTitle = new VBox(titleSounds, alwaysonCB),
                vSoundsMain = new VBox(vSoundsTitle, vSoundsButtons);
        vSoundsTitle.setAlignment(Pos.TOP_CENTER);
        vSoundsButtons.setAlignment(Pos.CENTER);
        vSoundsMain.setSpacing(30);
        vSoundsMain.setPadding(new Insets(20));
        vSoundsMain.setBackground(new Background((new BackgroundFill(Color.web("0x0875e2"), new CornerRadii(30), new Insets(-10)))));
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
        mainBox = new VBox(vboxTitle, vboxBtns, hboxSlider);
        ScrollPane mainPane = new ScrollPane(mainBox);
        Scene scene = new Scene(mainPane, 1100, 860);
        mainBox.setSpacing(30);
        mainBox.setPadding(new Insets(5));
        mainBox.setAlignment(Pos.TOP_CENTER);
        Background bg = new Background(new BackgroundFill(Color.web("0x0875e2"), CornerRadii.EMPTY, Insets.EMPTY));
        mainPane.setBackground(bg);
        mainBox.setBackground(bg);
        mainPane.setStyle("-fx-background-color: white; -fx-border-insets: 10; -fx-border-radius: 20; -fx-border-width: 2; -fx-border-color: white;");
        mainPane.setFitToWidth(true);
        mainPane.setPadding(new Insets(10));
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
        alwaysonCB.setOnAction(ae -> sbStage.setAlwaysOnTop(alwaysonCB.isSelected()));

        // POOL BANNED IMAGES
        Button temp = new Button("Pool Banned");
        temp.setOnAction(ae ->{
            File pDir = new File("Bano");
            Pooler pt = new Pooler(3);
            for(File f : pDir.listFiles()) {
                Image img = pt.Pool(new Image(f.toURI().toString())); // pooled
                BufferedImage bf = SwingFXUtils.fromFXImage(img, null);
                try {
                    ImageIO.write(bf, "PNG", f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        AtomicBoolean poolActive = new AtomicBoolean(false);
        scene.setOnKeyReleased(ae -> {
            if(ae.getCode() == KeyCode.P && !poolActive.get()) {
                poolActive.set(true);
                mainBox.getChildren().add(temp);
            }
        });
        AtomicBoolean setIP = new AtomicBoolean(false);
        scene.setOnKeyReleased(ae -> {
            if(ae.getCode() == KeyCode.I && !setIP.get()) {
                Button btn_ip = BtnFactory.CreateButton("Set IP", 200, 50);
                btn_ip.setFocusTraversable(false);

                Label ip_title = new Label("Please enter a valid ip:");
                ip_title.setFont(Font.font("Serif", FontWeight.BOLD, 14));
                ip_title.setTextFill(Color.WHITE);

                TextField iptf = new TextField();
                iptf.setMaxWidth(200);
                iptf.setAlignment(Pos.CENTER);

                VBox ipVB = new VBox(ip_title, iptf, btn_ip);
                ipVB.setAlignment(Pos.CENTER);
                ipVB.setSpacing(20);

                Scene ip_scene = new Scene(ipVB, 300, 200);

                Stage ipStage = new Stage();
                ipStage.setTitle("Set Server IP");
                ipVB.setBackground(new Background(new BackgroundFill(Color.web("0x0875e2"), CornerRadii.EMPTY, Insets.EMPTY)));
                ipStage.setScene(ip_scene);
                ipStage.setResizable(false);
                ipStage.setAlwaysOnTop(true);

                btn_ip.setOnAction(ae2 -> {
                    SERVER_IP = iptf.getText();
                    ipStage.close();
                });
                ipStage.show();
            }
        });

        // Icons
        Image APGicon = new Image(getClass().getResource("/Program Images/minilogo.png").toExternalForm());
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
            File photoDir = new File(TEMP_FOLDER_DIR);
            if (photoDir.listFiles() != null)
                for (File f : photoDir.listFiles())
                    f.delete();

            MediaPlayer btnSound;
            // Recreate the soundtrack each time - to replay it.
            if (isRangedSearch.get()) {
                RANGE = TOTAL;
                btnSound = new MediaPlayer(cashInSound);
                btnSound.setVolume((slider.getValue() / slider.getMax()) / 5); // play the volume in correlation to the RANGE
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

                        if (searchMethod.getValue().toString().contentEquals("Server")
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
                            clp.clear();
                            ClipboardContent content = new ClipboardContent();
                            content.putString(l1.getText().replace(" - Could not get the image!", "").substring(l1.getText().indexOf('h')));
                            clp.setContent(content);
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
                        File dir = new File(TEMP_FOLDER_DIR);
                        try {
                            if (!delaytf.getText().isEmpty() && !fromServer)
                                while (!counter.GetPermission(Integer.parseInt(delaytf.getText()))) { /* Wait for permission */ }
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
                                AI_value = BanScanner.runThroughAI(IMG, l1, !searchMethod.getValue().toString().contentEquals("Direct - No AI"));
                            mainPane.layout();
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

                                if (translateVRbtn.isSelected())
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
                                    getHostServices().showDocument(finalYandexSearch);
                                });
                                mi2.setOnAction(ae -> {
                                    // Copy file to Clipboard
                                    ClipboardContent content = new ClipboardContent();
                                    clp.clear();
                                    content.putImage(im1.getImage());
                                    clp.setContent(content);
                                });
                                mi3.setOnAction(ae -> {
                                    mi2.fire(); // fires the copyToClipboard action

                                    // Move to Good
                                    File sDir = new File(TEMP_FOLDER_DIR + "Good/");
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
                                while (im1.getFitWidth() > scene.getWidth() - 100 || im1.getFitHeight() > scene.getHeight() - 100) {
                                    im1.setFitWidth(im1.getFitWidth() / 2);
                                    im1.setFitHeight(im1.getFitHeight() / 2);
                                }

                                /*
                                if ((im1.getImage().getHeight() > 400 && im1.getImage().getWidth() > 600)) {
                                        /*|| im1.getImage().getHeight() > scene.getHeight() - 50
                                        || im1.getImage().getWidth() > scene.getWidth() - 50) {
                                    oldHeight = 400;
                                    oldWidth = 600;
                                } else {
                                    oldWidth = im1.getImage().getWidth();
                                    oldHeight = im1.getImage().getHeight();
                                }
                                im1.setFitWidth(oldWidth);
                                im1.setFitHeight(oldHeight);
                                */

                                im1.setOnMouseClicked(ae -> {
                                    if(ae.getButton() == MouseButton.PRIMARY)
                                        getHostServices().showDocument(im1.getImage().getUrl());
                                    /*
                                    if (ae.getButton() == MouseButton.PRIMARY) {
                                        if (im1.getFitWidth() == scene.getWidth()) {
                                            im1.setFitWidth(oldWidth);
                                            im1.setFitHeight(oldHeight);
                                        } else {
                                            im1.setFitWidth(scene.getWidth());
                                            im1.setFitHeight(scene.getHeight());
                                        }
                                    }*/
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
                                        if (!mute.isSelected()) {
                                            MediaPlayer mp = new MediaPlayer(monkey);
                                            mp.setVolume(0.1);
                                            mp.play();
                                        }

                                        like.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                                        dislike.setBackground(Background.EMPTY);

                                        // check if exists in Bad first
                                        File sDir = new File(TEMP_FOLDER_DIR + "Bad/");
                                        sDir.mkdirs();
                                        File tempFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");

                                        if (tempFile.exists())
                                            tempFile.delete();

                                        // Move to Good
                                        sDir = new File(TEMP_FOLDER_DIR + "Good/");
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
                                        if (!mute.isSelected()) {
                                            MediaPlayer mp = new MediaPlayer(pop);
                                            mp.setVolume(0.1);
                                            mp.play();
                                        }

                                        dislike.setBackground(new Background(new BackgroundFill(Color.INDIANRED, CornerRadii.EMPTY, Insets.EMPTY)));
                                        like.setBackground(Background.EMPTY);

                                        // check if exists in Good first
                                        File sDir = new File(TEMP_FOLDER_DIR + "Good/");
                                        sDir.mkdirs();
                                        File tempFile = new File(sDir.getAbsolutePath() + "/" + finalRhexa + ".png");

                                        if (tempFile.exists())
                                            tempFile.delete();

                                        // Move to Bad
                                        sDir = new File(TEMP_FOLDER_DIR + "Bad/");
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
                                    mainBox.getChildren().add(imBox);

                                    if (im1.getImage().getWidth() > mainPane.getWidth()) {
                                        mainPane.layout();
                                        mainPane.setHvalue(mainPane.getHmax() / 2);
                                    }
                                    imBox.setMaxWidth(im1.getFitWidth());

                                    mainBox.getChildren().get(mainBox.getChildren().indexOf(loading)).toFront();    // move loading to last
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
                            mainBox.getChildren().remove(loading);

                            // reset the buttons
                            gib.setDisable(false);
                            sliderbtn.setDisable(false);
                            translateVRbtn.setDisable(false);
                            mute.setDisable(false);
                            //useAI.setDisable(false);
                            searchMethod.setDisable(false);

                            Label bc = new Label("");
                            if (SyncCounter.getBanned() > 0) {
                                if (searchMethod.getValue().toString().contentEquals("Direct - AI"))
                                    bc = new Label("This search has filtered " + SyncCounter.getFiltered() +
                                            " bad images by the AI, and banned " + SyncCounter.getBanned() + ".");
                                else
                                    bc = new Label("This search has banned " + SyncCounter.getBanned() + " images.");
                                bc.setTextFill(Color.WHITE);
                                bc.setFont(Font.font("Helvetica", FontWeight.SEMI_BOLD, 13));
                                if(SyncCounter.getBanned() == 1)
                                    bc.setText(bc.getText().replace("images", "image"));
                            }
                            mainBox.getChildren().addAll(gibBottom, bc);

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
            mainBox.getChildren().addAll(vboxTitle, vboxBtns, hboxSlider, loading);

            gib.setDisable(true);
            sliderbtn.setDisable(true);
            translateVRbtn.setDisable(true);    // disable the checkbox of VR to failsafe from bugs
            mute.setDisable(true);
            //useAI.setDisable(true);
            searchMethod.setDisable(true);

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
        mainPane.setVvalue(mainPane.getVmax()); // fix the scroll adjustment so that everything is in the middle.
    }

    private String FetchFromServer() {
        String fileName = "EMPTY";
        Socket client = null;
        // try all possible 5 ports
        for (int i=0; i<5; i++) {
            try {
                client = new Socket(SERVER_IP, 5000 + i);
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

    public void setBtnAction(HBox hbox, Media media, Button btn) {
        /*btn.setPrefWidth(100);
        btn.setPrefHeight(100);
        Font oldF = btn.getFont();
        btn.setOnMousePressed(ae -> {
            btn.setTextFill(Color.BLUE);
            btn.setFont(Font.font("Imperial", FontWeight.EXTRA_BOLD, 12));
        });
        btn.setOnMouseReleased(ae -> {
            btn.setTextFill(Color.BLACK);
            btn.setFont(oldF);
        });*/
        btn.setStyle("-fx-border-color: #0875e2");
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
