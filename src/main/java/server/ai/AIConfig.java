package server.ai;

// bean class
public class AIConfig {

    public static final AIConfig BEGINNER = builder()
            .name("Beginner")
            .maxSamples(10)
            .maxSamplesEnemy(3)
            .maxSamplesMultiplier(0.7)
            .startSampleRate(5)
            .enemySampleRate(2)
            .sampleRateMultiplier(1.1)
            .rngPenalty(0.95)
            .rngPenaltyReduction(0.85)
            .reevaluationMaxSampleRateDiff(0.25)
            .rngMaxTrials(8)
            .rngMinTrials(2)
            .rngTrialReduction(3)
            .rngDensityMultiplier(0.93)
            .build();

    public static final AIConfig NOVICE = builder()
            .name("Novice")
            .maxSamples(15)
            .maxSamplesEnemy(3)
            .maxSamplesMultiplier(0.7)
            .startSampleRate(6)
            .enemySampleRate(2)
            .sampleRateMultiplier(1.15)
            .rngPenalty(0.9)
            .rngPenaltyReduction(0.85)
            .reevaluationMaxSampleRateDiff(0.2)
            .rngMaxTrials(10)
            .rngMinTrials(2)
            .rngTrialReduction(4)
            .rngDensityMultiplier(0.94)
            .build();

    public static final AIConfig PRO = builder()
            .name("Pro")
            .maxSamples(25)
            .maxSamplesEnemy(3)
            .maxSamplesMultiplier(0.7)
            .startSampleRate(10)
            .enemySampleRate(2.5)
            .sampleRateMultiplier(1.2)
            .rngPenalty(0.9)
            .rngPenaltyReduction(0.8)
            .reevaluationMaxSampleRateDiff(0.15)
            .rngMaxTrials(13)
            .rngMinTrials(2)
            .rngTrialReduction(4)
            .rngDensityMultiplier(0.96)
            .build();

    public static final AIConfig MASTER = builder()
            .name("Master")
            .maxSamples(30)
            .maxSamplesEnemy(5)
            .maxSamplesMultiplier(0.7)
            .startSampleRate(20)
            .enemySampleRate(4)
            .sampleRateMultiplier(1.4)
            .rngPenalty(0.8)
            .rngPenaltyReduction(0.8)
            .reevaluationMaxSampleRateDiff(0.1)
            .rngMaxTrials(15)
            .rngMinTrials(3)
            .rngTrialReduction(4)
            .rngDensityMultiplier(0.97)
            .build();

    public String name;

    // The maximum number of branches to sample at min depth
    int maxSamples;

    // When switching over to the enemy turn, reset the max samples
    int maxSamplesEnemy;

    // max number of rng trials
    int rngMaxTrials;

    // min number of rng trials
    int rngMinTrials;

    // how many less trials each subsequent depth gets
    int rngTrialReduction;

    // each additional branch multiplies the trial count by this amount
    double rngDensityMultiplier;

    // How much the max number of samples gets multiplied by per level
    double maxSamplesMultiplier;

    // Proportion of total possible actions that we sample at the start
    // This value get split amongst the branches, proportionally to their weight
    // should be greater than 1 lol
    double startSampleRate;
    double enemySampleRate;

    // Multiplier of sample rate at each depth, after the above "splitting" thing
    // Should reflect expected branching factor
    double sampleRateMultiplier;

    // After an rng event, we shouldn't care too much about evaluating in detail
    // proportion of the sample rate to throw away
    double rngPenalty;

    // how much to multiply the penalty multiplier per extra trial
    double rngPenaltyReduction;

    // When revisiting nodes, tolerate lower detail levels up to a certain amount
    double reevaluationMaxSampleRateDiff;

    // this behemoth should be constructed with a builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AIConfig built;
        private Builder() {
            this.built = new AIConfig();
        }

        public AIConfig build() {
            return this.built;
        }

        public Builder name(String name) {
            this.built.name = name;
            return this;
        }

        public Builder reevaluationMaxSampleRateDiff(double reevaluationMaxSampleRateDiff) {
            this.built.reevaluationMaxSampleRateDiff = reevaluationMaxSampleRateDiff;
            return this;
        }

        public Builder rngPenaltyReduction(double rngPenaltyReduction) {
            this.built.rngPenaltyReduction = rngPenaltyReduction;
            return this;
        }

        public Builder rngPenalty(double rngPenalty) {
            this.built.rngPenalty = rngPenalty;
            return this;
        }

        public Builder sampleRateMultiplier(double sampleRateMultiplier) {
            this.built.sampleRateMultiplier = sampleRateMultiplier;
            return this;
        }

        public Builder enemySampleRate(double enemySampleRate) {
            this.built.enemySampleRate = enemySampleRate;
            return this;
        }

        public Builder startSampleRate(double startSampleRate) {
            this.built.startSampleRate = startSampleRate;
            return this;
        }

        public Builder maxSamplesMultiplier(double maxSamplesMultiplier) {
            this.built.maxSamplesMultiplier = maxSamplesMultiplier;
            return this;
        }

        public Builder maxSamples(int maxSamples) {
            this.built.maxSamples = maxSamples;
            return this;
        }

        public Builder maxSamplesEnemy(int maxSamplesEnemy) {
            this.built.maxSamplesEnemy = maxSamplesEnemy;
            return this;
        }

        public Builder rngMaxTrials(int rngMaxTrials) {
            this.built.rngMaxTrials = rngMaxTrials;
            return this;
        }

        public Builder rngMinTrials(int rngMinTrials) {
            this.built.rngMinTrials = rngMinTrials;
            return this;
        }

        public Builder rngTrialReduction(int rngTrialReduction) {
            this.built.rngTrialReduction = rngTrialReduction;
            return this;
        }

        public Builder rngDensityMultiplier(double rngDensityMultiplier) {
            this.built.rngDensityMultiplier = rngDensityMultiplier;
            return this;
        }
    }
}
