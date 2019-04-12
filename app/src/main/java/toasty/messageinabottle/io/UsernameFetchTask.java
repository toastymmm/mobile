package toasty.messageinabottle.io;

import android.os.AsyncTask;
import android.util.Log;

import toasty.messageinabottle.data.Message;

public class UsernameFetchTask extends AsyncTask<Void, Void, String> {

    private final LiveBackend backend;
    private final Message message;
    private final Runnable onFinish;

    public UsernameFetchTask(LiveBackend backend, Message message, Runnable onFinish) {
        this.backend = backend;
        this.message = message;
        this.onFinish = onFinish;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return backend.username(message.getAuthor().getId());
        } catch (Exception e) {
            Log.i("TOAST", "Failed while trying to get a username", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String username) {
        Log.i("TOAST", "Fetched a username in the background for " + message.getID() + " => " + username);
        message.getAuthor().setUsername(username);
        onFinish.run();
    }
}
