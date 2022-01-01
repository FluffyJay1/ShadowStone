package client.ui.particle.strategy.timing;

public class IntervalEmissionTimingStrategy implements EmissionTimingStrategy {
    private final int countPerInterval;
    private int count;
    private final double interval;
    public IntervalEmissionTimingStrategy(int count, double interval) {
        this.interval = interval;
        this.count = 0;
        this.countPerInterval = count;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public double getNextEmissionTime() {
        this.count++;
        if (this.count >= this.countPerInterval) {
            this.count = 0;
            return interval;
        }
        return 0;
    }
}
