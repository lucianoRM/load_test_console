import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.InvalidScriptException;
import usercreationpattern.RampUserCreationPattern;
import usercreationpattern.UserCreationPattern;
import usercreationpattern.UserCreationPatternFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luciano on 18/03/17.
 *
 * This class handles the loading of the script to be replicated to every user.
 */
public class ScriptLoader {

    private static final String USER_PATTERN_JSON_KEY = "userCreationPattern";
    private static final String ACTIONS_JSON_KEY = "actions";

    private List<Action> actionList;
    private UserCreationPattern userCreationPattern;
    private JsonObject scriptFileObject;

    public ScriptLoader() {}

    /**
     * Loads userCreationPattern from json file
     * @throws InvalidScriptException
     */
    private void loadUserCreationPattern() throws InvalidScriptException{
        JsonObject userPatternObject;
        try {
            userPatternObject = this.scriptFileObject.get(USER_PATTERN_JSON_KEY).getAsJsonObject();
        }catch(NullPointerException e) {
            throw new InvalidScriptException();
        }
        this.userCreationPattern = UserCreationPatternFactory.createUserCreationPattern(userPatternObject);
    }

    /**
     * Loads actions array from json file
     * @throws InvalidScriptException
     */
    private void loadActions() throws InvalidScriptException {
        JsonArray actionsArray;
        try {
            actionsArray = this.scriptFileObject.get(ACTIONS_JSON_KEY).getAsJsonArray();
        }catch(NullPointerException e){
            throw new InvalidScriptException();
        }
        this.actionList = new ArrayList<>();
        for(JsonElement actionElement : actionsArray) {
            this.actionList.add(new Action(actionElement.getAsJsonObject()));
        }
    }

    /**
     * Loads script into class. The script must contain a configuration object and an actions array
     * @param scriptFilePath
     * @throws FileNotFoundException
     * @throws InvalidScriptException
     */
    public void loadScript(String scriptFilePath) throws FileNotFoundException,InvalidScriptException {
        JsonParser jsonParser = new JsonParser();
        JsonElement scriptElement = jsonParser.parse(new FileReader(scriptFilePath));
        this.scriptFileObject = scriptElement.getAsJsonObject();
        this.loadActions();
        this.loadUserCreationPattern();
    }

    public List<Action> getActionList() {
        return this.actionList;
    }

    public UserCreationPattern getUserCreationPattern() {
        return this.userCreationPattern;
    }


}
