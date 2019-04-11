package toasty.messageinabottle.io;

import android.content.Context;

import com.google.gson.Gson;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.remote.RemoteMessage;

public class LiveBackend {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final Gson gson = new Gson();
    private OkHttpClient client;

    public LiveBackend(Context ctx) {
        PersistentCookieJar cookieJar = new PersistentCookieJar(ctx);
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .cookieJar(cookieJar)
                .build();
    }

    public void createMessage(Message message) throws IOException {
        HttpUrl httpUrl = HttpUrl.get("http://toastymmm.hopto.org/api/message");
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, message.toRemoteJson());
        Request req = new Request.Builder().url(httpUrl).post(body).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }

    public List<Message> messages(IGeoPoint geoPoint) throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/messages")
                .newBuilder()
                .addQueryParameter("lat", Double.toString(geoPoint.getLatitude()))
                .addQueryParameter("lon", Double.toString(geoPoint.getLongitude()))
                .build();

        Request req = new Request.Builder().get().url(url).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.body() == null)
                return new ArrayList<>();

            // Parse the response body using Gson
            RemoteMessage[] messages = gson.fromJson(response.body().string(), RemoteMessage[].class);

            // Convert the RemoteMessages into Messages
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

    public List<Message> messageHistory() throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/messages/me");

        Request req = new Request.Builder().get().url(url).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());

            if (response.body() == null)
                return new ArrayList<>();

            RemoteMessage[] messages = gson.fromJson(response.body().charStream(), RemoteMessage[].class);

            // Convert the RemoteMessages into Messages
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

    public void markFavorite(Message message) throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/favorite/" + message.getID());

        RequestBody emptyBody = RequestBody.create(null, "");
        Request req = new Request.Builder().url(url).post(emptyBody).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }

    public void unmarkFavorite(Message message) throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/favorite/" + message.getID());

        Request req = new Request.Builder().url(url).delete().build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }


    public List<Message> favorites() throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/favorites/");

        Request req = new Request.Builder().get().url(url).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());

            if (response.body() == null)
                return new ArrayList<>();

            RemoteMessage[] messages = gson.fromJson(response.body().charStream(), RemoteMessage[].class);

            // Convert the RemoteMessages into Messages
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
