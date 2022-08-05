package com.example.unicornrace.login;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unicornrace.Activities.MainActivity;
import com.example.unicornrace.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {
    private TextView textViewSignUp;
    private Button btnSignIn;
    private EditText edtEmail, edtPassword;
    private TextView textView;
    private static final String TAG = LoginActivity.class.getSimpleName();
    CallbackManager callbackManager;
    LoginButton fbLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        initUi();
        signIn();
        initListener();
        FacebookSdk.getSdkVersion();
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton =  findViewById(R.id.login_button);
        fbLoginButton.setReadPermissions("email");
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "======Facebook login success======");
                Log.d(TAG, "Facebook Access Token: " + loginResult.getAccessToken().getToken());
                Toast.makeText(LoginActivity.this, "Login Facebook success.", Toast.LENGTH_SHORT).show();

                getFbInfo();
            }


            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login Facebook cancelled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "======Facebook login error======");
                Log.e(TAG, "Error: " + error.toString());
                Toast.makeText(LoginActivity.this, "Login Facebook error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signIn() {
        btnSignIn.setOnClickListener(view -> onClickSignIn());
    }


    private void initListener() {
        textViewSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    @SuppressLint("SetTextI18n")
    private void onClickSignIn() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        ModelLogin modelLogin = new ModelLogin(email, password);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Sign in success
                    if (task.isSuccessful()) {
                        textView.setVisibility(View.VISIBLE);
                        if (modelLogin.isValidEmail() && modelLogin.isValidPassword()) {
                            textView.setText("Đăng nhập thành công");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại.",
                                Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void initUi() {
        btnSignIn = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.inputEmail);
        edtPassword = findViewById(R.id.inputPassword);
        textView = findViewById(R.id.textView0);
        textViewSignUp=findViewById(R.id.signUpText);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getFbInfo() {
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                    (me, response) -> {
                        if (me != null) {
                            Log.i("Login: ", me.optString("name"));
                            Log.i("ID: ", me.optString("id"));

                            Toast.makeText(LoginActivity.this, "Name: " + me.optString("name"), Toast.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this, "ID: " + me.optString("id"), Toast.LENGTH_SHORT).show();
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }
}


