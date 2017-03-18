import exception.InvalidActionException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Created by luciano on 18/03/17.
 */
public class TestAction {


    @Test
    public void testCorrectActionCreation() throws InvalidActionException {
        Action action = new Action("GET","some url");
        assertEquals("GET",action.getMethod());
        assertEquals("some url",action.getUrl());

    }

    @Test(expected = InvalidActionException.class)
    public void testNewActionRaisesExceptionWhenMethodIsInvalid() throws InvalidActionException {
        Action action = new Action("invalid method", "an url");
    }

}
