package com.company.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginAcitivity extends AppCompatActivity {

    TextInputEditText editTextEmail,editTextPassword;
    Button buttonSignIn,buttonSignUp;
    TextView textViewForgotPassword;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            Intent intent = new Intent(LoginAcitivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextemail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.button2);
        textViewForgotPassword = findViewById(R.id.textViewForgotPass);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userEmail = editTextEmail.getText().toString();
                String userPassword = editTextPassword.getText().toString();
                if(userEmail.equals("") || userPassword.equals(""))
                {
                    Toast.makeText(LoginAcitivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
                else {
                    signIn(userEmail,userPassword);
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginAcitivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginAcitivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    public void signIn(String email,String password)
    {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    Intent intent = new Intent(LoginAcitivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(LoginAcitivity.this, "Login successfull !", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    Toast.makeText(LoginAcitivity.this, "There was an error while trying to login. Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}