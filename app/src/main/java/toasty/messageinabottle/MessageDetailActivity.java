package toasty.messageinabottle;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.io.LiveBackend;

public class MessageDetailActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    public static final String MESSAGE_KEY = "message";

    private Message message;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            LiveBackend backend = LiveBackend.getInstance(this);
            ToggleFavoriteTask toggleFavoriteTask = new ToggleFavoriteTask(backend);
            toggleFavoriteTask.execute(message);
        });

        TextView contents = findViewById(R.id.message_detail_message_contents);
        TextView author = findViewById(R.id.message_detail_author);
        TextView date = findViewById(R.id.message_detail_date);
        Button reportButton = findViewById(R.id.report_button);

        message = getIntent().getParcelableExtra(MESSAGE_KEY);
        if (message == null) {
            // Pass a message to display via the intent and using MESSAGE_KEY
            throw new RuntimeException("A message must be passed to the MessageDetailActivity");
        }
        contents.setText(message.getMsg());
        author.setText(message.getAuthor().getUsername());
        date.setText(message.getCreated().toString());
        updateFloatingActionButtonIcon(message);

        reportButton.setOnClickListener((v) -> {
            Log.i("TOAST", "Report confirmation triggered.");
            new AlertDialog.Builder(this)
                    .setTitle(R.string.report_confirmation)
                    .setMessage("Message author (" + message.getAuthor().getUsername() + ") will be reported to the admins.")
                    .setPositiveButton(R.string.report_button, this)
                    .setNegativeButton(R.string.cancel_button, this)
                    .create()
                    .show();
        });
    }

    private void updateFloatingActionButtonIcon(Message message) {
        Context ctx = getApplicationContext();
        if (message.isFavorite()) {
            fab.setImageDrawable(ctx.getDrawable(android.R.drawable.star_big_on));
        } else {
            fab.setImageDrawable(ctx.getDrawable(android.R.drawable.star_big_off));
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                Log.i("TOAST", "Sending report.");
                LiveBackend backend = LiveBackend.getInstance(this);
                new ReportMessageTask(backend).execute(message);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Log.i("TOAST", "Report cancelled.");
                break;
            default:
                throw new RuntimeException("Invalid state encountered"); // This should never happen
        }
    }

    private class ReportMessageTask extends AsyncTask<Message, Void, Void> {

        private final LiveBackend backend;
        private Exception taskFailedException;

        public ReportMessageTask(LiveBackend backend) {
            this.backend = backend;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            try {
                // TODO send report to backend
            } catch (Exception e) {
                taskFailedException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (taskFailedException != null) {
                // TODO notify the user
                return;
            }
        }
    }

    private class ToggleFavoriteTask extends AsyncTask<Message, Void, Boolean> {

        private final LiveBackend backend;
        private Message taskMessage;
        private Exception taskFailedException;

        public ToggleFavoriteTask(LiveBackend backend) {
            this.backend = backend;
        }

        @Override
        protected Boolean doInBackground(Message... messages) {
            if (messages.length != 1) {
                throw new IllegalArgumentException("invalid number of messages passed");
            }
            taskMessage = messages[0];

            try {
                if (taskMessage.isFavorite()) {
                    backend.unmarkFavorite(taskMessage);
                    return false;
                } else {
                    backend.markFavorite(message);
                    return true;
                }
            } catch (Exception e) {
                taskFailedException = e;
                return false; // onPostExecute should not use this value
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (taskFailedException != null) {
                Log.i("TOAST", "Failed to do favorite operation", taskFailedException);
                // TODO notify the user
                return;
            }
            Log.i("TOAST", "Marking the message as: " + success);
            taskMessage.setFavorite(success);
            updateFloatingActionButtonIcon(taskMessage);
        }
    }
}
