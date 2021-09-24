package dcaiti.tu_berlin.de.demoapp.stepdetection;

/**
 * Structure to hold the classification result to pass it around easily.
 *
 * Created by Joris Clement on 05.03.18.
 */

public class ClassifyResult {

    public final float walkingProbability;
    public final float notWalkingProbability;
    public final long timeDiffPreviousResultMillis;

    ClassifyResult(float notWalkingProbability, float walkingProbability,
                   long timeDiffPreviousResultMillis) {
        this.notWalkingProbability = notWalkingProbability;
        this.walkingProbability = walkingProbability;
        this.timeDiffPreviousResultMillis = timeDiffPreviousResultMillis;
    }

    public boolean isWalking() {
        return this.walkingProbability > this.notWalkingProbability;
    }

    public float classificationRate() { return  1000f / timeDiffPreviousResultMillis; }
}
