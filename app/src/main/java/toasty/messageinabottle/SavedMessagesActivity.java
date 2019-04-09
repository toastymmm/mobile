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

public class SavedMessagesActivity extends AppCompatActivity {

    private final List<Message> activeMessages = new ArrayList<>();
    private MessagePreviewAdapter messagePreviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_messages);

        RecyclerView recyclerView = findViewById(R.id.saved_messages);

        LinearLayoutManager layoutManger = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManger);

        messagePreviewAdapter = new MessagePreviewAdapter(activeMessages);
        recyclerView.setAdapter(messagePreviewAdapter);
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
                return backend.favorites();
            } catch (Exception e) {
                taskFailedException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            if (taskFailedException != null) {
                if (taskFailedException instanceof AuthenticationException) {
                    Toast.makeText(SavedMessagesActivity.this, "Invalid state. Not logged in.", Toast.LENGTH_LONG).show();
                } else if (taskFailedException instanceof IOException) {
                    Toast.makeText(SavedMessagesActivity.this, "Failed to load message favorites.", Toast.LENGTH_LONG).show();
                }
                finish();
            }
            activeMessages.clear();
            activeMessages.addAll(messages);
            messagePreviewAdapter.notifyDataSetChanged();
        }
    }
}
