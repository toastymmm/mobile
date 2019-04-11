package toasty.messageinabottle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.User;
import toasty.messageinabottle.io.LiveBackend;

public class CreateMessageActivity extends AppCompatActivity {

    public static final String LAST_KNOWN_LOCATION = "LAST_KNOWN_LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText messageEditText = findViewById(R.id.messageEditText);

        GeoPoint lastKnownLocation = getIntent().getParcelableExtra(LAST_KNOWN_LOCATION);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            String editTextContent = messageEditText.getText().toString();

            if (TextUtils.isEmpty(editTextContent)) {
                Snackbar.make(view, R.string.must_not_be_empty, Snackbar.LENGTH_LONG).show();
                return;
            }

            // TODO id and username are wrong
            Message message = new Message("", editTextContent, lastKnownLocation, new User("me"), new Date());

            LiveBackend backend = new LiveBackend(this);
            CreateMessageTask createMessageTask = new CreateMessageTask(backend);
            createMessageTask.execute(message);
        });
    }

    private class CreateMessageTask extends AsyncTask<Message, Void, Void> {

        private final LiveBackend backend;
        private Exception taskFailedException;

        public CreateMessageTask(LiveBackend backend) {
            this.backend = backend;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            if (messages.length != 1)
                throw new IllegalArgumentException("CreateMessageTask takes 1 argument!");
            Message message = messages[0];

            try {
                backend.createMessage(message);
            } catch (Exception e) {
                taskFailedException = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (taskFailedException != null) {
                // TODO handle message creation failure
                return;
            }
            Toast.makeText(getApplicationContext(), "Message created!", Toast.LENGTH_SHORT).show();
        }
    }
}
