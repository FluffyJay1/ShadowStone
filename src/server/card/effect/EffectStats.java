package server.card.effect;

import server.card.*;

public class EffectStats { // this is literally just a struct
	public static final int COST_I = 0, ATTACK_I = 1, MAGIC_I = 2, HEALTH_I = 3;
	public int[] stats = new int[4];
	public boolean[] use = new boolean[4];

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
		this.setStat(ATTACK_I, stats.a);
		this.setStat(MAGIC_I, stats.m);
		this.setStat(HEALTH_I, stats.h);
	}
}
