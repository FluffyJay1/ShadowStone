package client.ui.particle.strategy.property;

import client.ui.particle.Particle;

public class CirclePositionEmissionPropertyStrategy implements EmissionPropertyStrategy {
    private double radius;
    public CirclePositionEmissionPropertyStrategy(double radius) {
        this.radius = radius;
    }

    @Override
    public void applyProperties(Particle p) {
        // set particle p to a random position within a circle
        double distance = (Math.sqrt(Math.random())) * radius;
        double theta = Math.random() * Math.PI * 2;
        p.pos.set((float) (Math.cos(theta) * distance), (float) (Math.sin(theta) * distance));
    }
}
