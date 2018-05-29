package server.card.effect;

import server.card.*;

public class EffectStats { // this is literally just a struct
	public static final int COST = 0, ATTACK = 1, MAGIC = 2, HEALTH = 3, ATTACKS_PER_TURN = 4, STORM = 5, RUSH = 6,
			WARD = 7;
	public int[] stats = new int[8];
	public boolean[] use = new boolean[8];

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
}
