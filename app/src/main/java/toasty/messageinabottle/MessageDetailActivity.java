package toasty.messageinabottle;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import toasty.messageinabottle.data.Message;

public class MessageDetailActivity extends AppCompatActivity {

    public static final String MESSAGE_KEY = "message";

    private TextView contents;
    private TextView author;
    private TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        contents = findViewById(R.id.message_detail_message_contents);
        author = findViewById(R.id.message_detail_author);
        date = findViewById(R.id.message_detail_date);

        Message message = getIntent().getParcelableExtra(MESSAGE_KEY);
        if (message == null) {
            Snackbar.make(fab, "No message passed to Activity", Snackbar.LENGTH_LONG).show();
        } else {
            contents.setText(message.getMsg());
            author.setText(message.getAuthor().getUsername());
            date.setText(message.getCreated().toString());
        }
    }
}
