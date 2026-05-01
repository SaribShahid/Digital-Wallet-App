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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class messages extends Fragment {

    RecyclerView rvMessages;
    MessageAdapter adapter;
    ArrayList<MessageModel> messageList = new ArrayList<>();

    FirebaseFirestore db;
    String userId;

    public messages() {}

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_messages,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MessageAdapter(getContext(), messageList);
        rvMessages.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        // 🔥 REAL-TIME LISTENER
        db.collection("users")
                .document(userId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) return;

                    messageList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        messageList.add(
                                doc.toObject(MessageModel.class)
                        );
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
