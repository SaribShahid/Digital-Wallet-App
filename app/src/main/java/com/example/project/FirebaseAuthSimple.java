package com.example.project;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Firebase helper for OTP + Firestore operations
 */
public class FirebaseAuthSimple {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public FirebaseAuthSimple() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /** Get FirebaseAuth instance */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /** Get Firestore instance */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    // -------------------
    // PHONE OTP METHODS
    // -------------------

    /**
     * Send OTP to phone number
     *
     * @param activity  Current activity
     * @param phoneNumber Full phone number with country code (+92XXXXXXXXX)
     * @param callbacks PhoneAuthProvider callbacks
     */
    public void sendOtp(Activity activity, String phoneNumber,
                        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Verify OTP code
     *
     * @param verificationId verificationId received in onCodeSent
     * @param code OTP code entered by user
     * @param onComplete callback on success or failure
     */
    public void verifyOtp(String verificationId, String code, FirebaseAuthCallback onComplete) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onComplete.onSuccess(task.getResult());
            } else {
                onComplete.onFailure(task.getException());
            }
        });
    }

    // -------------------
    // FIRESTORE METHODS
    // -------------------

    /**
     * Save user info in Firestore
     *
     * @param userId Firebase UID
     * @param userData Map of user data (name, email, age, cnic...)
     * @param callback On success/failure
     */
    public void saveUser(String userId, Map<String, Object> userData, FirebaseFirestoreCallback callback) {
        firestore.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetch user info from Firestore
     *
     * @param userId Firebase UID
     * @param callback Return DocumentSnapshot or error
     */
    public void getUser(String userId, FirebaseFirestoreGetCallback callback) {
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> callback.onSuccess(documentSnapshot))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Update user info in Firestore
     *
     * @param userId Firebase UID
     * @param updates Map of fields to update
     * @param callback On success/failure
     */
    public void updateUser(String userId, Map<String, Object> updates, FirebaseFirestoreCallback callback) {
        firestore.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // -------------------
    // CALLBACK INTERFACES
    // -------------------

    public interface FirebaseAuthCallback {
        void onSuccess(AuthResult result);
        void onFailure(Exception e);
    }

    public interface FirebaseFirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface FirebaseFirestoreGetCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(Exception e);
    }
}
