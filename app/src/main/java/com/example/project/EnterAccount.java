package com.example.project;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EnterAccount extends AppCompatActivity {

    // UI
    EditText etAccount;
    TextView tvAmount;
    MaterialButton btnSend;

    // Data
    double amount;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_account);

        // ✅ INIT FIRESTORE FIRST (VERY IMPORTANT)
        db = FirebaseFirestore.getInstance();

        // UI BINDING
        etAccount = findViewById(R.id.etAccount);
        tvAmount = findViewById(R.id.tvAmount);
        btnSend = findViewById(R.id.btnNext);

        // GET AMOUNT
        amount = getIntent().getDoubleExtra("amount", 0);
        tvAmount.setText("Rs. " + String.format("%,.0f", amount));

        // QR FLOW DATA
        String receiverId = getIntent().getStringExtra("receiverId");
        boolean fromQr = getIntent().getBooleanExtra("fromQr", false);

        // ✅ IF FROM QR → DIRECT TRANSFER
        if (fromQr && receiverId != null) {
            transferMoney(receiverId);
            return;
        }

        // NORMAL FLOW
        btnSend.setOnClickListener(v -> verifyAndSend());
    }

    // ================= VERIFY ACCOUNT =================
    private void verifyAndSend() {

        String input = etAccount.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "Enter account number", Toast.LENGTH_SHORT).show();
            return;
        }

        String normalized = normalizeNumber(input);

        db.collection("users")
                .whereEqualTo("phone", normalized)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this,
                                "Account not available",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String receiverId = query.getDocuments().get(0).getId();
                        transferMoney(receiverId);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to verify account",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ================= TRANSFER MONEY =================
    private void transferMoney(String receiverId) {

        String senderId = FirebaseAuth.getInstance().getUid();
        if (senderId == null) return;

        // ❌ Prevent sending to self
        if (senderId.equals(receiverId)) {
            Toast.makeText(this,
                    "You cannot send money to yourself",
                    Toast.LENGTH_LONG).show();
            return;
        }

        DocumentReference senderRef = db.collection("users").document(senderId);
        DocumentReference receiverRef = db.collection("users").document(receiverId);

        db.runTransaction(transaction -> {

                    DocumentSnapshot senderSnap = transaction.get(senderRef);
                    DocumentSnapshot receiverSnap = transaction.get(receiverRef);

                    double senderBalance = senderSnap.getDouble("balance") != null
                            ? senderSnap.getDouble("balance") : 0;

                    double receiverBalance = receiverSnap.getDouble("balance") != null
                            ? receiverSnap.getDouble("balance") : 0;

                    if (senderBalance < amount) {
                        throw new RuntimeException("Insufficient balance");
                    }

                    String senderName = senderSnap.getString("name");
                    String receiverName = receiverSnap.getString("name");

                    long time = System.currentTimeMillis();

                    // 💰 UPDATE BALANCES
                    transaction.update(senderRef, "balance", senderBalance - amount);
                    transaction.update(receiverRef, "balance", receiverBalance + amount);

                    // 📩 RECEIVER MESSAGE
                    transaction.set(
                            receiverRef.collection("messages").document(),
                            new MessageModel(
                                    "Money Received",
                                    "Rs. " + (int) amount + " received from " + senderName,
                                    time
                            )
                    );


                    // 📩 SENDER MESSAGE
                    transaction.set(
                            senderRef.collection("messages").document(),
                            new MessageModel(
                                    "Money Sent",
                                    "You sent Rs. " + (int) amount + " to " + receiverName,
                                    time
                            )
                    );


                    // 📜 TRANSACTIONS
                    transaction.set(
                            senderRef.collection("transactions").document(),
                            new TransactionModel("sent", amount, receiverName, time)
                    );

                    transaction.set(
                            receiverRef.collection("transactions").document(),
                            new TransactionModel("received", amount, senderName, time)
                    );

                    return null;
                })
                .addOnSuccessListener(unused -> showSuccessDialog())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // ================= SUCCESS POPUP =================
    private void showSuccessDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_transfer_success)
                .setCancelable(false)
                .create();

        dialog.show();

        dialog.findViewById(R.id.btnDone)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    finish();
                });
    }

    // ================= NORMALIZE NUMBER =================
    private String normalizeNumber(String phone) {

        phone = phone.replace(" ", "");

        if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }

        if (phone.startsWith("+92")) {
            phone = phone.substring(3);
        }

        return "+92" + phone;
    }
}
