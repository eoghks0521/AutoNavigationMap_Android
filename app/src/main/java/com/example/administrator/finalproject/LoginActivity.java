package com.example.administrator.finalproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Email = findViewById(R.id.et_email);
        Pwd = findViewById(R.id.et_pwd);

    }

    public void login_bt(View view) {
        String email = Email.getText().toString();
        String pwd = Pwd.getText().toString();

        MapsActivity.getmAuth().signInWithEmailAndPassword(email,pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,"로그인 성공",Toast.LENGTH_LONG).show();
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this,"다시다시다시",Toast.LENGTH_LONG).show();
                            Email.setText("");
                            Pwd.setText("");
                        }
                    }
                });
    }

    public void regist_bt(View view) {
        Intent intent = new Intent(this,RegistActivity.class);
        startActivity(intent);
    }
}
