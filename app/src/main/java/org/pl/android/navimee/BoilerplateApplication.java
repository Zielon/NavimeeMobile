package org.pl.android.navimee;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.github.stkent.amplify.feedback.DefaultEmailFeedbackCollector;
import com.github.stkent.amplify.feedback.GooglePlayStoreFeedbackCollector;
import com.github.stkent.amplify.tracking.Amplify;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import org.pl.android.navimee.injection.component.ApplicationComponent;
import org.pl.android.navimee.injection.component.DaggerApplicationComponent;
import org.pl.android.navimee.injection.module.ApplicationModule;

public class BoilerplateApplication extends Application  {

    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Fabric.with(this, new Crashlytics());
        }
        Amplify.initSharedInstance(this)
                .setPositiveFeedbackCollectors(new DefaultEmailFeedbackCollector("wojciech.grazawski@wp.pl"))
                .setCriticalFeedbackCollectors(new DefaultEmailFeedbackCollector("wojciech.grazawski@wp.pl"))
            //    .setInstallTimeCooldownDays(1)   // Prompt not shown within two weeks of initial install.
            //    .setLastUpdateTimeCooldownDays(1) // Prompt not shown within one week of most recent update.
               // .setLastCrashTimeCooldownDays(1) // Prompt not shown within one week of most recent crash.
                .setAlwaysShow(BuildConfig.DEBUG)
                .applyAllDefaultRules();
    }

    public static BoilerplateApplication get(Context context) {
        return (BoilerplateApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        if (mApplicationComponent == null) {
            mApplicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }
}
