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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button SignUpButton;
    private TextView LoginPageLink;
    private EditText UserEmail;
    private EditText UserPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), BusFinderActivity.class));

        }
        LoginPageLink = findViewById(R.id.login_page_link);
        LoginPageLink.setOnClickListener(this);
        SignUpButton = findViewById(R.id.sign_up_button);
        SignUpButton.setOnClickListener(this);
        UserEmail = findViewById(R.id.user_email);
        UserPassword = findViewById(R.id.user_password);
        progressDialog = new ProgressDialog(this);



    }

    @Override
    public void onClick(View view) {

        if(view == SignUpButton){

            registerUser();

        }
        if (view == LoginPageLink){

            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

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
