package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class IMGhandler {
    public static void CreateEmbed(Image img, String rHexa, String imgDir) throws IOException {
        BufferedImage b_img = SwingFXUtils.fromFXImage(img, null);
        int h = b_img.getHeight() - 1;
        int w = b_img.getWidth() - 1;
        b_img.setRGB(0, 0, rHexa.charAt(0) | (255 << 24));
        b_img.setRGB(1, 0, rHexa.charAt(1) | (255 << 24));
        b_img.setRGB(w-1, 0, rHexa.charAt(2) | (255 << 24));
        b_img.setRGB(w, 0, rHexa.charAt(3) | (255 << 24));
        b_img.setRGB(0, h-1, rHexa.charAt(4) | (255 << 24));
        b_img.setRGB(0, h, rHexa.charAt(5) | (255 << 24));

        ImageIO.write(b_img, "png", new File(imgDir));
    }

    public static String GetEmbed(String imgDir) throws IOException {
        BufferedImage b_img = ImageIO.read(new File(imgDir));
        int h = b_img.getHeight() - 1;
        int w = b_img.getWidth() - 1;
        String rHexa = "";
//        for (int j = 0; j < 6; j++)
//            rHexa += (char) (b_img.getRGB(j, 0) & 255);
        rHexa += (char)(b_img.getRGB(0,0) & 255);
        rHexa += (char)(b_img.getRGB(1,0) & 255);
        rHexa += (char)(b_img.getRGB(w-1,0) & 255);
        rHexa += (char)(b_img.getRGB(w,0) & 255);
        rHexa += (char)(b_img.getRGB(0,h-1) & 255);
        rHexa += (char)(b_img.getRGB(0,h) & 255);

        return rHexa;
    }
}
