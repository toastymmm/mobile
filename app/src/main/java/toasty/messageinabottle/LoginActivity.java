package toasty.messageinabottle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import toasty.messageinabottle.data.LoginResult;
import toasty.messageinabottle.io.LiveBackend;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    public static final int LOGIN_SUCCESS = 1;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask userLoginTask = null;

    // UI references.
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        username = findViewById(R.id.username);

        password = findViewById(R.id.password);
        password.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptSignInOrCreate(true);
                return true;
            }
            return false;
        });

        Button emailSignInButton = findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(view -> attemptSignInOrCreate(true));

        Button createAccountButton = findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(v -> attemptSignInOrCreate(false));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     * <p>
     * If signIn is true, then this method will attempt to sign the user in.
     * Otherwise, it will try to create a new account
     */
    private void attemptSignInOrCreate(boolean signIn) {
        if (userLoginTask != null) {
            return;
        }

        // Reset errors.
        username.setError(null);
        password.setError(null);

        // Store values at the time of the login attempt.
        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            this.password.setError(getString(R.string.error_invalid_password));
            focusView = this.password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            this.username.setError(getString(R.string.error_field_required));
            focusView = this.username;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            userLoginTask = new UserLoginTask(username, password, signIn);
            userLoginTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, LoginResult> {

        private final String mEmail;
        private final String mPassword;
        private final boolean signIn;
        private final LiveBackend backend;

        UserLoginTask(String email, String password, boolean signIn) {
            mEmail = email;
            mPassword = password;
            this.signIn = signIn;
            backend = LiveBackend.getInstance(getApplicationContext());
        }

        @Override
        protected LoginResult doInBackground(Void... params) {
            if (signIn) {
                return backend.attemptLogin(mEmail, mPassword);
            } else {
                return backend.attemptCreateAccount(mEmail, mPassword);
            }
        }

        @Override
        protected void onPostExecute(final LoginResult result) {
            userLoginTask = null;

            switch (result) {
                case LOGIN_SUCCESSFUL:
                    setResult(LOGIN_SUCCESS);
                    finish();
                    break;
                case ACCOUNT_SUCCESSFULLY_CREATED:
                    setResult(LOGIN_SUCCESS);
                    finish();
                    break;
                case USERNAME_ALREADY_EXISTS:
                    username.setError(getString(R.string.username_already_exists));
                    username.requestFocus();
                    break;
                case INCORRECT_PASSWORD:
                    password.setError(getString(R.string.error_incorrect_password));
                    password.requestFocus();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            userLoginTask = null;
        }
    }
}

