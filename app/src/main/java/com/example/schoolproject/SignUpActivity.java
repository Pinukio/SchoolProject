package com.example.schoolproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText email, password;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.Signup_email);
        password = findViewById(R.id.SignUp_password);

        Button join = findViewById(R.id.SignUp_Btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        dialog = builder.create();

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                if(emailText.isEmpty() || !emailText.contains("@")) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(passwordText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    dialog.show();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(emailText, passwordText)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        dialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("id", emailText);
                                        db.collection("users").document(emailText).set(user);
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                    else {
                                        dialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }
}
