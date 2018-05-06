package com.example.hasthi.nsbmtravelmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverLoginActivity extends AppCompatActivity {

    private static final String TAG = "DriverLoginActivity";
    private Button loginButton;
    private EditText emailEditText, passwordEditText;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();
        checkDriverLoggedIn();

        loginButton = findViewById(R.id.login_button);
        emailEditText = findViewById(R.id.driver_email);
        passwordEditText = findViewById(R.id.driver_password);
        progressDialog = new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();

                // Validation
                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is Empty");
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is Empty");
                }

                progressDialog.setMessage("Logging Driver");
                progressDialog.show();

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            onSigninFailed(task.getException().getMessage());
                        } else {
                            onSigninSuccess();
                        }
                    }
                });
            }
        });
    }

    private void checkDriverLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class));
        }
    }

    private void onSigninSuccess() {
        String key = mAuth.getCurrentUser().getUid();

        // Check User Type is Driver and Allows Login
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(key);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                Long userType = dataSnapshot.child("user_type").getValue(Long.class);
                onUserTypeReceived(userType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                onSigninFailed(databaseError.getMessage());
            }
        });
    }

    private void onSigninFailed(String message) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(DriverLoginActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

    // Check User Type
    private void onUserTypeReceived(long type) {
        if (type == 2) {
            finish();
            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class));
        } else {
            onSigninFailed("Invalid User");
        }
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
