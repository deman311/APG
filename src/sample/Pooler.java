package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

public class Pooler {
    int FACTOR;

    public Pooler(int FACTOR) {
        this.FACTOR = FACTOR;
    }
    public Image Pool(Image IMG) {
        int pH = (int) IMG.getHeight() / FACTOR;
        int pW = (int) IMG.getWidth() / FACTOR;
        ArrayList pool = new ArrayList();

        BufferedImage pooledIMG = new BufferedImage(pW, pH, BufferedImage.TYPE_INT_ARGB);
        for (int a = 0, a1 = 0; a < IMG.getHeight() - FACTOR; a += FACTOR, a1++)
            for (int b = 0, b1 = 0; b < IMG.getWidth() - FACTOR; b += FACTOR, b1++) {
                // Get all adjacent values
                for(int i=0;i<FACTOR;i++)
                    for(int j=0;j<FACTOR;j++)
                        pool.add(IMG.getPixelReader().getArgb(b + j, a + i));
                // Get MaxPool
                int min = (int)Collections.min(pool);
                pool.clear();
                pooledIMG.setRGB(b1, a1, min);
            }
        return SwingFXUtils.toFXImage(pooledIMG,null);
    }
}
