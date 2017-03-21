package usercreationpattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import exception.InvalidScriptException;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Created by luciano on 18/03/17.
 */
public class TestUserCreationPatternFactory {

    private static UserCreationPatternFactory userCreationPatternFactory = new UserCreationPatternFactory();

    @Test
    public void testCorrectRampPatternCreation() throws FileNotFoundException, InvalidScriptException{
        URL scriptPathUrl = this.getClass().getResource("validRamp.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement patternElement = jsonParser.parse(new FileReader(scriptPathUrl.getPath()));

        UserCreationPattern userCreationPattern = userCreationPatternFactory.createUserCreationPattern(patternElement.getAsJsonObject());
        assertTrue(userCreationPattern.getClass() == RampUserCreationPattern.class);
    }

    @Test
    public void testCorrectStairsPatternCreation() throws FileNotFoundException, InvalidScriptException{
        URL scriptPathUrl = this.getClass().getResource("validRamp.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement patternElement = jsonParser.parse(new FileReader(scriptPathUrl.getPath()));

        UserCreationPattern userCreationPattern = userCreationPatternFactory.createUserCreationPattern(patternElement.getAsJsonObject());
        assertTrue(userCreationPattern.getClass() == RampUserCreationPattern.class);
    }

    @Test
    public void testCorrectCustomPatternCreation() throws FileNotFoundException, InvalidScriptException{
        URL scriptPathUrl = this.getClass().getResource("validRamp.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement patternElement = jsonParser.parse(new FileReader(scriptPathUrl.getPath()));

        UserCreationPattern userCreationPattern = userCreationPatternFactory.createUserCreationPattern(patternElement.getAsJsonObject());
        assertTrue(userCreationPattern.getClass() == RampUserCreationPattern.class);
    }

    @Test
    public void testInvalidTypeRaiseException() throws FileNotFoundException, InvalidScriptException{
        URL scriptPathUrl = this.getClass().getResource("validRamp.json");
        JsonParser jsonParser = new JsonParser();
        JsonElement patternElement = jsonParser.parse(new FileReader(scriptPathUrl.getPath()));

        UserCreationPattern userCreationPattern = userCreationPatternFactory.createUserCreationPattern(patternElement.getAsJsonObject());
        assertTrue(userCreationPattern.getClass() == RampUserCreationPattern.class);
    }
}
