package com.example.project;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class transaction_fragment extends Fragment {

    RecyclerView rvTransactions;
    TransactionAdapter adapter;
    ArrayList<TransactionModel> list = new ArrayList<>();

    FirebaseFirestore db;
    ListenerRegistration transactionListener;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.transaction_fragment,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        adapter = new TransactionAdapter(getContext(), list);
        rvTransactions.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(
                    getContext(),
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // 🔥 REAL-TIME TRANSACTION LISTENER
        transactionListener = db.collection("users")
                .document(user.getUid())
                .collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) return;

                    list.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(TransactionModel.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 🧹 Clean up listener
        if (transactionListener != null) {
            transactionListener.remove();
        }
    }
}
