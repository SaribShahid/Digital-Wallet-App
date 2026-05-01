package com.example.project;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QuickPayActivity extends AppCompatActivity {

    TextView tvUserName, tvUserEmail;
    ImageView imgQr;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_pay);

        // 🔗 Bind UI
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        imgQr = findViewById(R.id.imgQr);

        // 🔥 Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fetchUserData();
    }

    // ================= FETCH USER =================
    private void fetchUserData() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) return;

                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    String phone = snapshot.getString("phone"); // 🔥 FOR QR

                    if (name != null) tvUserName.setText(name);
                    if (email != null) tvUserEmail.setText(email);

                    if (phone != null && !phone.isEmpty()) {
                        generateQr(phone);
                    } else {
                        Toast.makeText(this,
                                "Phone number not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load user info",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ================= GENERATE QR =================
    private void generateQr(String data) {

        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(
                    data,
                    BarcodeFormat.QR_CODE,
                    600,
                    600
            );

            imgQr.setImageBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(this,
                    "Failed to generate QR",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
