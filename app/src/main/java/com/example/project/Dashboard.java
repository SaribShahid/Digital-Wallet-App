package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dashboard extends AppCompatActivity {

    // UI
    DrawerLayout drawerLayout;
    TextView tvProfile, btnAccountDetails, btnLogout;
    ImageView ivBell, ivQr;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI refs
        drawerLayout = findViewById(R.id.drawerLayout);
        tvProfile = findViewById(R.id.tvProfile);
        btnAccountDetails = findViewById(R.id.btnAccountDetails);
        btnLogout = findViewById(R.id.btnLogout);
        ivBell = findViewById(R.id.ivBell);
        ivQr = findViewById(R.id.ivQr);


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Load default fragment
        loadFragment(new Home());

        // Bottom navigation logic
        bottomNav.setOnItemSelectedListener(item -> {

            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_home) {
                fragment = new Home();
            } else if (item.getItemId() == R.id.nav_message) {
                fragment = new messages();
            } else if (item.getItemId() == R.id.nav_card) {
                fragment = new CardFragment();
            } else if (item.getItemId() == R.id.nav_transaction) {
                fragment = new transaction_fragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });

        // Load profile initials
        loadUserInitials();

        // Open drawer on profile click
        tvProfile.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // Account details
        btnAccountDetails.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, accountdetail.class));
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // QR → scan and pay
        ivQr.setOnClickListener(v ->
                startActivity(new Intent(this, ScanQrActivity.class))
        );


        // Bell → notifications page
        ivBell.setOnClickListener(v ->
                startActivity(new Intent(this, notifications.class))
        );
    }

    // ================= FRAGMENT LOADER =================

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // ================= PROFILE INITIALS =================

    private void loadUserInitials() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    String name = snapshot.getString("name");
                    if (name == null || name.isEmpty()) return;

                    String[] parts = name.trim().split(" ");
                    String initials;

                    if (parts.length >= 2) {
                        initials = "" + parts[0].charAt(0) + parts[1].charAt(0);
                    } else {
                        initials = "" + parts[0].charAt(0);
                    }

                    tvProfile.setText(initials.toUpperCase());
                });
    }
}
