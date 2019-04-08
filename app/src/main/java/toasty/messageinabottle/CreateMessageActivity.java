package toasty.messageinabottle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class CreateMessageActivity extends AppCompatActivity {

    public static final String LAST_KNOWN_LOCATION = "LAST_KNOWN_LOCATION";

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText messageEditText = findViewById(R.id.messageEditText);

        GeoPoint lastKnownLocation = getIntent().getParcelableExtra(LAST_KNOWN_LOCATION);
        Toast.makeText(this, "location: " + lastKnownLocation, Toast.LENGTH_LONG).show();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextContent = messageEditText.getText().toString();

                if (TextUtils.isEmpty(editTextContent)) {
                    Snackbar.make(view, R.string.must_not_be_empty, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Message message = new Message(editTextContent, lastKnownLocation, new User("me"), new Date());

                CreateMessageTask createMessageTask = new CreateMessageTask();
                createMessageTask.execute(message);
            }
        });
    }

    private class CreateMessageTask extends AsyncTask<Message, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Message... messages) {
            // TODO send the message to the server!
            try {
                Thread.sleep(500);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(getApplicationContext(), "Message created!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Snackbar.make(fab, "Failed to create the message. :(", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
