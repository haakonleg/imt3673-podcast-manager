package haakoleg.imt3673_podcast_manager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import haakoleg.imt3673_podcast_manager.models.User;
import haakoleg.imt3673_podcast_manager.utils.CheckNetwork;
import haakoleg.imt3673_podcast_manager.utils.Messages;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private DatabaseReference dbRef;

    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbRef = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();

        // Check if user is signed in and show main activity
        if (fAuth.getCurrentUser() != null) {
            goToMain();
        } else {

            // Not signed in, set view and find elements
            setContentView(R.layout.activity_login);
            emailInput = findViewById(R.id.email_input);
            passwordInput = findViewById(R.id.password_input);
            Button registerBtn = findViewById(R.id.create_user_btn);
            Button signInBtn = findViewById(R.id.sign_in_btn);

            // Set click listener for register button
            registerBtn.setOnClickListener(v -> {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                if (checkEmailPwd(email, password)) {
                    registerUser(email, password);
                }
            });

            // Set click listener for sign in button
            signInBtn.setOnClickListener(v -> {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                if (checkEmailPwd(email, password)) {
                    signInUser(email, password);
                }
            });
        }
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
    private void registerUser(String email, String password) {
        // Check that internet connectivity exists
        if (!CheckNetwork.hasNetwork(this)) {
            Messages.showError(this, getString(R.string.error_no_internet), null);
            return;
        }

        fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(result -> {
            FirebaseUser user = fAuth.getCurrentUser();

            // Set user display name
            // TODO: Don't use email
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(email)
                    .build();
            user.updateProfile(changeRequest);

            // Save user to database
            dbRef.child("users").child(user.getUid()).setValue(new User(user.getEmail(), user.getEmail()));
            goToMain();
        }).addOnFailureListener(ex -> {
            Messages.showError(this, ex.getLocalizedMessage(), null);
            Log.e("LoginActivity", Log.getStackTraceString(ex));
        });
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
        ).addOnFailureListener(ex -> {
            Messages.showError(this, ex.getLocalizedMessage(), null);
            Log.e("LoginActivity", Log.getStackTraceString(ex));
        });
    }
}
