package org.pl.android.navimee.data;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import org.pl.android.navimee.data.local.DatabaseHelper;
import org.pl.android.navimee.data.local.PreferencesHelper;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.data.model.Ribot;
import org.pl.android.navimee.data.remote.EventsService;
import org.pl.android.navimee.data.remote.FirebaseService;
import org.pl.android.navimee.data.remote.RibotsService;

@Singleton
public class DataManager {

    private final RibotsService mRibotsService;
    private final EventsService mEventsService;
    private final DatabaseHelper mDatabaseHelper;
    private final PreferencesHelper mPreferencesHelper;
    private final FirebaseService mFirebaseService;

    @Inject
    public DataManager(RibotsService ribotsService, EventsService eventsService, PreferencesHelper preferencesHelper,
                       DatabaseHelper databaseHelper,FirebaseService firebaseService) {
        mRibotsService = ribotsService;
        mEventsService = eventsService;
        mPreferencesHelper = preferencesHelper;
        mDatabaseHelper = databaseHelper;
        mFirebaseService = firebaseService;
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public  FirebaseService getFirebaseService() {
        return mFirebaseService;
    }


    public Observable<Ribot> syncRibots() {
        return mRibotsService.getRibots()
                .concatMap(new Function<List<Ribot>, ObservableSource<? extends Ribot>>() {
                    @Override
                    public ObservableSource<? extends Ribot> apply(@NonNull List<Ribot> ribots)
                            throws Exception {
                        return mDatabaseHelper.setRibots(ribots);
                    }
                });
    }


    public Observable<List<Event>> loadEvents() {
        return mEventsService.getEvents();
    }


    public Observable<List<Ribot>> getRibots() {
        return mDatabaseHelper.getRibots().distinct();
    }

}
