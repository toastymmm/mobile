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
    public static final String REQUEST_KEY = "REQUEST_KEY";
    public static final String MESSAGE_KEY = "MESSAGE_KEY";
    public static final int CREATE_REQUEST = 0;
    public static final int EDIT_REQUEST = 1;

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

        GeoPoint providedLocation = null;
        Message providedMessage = null;
        int request = getIntent().getIntExtra(REQUEST_KEY, CREATE_REQUEST);
        switch (request) {
            case CREATE_REQUEST:
                providedLocation = getIntent().getParcelableExtra(LAST_KNOWN_LOCATION);
                break;
            case EDIT_REQUEST:
                getSupportActionBar().setTitle(R.string.title_edit_message);

                providedMessage = getIntent().getParcelableExtra(MESSAGE_KEY);
                messageEditText.setText(providedMessage.getMsg());
                // TODO set category
                break;
            default:
                throw new IllegalStateException("invalid request code (" + request + ") passed to CreateMessageActivity");
        }

        // Stupid java requiring lambda references to be final
        GeoPoint lastKnownLocation = providedLocation;
        Message givenMessage = providedMessage;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            String editTextContent = messageEditText.getText().toString();

            if (TextUtils.isEmpty(editTextContent)) {
                Snackbar.make(view, R.string.must_not_be_empty, Snackbar.LENGTH_LONG).show();
                return;
            }

            if (givenMessage == null) {
                // TODO add category to the the message
                // TODO id and username are wrong
                Message message = new Message("", editTextContent, lastKnownLocation, new User("me"), new Date(), false);

                LiveBackend backend = LiveBackend.getInstance(this);
                CreateMessageTask createMessageTask = new CreateMessageTask(backend, true);
                createMessageTask.execute(message);
            } else {
                givenMessage.setMsg(editTextContent);
                LiveBackend backend = LiveBackend.getInstance(this);

                CreateMessageTask createMessageTask = new CreateMessageTask(backend, false);
                createMessageTask.execute(givenMessage);
            }
        });
    }

    private class CreateMessageTask extends AsyncTask<Message, Void, Void> {

        private final LiveBackend backend;
        private final boolean create;
        private Exception taskFailedException;

        public CreateMessageTask(LiveBackend backend, boolean create) {
            this.backend = backend;
            this.create = create;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            if (messages.length != 1)
                throw new IllegalArgumentException("CreateMessageTask takes 1 argument!");
            Message message = messages[0];

            try {
                if (create) {
                    backend.createMessage(message);
                } else {
                    backend.editMessage(message);
                }
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
            if (create) {
                Toast.makeText(getApplicationContext(), "Message created!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Message updated!", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
}
