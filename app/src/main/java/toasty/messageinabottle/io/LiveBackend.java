package toasty.messageinabottle.io;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import toasty.messageinabottle.data.LoginResult;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.remote.RemoteMessage;

public class LiveBackend {

    private final Gson gson = new Gson();
    private OkHttpClient client;
    private static LiveBackend backend;

    public static LiveBackend getInstance(Context ctx) {
        return backend==null?backend=new LiveBackend(ctx):backend;
    }

    private LiveBackend(Context ctx) {
        PersistentCookieJar cookieJar = new PersistentCookieJar(ctx);
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .cookieJar(cookieJar)
                .build();
    }

    public List<Message> messages(IGeoPoint geoPoint) throws IOException {
        HttpUrl url = HttpUrl.get("http://toastymmm.hopto.org/api/messages")
                .newBuilder()
                .addQueryParameter("lat", Double.toString(geoPoint.getLatitude()))
                .addQueryParameter("lon", Double.toString(geoPoint.getLongitude()))
                .build();

        Request req = new Request.Builder().get().url(url).build();

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
                    result.add(rm.toMessage());
                } catch (ParseException e) {
                    // TODO handle me
                    throw new RuntimeException(e);
                }
            }
            return result;
        }
    }

    public LoginResult attemptLogin(String username, String password) {
        String tag="in Live Backend: ";
        RequestBody formBody=new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request=new Request.Builder()
                .url("http://toastymmm.hopto.org/api/userLogin")
                .post(formBody)
                .build();

        try {
            Response response=client.newCall(request).execute();
            int code=response.code();
            String body=response.body().string();
            Log.d(tag, body);
            if (code==200) {
                return LoginResult.LOGIN_SUCCESSFUL;
            }
            else {
                return LoginResult.INCORRECT_PASSWORD;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
            return LoginResult.INCORRECT_PASSWORD;
        }
    }

    public LoginResult attemptCreateAccount(String username, String password) {
        String tag="in Live Backend: ";
        RequestBody formBody=new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request=new Request.Builder()
                .url("http://toastymmm.hopto.org/api/signup")
                .post(formBody)
                .build();

        try {
            Response response=client.newCall(request).execute();
            int code=response.code();
            String body=response.body().string();
            Log.d(tag, body);
            if (code==200) {
                return LoginResult.ACCOUNT_SUCCESSFULLY_CREATED;
            }
            else {
                return LoginResult.USERNAME_ALREADY_EXISTS;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
            return LoginResult.INCORRECT_PASSWORD;
        }
    }
}
