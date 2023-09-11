package sample;

public class TimingHandler {
    private static int counter = 0;

    public static synchronized void timingRequest() {
        counter++;
        if (counter >= 20) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter=0;
        }
    }
}
