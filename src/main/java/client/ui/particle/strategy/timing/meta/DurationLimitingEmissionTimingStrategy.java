package client.ui.particle.strategy.timing.meta;

import client.ui.particle.strategy.timing.EmissionTimingStrategy;

// when composed with another emission timing strategy, this one limits the other one to a max duration
public class DurationLimitingEmissionTimingStrategy implements EmissionTimingStrategy {
    private final EmissionTimingStrategy ets;
    double time;
    final double maxDuration;

    public DurationLimitingEmissionTimingStrategy(double maxDuration, EmissionTimingStrategy ets) {
        this.ets = ets;
        this.time = 0;
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean isFinished() {
        return this.time > this.maxDuration;
    }

    @Override
    public double getNextEmissionTime() {
        double deltaT = ets.getNextEmissionTime();
        this.time += deltaT;
        return deltaT;
    }
}
