package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Soundboard {
    public Soundboard(Stage stage) {
        // Declare Sounds
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
        Image SBicon = new Image(getClass().getResource("/Program Images/SBico.png").toExternalForm());
        sbStage.getIcons().add(SBicon);
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
