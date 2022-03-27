package sample;

// Singleton implementation
public class SyncCounter {
    private int counter;
    private static int banCounter;

    public SyncCounter(int value) {
        counter = value;
        banCounter = 0;
    }

    public synchronized void Count() { counter--; }

    public synchronized void Count(int amount) {counter -=amount;}

    public static synchronized void foundBanned() {
        banCounter++;
    }

    public synchronized boolean isDone() {
        if (counter == 0)
            return true;
        return false;
    }

    public static synchronized int getBanned() {
        return banCounter;
    }
}
