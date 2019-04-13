package toasty.messageinabottle;

import android.os.AsyncTask;
import android.os.Bundle;
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

public class MessageHistoryActivity extends AppCompatActivity {

    public static final String USER_ID_KEY = "USER_ID_KEY";
    private final List<Message> displayedMessages = new ArrayList<>();
    private MessagePreviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_history);
        RecyclerView messageHistory = findViewById(R.id.message_history);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messageHistory.setLayoutManager(layoutManager);

        String userID = getIntent().getStringExtra(USER_ID_KEY);
        adapter = new MessagePreviewAdapter(displayedMessages, userID);
        messageHistory.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LiveBackend backend = LiveBackend.getInstance(this);
        new FetchFavoritesTask(backend).execute();
    }

    private class FetchFavoritesTask extends AsyncTask<Void, Void, List<Message>> {

        private final LiveBackend backend;
        private Exception taskFailedException = null;

        public FetchFavoritesTask(LiveBackend backend) {
            this.backend = backend;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            try {
                return backend.messageHistory();
            } catch (Exception e) {
                taskFailedException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            if (taskFailedException != null) {
                if (taskFailedException instanceof AuthenticationException) {
                    Toast.makeText(MessageHistoryActivity.this, "Invalid state. Not logged in.", Toast.LENGTH_LONG).show();
                } else if (taskFailedException instanceof IOException) {
                    Toast.makeText(MessageHistoryActivity.this, "Failed to load message history.", Toast.LENGTH_LONG).show();
                }
                finish();
                return;
            }

            displayedMessages.clear();
            displayedMessages.addAll(messages);
            adapter.notifyDataSetChanged();

            for (Message message : displayedMessages) {
                UsernameFetchTask usernameFetchTask = new UsernameFetchTask(backend, message, () -> adapter.notifyDataSetChanged());
                usernameFetchTask.execute();
            }
        }
    }
}
