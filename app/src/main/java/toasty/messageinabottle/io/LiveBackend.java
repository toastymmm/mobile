package toasty.messageinabottle.io;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import toasty.messageinabottle.data.LoginResult;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.cookie.CookieDatabaseAccessor;
import toasty.messageinabottle.data.cookie.DatabaseCookie;
import toasty.messageinabottle.data.remote.Favorite;
import toasty.messageinabottle.data.remote.FullUser;
import toasty.messageinabottle.data.remote.RemoteMessage;
import toasty.messageinabottle.exception.MessageDoesNotExistException;

public class LiveBackend {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static LiveBackend backend;

    private final Gson gson = new Gson();
    private final Context ctx;
    private final OkHttpClient client;

    public static LiveBackend getInstance(Context ctx) {
        return backend == null ? backend = new LiveBackend(ctx) : backend;
    }

    private LiveBackend(Context ctx) {
        this.ctx = ctx;
        PersistentCookieJar cookieJar = new PersistentCookieJar(ctx);
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT))
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

    public List<Message> messages(IGeoPoint geoPoint, boolean loggedIn) throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/messages")
                .newBuilder()
                .addQueryParameter("lat", Double.toString(geoPoint.getLatitude()))
                .addQueryParameter("lon", Double.toString(geoPoint.getLongitude()))
                .build();

        Request req = new Request.Builder().get().url(url).build();

        Set<String> favoriteIDs = new HashSet<>();
        if (loggedIn) {
            for (Message favorite : favorites()) {
                favoriteIDs.add(favorite.getID());
            }
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
        List<Favorite> favorites = rawFavorites();

        Map<String, String> conversion = new HashMap<>();
        for (Favorite favorite : favorites) {
            conversion.put(favorite.messageId, favorite._id);
        }

        // No need to un-favorite
        if (!conversion.containsKey(message.getID()))
            return;

        String favoriteID = conversion.get(message.getID());

        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/favorite/" + favoriteID);

        Request req = new Request.Builder().url(url).delete().build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }

    public List<Favorite> rawFavorites() throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/favorites/me");

        Request req = new Request.Builder().get().url(url).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());

            if (response.body() == null)
                return new ArrayList<>();

            Favorite[] favorites = gson.fromJson(response.body().charStream(), Favorite[].class);
            return Arrays.asList(favorites);
        }
    }


    public List<Message> favorites() throws IOException {
        List<Message> result = new ArrayList<>();
        for (Favorite fav : rawFavorites()) {
            try {
                Message message = message(fav.messageId);
                message.setFavorite(true);
                result.add(message);
            } catch (ParseException e) {
                // TODO handle me
                throw new RuntimeException(e);
            } catch (MessageDoesNotExistException e) {
                // Message has been deleted
                Log.i("TOAST", "Favorite has already been deleted: " + e.getId());
            }
        }
        return result;
    }

    public Message message(String id) throws IOException, ParseException, MessageDoesNotExistException {
        Request req = new Request.Builder().url("http://toastymmm.hopto.org/api/message/" + id).get().build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() == 404)
                throw new MessageDoesNotExistException(id);
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

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Log.d(tag, body);
            if (response.code() == 200) {
                String userID = getUserIDCookieValue();
                return new LoginResult(LoginResult.LOGIN_SUCCESSFUL, userID);
            } else {
                return new LoginResult(LoginResult.INCORRECT_PASSWORD);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
            return new LoginResult(LoginResult.INCORRECT_PASSWORD);
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

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Log.d(tag, body);
            if (response.code() == 200) {
                String userID = getUserIDCookieValue();
                return new LoginResult(LoginResult.ACCOUNT_SUCCESSFULLY_CREATED, userID);
            } else {
                return new LoginResult(LoginResult.USERNAME_ALREADY_EXISTS);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
            return new LoginResult(LoginResult.INCORRECT_PASSWORD);
        }
    }


    public String username(String id) throws IOException {
        Request req = new Request.Builder().url("http://toastymmm.hopto.org/api/user/" + id).get().build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
            FullUser user = gson.fromJson(response.body().charStream(), FullUser.class);
            if (!user._id.equals(id))
                throw new RuntimeException("Server returned the wrong user!?");
            return user.username;
        }
    }

    public void editMessage(Message message) throws IOException {
        String json = message.toRemoteJson();
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);

        Request req = new Request.Builder()
                .url("http://toastymmm.hopto.org/api/message/" + message.getID())
                .patch(body)
                .build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }

    public void delete(Message message) throws IOException {
        Request req = new Request.Builder()
                .url("http://toastymmm.hopto.org/api/message/" + message.getID())
                .delete()
                .build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }

    public void report(Message message) throws IOException {
        message.incrementReports();

        Request req = new Request.Builder()
                .url("http://toastymmm.hopto.org/api/report/" + message.getID())
                .post(RequestBody.create(null, ""))
                .build();

        try (Response response = client.newCall(req).execute()) {
            if (response.code() != 200)
                throw new IOException("Server returned invalid code: " + response.code());
        }
    }

    private String getUserIDCookieValue() {
        List<DatabaseCookie> cookieList = CookieDatabaseAccessor.getCookieDatabase(ctx).databaseCookieDao().find("userid");
        if (cookieList.size() != 1)
            throw new RuntimeException("Too many userid cookies");
        return cookieList.get(0).value;
    }
}
