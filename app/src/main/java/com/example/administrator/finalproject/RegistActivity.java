package com.example.administrator.finalproject;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Pwd;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        Email = findViewById(R.id.et_em);
        Pwd = findViewById(R.id.et_pass);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("가입중...");
    }


    public void regist_bt(View view) {
        String email = Email.getText().toString();
        String pwd = Pwd.getText().toString();

        createAccount(email,pwd);
    }


    private void createAccount(String email, String pwd) {

        progressDialog.show();

        MapsActivity.getmAuth().createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(RegistActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegistActivity.this, "가입 완료", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(RegistActivity.this, "가입 실패실패실패", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.hide();
                    }
                });
    }
}
