package org.pl.android.drively.ui.chat.friends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.data.model.chat.User;
import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2018-01-11.
 */
public class FriendsPresenter extends BasePresenter<FriendsMvpView> {

    private final DataManager mDataManager;

    private ListenerRegistration mListener;

    private Context mContext;

    @Inject
    public FriendsPresenter(DataManager dataManager, @ActivityContext Context context) {
        mDataManager = dataManager;
        mContext = context;
    }

    @Override
    public void attachView(FriendsMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }


    public void updateUserStatus() {
        if (NetworkUtil.isNetworkConnected(mContext)) {
            String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
            if (!userId.equals("")) {
                Map<String, Object> data = new HashMap<>();
                data.put("isOnline", true);
                data.put("timestamp", System.currentTimeMillis());
                mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).update(data);
            }
        }
    }

    public void updateFriendStatus(ListFriend listFriend) {
        if (NetworkUtil.isNetworkConnected(mContext)) {
            for (Friend friend : listFriend.getListFriend()) {
                final String fid = friend.id;
                mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(fid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @SuppressLint("TimberArgCount")
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                if (task.getResult().getData().get("isOnline") != null && task.getResult().getData().get("timestamp") != null && (boolean) task.getResult().getData().get("isOnline")
                                        && (System.currentTimeMillis() - (long) task.getResult().getData().get("timestamp")) > Const.TIME_TO_OFFLINE) {
                                    mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(fid).update("isOnline",false);
                                }
                            } else {
                               Timber.d( "No such document");
                            }
                        } else {
                            Timber.e("get failed with ", task.getException());
                        }
                    }
                });
            }
        }
    }

    public void findByEmail(String email) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (getMvpView() != null) {
                                getMvpView().userFound(user);
                            }
                            break;
                        }
                    }
                } else {
                    if (getMvpView() != null) {
                        getMvpView().userNotFound();
                    }

                }
            }
        });
    }

    public void getAllFriendInfo(int index, String id) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("TimberArgCount")
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Timber.e("Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Friend friend = snapshot.toObject(Friend.class);
                    friend.idRoom = friend.id.compareTo(getId()) > 0 ? (getId() + friend.id).hashCode() + "" : "" + (friend.id + getId()).hashCode();
                    if (getMvpView() != null) {
                        getMvpView().friendInfoFound(index,friend);
                    }
                }
            }

        });
    }



    public void getListFriendUId() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId)
                .collection("FRIENDS").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List friends = new ArrayList<String>();
                            if(task.getResult().size() == 0) {
                                getMvpView().listFriendNotFound();
                                return;
                            }
                            for (DocumentSnapshot document : task.getResult()) {
                                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                    friends.add(documentSnapshot.get("id"));
                                }
                                if (getMvpView() != null) {
                                    getMvpView().listFriendFound(friends);
                                    friends.clear();
                                }

                            }
                        } else {
                            if (getMvpView() != null) {
                                getMvpView().listFriendNotFound();
                            }

                        }
                    }
                });
    }

    public void addFriend(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Map<String, Object> friendMap = new HashMap<>();
        friendMap.put("id",idFriend);
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId)
                    .collection("FRIENDS").add(friendMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (getMvpView() != null) {
                            getMvpView().addFriendSuccess(idFriend);
                        }
                    }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        getMvpView().addFriendFailure();
                    }
                    });
    }

    public void addFriendForFriendId(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Map<String, Object> friendMap = new HashMap<>();
        friendMap.put("id",userId);
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(idFriend).collection("FRIENDS")
                    .add(friendMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            if (getMvpView() != null) {
                                getMvpView().addFriendIsNotIdFriend();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            getMvpView().addFriendFailure();
                        }
                    });
    }

    public String getId() {
        return mDataManager.getPreferencesHelper().getUID();
    }

    public Query getLastMessage(String idRoom) {
        return mDataManager.getFirebaseService().getFirebaseFirestore().collection("MESSAGES").document(idRoom)
                .collection("MESSAGES")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }

    public DocumentReference getStatus(String id) {
        return mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(id);
    }

    public void deleteFriend(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).collection("FRIENDS")
                .whereEqualTo("id",idFriend).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Timber.d( document.getId() + " => " + document.getData());
                                mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(userId).collection("FRIENDS")
                                            .document(document.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (getMvpView() != null) {
                                                        getMvpView().onSuccessDeleteFriend(idFriend);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @SuppressLint("TimberArgCount")
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Timber.w("Error deleting document", e);
                                                    if (getMvpView() != null) {
                                                        getMvpView().onFailureDeleteFriend();
                                                    }
                                                }
                                            });
                            }
                        } else {
                            Timber.d("Listen failed");
                            if (getMvpView() != null) {
                                getMvpView().onFailureDeleteFriend();
                            }
                        }
                    }
                });
    }
}