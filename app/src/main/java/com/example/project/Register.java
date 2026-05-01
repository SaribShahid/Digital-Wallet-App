package com.example.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {

    EditText etPhone;
    MaterialButton btnRegister;

    FirebaseAuth mAuth;
    String verificationId;
    String fullPhone; // ✅ STORE PHONE

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> {

            String phone = etPhone.getText().toString().trim();

            if (phone.length() != 10) {
                Toast.makeText(this, "Enter valid 10-digit number", Toast.LENGTH_SHORT).show();
                return;
            }

            fullPhone = "+92" + phone;
            sendOtp(fullPhone);
        });
    }

    private void sendOtp(String phoneNumber) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {}

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(Register.this,
                            "OTP Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String id,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    verificationId = id;

                    Intent intent = new Intent(Register.this, otp.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phoneNumber", fullPhone); // ✅ PASS PHONE
                    startActivity(intent);
                }
            };
}
