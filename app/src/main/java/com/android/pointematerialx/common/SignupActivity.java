package com.android.pointematerialx.common;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.pointematerialx.R;

import com.android.pointematerialx.utils.PasswordChecker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private long backPressedTime;
    private FirebaseAuth mAuth;
    private ConstraintLayout signlayout;

    TextInputLayout signupemail, signuppass, confpass;
    Button signup, calllogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signup = findViewById(R.id.btn_signup);
        calllogin = findViewById(R.id.btn_calllogin);

        signupemail = findViewById(R.id.sign_email);
        signuppass = findViewById(R.id.signup_pass);
        confpass = findViewById(R.id.signup_confpass);
        signlayout = findViewById(R.id.signup_layout);

        mAuth = FirebaseAuth.getInstance();

        calllogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        signup.setOnClickListener(v -> {
            CloseKeyboard();
            CreatUser();
        });
    }

    private void CreatUser() {
        String umail = signupemail.getEditText().getText().toString().trim();
        String upass = signuppass.getEditText().getText().toString().trim();

        if (validEmail() & validPass() & validConPass()) {
            mAuth.createUserWithEmailAndPassword(umail, upass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendVerifyMail();
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Snackbar.make(signlayout, "Email is already registered.", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void sendVerifyMail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String uid = firebaseUser.getUid();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                Snackbar.make(signlayout, "Verification mail have been send to " + firebaseUser.getEmail(), Snackbar.LENGTH_INDEFINITE).setAction("Ok", v -> {
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    mAuth.signOut();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }).show();
            });
        } else {
            Snackbar.make(signlayout, "Failed to send verification mail.", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean validEmail() {
        String val = signupemail.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            signupemail.setError("Field can't be empty.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
            signupemail.setError("Invalid E-mail address.");
            return false;
        } else {
            signupemail.setError(null);
            signupemail.setErrorEnabled(false);
            return true;
        }

    }

    private boolean validPass() {
        String val = signuppass.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            signuppass.setError("Field can't be empty.");
            return false;
        } else if (!PasswordChecker.PASSWORD_PATTERN.matcher(val).matches()) {
            signuppass.setError("Password doesn't match the REGEX Format.");
            return false;
        } else if (!(val.length() > 8)) {
            signuppass.setError("Password must contain minimum of 8 characters.");
            return false;
        } else {
            signuppass.setError(null);
            signuppass.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validConPass() {
        String pass = signuppass.getEditText().getText().toString().trim();
        String cpass = confpass.getEditText().getText().toString().trim();

        if (cpass.isEmpty()) {
            confpass.setError("Field can't be empty.");
            return false;
        } else if (!pass.equals(cpass)) {
            confpass.setError("Password do not match.");
            return false;
        } else {
            confpass.setError(null);
            confpass.setErrorEnabled(false);
            return true;
        }
    }

    private void CloseKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            methodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        }
        backPressedTime = System.currentTimeMillis();

    }
}

