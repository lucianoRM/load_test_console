import exception.InvalidScriptException;
import org.junit.Test;
import utils.ScriptLoader;

import java.io.FileNotFoundException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by luciano on 18/03/17.
 */
public class TestScriptLoader {

    private ScriptLoader scriptLoader = new ScriptLoader();


    @Test(expected = InvalidScriptException.class)
    public void testInvalidKeysRaisesException() throws InvalidScriptException,FileNotFoundException {
        URL scriptPathUrl = this.getClass().getResource("invalidKeys.json");
        this.scriptLoader.loadScript(scriptPathUrl.getPath());
    }

    


}
