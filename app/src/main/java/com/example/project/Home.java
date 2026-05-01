package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class Home extends Fragment {

    // UI
    TextView avbal, tvGreeting;
    ImageView btnHideBalance;

    LinearLayout btnAddMoney, btnSendMoney, btnMoreActions;

    View btnQuickPay, btnBills, btnMerchants, btnTopup, btnSplit, btnGift;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    boolean isBalanceHidden = false;
    double cachedBalance = 0.0;

    public Home() {}

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Bind UI
        tvGreeting = view.findViewById(R.id.tvGreeting);
        avbal = view.findViewById(R.id.avbalance);
        btnHideBalance = view.findViewById(R.id.btnHideBalance);

        btnAddMoney = view.findViewById(R.id.btnAddMoney);
        btnSendMoney = view.findViewById(R.id.btnSendMoney);
        btnMoreActions = view.findViewById(R.id.btnMoreActions);

        btnQuickPay = view.findViewById(R.id.btnQuickPay);
        btnBills = view.findViewById(R.id.btnBills);
        btnMerchants = view.findViewById(R.id.btnMerchants);
        btnTopup = view.findViewById(R.id.btnTopup);
        btnSplit = view.findViewById(R.id.btnSplit);
        btnGift = view.findViewById(R.id.btnGift);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fetchUserName();
        fetchBalance();

        btnHideBalance.setOnClickListener(v -> toggleBalance());

        btnAddMoney.setOnClickListener(v ->
                openActivity(AddMoney.class));

        btnSendMoney.setOnClickListener(v ->
                openActivity(SendMoney.class));

        // ✅ ONLY CHANGE IS HERE
        btnMoreActions.setOnClickListener(v -> showMoreBottomSheet());

        // Grid (unchanged)
        btnQuickPay.setOnClickListener(v -> openActivity(QuickPayActivity.class));
        btnBills.setOnClickListener(v -> openActivity(BillsActivity.class));
        btnMerchants.setOnClickListener(v -> openActivity(MerchantActivity.class));
        btnTopup.setOnClickListener(v -> openActivity(TopupActivity.class));
        btnSplit.setOnClickListener(v -> openActivity(BillSplitActivity.class));
        btnGift.setOnClickListener(v -> openActivity(GiftActivity.class));

        return view;
    }

    // ---------------- GREETING ----------------
    private void fetchUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("name");
                    if (name != null && !name.isEmpty()) {
                        tvGreeting.setText("Hi " + name.split(" ")[0] + " 👋");
                    }
                });
    }

    // ---------------- BALANCE ----------------
    private ListenerRegistration balanceListener;

    private void fetchBalance() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        balanceListener = db.collection("users")
                .document(user.getUid())
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }

                    Object balanceObj = snapshot.get("balance");

                    cachedBalance = balanceObj instanceof Number
                            ? ((Number) balanceObj).doubleValue()
                            : 0.0;

                    showBalance();
                });
    }


    private void handleBalance(@NonNull DocumentSnapshot snapshot) {
        Object bal = snapshot.get("balance");
        cachedBalance = bal instanceof Number ? ((Number) bal).doubleValue() : 0;
        showBalance();
    }

    private void toggleBalance() {
        isBalanceHidden = !isBalanceHidden;
        if (isBalanceHidden) {
            avbal.setText("Rs. ****");
            btnHideBalance.setImageResource(R.drawable.ic_eye_off);
        } else {
            showBalance();
        }
    }

    private void showBalance() {
        avbal.setText("Rs. " + String.format("%,.2f", cachedBalance));
        btnHideBalance.setImageResource(R.drawable.ic_eye);
    }

    // ---------------- MORE BOTTOM SHEET ----------------
    private void showMoreBottomSheet() {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheet = LayoutInflater.from(getContext())
                .inflate(R.layout.sheet_more_wallet, null);

        dialog.setContentView(sheet);
        dialog.show();
    }

    // ---------------- NAVIGATION ----------------
    private void openActivity(Class<?> cls) {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), cls));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (balanceListener != null) {
            balanceListener.remove();
            balanceListener = null;
        }
    }
}

