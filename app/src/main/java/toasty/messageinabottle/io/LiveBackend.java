package toasty.messageinabottle.io;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.remote.RemoteMessage;

public class LiveBackend {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static List<Message> messages(IGeoPoint geoPoint) throws IOException {
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
            List<RemoteMessage> messages = gson.fromJson(response.body().charStream(),
                    new TypeToken<ArrayList<RemoteMessage>>() {
                    }.getType());

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

    public static boolean tryToCreateAccount(String username, String password) throws IOException {
        RequestBody formBody=new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request=new Request.Builder()
                .url("http://toastymmm.hopto.org/api/signup")
                .post(formBody)
                .build();
        Response response=client.newCall(request).execute();
        String result=response.body().string();

        //if we get the response that it worked, then we created an account successfully,
        //otherwise this username already exists.
        return result.toLowerCase().contains("it worked");
    }

    public static boolean login(String username, String password) throws IOException {
        String tag="Live backend";

        Log.d(tag, "Trying to create account");
        RequestBody formBody=new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request=new Request.Builder()
                .url("http://toastymmm.hopto.org/api/userLogin")
                .post(formBody)
                .build();
        Response response=client.newCall(request).execute();
        String result=response.body().string();

        return result.toLowerCase().contains("it worked");
    }
}
