package toasty.messageinabottle;

import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.text.ParseException;

import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.User;
import toasty.messageinabottle.data.remote.RemoteMessage;

import static org.junit.Assert.assertEquals;

public class RemoteMessageTest {

    @Test
    public void basicRemoteMessage() throws ParseException {
        String given = "{\n" +
                "  \"feature\": {\n" +
                "    \"type\": \"Feature\",\n" +
                "    \"properties\": {\n" +
                "      \"text\": \"I am in charge of my life\",\n" +
                "      \"category\": \"Confidence\",\n" +
                "      \"date\": \"2019-03-27T00:28:19.958Z\",\n" +
                "      \"numReports\": 0\n" +
                "    },\n" +
                "    \"geometry\": {\n" +
                "      \"type\": \"Point\",\n" +
                "      \"coordinates\": [\n" +
                "        -81.1997889146089,\n" +
                "        28.602251226391626\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"creator\": {\n" +
                "    \"_id\": \"5c78b456fd1441656500d212\"\n" +
                "  },\n" +
                "  \"_id\": \"5c78b456fd1441656500d212\"\n" +
                "}";

        Message expected = new Message(
                "I am in charge of my life",
                new GeoPoint(-81.1997889146089, 28.602251226391626),
                new User("5c78b456fd1441656500d212"),
                RemoteMessage.ISO8601.parse("2019-03-27T00:28:19.958Z")
        );
        Message actual = RemoteMessage.parseJson(given);
        assertEquals(expected, actual);
    }
}
