package haakoleg.imt3673_podcast_manager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import haakoleg.imt3673_podcast_manager.models.User;
import haakoleg.imt3673_podcast_manager.utils.CheckNetwork;
import haakoleg.imt3673_podcast_manager.utils.Messages;

public class LoginActivity extends AppCompatActivity {
    private static boolean runOnce;

    private FirebaseAuth fAuth;
    private DatabaseReference dbRef;

    private View loginView;
    private View registerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set persistence for Firebase, this can only be run once or the app
        // will crash if it is restarted, also set default preferences
        if (!runOnce) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            runOnce = true;
        }

        dbRef = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();

        // Check if user is signed in and show main activity
        if (fAuth.getCurrentUser() != null) {
            goToMain();
        } else {

            // Not signed in, set view and find elements
            setContentView(R.layout.activity_login);
            loginView = findViewById(R.id.sign_in_view);
            registerView = findViewById(R.id.register_view);
            EditText emailInput = findViewById(R.id.email_input);
            EditText passwordInput = findViewById(R.id.password_input);

            TextView showregisterBtn = findViewById(R.id.show_register_btn);
            TextView showLoginBtn = findViewById(R.id.show_login_btn);
            Button signInBtn = findViewById(R.id.sign_in_btn);

            EditText registerUsernameInput = findViewById(R.id.register_username_input);
            EditText registerEmailInput = findViewById(R.id.register_email_input);
            EditText registerPasswordInput = findViewById(R.id.register_password_input);
            Button registerUserBtn = findViewById(R.id.register_user_btn);

            // Onclick listener for show register and login views
            showregisterBtn.setOnClickListener(v -> showRegisterUserView());
            showLoginBtn.setOnClickListener(v -> showLoginUserView());

            // Set click listener for sign in button
            signInBtn.setOnClickListener(v -> {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                if (checkEmailPwd(email, password)) {
                    signInUser(email, password);
                }
            });

            // Set click listener for register button
            registerUserBtn.setOnClickListener(v -> {
                String username = registerUsernameInput.getText().toString();
                String email = registerEmailInput.getText().toString();
                String password = registerPasswordInput.getText().toString();
                if (username.length() < 3) {
                    Messages.showError(this, getString(R.string.error_username_len), null);
                } else if (checkEmailPwd(email, password)) {
                    registerUser(username, email, password);
                }
            });
        }
    }

    // Switches view to register user
    private void showRegisterUserView() {
        TransitionManager.beginDelayedTransition((ViewGroup) loginView.getParent(), new Slide(Gravity.END));
        loginView.setVisibility(View.GONE);
        registerView.setVisibility(View.VISIBLE);
    }

    // Switches view to login user
    private void showLoginUserView() {
        TransitionManager.beginDelayedTransition((ViewGroup) loginView.getParent(), new Slide(Gravity.START));
        registerView.setVisibility(View.GONE);
        loginView.setVisibility(View.VISIBLE);
    }

    private boolean checkEmailPwd(String email, String password) {
        if (email.length() < 1 || password.length() < 1) {
            Messages.showError(this, getString(R.string.error_empty), null);
            return false;
        }
        return true;
    }

    /**
     * Switches to the main activity by calling startActivity and finishes this activity
     */
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    /**
     * Creates a new user with Firebase using an e-mail and password
     * @param email The e-mail for the new user
     * @param password The password for the new user
     */
    private void registerUser(String username, String email, String password) {
        // Check that internet connectivity exists
        if (!CheckNetwork.hasNetwork(this)) {
            Messages.showError(this, getString(R.string.error_no_internet), null);
            return;
        }

        fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(result -> {
            FirebaseUser user = fAuth.getCurrentUser();
            
            // Set user display name
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();
            user.updateProfile(changeRequest);

            // Save user to database
            dbRef.child("users").child(user.getUid()).setValue(new User(username, user.getEmail()));
            goToMain();
        }).addOnFailureListener(ex -> Messages.showError(this, ex.getLocalizedMessage(), null));
    }

    /**
     * Signs in a user using e-mail and password
     * @param email E-mail for the user
     * @param password Password for the user
     */
    private void signInUser(String email, String password) {
        // Check that internet connectivity exists
        if (!CheckNetwork.hasNetwork(this)) {
            Messages.showError(this, getString(R.string.error_no_internet), null);
            return;
        }

        fAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(result ->
                goToMain()
        ).addOnFailureListener(ex -> Messages.showError(this, ex.getLocalizedMessage(), null));
    }
}
