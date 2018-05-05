package com.example.hasthi.nsbmtravelmate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
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
            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class).putExtra("DRIVER_KEY", user.getUid()));
        }
    }

    private void onSigninSuccess() {
        String key = mAuth.getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(key);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long userType = dataSnapshot.child("user_type").getValue(Long.class);
                onUserTypeReceived(userType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onSigninFailed(databaseError.getMessage());
            }
        });
    }

    private void onSigninFailed(String message) {
        Toast.makeText(DriverLoginActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

    private void onUserTypeReceived(long type) {
        if (type == 2) {
            finish();
            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class).putExtra("DRIVER_KEY", mAuth.getCurrentUser().getUid()));
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
