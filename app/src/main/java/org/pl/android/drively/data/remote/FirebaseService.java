package org.pl.android.drively.data.remote;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;


import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Wojtek on 2017-10-24.
 */
@Singleton
public class FirebaseService {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseFirestore getFirebaseFirestore() {
        return firebaseFirestore;
    }

    @Inject
    public FirebaseService(FirebaseAuth firebaseAuth, FirebaseDatabase firebaseDatabase,FirebaseFirestore firebaseFirestore) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
    }

}
