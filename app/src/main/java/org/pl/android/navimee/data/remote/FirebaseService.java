package org.pl.android.navimee.data.remote;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

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
