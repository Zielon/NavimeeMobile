package org.pl.android.navimee.ui.chat.friends;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.navimee.data.DataManager;
import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.data.model.chat.Friend;
import org.pl.android.navimee.data.model.chat.ListFriend;
import org.pl.android.navimee.data.model.chat.User;
import org.pl.android.navimee.injection.ActivityContext;
import org.pl.android.navimee.ui.base.BasePresenter;
import org.pl.android.navimee.util.Const;
import org.pl.android.navimee.util.NetworkUtil;

import java.net.ContentHandler;
import java.util.HashMap;

import javax.inject.Inject;

/**
 * Created by Wojtek on 2018-01-11.
 */
public class FriendsPresenter extends BasePresenter<FriendsMvpView> {

    private final DataManager mDataManager;

    private ListenerRegistration mListener;

    private Context mContext;

    @Inject
    public FriendsPresenter(DataManager dataManager,@ActivityContext Context context) {
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
        if(NetworkUtil.isNetworkConnected(mContext)) {
            String uid =  mDataManager.getPreferencesHelper().getUID();
            if (!uid.equals("")) {
                FirebaseDatabase.getInstance().getReference().child("user/" + uid + "/status/isOnline").setValue(true);
                FirebaseDatabase.getInstance().getReference().child("user/" + uid + "/status/timestamp").setValue(System.currentTimeMillis());
            }
        }
    }

    public void updateFriendStatus(ListFriend listFriend) {
        if(NetworkUtil.isNetworkConnected(mContext)) {
            for (Friend friend : listFriend.getListFriend()) {
                final String fid = friend.id;
                FirebaseDatabase.getInstance().getReference().child("user/" + fid + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            HashMap mapStatus = (HashMap) dataSnapshot.getValue();
                            if ((boolean) mapStatus.get("isOnline") && (System.currentTimeMillis() - (long) mapStatus.get("timestamp")) > Const.TIME_TO_OFFLINE) {
                                FirebaseDatabase.getInstance().getReference().child("user/" + fid + "/status/isOnline").setValue(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public void findByEmail(String email) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            User user = documentSnapshot.toObject(User.class);
                            if(getMvpView() != null) {
                                getMvpView().userFound(user);
                            }
                            break;
                        }
                    }
                } else {
                    if(getMvpView() != null) {
                        getMvpView().userNotFound();
                    }

                }
            }
        });
    }
}
