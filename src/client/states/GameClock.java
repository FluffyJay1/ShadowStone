package client.states;

public class GameClock extends Thread {
	long startTime;

	public void run() {
		try {
			startTime = System.nanoTime();
		} catch (Exception e) {

		}
	}

	public long getTimeNano() {
		long currTime = System.nanoTime();
		return currTime - startTime;
	}

	public String getTimeReadable() {
		return "";
	}
}
