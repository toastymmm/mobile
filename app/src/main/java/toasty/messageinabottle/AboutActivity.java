package toasty.messageinabottle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ImageView toastImageView = findViewById(R.id.toastImageView);
        TextView toastTextView = findViewById(R.id.toastTextView);

        View.OnClickListener gitHubLaunchClickListener = v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/toastymmm/"));
            startActivity(intent);
        };

        toastImageView.setOnClickListener(gitHubLaunchClickListener);
        toastTextView.setOnClickListener(gitHubLaunchClickListener);
    }
}
