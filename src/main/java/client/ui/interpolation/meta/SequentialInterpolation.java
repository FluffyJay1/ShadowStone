package client.ui.interpolation.meta;

import client.ui.interpolation.Interpolation;

import java.util.ArrayList;
import java.util.List;

public class SequentialInterpolation<T> implements Interpolation<T> {
    private final List<Interpolation<T>> interpolationList;
    private final List<Double> transitionPoints;
    private int currInd; // to avoid having to find which interpolation to use every time

    public SequentialInterpolation(List<Interpolation<T>> interpolationList, List<Double> weightList) {
        this.interpolationList = interpolationList;
        assert(interpolationList.size() == weightList.size());
        // process weight list into transition points
        this.transitionPoints = new ArrayList<>(weightList.size() - 1);
        double totalWeight = weightList.stream().reduce(0., Double::sum);
        double cumWeight = 0;
        for (int i = 0; i < interpolationList.size() - 1; i++) {
            cumWeight += weightList.get(i);
            this.transitionPoints.add(cumWeight / totalWeight);
        }
        this.currInd = 0;
    }

    private double getLowerTBound(int ind) {
        return ind == 0 ? 0 : this.transitionPoints.get(ind - 1);
    }

    private double getUpperTBound(int ind) {
        return ind == this.transitionPoints.size() ? 1 : this.transitionPoints.get(ind);
    }

    @Override
    public T get(double t) {
        // if currInd is too small, move to right
        while (this.getUpperTBound(this.currInd) < t) {
            this.currInd++;
        }

        // opposite direction time
        while (this.getLowerTBound(this.currInd) > t) {
            this.currInd--;
        }
        double upper = this.getUpperTBound(this.currInd);
        double lower = this.getLowerTBound(this.currInd);
        double rescaledT = (t - lower) / (upper - lower);
        return this.interpolationList.get(this.currInd).get(rescaledT);
    }
}
