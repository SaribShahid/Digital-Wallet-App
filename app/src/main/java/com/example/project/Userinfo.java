package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Userinfo extends AppCompatActivity {

    EditText etName, etEmail, etAge, etCnic, etPassword;
    MaterialButton btnContinue;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etCnic = findViewById(R.id.etCnic);
        etPassword = findViewById(R.id.etPassword);
        btnContinue = findViewById(R.id.btnContinue);

        phoneNumber = getIntent().getStringExtra("phoneNumber");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnContinue.setOnClickListener(v -> saveAndLinkUser());
    }

    private void saveAndLinkUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String cnic = etCnic.getText().toString().trim();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(cnic)) {

            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔐 LINK EMAIL + PASSWORD TO PHONE USER
        AuthCredential emailCredential =
                EmailAuthProvider.getCredential(email, password);

        user.linkWithCredential(emailCredential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveProfileToFirestore(user, name, email, age, cnic);
                    } else {
                        Toast.makeText(this,
                                "Email already in use",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveProfileToFirestore(FirebaseUser user,
                                        String name,
                                        String email,
                                        String age,
                                        String cnic) {

        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("name", name);
        data.put("email", email);
        data.put("age", age);
        data.put("cnic", cnic);
        data.put("phone", phoneNumber);
        data.put("balance",10000);
        data.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getUid())
                .set(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Profile created successfully",
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
