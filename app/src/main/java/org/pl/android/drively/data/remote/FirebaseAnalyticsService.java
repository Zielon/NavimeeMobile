package org.pl.android.drively.data.remote;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.pl.android.drively.injection.ApplicationContext;

import javax.inject.Inject;

public class FirebaseAnalyticsService {

    private final FirebaseAnalytics firebaseAnalyticsInstance;

    private Context context;

    @Inject
    public FirebaseAnalyticsService(@ApplicationContext Context context) {
        this.context = context;
        this.firebaseAnalyticsInstance = FirebaseAnalytics.getInstance(context);
    }

    public void reportEvent(String id, String name, Object content) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT, new Gson().toJson(content));
        firebaseAnalyticsInstance.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}
