package server.card;

public class Stats {
	public int a, m, h;

	public Stats(int a, int m, int h) {
		this.a = a;
		this.m = m;
		this.h = h;
	}

	public String toString() {
		return "(" + a + " " + m + " " + h + ")";
	}

	public static String statsToString(int attack, int magic, int health) {
		return "(" + attack + " " + magic + " " + health + ")";
	}
}
