package usercreationpattern;

import com.google.gson.JsonObject;
import exception.InvalidScriptException;

/**
 * Created by luciano on 18/03/17.
 */
public class UserCreationPatternFactory {
    private final static String PATTERN_TYPE_KEY = "type";
    private final static String RAMP_PATTERN_VALUE = "ramp";
    private final static String STAIRS_PATTERN_VALUE = "stairs";
    private final static String CUSTOM_PATTERN_VALUE = "custom";


    public UserCreationPattern createUserCreationPattern(JsonObject patternObject) throws InvalidScriptException {
        String type;
        try{
            type = patternObject.get(PATTERN_TYPE_KEY).getAsString();
        }catch(NullPointerException e) {
            throw new InvalidScriptException();
        }

        if(type.equals(RAMP_PATTERN_VALUE)){
            return new RampUserCreationPattern(patternObject);
        }
        if(type.equals(STAIRS_PATTERN_VALUE)) {
            return new StairsUserCreationPattern(patternObject);
        }
        if(type.equals(CUSTOM_PATTERN_VALUE)) {
            return new CustomUserCreationPattern(patternObject);
        }
        throw new InvalidScriptException();
    }


}
