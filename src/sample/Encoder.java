package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class Encoder {
    public static void CreateEmbed(Image img, String rHexa, String imgDir) throws IOException {
        BufferedImage b_img = SwingFXUtils.fromFXImage(img, null);
        int h = b_img.getHeight() - 1;
        int w = b_img.getWidth() - 1;
        b_img.setRGB(0, 0, (rHexa.charAt(0) + b_img.getRGB(0,1)) | (255 << 24));
        b_img.setRGB(1, 0, (rHexa.charAt(1) + b_img.getRGB(1,1)) | (255 << 24));
        b_img.setRGB(2, 0, (rHexa.charAt(2) + b_img.getRGB(2,1)) | (255 << 24));
        b_img.setRGB(3, 0, (rHexa.charAt(3) + b_img.getRGB(3,1)) | (255 << 24));
        b_img.setRGB(4, 0, (rHexa.charAt(4) + b_img.getRGB(4,1)) | (255 << 24));
        b_img.setRGB(5, 0, (rHexa.charAt(5) + b_img.getRGB(5,1)) | (255 << 24));

        ImageIO.write(b_img, "png", new File(imgDir));
    }

    public static String GetEmbed(String imgDir) throws IOException {
        BufferedImage b_img = ImageIO.read(new File(imgDir));
        int h = b_img.getHeight() - 1;
        int w = b_img.getWidth() - 1;
        String rHexa = "";
//        for (int j = 0; j < 6; j++)
//            rHexa += (char) (b_img.getRGB(j, 0) & 255);
        rHexa += (char)((b_img.getRGB(0,0) - b_img.getRGB(0,1)) & 255);
        rHexa += (char)((b_img.getRGB(1,0) - b_img.getRGB(1,1)) & 255);
        rHexa += (char)((b_img.getRGB(2,0) - b_img.getRGB(2,1)) & 255);
        rHexa += (char)((b_img.getRGB(3,0) - b_img.getRGB(3,1)) & 255);
        rHexa += (char)((b_img.getRGB(4,0) - b_img.getRGB(4,1)) & 255);
        rHexa += (char)((b_img.getRGB(5,0) - b_img.getRGB(5,1)) & 255);

        return rHexa;
    }
}
