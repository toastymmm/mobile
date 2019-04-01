package toasty.messageinabottle.data.remote;

import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.User;

public class RemoteMessage {

    public static final DateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    Feature feature;
    Creator creator;
    String _id;

    public static Message parseJson(String json) throws ParseException {
        Gson gson = new Gson();

        RemoteMessage remoteMessage = gson.fromJson(json, RemoteMessage.class);
        Message message = new Message(
                remoteMessage.feature.properties.text,
                new GeoPoint(
                        remoteMessage.feature.geometry.coordinates[0],
                        remoteMessage.feature.geometry.coordinates[1]
                ),
                new User(remoteMessage.creator._id),
                ISO8601.parse(remoteMessage.feature.properties.date)
        );
        return message;
    }
}
