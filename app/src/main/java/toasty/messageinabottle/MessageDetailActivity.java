package toasty.messageinabottle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.io.LiveBackend;
import toasty.messageinabottle.io.UsernameFetchTask;

public class MessageDetailActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    public static final String MESSAGE_KEY = "message";
    public static final String USER_ID_KEY = "USER_ID_KEY";

    private FloatingActionButton fab;
    private Message message;
    private String userID;

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

        message = getIntent().getParcelableExtra(MESSAGE_KEY);
        userID = getIntent().getStringExtra(USER_ID_KEY);
        if (message == null) {
            // Pass a message to display via the intent and using MESSAGE_KEY
            throw new RuntimeException("A message must be passed to the MessageDetailActivity");
        }
        contents.setText(message.getMsg());
        author.setText(message.getAuthor().getUsername());
        date.setText(message.getCreated().toString());
        updateFloatingActionButtonIcon(message);

        if (message.getAuthor().getUsername() == null) {
            UsernameFetchTask usernameFetchTask = new UsernameFetchTask(LiveBackend.getInstance(this), message, () -> author.setText(message.getAuthor().getUsername()));
            usernameFetchTask.execute();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_menu_item || item.getItemId() == R.id.delete_menu_item) {
            if (userID == null) {
                Snackbar.make(fab, "You must be logged in to edit your messages.", Snackbar.LENGTH_LONG).show();
                return false;
            } else if (!message.getAuthor().getId().equals(userID)) {
                Snackbar.make(fab, "You cannot edit other user's messages.", Snackbar.LENGTH_LONG).show();
                return false;
            }
        }

        switch (item.getItemId()) {
            case R.id.edit_menu_item:
                Intent intent = new Intent(this, CreateMessageActivity.class);
                intent.putExtra(CreateMessageActivity.REQUEST_KEY, CreateMessageActivity.EDIT_REQUEST);
                intent.putExtra(CreateMessageActivity.MESSAGE_KEY, (Parcelable) message);
                startActivity(intent);
                finish();
                break;
            case R.id.delete_menu_item:
                Log.i("TOAST", "Delete confirmation triggered.");

                LiveBackend backend = LiveBackend.getInstance(this);
                DeleteListener deleteListener = new DeleteListener(backend, message);

                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_confirmation)
                        .setMessage("This cannot be undone.")
                        .setPositiveButton(R.string.delete, deleteListener)
                        .setNegativeButton(R.string.cancel_button, deleteListener)
                        .create()
                        .show();
                break;
            case R.id.report_menu_item:
                Log.i("TOAST", "Report confirmation triggered.");

                new AlertDialog.Builder(this)
                        .setTitle(R.string.report_confirmation)
                        .setMessage("Message author (" + message.getAuthor().getUsername() + ") will be reported to the admins.")
                        .setPositiveButton(R.string.report_button, this)
                        .setNegativeButton(R.string.cancel_button, this)
                        .create()
                        .show();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    private class DeleteListener implements DialogInterface.OnClickListener {

        private final LiveBackend backend;
        private final Message message;

        public DeleteListener(LiveBackend backend, Message message) {
            this.backend = backend;
            this.message = message;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Log.i("TOAST", "Deleting message");
                    DeleteMessageTask deleteMessageTask = new DeleteMessageTask(backend, message);
                    deleteMessageTask.execute();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    Log.i("TOAST", "Delete cancelled.");
                    break;
                default:
                    throw new RuntimeException("Invalid state encountered"); // This should never happen
            }
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

    private class DeleteMessageTask extends AsyncTask<Void, Void, Void> {

        private final LiveBackend backend;
        private final Message message;
        private Exception taskFailedException;

        public DeleteMessageTask(LiveBackend backend, Message message) {
            this.backend = backend;
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                backend.delete(message);
            } catch (Exception e) {
                taskFailedException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (taskFailedException != null) {
                Toast.makeText(MessageDetailActivity.this, "Failed to delete message", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(MessageDetailActivity.this, "Message deleted!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
