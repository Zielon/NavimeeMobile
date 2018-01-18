package org.pl.android.drively.ui.chat.group;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.chat.Group;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Wojtek on 2018-01-11.
 */
public class GroupPresenter extends BasePresenter<GroupMvpView> {

    private final DataManager mDataManager;

    private ListenerRegistration mListener;
    private Context mContext;


    @Inject
    public GroupPresenter(DataManager dataManager,@ActivityContext Context context) {
        mDataManager = dataManager;
        mContext = context;
    }

    @Override
    public void attachView(GroupMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }


    public void getListGroup() {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS")
                     .document(getId()).collection("GROUP").get()
                     .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<String> rooms = new ArrayList<>();
                                for (DocumentSnapshot document : task.getResult()) {
                                        if (document.get("roomId") != null) {
                                            rooms.add(document.getString("roomId"));
                                        }
                                }
                                if (getMvpView() != null) {
                                    getMvpView().setGroupList(rooms);
                                }
                            } else {
                                if (getMvpView() != null) {
                                    getMvpView().getGroupError();
                                }
                            }
                        }
                    });
    }


    public void getGroupInfo(int groupIndex, String id) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP").document(id)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Room room = new Room();
                                room.groupInfo.put("admin", task.getResult().getString("admin"));
                                room.groupInfo.put("name",  task.getResult().getString("name"));
                                mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP")
                                        .document(id).collection("MEMBERS")
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        room.member.add(document.getId());
                                                    }
                                                    if (getMvpView() != null) {
                                                        getMvpView().setGroupInfo(groupIndex,room);
                                                    }
                                                } else {
                                                    Timber.w("Error geting document");
                                                }
                                            }
                                });
                            } else {
                                Timber.w("Error geting document");
                            }
                        } else {
                            Timber.w("Error geting document");
                        }
                    }
                });
    }




    public void deleteGroup(Group group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP").document(group.id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (getMvpView() != null) {
                                getMvpView().deleteGroupSuccess(group);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            getMvpView().deleteGroupFailure();
                        }
                    });

    }

    public void deleteGroupReference(int index, Group group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS")
                .document(group.member.get(index))
                .collection("GROUP").whereEqualTo("roomId",group.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Timber.d( document.getId() + " => " + document.getData());
                                mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(group.member.get(index)).collection("GROUP")
                                        .document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (getMvpView() != null) {
                                                    getMvpView().onSuccessDeleteGroupReference(group,index);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @SuppressLint("TimberArgCount")
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Timber.w("Error deleting document", e);
                                                if (getMvpView() != null) {
                                                    getMvpView().onFailureGroupReference();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Timber.d("Listen failed");
                            if (getMvpView() != null) {
                                getMvpView().onFailureGroupReference();
                            }
                        }
                    }
                });
    }

    public void leaveGroup(Group group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP")
                     .document(group.id)
                     .collection("MEMBERS")
                     .document(getId()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (getMvpView() != null) {
                                getMvpView().onSuccessLeaveGroup(group);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (getMvpView() != null) {
                                getMvpView().onFailureLeaveGroup();
                            }
                        }
                    });
    }


    public void leaveGroupUserReference(Group group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS")
                .document(getId())
                .collection("GROUP").whereEqualTo("roomId",group.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Timber.d( document.getId() + " => " + document.getData());
                                mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(getId()).collection("GROUP")
                                        .document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (getMvpView() != null) {
                                                    getMvpView().onSuccessLeaveGroupReference(group);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @SuppressLint("TimberArgCount")
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Timber.w("Error deleting document", e);
                                                if (getMvpView() != null) {
                                                    getMvpView().onFailureLeaveGroup();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Timber.d("Listen failed");
                            if (getMvpView() != null) {
                                getMvpView().onFailureGroupReference();
                            }
                        }
                    }
                });

    }


    public String getId() {
        return mDataManager.getPreferencesHelper().getUID();
    }

}
