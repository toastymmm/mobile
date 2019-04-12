package toasty.messageinabottle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
    public static final String[] CATEGORY_CHOICES = new String[]{
            "General",
            "Breaking Bad Habits",
            "Challenges",
            "Confidence",
            "Entrepreneurship",
            "Forgiveness",
            "Gratitude",
            "Happiness",
            "Health",
            "Leadership",
            "Love",
            "Money",
            "Productivity",
            "Self-worth",
            "Success",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner categorySpinner = findViewById(R.id.category_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORY_CHOICES);
        categorySpinner.setAdapter(adapter);

        EditText messageEditText = findViewById(R.id.messageEditText);

        GeoPoint lastKnownLocation = getIntent().getParcelableExtra(LAST_KNOWN_LOCATION);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            String editTextContent = messageEditText.getText().toString();

            if (TextUtils.isEmpty(editTextContent)) {
                Snackbar.make(view, R.string.must_not_be_empty, Snackbar.LENGTH_LONG).show();
                return;
            }

            // TODO add category to the the message
            // TODO id and username are wrong
            Message message = new Message("", editTextContent, lastKnownLocation, new User("me"), new Date());

            LiveBackend backend = LiveBackend.getInstance(this);
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
            finish();
        }
    }
}
