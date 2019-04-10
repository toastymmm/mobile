package toasty.messageinabottle;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import toasty.messageinabottle.data.Message;

public class MessageDetailActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    public static final String MESSAGE_KEY = "message";

    private Message message;
    private FloatingActionButton fab;
    private TextView contents;
    private TextView author;
    private TextView date;
    private Button reportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context ctx = getApplicationContext();
        setContentView(R.layout.activity_message_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            ToggleFavoriteTask toggleFavoriteTask = new ToggleFavoriteTask();
            toggleFavoriteTask.doInBackground(message);
        });

        contents = findViewById(R.id.message_detail_message_contents);
        author = findViewById(R.id.message_detail_author);
        date = findViewById(R.id.message_detail_date);
        reportButton = findViewById(R.id.report_button);

        message = getIntent().getParcelableExtra(MESSAGE_KEY);
        if (message == null) {
            // TODO maybe error
            Snackbar.make(fab, "No message passed to Activity", Snackbar.LENGTH_LONG).show();
        } else {
            contents.setText(message.getMsg());
            author.setText(message.getAuthor().getUsername());
            date.setText(message.getCreated().toString());
        }

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
                // TODO create report task
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Log.i("TOAST", "Report cancelled.");
                break;
            default:
                throw new RuntimeException("Invalid state encountered"); // This should never happen
        }
    }

    private class ReportMessageTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... messages) {
            // TODO send report to backend
            return null;
        }
    }

    private class ToggleFavoriteTask extends AsyncTask<Message, Void, Boolean> {

        private Message taskMessage;

        @Override
        protected Boolean doInBackground(Message... messages) {
            if (messages.length != 1) {
                throw new IllegalArgumentException("invalid number of messages passed");
            }
            taskMessage = messages[0];
            // TODO call api
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            taskMessage.setFavorite(success);
            updateFloatingActionButtonIcon(taskMessage);
        }
    }
}
