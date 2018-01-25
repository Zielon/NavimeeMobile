package org.pl.android.drively.data.remote;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseService {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;

    @Inject
    public FirebaseService(FirebaseAuth firebaseAuth, FirebaseDatabase firebaseDatabase, FirebaseFirestore firebaseFirestore, FirebaseStorage firebaseStorage) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseStorage = firebaseStorage;
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseFirestore getFirebaseFirestore() {
        return firebaseFirestore;
    }

    public FirebaseStorage getFirebaseStorage() {
        return firebaseStorage;
    }
}
