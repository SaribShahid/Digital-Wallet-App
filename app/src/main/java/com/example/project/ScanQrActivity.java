package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanQrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR to Pay");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true); // 🔒 LOCK
        options.setCaptureActivity(CaptureActivityPortrait.class); // ✅ KEY LINE

        registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() == null) {
                        finish();
                        return;
                    }
                    handleResult(result.getContents());
                }
        ).launch(options);
    }

    private void handleResult(String phone) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(q -> {

                    if (q.isEmpty()) {
                        Toast.makeText(this,
                                "Account not found",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    String receiverId = q.getDocuments().get(0).getId();

                    Intent i = new Intent(this, SendMoney.class);
                    i.putExtra("receiverId", receiverId);
                    i.putExtra("fromQr", true);
                    startActivity(i);
                    finish();
                });
    }
}
