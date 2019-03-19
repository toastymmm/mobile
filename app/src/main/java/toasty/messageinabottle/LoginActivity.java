package toasty.messageinabottle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import toasty.messageinabottle.data.MockBackend;
import toasty.messageinabottle.data.User;
import toasty.messageinabottle.exceptions.AuthenticationException;

public class LoginActivity extends AppCompatActivity {

    private static User loggedInUser;
    private static final String tag=LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "Logging in...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton=findViewById(R.id.loginButton);
        final EditText emailField=findViewById(R.id.loginEmailField);
        final EditText passwordField=findViewById(R.id.loginPasswordField);
        final LoginActivity toClose=this;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "Login button clicked!");
                String email=emailField.getText().toString();
                String password=passwordField.getText().toString();
                if (tryToLogIn(email, password)) {
                    Log.d(tag, "Login succeeded...");
                    startActivity(new Intent(toClose, MainActivity.class));
                    toClose.finish();
                }
                else {
                    Log.d(tag, "Invalid password.");
                    emailField.setText("");
                    passwordField.setText("");
                    emailField.requestFocus();
                }
            }
        });
    }

    //returns true if login is successful, false otherwise
    private static boolean tryToLogIn(String email, String password) {
        try {
            MockBackend.getInstance().login(email, password);
            loggedInUser=new User(email);
            return true;
        }
        catch (AuthenticationException e){
            return false;
        }
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }
}
