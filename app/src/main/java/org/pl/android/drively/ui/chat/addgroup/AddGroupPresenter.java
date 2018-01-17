package org.pl.android.drively.ui.chat.addgroup;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Created by Wojtek on 2018-01-17.
 */

public class AddGroupPresenter extends BasePresenter<AddGroupMvpView> {
    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public AddGroupPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(AddGroupMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }


    public void createGroup(String idGroup, Room room) {

        Map<String, String> map = new HashMap<>();
        for (String i : room.member) map.put(i,"empty");
        map.putAll(room.groupInfo);

        mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection("GROUP")
                .document(idGroup).set(map)
                .addOnSuccessListener(aVoid -> {
                    if (getMvpView() != null) {
                        getMvpView().addRoomForUser(idGroup,0);
                    }
                })
                .addOnFailureListener(e -> Timber.w("Error writing document", e));
    }

    public void addRoomForUser(String roomId, int userIndex, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomId", roomId);
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).collection("GROUP").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (getMvpView() != null) {
                            getMvpView().addRoomForUser(roomId,userIndex+1);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getMvpView() != null) {
                            getMvpView().addRoomForUser(roomId,userIndex+1);
                        }
                    }
                });
    }

    public String getId() {
        return mDataManager.getPreferencesHelper().getUID();
    }
}
