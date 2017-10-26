package org.pl.android.navimee.data.remote;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by Wojtek on 2017-10-24.
 */
@Singleton
public class FirebaseService {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    @Inject
    public FirebaseService(FirebaseAuth firebaseAuth, FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
    }

    public void signUp(String email, String password) {
       firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Timber.d("createUser:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Timber.d("createUser:onComplete:" + task.isSuccessful());
                        } else {
                            Timber.d("createUser:onComplete:" + task.isSuccessful());
                        }
                    }
                });
    }

}
