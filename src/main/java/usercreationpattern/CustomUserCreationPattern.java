package usercreationpattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exception.InvalidScriptException;

import java.util.Map;
import java.util.Set;

/**
 * Created by luciano on 18/03/17.
 */
public class CustomUserCreationPattern extends UserCreationPattern {

    private final static String PATTERN_VALUES_KEY = "values";

    private void loadValues(JsonArray valuesArray) throws InvalidScriptException{
        for(JsonElement valueElement : valuesArray) {
            Set<Map.Entry<String,JsonElement>> value = valueElement.getAsJsonObject().entrySet();
            for(Map.Entry<String,JsonElement> entry : value) {
                int intKey = Integer.parseInt(entry.getKey());
                int intValue = entry.getValue().getAsInt();
                this.patternValues.put(intKey,intValue);
            }
        }
    }


    public CustomUserCreationPattern(JsonObject patternObject) throws InvalidScriptException{
        super(patternObject);
        JsonArray valuesArray;
        try{
            valuesArray = patternObject.get(PATTERN_VALUES_KEY).getAsJsonArray();
        }catch(NullPointerException e){
            throw new InvalidScriptException();
        }
        this.loadValues(valuesArray);
    }
}
