package client.ui.particle.strategy.timing;

public interface EmissionTimingStrategy {
    boolean isFinished();
    double getNextEmissionTime(); // called after an emission, wants to know when to next emit
}
