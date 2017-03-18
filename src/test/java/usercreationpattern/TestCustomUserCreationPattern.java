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
public class TestCustomUserCreationPattern {

    @Test
    public void testStairsCreationWithCorrectFile() throws FileNotFoundException, InvalidScriptException {
        URL scriptPathUrl = this.getClass().getResource("validCustom.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement scriptElement = jsonParser.parse(new FileReader(scriptPathUrl.getPath()));
        CustomUserCreationPattern customUserCreationPattern = new CustomUserCreationPattern(scriptElement.getAsJsonObject());

        assertEquals(3, customUserCreationPattern.getPatternValues().size());

        assertEquals(0, (int) customUserCreationPattern.getPatternValues().get(0));
        assertEquals(1000, (int) customUserCreationPattern.getPatternValues().get(10));
        assertEquals(100, (int) customUserCreationPattern.getPatternValues().get(50));
    }

}
