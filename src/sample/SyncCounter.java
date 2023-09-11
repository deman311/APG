package sample;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// Singleton implementation
public class SyncCounter {
    private int counter;
    private static int banCounter;
    private static int filterCounter;
    private final AtomicBoolean pullock = new AtomicBoolean(false);

    public SyncCounter(int value) {
        counter = value;
        banCounter = 0;
        filterCounter = 0;
    }

    public synchronized boolean GetPermission (int milis) {
        if (!pullock.get()) {
            pullock.set(true);  // lock the picture pulling
            new Thread(() -> {
                try {
                    Thread.sleep(milis);
                    pullock.set(false);
                } catch (InterruptedException ignored) {
                }
            }).start();
            return true;
        }
        return false;
    }

    public synchronized void Count() { counter--; }

    public synchronized void Count(int amount) {counter -=amount;}

    public static synchronized void foundBanned() {
        banCounter++;
    }

    public static synchronized void filteredBad() {
        filterCounter++;
    }

    public synchronized boolean isDone() {
        return counter == 0;
    }

    public static synchronized int getBanned() {
        return banCounter;
    }
    public static synchronized int getFiltered() {
        return filterCounter;
    }
}
