package com.example.unicornrace.login;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unicornrace.Activities.MainActivity;
import com.example.unicornrace.R;
import com.google.firebase.auth.FirebaseAuth;


public class SignUpActivity extends AppCompatActivity {
    private EditText edtEmail,edtPassword1,edtPassword2;
    private Button btnSignUp;
    private TextView txtBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
        initListener();
        onclickBackUp();
    }
    private void initListener() {
        btnSignUp.setOnClickListener(view -> onClickSignUp());
    }
    private void onclickBackUp(){
        txtBack.setOnClickListener(view -> {
            Intent intent=new Intent(SignUpActivity.this,LoginActivity.class);
            startActivity(intent);
        });
    }
    private void onClickSignUp(){
        String email=edtEmail.getText().toString().trim();
        String password=edtPassword1.getText().toString().trim();
        String confirmPassword=edtPassword2.getText().toString().trim();
        ModelLogin modelLogin=new ModelLogin(email,password);
         FirebaseAuth mAuth=FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        if(modelLogin.isValidEmail()&&modelLogin.isValidPassword()) {
                            if (password.equals(confirmPassword)) {
                                Toast.makeText(SignUpActivity.this, "Đăng ký thành công.",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(SignUpActivity.this, "Mật khẩu không đúng.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Đăng ký thất bại.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initUI() {
        edtEmail=findViewById(R.id.signUpEmail);
        edtPassword1=findViewById(R.id.signUpPassword);
        edtPassword2=findViewById(R.id.signUpConfirm);
        btnSignUp=findViewById(R.id.signUpButton);
        txtBack=findViewById(R.id.backToSignIn);
    }
}