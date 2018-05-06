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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import models.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private Button LoginButton;
    private TextView SignUpPageLink;
    private EditText UserEmail;
    private EditText UserPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    public User user = User.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){

            user.setUserID(firebaseAuth.getCurrentUser().getUid());
            DatabaseReference current_user = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid());

            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        user = dataSnapshot.getValue(User.class);
                        if (user.getUserType() == 3) {
                            finish();
                            Intent intent = new Intent(getApplicationContext(), BusLocationsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            current_user.addValueEventListener(postListener);

        }

        SignUpPageLink = findViewById(R.id.sign_up_page_link);
        SignUpPageLink.setOnClickListener(this);
        LoginButton = findViewById(R.id.login_button);
        LoginButton.setOnClickListener(this);
        UserEmail = findViewById(R.id.user_email);
        UserPassword = findViewById(R.id.user_password);
        progressDialog = new ProgressDialog(this);

    }

    @Override
    public void onClick(View view) {

        if(view == LoginButton){

            registerUser();

        }

        if (view == SignUpPageLink){

            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        }

    }

    private void registerUser() {

        String email = UserEmail.getText().toString().trim();
        String password = UserPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            UserEmail.setError("Email is Empty");
            return;
        }

        if(TextUtils.isEmpty(password)){
            UserPassword.setError("Password is Empty");
            return;
        }

        progressDialog.setMessage("Logging In User...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        try {
                            if (task.isSuccessful()) {
                                onSigninSuccess();
                            }

                            throw task.getException();
                        }

                        catch(Exception e) {
//                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
//                            Log.e(TAG, e.getMessage());
                        }

                    }
                });

    }

    private void onSigninSuccess() {
        String key = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(key);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                if(dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                    if (user.getUserType() == 3) {
                        finish();
                        Intent intent = new Intent(getApplicationContext(), BusLocationsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();

                onSigninFailed("Wrong user type");
            }
        });
    }
    private void onSigninFailed(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }

}
