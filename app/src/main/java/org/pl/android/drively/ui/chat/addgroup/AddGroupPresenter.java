package org.pl.android.drively.ui.chat.addgroup;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

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
        mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection("GROUP")
                .document(idGroup).set(room.groupInfo)
                .addOnSuccessListener(aVoid -> {
                    List<Task<Void>> tasks = new ArrayList<>();
                    Map<String, String> member = new HashMap<>();
                    for (String memberId : room.member) {
                        member.put("memberId", memberId);
                        tasks.add(mDataManager.getFirebaseService()
                                .getFirebaseFirestore()
                                .collection("GROUP")
                                .document(idGroup)
                                .collection("MEMBERS").document(memberId).set(member));
                        member.clear();
                    }
                    if (getMvpView() != null) {
                        getMvpView().addRoomForUser(idGroup, 0);
                    }
                })
                .addOnFailureListener(e -> Timber.w("Error writing document", e));
    }

    public void addRoomForUser(String roomId, int userIndex, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomId", roomId);
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).collection("GROUP").add(data)
                .addOnSuccessListener(documentReference -> {
                    if (getMvpView() != null) {
                        getMvpView().addRoomForUser(roomId, userIndex + 1);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getMvpView() != null) {
                        getMvpView().addRoomForUser(roomId, userIndex + 1);
                    }
                });
    }

    public String getId() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }

    public void editGroup(String idGroup, Room room) {

        mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection("GROUP")
                .document(idGroup)
                .delete()
                .addOnSuccessListener(aVoid -> mDataManager.getFirebaseService().getFirebaseFirestore()
                        .collection("GROUP")
                        .document(idGroup).set(room.groupInfo)
                        .addOnSuccessListener(aVoidd -> {
                            Map<String, String> member = new HashMap<>();
                            for (String memberId : room.member) {
                                member.put("memberId", memberId);
                                mDataManager.getFirebaseService()
                                        .getFirebaseFirestore()
                                        .collection("GROUP")
                                        .document(idGroup)
                                        .collection("MEMBERS").document(memberId).set(member);
                                member.clear();
                            }
                            if (getMvpView() != null) {
                                getMvpView().editGroupSuccess(idGroup);
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (getMvpView() != null) {
                                getMvpView().editGroupFailure();
                            }
                        }))
                .addOnFailureListener(e -> {
                    if (getMvpView() != null) {
                        getMvpView().editGroupFailure();
                    }
                });
    }

    public void deleteUserReference(String userId, String roomId, int userIndex) {

        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS")
                .document(userId)
                .collection("GROUP").whereEqualTo("roomId", roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0) {
                            if (getMvpView() != null) {
                                getMvpView().onFailureGroupReference();
                            }
                        }
                        for (DocumentSnapshot document : task.getResult()) {
                            Timber.d(document.getId() + " => " + document.getData());
                            mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).collection("GROUP")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (getMvpView() != null) {
                                            getMvpView().onSuccessDeleteGroupReference(roomId, userIndex);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Timber.w("Error deleting document", e);
                                        if (getMvpView() != null) {
                                            getMvpView().onFailureGroupReference();
                                        }
                                    });
                        }
                    } else {
                        Timber.d("Listen failed");
                        if (getMvpView() != null) {
                            getMvpView().onFailureGroupReference();
                        }
                    }
                });

    }

    public StorageReference getStorageReference(String avatar) {
        return mDataManager.getFirebaseService().getFirebaseStorage().getReference("AVATARS/" + avatar);
    }
}
