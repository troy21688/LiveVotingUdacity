package com.troychuinard.livevotingudacity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.troychuinard.livevotingudacity.Fragment.DisplayNameDialogFragment;


import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

import static android.os.Build.ID;


public class SignupActivity extends AppCompatActivity implements DisplayNameDialogFragment.EditNameDialogListener{

    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_signup)
    Button signupButton;
    @BindView(R.id.btn_signin)
    Button signInButton;



    private static final String FIREBASE_URL = "https://fan-polls.firebaseio.com/";
    private DatabaseReference mUserRef;
    private String mNewFirebaseUserEmail;
    private String mNewFirebaseUserPassword;
    private boolean isLoggedIn;
    private DatabaseReference mBaseRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBaseRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mBaseRef.child("Users");
        mAuth = FirebaseAuth.getInstance();




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    isLoggedIn = true;
                    if (user.getDisplayName() == null) {
                        //TODO Prompt Dialog for displayName Edit Text;
                        DialogFragment f = new DisplayNameDialogFragment();
                        f.show(getSupportFragmentManager(), "DisplayNameDialogFragment");
                    } else {

                        Intent toHomeActivity = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(toHomeActivity);
                        // User is fsigned in

                    }

                } else {
                    setContentView(R.layout.activity_signup);
                    ButterKnife.bind(SignupActivity.this);
                }

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @OnClick({R.id.btn_signup, R.id.btn_signin})
    public void onItemClicked(View v) {
        switch (v.getId()) {
            case R.id.btn_signup:
                signup();
                break;
            case R.id.btn_signin:
                signIn();
                break;
        }
    }


    public void signup() {
        boolean hasNotEnteredData = checkCredentials();
        if (hasNotEnteredData) {
            return;
        }

        if (!validate()) {
            onSignupFailed();
            return;
        }


        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getResources().getString(R.string.creating_account_delay));
        progressDialog.show();

        mNewFirebaseUserEmail = _emailText.getText().toString();
        mNewFirebaseUserPassword = _passwordText.getText().toString();

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success

                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    public void signIn() {

        boolean hasNotEnteredData = checkCredentials();
        if (hasNotEnteredData) {
            return;
        }


        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getResources().getString(R.string.sign_in_delay));
        progressDialog.show();

        mNewFirebaseUserEmail = _emailText.getText().toString();
        mNewFirebaseUserPassword = _passwordText.getText().toString();


        signInButton.setEnabled(true);
        signupButton.setEnabled(true);
        mAuth.signInWithEmailAndPassword(mNewFirebaseUserEmail, mNewFirebaseUserPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), R.string.no_user_found, Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getApplicationContext(), (CharSequence) task.getException(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
        progressDialog.dismiss();
    }

    private boolean checkCredentials() {
        if ((isNull(_emailText.getText().toString()) || isNull(_passwordText.getText().toString())) || (isEmpty(_emailText.getText().toString()) || isEmpty(_passwordText.getText().toString()))) {
            Toast.makeText(getApplicationContext(), R.string.enter_user_password, Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        //not sure why this line is here
        setResult(RESULT_OK, null);
        //this line below actually creates the user in Firebase; does not write or save to any locations
        //this line below actually creates the user in Firebase; does not write or save to any locations
        mAuth.createUserWithEmailAndPassword(mNewFirebaseUserEmail, mNewFirebaseUserPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, getResources().getString(R.string.authentication_failed_email),
                                    Toast.LENGTH_SHORT).show();
                        }

                        FirebaseAuth loggedInAuth = FirebaseAuth.getInstance();
                    }
                });
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
        signInButton.setEnabled(true);

    }


    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getResources().getString(R.string.valid_email));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 15) {
            _passwordText.setError(getResources().getString(R.string.alphanumeric));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public boolean isNull(String item) {
        return item == null;
    }

    public boolean isEmpty(String item) {
        return item.isEmpty();
    }


    @Override
    public void onFinishEditDialog(String inputText) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        UserProfileChangeRequest m = new UserProfileChangeRequest.Builder()
                .setDisplayName(inputText).build();
        user.updateProfile(m).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), user.getDisplayName().toString(), Toast.LENGTH_LONG).show();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("Display_Name", user.getDisplayName());
                userMap.put("ID", user.getUid());
                userMap.put("Provider", user.getProviders());
                mUserRef.child(user.getUid()).updateChildren(userMap);
            }
        });

        Intent toHome = new Intent(SignupActivity.this, HomeActivity.class);
        startActivity(toHome);
    }
}