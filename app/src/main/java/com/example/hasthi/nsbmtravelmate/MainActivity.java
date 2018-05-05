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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button SignUpButton;
    private TextView LoginPageLink;
    private TextView DriverPageLink;
    private EditText UserEmail;
    private EditText UserPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    public User user = User.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){

            user.setUserID(firebaseAuth.getCurrentUser().getUid());
            DatabaseReference current_user = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid());

            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        user = dataSnapshot.getValue(User.class);
                        if (user.getUserType() == 2) {
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
        LoginPageLink = findViewById(R.id.login_page_link);
        LoginPageLink.setOnClickListener(this);
        SignUpButton = findViewById(R.id.sign_up_button);
        SignUpButton.setOnClickListener(this);
        UserEmail = findViewById(R.id.user_email);
        UserPassword = findViewById(R.id.user_password);
        progressDialog = new ProgressDialog(this);
        DriverPageLink = findViewById(R.id.driver_login_page_link);
        DriverPageLink.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {

        if(view == SignUpButton){

            registerUser();

        }
        if (view == LoginPageLink){

            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

        }
        if (view == DriverPageLink){
            startActivity(new Intent(getApplicationContext(), DriverLoginActivity.class));
        }

    }

    private void registerUser() {

        String email = UserEmail.getText().toString().trim();
        String password = UserPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            UserEmail.setError("Email is Empty");
            return;
        } else {
            user.setEmail(email);
        }

        if(TextUtils.isEmpty(password)){
            UserPassword.setError("Password is Empty");
            return;
        }

        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        try {

                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "User registerd" + task.getResult().toString(), Toast.LENGTH_SHORT).show();

                            user.setUserID(firebaseAuth.getCurrentUser().getUid());
                            user.setUserType(2);
                            DatabaseReference current_student_db = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUserID());
                            current_student_db.setValue(user);
                            finish();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "User registration failed" + task.getResult().toString(), Toast.LENGTH_SHORT).show();
                        }

                        throw task.getException();

                        } catch(Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

    }
}
