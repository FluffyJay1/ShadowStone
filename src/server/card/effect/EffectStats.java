package server.card.effect;

import java.util.StringTokenizer;

import server.card.*;

public class EffectStats { // this is literally just a struct
	public static final int NUM_STATS = 11;
	// what's an enum
	public static final int COST = 0, ATTACK = 1, MAGIC = 2, HEALTH = 3, ATTACKS_PER_TURN = 4, STORM = 5, RUSH = 6,
			WARD = 7, BANE = 8, POISONOUS = 9, COUNTDOWN = 10;
	public int[] stats = new int[NUM_STATS];
	public boolean[] use = new boolean[NUM_STATS];

	public EffectStats() {

	}

	public void setStat(int index, int stat) {
		this.stats[index] = stat;
		this.use[index] = true;
	}

	public void changeStat(int index, int stat) {
		this.stats[index] += stat;
		this.use[index] = true;
	}

	public void resetStat(int index) {
		this.stats[index] = 0;
		this.use[index] = false;
	}

	public void applyStats(Stats stats) {
		this.setStat(ATTACK, stats.a);
		this.setStat(MAGIC, stats.m);
		this.setStat(HEALTH, stats.h);
	}

	public void copyStats(EffectStats stats) {
		for (int i = 0; i < NUM_STATS; i++) {
			if (stats.use[i]) {
				this.setStat(i, stats.stats[i]);
			} else {
				this.resetStat(i);
			}
		}
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < NUM_STATS; i++) {
			s += use[i] + " " + stats[i] + " ";
		}
		return s;
	}

	public static EffectStats fromString(StringTokenizer st) {
		EffectStats ret = new EffectStats();
		for (int i = 0; i < NUM_STATS; i++) {
			boolean use = st.nextToken().equals("true");
			int stat = Integer.parseInt(st.nextToken());
			if (use) {
				ret.setStat(i, stat);
			}
		}
		return ret;
	}
}
