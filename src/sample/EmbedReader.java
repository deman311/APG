package sample;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;

public class EmbedReader {
    Button btn_embed = BtnFactory.CreateButton("Decoder", 200, 50);

    public EmbedReader(HostServices hostServices) {
        btn_embed.setFocusTraversable(false);
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
                            hostServices.showDocument(code.getText());
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
    }

    public Button getEmbedBtn() {
        return btn_embed;
    }
}
