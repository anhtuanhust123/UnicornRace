package com.example.unicornrace.login;

import android.text.TextUtils;
import android.util.Patterns;

public class ModelLogin {
    private String email;
    private String password;

    public ModelLogin(String email,String password) {
        this.email = email;
        this.password=password;
    }
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isValidEmail(){
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public boolean isValidPassword(){
        return !TextUtils.isEmpty(password) && password.length()>=7;
    }
}
