package org.pl.android.drively.data;


import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.data.remote.FirebaseService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataManager {

    private final PreferencesHelper mPreferencesHelper;
    private final FirebaseService mFirebaseService;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper, FirebaseService firebaseService) {
        mPreferencesHelper = preferencesHelper;
        mFirebaseService = firebaseService;
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public FirebaseService getFirebaseService() {
        return mFirebaseService;
    }

}
