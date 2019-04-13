package toasty.messageinabottle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.MessagePreviewAdapter;
import toasty.messageinabottle.exception.AuthenticationException;
import toasty.messageinabottle.io.LiveBackend;
import toasty.messageinabottle.io.UsernameFetchTask;

public class SavedMessagesActivity extends AppCompatActivity {

    public static final String USER_ID_KEY = "USER_ID_KEY";
    private final List<Message> activeMessages = new ArrayList<>();
    private MessagePreviewAdapter messagePreviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_messages);

        RecyclerView recyclerView = findViewById(R.id.saved_messages);

        LinearLayoutManager layoutManger = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManger);

        String userID = getIntent().getStringExtra(USER_ID_KEY);
        messagePreviewAdapter = new MessagePreviewAdapter(activeMessages, userID);
        recyclerView.setAdapter(messagePreviewAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LiveBackend backend = LiveBackend.getInstance(this);
        new FetchSavedTask(backend).execute();
    }

    private class FetchSavedTask extends AsyncTask<Void, Void, List<Message>> {

        private final LiveBackend backend;
        private Exception taskFailedException = null;

        public FetchSavedTask(LiveBackend backend) {
            this.backend = backend;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            try {
                Log.i("TOAST", "Fetching favorites on background thread.");
                return backend.favorites();
            } catch (Exception e) {
                taskFailedException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            if (taskFailedException != null) {
                Log.i("TOAST", "An error occurred while loading favorites.", taskFailedException);
                if (taskFailedException instanceof AuthenticationException) {
                    Toast.makeText(getApplicationContext(), "Invalid state. Not logged in.", Toast.LENGTH_LONG).show();
                } else if (taskFailedException instanceof IOException) {
                    Toast.makeText(getApplicationContext(), "Failed to load message favorites.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "An unknown error occurred while loading favorites.", Toast.LENGTH_LONG).show();
                }
                finish();
                return;
            }

            Log.i("TOAST", "Updating favorites on main thread.");
            activeMessages.clear();
            activeMessages.addAll(messages);
            messagePreviewAdapter.notifyDataSetChanged();

            for (Message message : activeMessages) {
                UsernameFetchTask usernameFetchTask = new UsernameFetchTask(backend, message, () -> messagePreviewAdapter.notifyDataSetChanged());
                usernameFetchTask.execute();
            }
        }
    }
}
