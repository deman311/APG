package sample;

import javafx.scene.control.Label;
import javafx.scene.image.Image;

import java.util.ArrayList;

public class BanScanner {
    static int BANNED_COUNT = 124;
    static ArrayList<Image> BannedList = new ArrayList<>();

    public static void loadBanned() {
        // Banned Images Load
        for (int i = 1; i <= BANNED_COUNT; i++)
            BannedList.add(new Image(BanScanner.class.getResource("/Banned/ban" + i + ".png").toExternalForm()));
    }

    public static boolean checkIMG(Image IMG, Label l1) {
        // Scan for banned image
        Pooler plr = new Pooler(3);
        boolean isBanned = false;
        Image origPool = plr.Pool(IMG);
        float maxDelta = (int) (origPool.getHeight() * origPool.getWidth());
        float similarity = 0, maxSimilarity = 0;
        for (Image image : BannedList) {
            try {
                //Image banPool = plr.Pool(BannedList.get(l));
                for (int v = 0; v < (int) origPool.getWidth(); v++)
                    for (int j = 0; j < (int) origPool.getHeight(); j++) {
                        if (origPool.getPixelReader().getArgb(v, j) == image.getPixelReader().getArgb(v, j))
                            similarity++;
                        if (similarity >= (maxDelta * 0.95)) { // the value of pixel similarity to look for.
                            l1.setText(null);
                            return true;
                        }
                    }
            } catch (Exception e) {
                // Any exception during the scanning of the potentially banned image will just skip the check to avoid the killing of the whole thread.
            }
            if (similarity > maxSimilarity)
                maxSimilarity = similarity;
            similarity = 0;
        }
        if (maxSimilarity <= maxDelta * 0.95 && maxSimilarity >= maxDelta * 0.9)
            l1.setText(l1.getText() + " - [!] " + (Float.parseFloat(String.format("%.3f", maxSimilarity / maxDelta)) * 100) + "% Chance of being banned [!]");

        return false;
    }
}
