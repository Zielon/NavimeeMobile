package org.pl.android.drively.data.remote;

import android.content.Context;
import android.os.Bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.pl.android.drively.injection.ApplicationContext;

import javax.inject.Inject;

public class FirebaseAnalyticsService {

    private final FirebaseAnalytics firebaseAnalyticsInstance;

    private Context context;
    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    public FirebaseAnalyticsService(@ApplicationContext Context context) {
        this.context = context;
        this.firebaseAnalyticsInstance = FirebaseAnalytics.getInstance(context);
    }

    public void reportEvent(String id, String name, Object content) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
            bundle.putString(FirebaseAnalytics.Param.CONTENT, mapper.writeValueAsString(content));
            firebaseAnalyticsInstance.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
