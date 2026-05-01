package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class otp extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    MaterialButton btnVerify;

    FirebaseAuth mAuth;
    String verificationId;
    String phoneNumber; // ✅ RECEIVE PHONE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();

        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber"); // ✅

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerify = findViewById(R.id.btnVerify);

        setupOtpInputs();

        btnVerify.setOnClickListener(v -> {
            String otpCode =
                    otp1.getText().toString() +
                            otp2.getText().toString() +
                            otp3.getText().toString() +
                            otp4.getText().toString() +
                            otp5.getText().toString() +
                            otp6.getText().toString();

            if (otpCode.length() != 6) {
                Toast.makeText(this, "Enter valid OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            verifyOtp(otpCode);
        });
    }

    private void verifyOtp(String code) {

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Intent intent = new Intent(otp.this, Userinfo.class);
                        intent.putExtra("phoneNumber", phoneNumber); // ✅ PASS AGAIN
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this,
                                "OTP verification failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupOtpInputs() {
        otp1.addTextChangedListener(new OtpTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OtpTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OtpTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OtpTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(new OtpTextWatcher(otp5, otp6));
    }

    private class OtpTextWatcher implements TextWatcher {
        EditText current, next;
        OtpTextWatcher(EditText current, EditText next) {
            this.current = current;
            this.next = next;
        }
        @Override public void afterTextChanged(Editable s) {
            if (s.length() == 1 && next != null) next.requestFocus();
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
