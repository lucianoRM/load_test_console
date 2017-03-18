package usercreationpattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import exception.InvalidScriptException;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by luciano on 18/03/17.
 */
public class TestRampUserCreationPattern {

    @Test
    public void testRampCreationWithCorrectFile() throws FileNotFoundException, InvalidScriptException{
        URL scriptPathUrl = this.getClass().getResource("validRamp.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement scriptElement = jsonParser.parse(new FileReader(scriptPathUrl.getPath()));
        RampUserCreationPattern rampUserCreationPattern = new RampUserCreationPattern(scriptElement.getAsJsonObject());

        assertEquals(51,rampUserCreationPattern.getPatternValues().size());

        assertEquals(50,(int)rampUserCreationPattern.getPatternValues().get(0));
        assertEquals(100,(int)rampUserCreationPattern.getPatternValues().get(50));



    }
}
