package com.example.project;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class accountdetail extends AppCompatActivity {

    TextView tvName, tvEmail, tvPhone, tvAge, tvCnic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountdetail);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAge = findViewById(R.id.tvAge);
        tvCnic = findViewById(R.id.tvCnic);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    tvName.setText("Name: " + snapshot.getString("name"));
                    tvEmail.setText("Email: " + snapshot.getString("email"));
                    tvPhone.setText("Phone: " + snapshot.getString("phone"));
                    tvAge.setText("Age: " + snapshot.getString("age"));
                    tvCnic.setText("CNIC: " + snapshot.getString("cnic"));
                });
    }
}
