package usercreationpattern;

import com.google.gson.JsonObject;
import exception.InvalidScriptException;

/**
 * Created by luciano on 18/03/17.
 */
public class StairsUserCreationPattern extends UserCreationPattern {
    private static final String PATTERN_STEP_WIDTH_VALUE_KEY = "stepWidth";
    private static final String PATTERN_STEP_HEIGHT_VALUE_KEY = "stepHeight";
    private static final String PATTERN_INITIAL_VALUE_KEY = "initialValue";
    private static final String PATTERN_THRESHOLD_KEY = "threshold";

    private int initialValue;
    private int stepWidth;
    private int stepHeight;
    private int threshold;

    private void createValuesMap() {
        int totalUsers = initialValue;
        int time = 0;
        int lastStepTime = 0;

        this.patternValues.put(time,totalUsers);

        while(totalUsers <= threshold){
            if((time - lastStepTime) >= stepWidth) {
                totalUsers = totalUsers + stepHeight;
                if(totalUsers <= threshold) this.patternValues.put(time,totalUsers);
                lastStepTime = time;
            }
            time++;
        }

    }

    public StairsUserCreationPattern(JsonObject patternObject) throws InvalidScriptException{
        super(patternObject);
        try {
            this.stepWidth = patternObject.get(PATTERN_STEP_WIDTH_VALUE_KEY).getAsInt();
            this.stepHeight = patternObject.get(PATTERN_STEP_HEIGHT_VALUE_KEY).getAsInt();
            this.initialValue = patternObject.get(PATTERN_INITIAL_VALUE_KEY).getAsInt();
            this.threshold = patternObject.get(PATTERN_THRESHOLD_KEY).getAsInt();
        }catch(NullPointerException e){
            throw new InvalidScriptException();
        }
        this.createValuesMap();
    }
}
