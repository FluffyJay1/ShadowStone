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
		long timeNano = getTimeNano();
		int seconds = (int)(timeNano / 1000000000);
		String s = "";
		s += Integer.toString(seconds / 60) + ":" + Integer.toString(seconds % 60);
		return s;
	}
}
