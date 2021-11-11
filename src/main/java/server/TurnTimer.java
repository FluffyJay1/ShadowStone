package server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

class ForceEnd extends TimerTask {
    Board b;

    public ForceEnd(Board b) {
        this.b = b;
    }

    @Override
    public void run() {
        b.endCurrentPlayerTurn();
    }
}

public class TurnTimer extends Thread {
    Timer t;
    Board b;
    Date d;

    public TurnTimer(Board b) {
        this.b = b;
    }

    public void run() {
        try {
            t = new Timer();
            d = new Date();

        } catch (Exception e) {
            // dumb
        }
    }

    public void nextTurn() {
        t.purge();
        // currTurn = TurnEnd();
        t.schedule(new ForceEnd(b), null);
    }

}
