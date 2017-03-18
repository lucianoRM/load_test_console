package usercreationpattern;

import com.google.gson.JsonObject;
import exception.InvalidScriptException;

/**
 * Created by luciano on 18/03/17.
 */
public class RampUserCreationPattern extends UserCreationPattern {
    private static final String PATTERN_SLOPE_KEY = "slope";
    private static final String PATTERN_INITIAL_VALUE_KEY = "initialValue";
    private static final String PATTERN_THRESHOLD_KEY = "threshold";

    private int initialValue;
    private int slope;
    private int threshold;

    private void createValuesMap() {

        int totalUsers = initialValue;
        int logicTime = 0;
        while(totalUsers <= threshold) {
            this.patternValues.put(logicTime,totalUsers);
            logicTime++;
            totalUsers = this.slope*logicTime + initialValue;
        }
    }

    public RampUserCreationPattern(JsonObject patternObject) throws InvalidScriptException {
        super(patternObject);
        try {
            this.slope = patternObject.get(PATTERN_SLOPE_KEY).getAsInt();
            this.initialValue = patternObject.get(PATTERN_INITIAL_VALUE_KEY).getAsInt();
            this.threshold = patternObject.get(PATTERN_THRESHOLD_KEY).getAsInt();
        }catch(NullPointerException e){
            throw new InvalidScriptException();
        }
        this.createValuesMap();
    }


}
