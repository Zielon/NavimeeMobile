package org.pl.android.drively.injection.module;

import android.app.Application;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.injection.ApplicationContext;
import org.pl.android.drively.repositories.UsersRepositoryImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    protected final Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    public FirebaseDatabase provideFirebaseDatabase() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        return firebaseDatabase;
    }

    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth;
    }

    @Provides
    @Singleton
    public FirebaseFirestore provideFirebaseFirestore() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        return firebaseFirestore;
    }

    @Provides
    @Singleton
    public FirebaseStorage provideFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        return storage;
    }

    @Provides
    @Singleton
    public UsersRepository provideUserRepository() {
        return new UsersRepositoryImpl();
    }
}
