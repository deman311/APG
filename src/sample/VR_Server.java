package sample;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class VR_Server {

    static Stage vr_stage = new Stage();
    static TextArea log1 = new TextArea();

    public static void sendPicture(String img_path, int width, int height) {
        try {
            Socket soc = new Socket("localhost", 7777);
            while (!soc.isConnected()) ;
            DataOutputStream out = new DataOutputStream(soc.getOutputStream());
            out.write((img_path + "#" + width + "$" + height).getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            soc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int awaitInput() {
        try {
            Socket soc;
            while (true) {
                try {
                    soc = new Socket("localhost", 7777);
                    break;
                } catch (ConnectException e) {
                    Thread.sleep(2000);
                }
            }

            DataOutputStream out = new DataOutputStream(soc.getOutputStream());
            out.write("@READY@".getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            soc.close();

            ServerSocket server = new ServerSocket(7778);
            soc = server.accept();
            updateLog("Connected back!");
            DataInputStream in = new DataInputStream(soc.getInputStream());
            byte[] buff = new byte[1024];
            int bytesRead = in.read(buff);
            byte[] newBuff = Arrays.copyOf(buff, bytesRead);
            String text = new String(newBuff, "UTF-8");
            updateLog("Got: " + text);
            in.close();
            soc.close();

            return Integer.parseInt(text);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1; // error
    }

    public static void updateLog(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                log1.appendText(text + "\n");
            }
        });
    }

    public static void Show() {
        if (!vr_stage.isShowing())
            vr_stage.show();
    }
}
