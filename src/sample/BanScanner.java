package sample;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

public class BanScanner {
    static int BANNED_COUNT = 129;
    static ArrayList<Image> BannedList = new ArrayList<>();
    static OrtEnvironment env;
    static OrtSession.SessionOptions sessionOptions;
    static OrtSession session;

    public static void loadBanned() {
        // Banned Images Load
        for (int i = 1; i <= BANNED_COUNT; i++)
            BannedList.add(new Image(BanScanner.class.getResource("/Banned/ban" + i + ".png").toExternalForm()));
    }

    public static void initONNX() throws OrtException {
        env = OrtEnvironment.getEnvironment();
        sessionOptions = new OrtSession.SessionOptions();
        session = env.createSession("APG_AI.onnx", sessionOptions);
    }

    /**
     *
     * @param img
     * @param l1
     * @param useAI
     * @return 0 for banned, 1 for filtered, 2 for approved
     */
    public static int runThroughAI(Image img, Label l1, boolean useAI) {
        if(checkIMG(img, l1)) // check ban first
            return 0;

        try {
            // preprocess image
            BufferedImage pre_image = SwingFXUtils.fromFXImage(img, null); // convert to Buffered
            // resize to 224x224
            BufferedImage resized = new BufferedImage(224, 224, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = resized.createGraphics();
            graphics2D.drawImage(pre_image, 0, 0, 224, 224, null);
            graphics2D.dispose();
            // convert to array
            float[][][][] img_array = new float[1][3][224][224];
            for (int i = 0; i < 224; i++) {
                for (int j = 0; j < 224; j++) {
                    int rgb = resized.getRGB(j, i);
                    img_array[0][2][i][j] = (rgb & 0xff) / 255f; // blue
                    img_array[0][1][i][j] = ((rgb & 0xff00) >> 8) / 255f; // green
                    img_array[0][0][i][j] = ((rgb & 0xff0000) >> 16) / 255f; // red
                }
            }

            OnnxTensor tensor = OnnxTensor.createTensor(env, img_array);
            var input = Map.of("IN", tensor);

            // Inject to the model
            try (var output = session.run(input)) {
                if(outputToArgmax(output) == 1) {
                    if (!useAI)
                        l1.setText("{AI: GOOD} " + l1.getText());
                }
                else {
                    l1.setText("{AI: BAD} " + l1.getText());
                    if (useAI)
                        return 1;
                }
            }
        } catch (OrtException e) {
            e.printStackTrace();
        }

        return 2;
    }

    public static int outputToArgmax(OrtSession.Result output) throws OrtException {
        float[][] probs = (float[][]) output.get(0).getValue();
        float[] probabilities = probs[0];
        float maxVal = Float.NEGATIVE_INFINITY;
        int idx = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxVal) {
                maxVal = probabilities[i];
                idx = i;
            }
        }
        return idx;
    }

    public static boolean checkIMG(Image img, Label l1) {
        // Scan for banned image
        Pooler plr = new Pooler(3);
        boolean isBanned = false;
        Image origPool = plr.Pool(img);
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
