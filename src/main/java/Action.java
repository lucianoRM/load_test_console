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
    private static String BODY_JSON_KEY = "body";

    private static ImmutableMap<String,Boolean> POSSIBLE_ACTIONS = new ImmutableMap.Builder<String,Boolean>()
            .put("GET",true)
            .put("PUT",true)
            .put("POST",true)
            .build();

    private String method;
    private String url;
    private String body;

    public Action(JsonObject actionObject) throws InvalidScriptException {
        if(actionObject.has(METHOD_JSON_KEY) && actionObject.has(URL_JSON_KEY)) {
            this.method = actionObject.get(METHOD_JSON_KEY).getAsString();
            this.url = actionObject.get(URL_JSON_KEY).getAsString();
            if(actionObject.has(BODY_JSON_KEY)) {
                this.body = actionObject.get(BODY_JSON_KEY).getAsString();
            }else if(!this.method.equals("GET")) {
                throw new InvalidScriptException();
            }
        }else {
            throw new InvalidScriptException();
        }
    }

    public Action(String method,String url,String body) throws InvalidActionException{
        if(! POSSIBLE_ACTIONS.containsKey(method)) throw new InvalidActionException("Method : " + method + " is not valid");
        this.method = method;
        this.url = url;
        this.body =  body;
    }

    public String getMethod() {
        return this.method;
    }

    public String getUrl() {
        return this.url;
    }

    public String getBody() {
        return this.body;
    }


}
