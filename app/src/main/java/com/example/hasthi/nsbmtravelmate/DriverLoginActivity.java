package com.example.hasthi.nsbmtravelmate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DriverLoginActivity extends AppCompatActivity {

    private static final String TAG = "DriverLoginActivity";
    private Button loginButton;
    private EditText emailEditText, passwordEditText;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();
        checkDriverLoggedIn();

        loginButton = findViewById(R.id.login_button);
        emailEditText = findViewById(R.id.driver_email);
        passwordEditText = findViewById(R.id.driver_password);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Validation
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.wtf(TAG,"Login Failed");
                        } else {
                            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class).putExtra("DRIVER_KEY", mAuth.getCurrentUser().getUid()));
                        }
                    }
                });
            }
        });
    }

    private void checkDriverLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class).putExtra("DRIVER_KEY", user.getUid()));
        }
//        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    startActivity(new Intent(getApplicationContext(), DriverMapActivity.class).putExtra("DRIVER_KEY", user.getUid()));
//                    finish();
//                }
//            }
//        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
