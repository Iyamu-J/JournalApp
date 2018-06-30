package ayevbeosa.com.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mFirebaseAuth;

    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton mSignInButton;

    private TextView mProfileNameTextView;
    private TextView mProfileEmailTextView;
    private ImageView mProfilePictureImageView;
    private ProgressBar mProgressBar;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // textviews
        mProfileNameTextView = findViewById(R.id.tv_profile_name);
        mProfileEmailTextView = findViewById(R.id.tv_profile_email);

        // button listeners
        mSignInButton = findViewById(R.id.btn_sign_in);
        mSignInButton.setOnClickListener(this);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.btn_sign_out).setOnClickListener(this);
        findViewById(R.id.btn_disconnect).setOnClickListener(this);

        mProgressBar = findViewById(R.id.pb_sign_in);

        mProfilePictureImageView = findViewById(R.id.imageViewProfilePic);

        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_sign_in:
                signIn();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
            case R.id.btn_disconnect:
                revokeAccess();
                break;
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        mProgressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.sign_in_layout), "Sign In Failed", Snackbar.LENGTH_SHORT)
                                    .show();
                            updateUI(null);
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (user != null) {
            mProfileNameTextView.setText(user.getDisplayName());
            mProfileEmailTextView.setText(user.getEmail());
            mProfilePictureImageView.setImageURI(user.getPhotoUrl());

            mSignInButton.setVisibility(View.GONE);
            findViewById(R.id.btn_sign_out).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_disconnect).setVisibility(View.VISIBLE);
            if (mSnackbar == null) {
                mSnackbar = Snackbar.make(findViewById(R.id.sign_in_layout), "Signed In Successfully", Snackbar.LENGTH_SHORT);
                mSnackbar.show();
            }
            mSnackbar = null;

        } else {
            mSignInButton.setVisibility(View.VISIBLE);
            findViewById(R.id.btn_sign_out).setVisibility(View.GONE);
            findViewById(R.id.btn_disconnect).setVisibility(View.GONE);
        }
    }

    private void signIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void signOut() {
        mFirebaseAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }

    private void revokeAccess() {
        mFirebaseAuth.signOut();

        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }
}
