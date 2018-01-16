package org.pl.android.drively;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.local.PreferencesHelper;
import org.pl.android.drively.data.remote.EventsService;
import org.pl.android.drively.data.remote.FirebaseService;
import org.pl.android.drively.data.remote.RibotsService;
import org.pl.android.drively.test.common.TestDataFactory;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test class performs local unit tests without dependencies on the Android framework
 * For testing methods in the DataManager follow this approach:
 * 1. Stub mock helper classes that your method relies on. e.g. RetrofitServices or DatabaseHelper
 * 2. Test the Observable using TestSubscriber
 * 3. Optionally write a SEPARATE test that verifies that your method is calling the right helper
 * using Mockito.verify()
 */
@RunWith(MockitoJUnitRunner.class)
public class DataManagerTest {

    @Mock DatabaseHelper mMockDatabaseHelper;
    @Mock PreferencesHelper mMockPreferencesHelper;
    @Mock RibotsService mMockRibotsService;
    @Mock EventsService mMockEventsService;
    @Mock FirebaseService mFirebaseService;
    private DataManager mDataManager;

    @Before
    public void setUp() {
        mDataManager = new DataManager(mMockRibotsService,mMockEventsService, mMockPreferencesHelper,
                mMockDatabaseHelper,mFirebaseService);
    }

    @Test
    public void syncRibotsEmitsValues() {
        List<Ribot> ribots = Arrays.asList(TestDataFactory.makeRibot("r1"),
                TestDataFactory.makeRibot("r2"));
        stubSyncRibotsHelperCalls(ribots);

        TestObserver<Ribot> result = new TestObserver<>();
        mDataManager.syncRibots().subscribe(result);
        result.assertNoErrors();
        result.assertValueSequence(ribots);
    }

    @Test
    public void syncRibotsCallsApiAndDatabase() {
        List<Ribot> ribots = Arrays.asList(TestDataFactory.makeRibot("r1"),
                TestDataFactory.makeRibot("r2"));
        stubSyncRibotsHelperCalls(ribots);

        mDataManager.syncRibots().subscribe();
        // Verify right calls to helper methods
        verify(mMockRibotsService).getRibots();
        verify(mMockDatabaseHelper).setRibots(ribots);
    }

    @Test
    public void syncRibotsDoesNotCallDatabaseWhenApiFails() {
        when(mMockRibotsService.getRibots())
                .thenReturn(Observable.<List<Ribot>>error(new RuntimeException()));

        mDataManager.syncRibots().subscribe(new TestObserver<Ribot>());
        // Verify right calls to helper methods
        verify(mMockRibotsService).getRibots();
        verify(mMockDatabaseHelper, never()).setRibots(ArgumentMatchers.<Ribot>anyList());
    }

    private void stubSyncRibotsHelperCalls(List<Ribot> ribots) {
        // Stub calls to the ribot service and database helper.
        when(mMockRibotsService.getRibots())
                .thenReturn(Observable.just(ribots));
        when(mMockDatabaseHelper.setRibots(ribots))
                .thenReturn(Observable.fromIterable(ribots));
    }

}
