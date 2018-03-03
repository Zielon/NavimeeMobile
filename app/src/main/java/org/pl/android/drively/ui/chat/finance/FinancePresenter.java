package org.pl.android.drively.ui.chat.finance;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.data.model.chat.RoomMember;
import org.pl.android.drively.ui.base.tab.BaseTabPresenter;
import org.pl.android.drively.util.FirebasePaths;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

import static org.pl.android.drively.util.FirebasePaths.MEMBERS;
import static org.pl.android.drively.util.FirebasePaths.ROOM_DETAILS;

public class FinancePresenter extends BaseTabPresenter<FinanceMvpView> {

    private Disposable mDisposable;

    @Inject
    public FinancePresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(FinanceMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

    public void getDrivelyGroup() {
        DocumentReference groupRef = mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(FirebasePaths.GROUP).document(mDataManager.getPreferencesHelper().getCountry());

        groupRef.collection("DRIVELY").document(ROOM_DETAILS).get()
                .continueWith(task -> {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.exists()) {
                        Room room = snapshot.toObject(Room.class);
                        return groupRef.collection("DRIVELY").document(MEMBERS).collection(MEMBERS).get().continueWith(members -> {
                            if (members.isSuccessful()) {
                                for (DocumentSnapshot doc : members.getResult().getDocuments()) {
                                    room.getMembers().add(doc.toObject(RoomMember.class));
                                }
                            }
                            getMvpView().goToDrivelyChat(room);
                            return room;
                        });

                    }
                    return Tasks.forResult(null);
                });
    }

}
