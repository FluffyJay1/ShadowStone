package client.ui.particle.strategy.timing;

public class InstantEmissionTimingStrategy implements EmissionTimingStrategy {
    private final int maxCount;
    private int count;
    public InstantEmissionTimingStrategy(int count) {
        this.maxCount = count;
        this.count = 0;
    }

    @Override
    public boolean isFinished() {
        return this.count >= this.maxCount;
    }

    @Override
    public double getNextEmissionTime() {
        this.count++;
        return 0;
    }
}
