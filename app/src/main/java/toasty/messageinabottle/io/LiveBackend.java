package toasty.messageinabottle.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.remote.RemoteMessage;

public class LiveBackend {

    public static List<Message> messages(IGeoPoint geoPoint) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/messages")
                .newBuilder()
                .addQueryParameter("lat", Double.toString(geoPoint.getLatitude()))
                .addQueryParameter("lon", Double.toString(geoPoint.getLongitude()))
                .build();
        Request req = new Request.Builder().get().url(url).build();
        try (Response response = okHttpClient.newCall(req).execute()) {
            Gson gson = new Gson();
            List<RemoteMessage> messages = gson.fromJson(response.body().charStream(),
                    new TypeToken<ArrayList<RemoteMessage>>() {
                    }.getType());

            List<Message> result = new ArrayList<>();
            for (RemoteMessage rm : messages) {
                try {
                    result.add(rm.toMessage());
                } catch (ParseException e) {
                    // TODO handle me
                    throw new RuntimeException(e);
                }
            }
            return result;
        }
    }
}
