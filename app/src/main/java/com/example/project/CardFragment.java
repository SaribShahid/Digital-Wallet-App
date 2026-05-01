package com.example.project;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class CardFragment extends Fragment {

    // UI
    TextView tvCardName, tvCardNumber;
    ImageView btnFreeze, btnReveal, btnMore;
    RelativeLayout cardContainer;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    // States
    boolean isFrozen = false;
    boolean isRevealed = false;

    // Cached data
    String cachedName = "";
    String cachedCard = "";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card, container, false);

        // Init UI
        tvCardName = view.findViewById(R.id.tvCardName);
        tvCardNumber = view.findViewById(R.id.tvCardNumber);
        cardContainer = view.findViewById(R.id.cardContainer);

        btnFreeze = view.findViewById(R.id.freeze);
        btnReveal = view.findViewById(R.id.reveal);
        btnMore   = view.findViewById(R.id.more);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ALWAYS hide card on open
        isRevealed = false;

        // Clicks
        btnFreeze.setOnClickListener(v -> toggleFreeze());
        btnReveal.setOnClickListener(v -> revealCard());
        btnMore.setOnClickListener(v -> showMoreOptions());

        fetchCardState();
        fetchUserData();

        return view;
    }

    // ================= FETCH DATA =================

    private void fetchCardState() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .collection("card")
                .document("details")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Boolean frozen = snapshot.getBoolean("isFrozen");
                        if (frozen != null) {
                            isFrozen = frozen;
                        }
                    }
                    applyCardUI();
                });
    }

    private void fetchUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(this::handleUserData);
    }

    private void handleUserData(@NonNull DocumentSnapshot snapshot) {
        if (!snapshot.exists()) return;

        cachedName = snapshot.getString("name");

        String phone = snapshot.getString("phone");
        if (phone != null && phone.startsWith("+92")) {
            phone = phone.substring(3);
        }

        cachedCard = formatCardNumber("121432" + phone);
        applyCardUI();
    }

    // ================= UI LOGIC =================

    private void applyCardUI() {

        // FROZEN → always hidden
        if (isFrozen) {
            cardContainer.setBackgroundResource(R.drawable.card_grey_bg);
            tvCardName.setText("••••••");
            tvCardNumber.setText("**** **** **** ****");
            return;
        }

        // NOT frozen
        cardContainer.setBackgroundResource(R.drawable.card_bg);

        if (isRevealed) {
            tvCardName.setText(cachedName.toUpperCase());
            tvCardNumber.setText(cachedCard);
        } else {
            tvCardName.setText("••••••");
            tvCardNumber.setText("**** **** **** ****");
        }
    }

    // ================= FREEZE =================

    private void toggleFreeze() {

        isFrozen = !isFrozen;
        isRevealed = false; // reset reveal

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("isFrozen", isFrozen);

        db.collection("users")
                .document(user.getUid())
                .collection("card")
                .document("details")
                .set(data, SetOptions.merge());

        applyCardUI();
    }

    // ================= REVEAL =================

    private void revealCard() {

        if (isFrozen) {
            Toast.makeText(getContext(),
                    "Unfreeze card to reveal details",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        isRevealed = true;
        applyCardUI();
    }

    // ================= MORE =================

    private void showMoreOptions() {

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = getLayoutInflater()
                .inflate(R.layout.sheet_card_more, null);

        sheet.findViewById(R.id.btnOrderCard)
                .setOnClickListener(v -> {
                    placePhysicalCardOrder();
                    dialog.dismiss();
                });

        dialog.setContentView(sheet);
        dialog.show();
    }

    private void placePhysicalCardOrder() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> order = new HashMap<>();
        order.put("physicalCardOrdered", true);
        order.put("orderedAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getUid())
                .collection("card")
                .document("details")
                .set(order, SetOptions.merge());

        Toast.makeText(getContext(),
                "Physical card ordered successfully",
                Toast.LENGTH_LONG).show();
    }

    // ================= UTIL =================

    private String formatCardNumber(String number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(" ");
            sb.append(number.charAt(i));
        }
        return sb.toString();
    }
}
