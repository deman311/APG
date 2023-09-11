package sample;

import ai.onnxruntime.OrtException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainScreen {
    String SERVER_IP = "192.168.1.61";     // default for now
    final String TEMP_FOLDER_DIR = "./APG Temp Images/";

    Slider slider = new Slider(10, 100, 10);
    CheckBox translateVRbtn, mute;
    ChoiceBox searchMethod;
    Clipboard clp;
    TextField delaytf;
    ScrollPane mainPane;
    VBox mainBox, vboxTitle, vboxBtns;
    HBox hboxSlider;
    Scene scene;
    Button gib, gibBottom, sliderbtn;
    AtomicBoolean isRangedSearch;

    public MainScreen(Stage stage, Button btn_embed) {
        stage.setTitle("AUTOMATIC PRNT.SC GENERATOR v" + Main.VERSION);
        stage.setResizable(true);

        clp = Clipboard.getSystemClipboard();

        // Title related
        ImageView title = new ImageView(new Image(getClass().getResource("/Program Images/logo.png").toExternalForm()));
        title.setFitHeight(title.getFitHeight() / 2);
        title.setFitWidth(title.getFitWidth() / 2);
        Label subtitle = new Label((char) 169 + "Dmitry Gribovsky --- Version " + Main.VERSION);
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

        mute = new CheckBox("Mute APG sounds");
        searchMethod = new ChoiceBox(FXCollections.observableArrayList("Server", "Direct - AI", "Direct - No AI"));

        ImageView ai_logo = new ImageView(new Image(getClass().getResource("/Program Images/ai_logo.gif").toExternalForm()));
        ai_logo.setFitHeight(60);
        ai_logo.setFitWidth(80);
        ai_logo.setVisible(false);

        // CHECKBOXES
        translateVRbtn = new CheckBox("Translate to VR");
        HBox checkHB = new HBox(mute, translateVRbtn), searchHB = new HBox(new Label("Search Method: "), searchMethod, ai_logo);
        VBox choiceVB = new VBox(checkHB, searchHB);
        checkHB.setSpacing(20); checkHB.setPadding(new Insets(10)); checkHB.setAlignment(Pos.CENTER);
        searchHB.setSpacing(10); searchHB.setAlignment(Pos.CENTER);
        choiceVB.setSpacing(20); choiceVB.setPadding(new Insets(10)); choiceVB.setAlignment(Pos.CENTER);

        // DELAYBOX
        delaytf = new TextField();
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

        gib = BtnFactory.CreateButton("Give Me 5", 200, 50);
        gibBottom = BtnFactory.CreateButton("GIVE ME MORE", 200, 50);

        vboxTitle = new VBox(title, subtitle, banCount, choiceVB, delaybox);
        vboxBtns = new VBox(btn_embed, gib);
        vboxBtns.setAlignment(Pos.TOP_CENTER);
        vboxTitle.setAlignment(Pos.TOP_CENTER);
        vboxBtns.setSpacing(10);

        // Slider section
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
        sliderbtn = BtnFactory.CreateButton("SHOWER ME CONTENT", 200, 50);
        hboxSlider = new HBox(sliderValue, slider, sliderbtn);
        hboxSlider.setSpacing(20);
        hboxSlider.setPadding(new Insets(15));
        hboxSlider.setAlignment(Pos.CENTER);
        isRangedSearch = new AtomicBoolean(false);

        // fix the scroll jumping to focus on the button after it was clicked.
        gib.setFocusTraversable(false);
        gibBottom.setFocusTraversable(false);
        sliderbtn.setFocusTraversable(false);

        // Kill everything related on exit
        stage.setOnCloseRequest(ae -> {
            Platform.exit();
            System.exit(0);
        });

        // MAIN SCENE
        mainBox = new VBox(vboxTitle, vboxBtns, hboxSlider);
        mainPane = new ScrollPane(mainBox);
        scene = new Scene(mainPane, 1100, 860);
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
        stage.getIcons().add(APGicon);

        mainPane.setVvalue(mainPane.getVmax()); // fix the scroll adjustment so that everything is in the middle.
    }

    public boolean isRangedSearch() {
        return isRangedSearch.get();
    }

    public void SetEventHandler(SearchHandler searchHandler) {
        sliderbtn.setOnAction(ae ->
        {
            isRangedSearch.compareAndSet(false, true);
            sliderbtn.setDisable(true);
            searchHandler.handle(ae);
        });

        gib.setOnAction(ae ->
        {
            isRangedSearch.compareAndSet(true, false);
            searchHandler.handle(ae);
        });
        gibBottom.setOnAction(searchHandler);
    }
}
