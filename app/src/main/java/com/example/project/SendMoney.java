package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SendMoney extends AppCompatActivity {

    // UI
    TextView tvAmount, tvCurrentBalance;
    ImageView btnDelete;
    MaterialButton btnNext;

    // State
    StringBuilder amountBuilder = new StringBuilder();
    double currentBalance = 0;

    // QR-related
    String receiverId = null;
    boolean fromQr = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        // Bind UI
        tvAmount = findViewById(R.id.tvAmount);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        btnDelete = findViewById(R.id.btnDelete);
        btnNext = findViewById(R.id.btnSend);

        // 🔥 Get QR data (if any)
        receiverId = getIntent().getStringExtra("receiverId");
        fromQr = getIntent().getBooleanExtra("fromQr", false);

        fetchBalance();
        setupKeypad();

        btnNext.setOnClickListener(v -> proceedNext());
    }

    // ================= BALANCE =================

    private void fetchBalance() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            tvCurrentBalance.setText("Available: Rs. 0");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    Double bal = doc.getDouble("balance");
                    currentBalance = bal != null ? bal : 0;
                    tvCurrentBalance.setText(
                            "Available: Rs. " + String.format("%,.0f", currentBalance)
                    );
                });
    }

    // ================= KEYPAD =================

    private void setupKeypad() {

        int[] keys = {
                R.id.keypad1, R.id.keypad2, R.id.keypad3,
                R.id.keypad4, R.id.keypad5, R.id.keypad6,
                R.id.keypad7, R.id.keypad8, R.id.keypad9,
                R.id.keypad0
        };

        for (int id : keys) {
            TextView key = findViewById(id);
            if (key != null) {
                key.setOnClickListener(v -> {
                    if (amountBuilder.length() < 7) {
                        amountBuilder.append(key.getText());
                        tvAmount.setText("Rs. " + amountBuilder);
                    }
                });
            }
        }

        btnDelete.setOnClickListener(v -> {
            if (amountBuilder.length() > 0) {
                amountBuilder.deleteCharAt(amountBuilder.length() - 1);
                tvAmount.setText(
                        "Rs. " + (amountBuilder.length() == 0 ? "0" : amountBuilder)
                );
            }
        });
    }

    // ================= NEXT =================

    private void proceedNext() {

        if (amountBuilder.length() == 0) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountBuilder.toString());

        if (amount > currentBalance) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 QR FLOW → SKIP ENTER ACCOUNT
        if (fromQr && receiverId != null) {

            Intent intent = new Intent(this, EnterAccount.class);
            intent.putExtra("amount", amount);
            intent.putExtra("receiverId", receiverId);
            intent.putExtra("fromQr", true);
            startActivity(intent);
            return;
        }

        // 🔁 NORMAL FLOW
        Intent intent = new Intent(this, EnterAccount.class);
        intent.putExtra("amount", amount);
        startActivity(intent);
    }
}
