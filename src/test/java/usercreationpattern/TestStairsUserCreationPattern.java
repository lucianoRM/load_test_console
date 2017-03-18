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
public class TestStairsUserCreationPattern {

    @Test
    public void testStairsCreationWithCorrectFile() throws FileNotFoundException, InvalidScriptException {
        URL scriptPathUrl = this.getClass().getResource("validStairs.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement scriptElement = jsonParser.parse(new FileReader(scriptPathUrl.getPath()));
        StairsUserCreationPattern stairsUserCreationPattern = new StairsUserCreationPattern(scriptElement.getAsJsonObject());

        assertEquals(11, stairsUserCreationPattern.getPatternValues().size());

        assertEquals(0, (int) stairsUserCreationPattern.getPatternValues().get(0));
        assertEquals(10, (int) stairsUserCreationPattern.getPatternValues().get(10));
        assertEquals(20, (int) stairsUserCreationPattern.getPatternValues().get(20));
        assertEquals(30, (int) stairsUserCreationPattern.getPatternValues().get(30));
        assertEquals(40, (int) stairsUserCreationPattern.getPatternValues().get(40));
        assertEquals(50, (int) stairsUserCreationPattern.getPatternValues().get(50));
        assertEquals(60, (int) stairsUserCreationPattern.getPatternValues().get(60));
        assertEquals(70, (int) stairsUserCreationPattern.getPatternValues().get(70));
        assertEquals(80, (int) stairsUserCreationPattern.getPatternValues().get(80));
        assertEquals(90, (int) stairsUserCreationPattern.getPatternValues().get(90));
        assertEquals(100, (int) stairsUserCreationPattern.getPatternValues().get(100));

    }
}
