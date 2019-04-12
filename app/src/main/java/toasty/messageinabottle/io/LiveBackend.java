package toasty.messageinabottle.io;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import toasty.messageinabottle.data.LoginResult;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.remote.Favorite;
import toasty.messageinabottle.data.remote.RemoteMessage;

public class LiveBackend {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static LiveBackend backend;

    private final Gson gson = new Gson();
    private final OkHttpClient client;

    public static LiveBackend getInstance(Context ctx) {
        return backend == null ? backend = new LiveBackend(ctx) : backend;
    }

    private LiveBackend(Context ctx) {
        PersistentCookieJar cookieJar = new PersistentCookieJar(ctx);
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Log.i("TOAST_NETWORK", "sending: " + request.toString());
                    Response response = chain.proceed(request);
                    Log.i("TOAST_NETWORK", "receiving: " + response.toString());
                    return response;
                })
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

        Set<String> favoriteIDs = new HashSet<>();
        for (Message favorite : favorites()) {
            favoriteIDs.add(favorite.getID());
        }

        try (Response response = client.newCall(req).execute()) {
            if (response.body() == null) {
                return new ArrayList<>();
            }

            // Parse the response body using Gson
            RemoteMessage[] messages = gson.fromJson(response.body().string(), RemoteMessage[].class);

            // Convert the RemoteMessages into Messages
            List<Message> result = new ArrayList<>();
            for (RemoteMessage rm : messages) {
                try {
                    Message message = rm.toMessage();

                    if (favoriteIDs.contains(message.getID())) {
                        message.setFavorite(true);
                    }

                    result.add(message);
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

        Set<String> favoriteIDs = new HashSet<>();
        for (Message favorite : favorites()) {
            favoriteIDs.add(favorite.getID());
        }

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
                    Message message = rm.toMessage();

                    if (favoriteIDs.contains(message.getID())) {
                        message.setFavorite(true);
                    }

                    result.add(message);
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

        // FIXME delete endpoint takes the wrong id value
        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }


    public List<Message> favorites() throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/favorites/me");

        Request req = new Request.Builder().get().url(url).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());

            if (response.body() == null)
                return new ArrayList<>();

            Favorite[] favorites = gson.fromJson(response.body().charStream(), Favorite[].class);

            // Convert the RemoteMessages into Messages
            List<Message> result = new ArrayList<>();
            for (Favorite fav : favorites) {
                try {
                    Message message = message(fav.messageId);
                    message.setFavorite(true);
                    result.add(message);
                } catch (ParseException e) {
                    // TODO handle me
                    throw new RuntimeException(e);
                }
            }
            return result;
        }
    }

    public Message message(String id) throws IOException, ParseException {
        Request req = new Request.Builder().url("http://toastymmm.hopto.org/api/message/" + id).get().build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
            if (response.body() == null)
                return null;

            RemoteMessage remoteMessages = gson.fromJson(response.body().charStream(), RemoteMessage.class);
            return remoteMessages.toMessage();
        }
    }

    public LoginResult attemptLogin(String username, String password) {
        String tag = "in Live Backend: ";
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url("http://toastymmm.hopto.org/api/userLogin")
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            int code = response.code();
            String body = response.body().string();
            Log.d(tag, body);
            if (code == 200) {
                return LoginResult.LOGIN_SUCCESSFUL;
            } else {
                return LoginResult.INCORRECT_PASSWORD;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
            return LoginResult.INCORRECT_PASSWORD;
        }
    }

    public LoginResult attemptCreateAccount(String username, String password) {
        String tag = "in Live Backend: ";
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url("http://toastymmm.hopto.org/api/signup")
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            int code = response.code();
            String body = response.body().string();
            Log.d(tag, body);
            if (code == 200) {
                return LoginResult.ACCOUNT_SUCCESSFULLY_CREATED;
            } else {
                return LoginResult.USERNAME_ALREADY_EXISTS;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
            return LoginResult.INCORRECT_PASSWORD;
        }
    }
}
