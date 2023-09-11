package sample;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.concurrent.atomic.AtomicBoolean;

abstract class BtnFactory {
    static Background mainColor = new Background(new BackgroundFill(Color.WHITE, new CornerRadii(3), Insets.EMPTY));
    static Background secondColor = new Background(new BackgroundFill(Color.LIGHTSKYBLUE, new CornerRadii(3), Insets.EMPTY));
    static Background thirdColor = new Background(new BackgroundFill(Color.LIGHTGREY, new CornerRadii(3), Insets.EMPTY));

    public static Button CreateButton(String label, int w, int h) {
        AtomicBoolean isInside = new AtomicBoolean(false);
        Button gib = new Button(label);
        gib.setPrefWidth(w);
        gib.setPrefHeight(h);
        Background gibBG = mainColor;
        gib.setBackground(gibBG);
        gib.setTextFill(Color.GREY);
        gib.setFont(Font.font("Helvetica", FontWeight.BOLD, 13 ));

        gib.setOnMouseEntered(ae -> {
            isInside.set(true);
            gib.setBackground(secondColor);
        });
        gib.setOnMouseExited(ae -> {
            isInside.set(false);
            gib.setBackground(mainColor);
        });

        gib.setOnMousePressed(ae -> {
            gib.setBackground(thirdColor);
        });
        gib.setOnMouseReleased(ae -> {
            if(isInside.get())
                gib.setBackground(secondColor);
            else
                gib.setBackground(mainColor);
        });

        return gib;
    }
}
