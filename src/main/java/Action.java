import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import exception.InvalidActionException;
import exception.InvalidScriptException;

/**
 * Created by luciano on 18/03/17.
 * Wraps the actions that the user would do
 */
public class Action {
    private static String METHOD_JSON_KEY = "method";
    private static String URL_JSON_KEY = "url";

    private static ImmutableMap<String,Boolean> POSSIBLE_ACTIONS = new ImmutableMap.Builder<String,Boolean>()
            .put("GET",true)
            .put("PUT",true)
            .put("POST",true)
            .build();

    private String method;
    private String url;

    public Action(JsonObject actionObject) throws InvalidScriptException {
        String method = actionObject.get(METHOD_JSON_KEY).getAsString();
        String url = actionObject.get(URL_JSON_KEY).getAsString();
        if(url == null || method == null) throw new InvalidScriptException();
        this.method = method;
        this.url = url;
    }

    public Action(String method,String url) throws InvalidActionException{
        if(! POSSIBLE_ACTIONS.containsKey(method)) throw new InvalidActionException("Method : " + method + " is not valid");
        this.method = method;
        this.url = url;
    }

    public String getMethod() {
        return this.method;
    }

    public String getUrl() {
        return this.url;
    }


}
