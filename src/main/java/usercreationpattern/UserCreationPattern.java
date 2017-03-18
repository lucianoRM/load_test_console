package usercreationpattern;

import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import exception.InvalidScriptException;

import javax.jws.soap.SOAPBinding;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luciano on 18/03/17.
 *
 * The time slices defines the unit in ms. If timeSlice = 1000ms then the logic time will move 1 unit every
 * second.
 * patternValues is a map of time units and values. The key defines in which moment the values of generated
 * users every timeSlice changes.
 *
 * For example, if we want a linear pattern of 1 user created every second then timeSlice = 1000 and patternValues
 * should be {0:1}
 *
 * For one user every 10 seconds then the map should be the same but timeSlice = 10000
 *
 * If for example we want a ramp of 1 new user every second then the values would be:
 * timeSlice = 1000, patternValues = {0:1,1:2,2:3,3:4,....}
 *
 * If we want to start with 10 users,then have a peak of 100 users for 10 seconds when 2 seconds have passed and then go back
 * to 10. it will be:
 * timeSlice = 1000, patternValues = {0:10,2:100,12:10}
 */
public class UserCreationPattern {

    private final static String TIME_SLICE_KEY = "timeSlice";

    Long timeSlice;
    Map<Integer,Integer> patternValues;

    UserCreationPattern(JsonObject patternObject) throws InvalidScriptException {
        Long timeSlice;
        try{
            this.timeSlice = patternObject.get(TIME_SLICE_KEY).getAsLong();
        }catch(NullPointerException e){
            throw new InvalidScriptException();
        }
        this.patternValues = new HashMap<>();
    }

    public Map<Integer,Integer> getPatternValues() {
        return this.patternValues;
    }

    public Long getTimeSlice() {
        return this.timeSlice;
    }

}
