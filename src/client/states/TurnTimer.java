package client.states;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

class TurnEnd extends TimerTask {
	public void run() {

	}
}

public class TurnTimer extends Thread {
	Timer t;
	TurnEnd currTurn;

	public void run() {
		try {
			t = new Timer();

		} catch (Exception e) {

		}
	}

	public void nextTurn() {
		t.purge();
		// currTurn = TurnEnd();
		// t.schedule();
	}

}
