package toasty.messageinabottle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ImageView toastImageView = findViewById(R.id.toastImageView);
        TextView toastTextView = findViewById(R.id.toastTextView);

        View.OnClickListener gitHubLaunchClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/toastymmm/"));
                startActivity(intent);
            }
        };

        toastImageView.setOnClickListener(gitHubLaunchClickListener);
        toastTextView.setOnClickListener(gitHubLaunchClickListener);
    }
}
