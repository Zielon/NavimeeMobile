package org.pl.android.drively.ui.chat.friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.R;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.chat.ChatUser;
import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.friendsearch.FriendModel;
import org.pl.android.drively.ui.chat.friendsearch.FriendSearchDialogCompat;
import org.pl.android.drively.util.ChatUtils;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;
import org.pl.android.drively.util.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ir.mirrajabi.searchdialog.core.BaseFilter;
import timber.log.Timber;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;
import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class FriendsPresenter extends BasePresenter<FriendsMvpView> {

    private final DataManager mDataManager;
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

    public void findFriend(BaseFilter baseFilter, FriendSearchDialogCompat searchDialogCompat, String stringQuery, List friendList) {
        CollectionReference usersReference = mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS);
        friendList.add(getId());
        baseFilter.doBeforeFiltering();
        int strlength = stringQuery.length();
        String endcode = "";
        String upperCase = "";
        String encodeUpperCase = "";

        if (strlength > 0) {
            String strFrontCode = stringQuery.substring(0, strlength - 1);
            String strEndCode = stringQuery.substring(strlength - 1, strlength);

            endcode = strFrontCode + Character.toString((char) (strEndCode.charAt(0) + 1));
            upperCase = stringQuery.substring(0, 1).toUpperCase() + stringQuery.substring(1);
            encodeUpperCase = upperCase.substring(0, upperCase.length() - 1) + Character.toString((char) (strEndCode.charAt(0) + 1));
            if (encodeUpperCase.length() == 1) {
                encodeUpperCase = encodeUpperCase.toUpperCase();
            }
        }

        ArrayList<FriendModel> result = new ArrayList<>();
        searchDialogCompat.getItems().clear();

        String finalUpperCase = upperCase;
        String finalEncodeUpperCase = encodeUpperCase;
        String finalEndcode = endcode;
        try {
            String nameField = nameof(User.class, "name");
            String emailField = nameof(User.class, "email");
            usersReference
                    .whereGreaterThanOrEqualTo(nameField, stringQuery).whereLessThan(nameField, finalEndcode).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                FriendModel friend = new FriendModel(document.toObject(ChatUser.class));
                                if (!result.contains(friend) && !friendList.contains(friend.getId())) {
                                    result.add(friend);
                                }
                            }
                        } else {
                            Timber.w("Error getting documents: ", task.getException());
                        }

                        usersReference.whereGreaterThanOrEqualTo(nameField, finalUpperCase).whereLessThan(nameField, finalEncodeUpperCase).get()
                                .addOnCompleteListener(upperTask -> {
                                    if (upperTask.isSuccessful()) {
                                        for (DocumentSnapshot document : upperTask.getResult()) {
                                            FriendModel friend = new FriendModel(document.toObject(ChatUser.class));
                                            if (!result.contains(friend) && !friendList.contains(friend.getId())) {
                                                result.add(friend);
                                            }
                                        }
                                    } else {
                                        Timber.w("Error getting documents: ", upperTask.getException());
                                    }

                                    usersReference.whereGreaterThanOrEqualTo(emailField, stringQuery).whereLessThan(emailField, finalEndcode).get()
                                            .addOnCompleteListener(stringTask -> {
                                                if (stringTask.isSuccessful()) {
                                                    for (DocumentSnapshot document : stringTask.getResult()) {
                                                        FriendModel friend = new FriendModel(document.toObject(ChatUser.class));
                                                        if (!result.contains(friend) && !friendList.contains(friend.getId())) {
                                                            result.add(friend);
                                                        }
                                                    }
                                                } else {
                                                    Timber.w("Error getting documents: ", stringTask.getException());
                                                }

                                                searchDialogCompat.getFilterResultListener().onFilter(result);
                                                baseFilter.doAfterFiltering();
                                            });
                                });

                    });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void getAllFriendInfo(List<String> friendList) {
        List<Task<Task<Friend>>> tasks = new ArrayList<>();
        for (String id : friendList) {
            tasks.add(mDataManager.getFirebaseService().getFirebaseFirestore()
                    .collection(FirebasePaths.USERS).document(id).get().continueWith(task -> {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot != null && snapshot.exists()) {
                            Friend friend = snapshot.toObject(Friend.class);
                            friend.idRoom = ChatUtils.getRoomId(friend.id, getId());
                            return getStorageReference(friend.avatar).getBytes(Const.FIVE_MEGABYTE).continueWith(avatar -> {
                                if (avatar.isSuccessful())
                                    friend.avatarBytes = avatar.getResult();

                                return friend;
                            });
                        }
                        return Tasks.forResult(null);
                    }));
        }

        Tasks.whenAll(tasks).addOnSuccessListener(friends -> {
            List<Task<Friend>> friendsTasks = new ArrayList<>();
            for (Task<Task<Friend>> task : tasks)
                if (task.isSuccessful() && task.getResult() != null)
                    friendsTasks.add(task.getResult());

            Tasks.whenAll(friendsTasks).addOnSuccessListener(avatars -> {
                for (Task<Friend> task : friendsTasks)
                    if (task.isSuccessful() && task.getResult() != null && getMvpView() != null)
                        getMvpView().addFriendInfo(task.getResult());

                if(getMvpView() != null)
                    getMvpView().allFriendsFound();
            });
        });
    }

    public void getListFriendUId() {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        try {
            String idField = nameof(Friend.class, "id");
            mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(userId)
                    .collection(FirebasePaths.FRIENDS).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List friends = new ArrayList<String>();
                    if (task.getResult().size() == 0) {
                        getMvpView().listFriendNotFound();
                        return;
                    }
                    for (DocumentSnapshot document : task.getResult()) {
                        friends.add(document.get(idField));
                    }
                    if (getMvpView() != null) {
                        getMvpView().listFriendFound(friends);
                        friends.clear();
                    }
                } else {
                    if (getMvpView() != null) {
                        getMvpView().listFriendNotFound();
                    }
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void addFriend(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Map<String, Object> friendMap = new HashMap<>();
        try {
            String idField = nameof(Friend.class, "id");
            mDataManager.getFirebaseService().getFirebaseFirestore()
                    .collection(FirebasePaths.USERS)
                    .document(userId)
                    .collection(FirebasePaths.FRIENDS)
                    .whereEqualTo(idField, userId).get()
                    .addOnSuccessListener(documentSnapshots -> {
                        if (!documentSnapshots.isEmpty()) return;
                        friendMap.put(idField, idFriend);
                        mDataManager.getFirebaseService().getFirebaseFirestore()
                                .collection(FirebasePaths.USERS)
                                .document(userId)
                                .collection(FirebasePaths.FRIENDS)
                                .add(friendMap).addOnSuccessListener(documentReference -> {
                            if (getMvpView() != null)
                                getMvpView().addFriendSuccess(idFriend);
                        }).addOnFailureListener(e -> getMvpView().addFriendFailure());
                    });

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void addFriendForFriendId(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        Map<String, Object> friendMap = new HashMap<>();
        try {
            String idField = nameof(Friend.class, "id");
            friendMap.put(idField, userId);
            mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(idFriend).collection(FirebasePaths.FRIENDS)
                    .add(friendMap).addOnSuccessListener(documentReference -> {
                if (getMvpView() != null) {
                    getMvpView().addFriendIsNotIdFriend();
                }
            }).addOnFailureListener(e -> getMvpView().addFriendFailure());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    public Query getLastMessage(String idRoom) {
        try {
            String timestampField = nameof(Message.class, "timestamp");
            return mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.MESSAGES_PRIVATE)
                    .document(mDataManager.getPreferencesHelper().getCountry())
                    .collection(idRoom)
                    .orderBy(timestampField, Query.Direction.DESCENDING).limit(1);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public DocumentReference getStatus(String id) {
        return mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(id);
    }

    public void deleteFriend(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(userId).collection(FirebasePaths.FRIENDS)
                .whereEqualTo("id", idFriend).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0) {
                            if (getMvpView() != null) {
                                getMvpView().onFailureDeleteFriend();
                            }
                        }
                        // Delete a user from the friend list of the current user.
                        for (DocumentSnapshot document : task.getResult()) {
                            Timber.d(document.getId() + " => " + document.getData());
                            mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS)
                                    .document(userId).collection(FirebasePaths.FRIENDS)
                                    .document(document.getId()).delete()
                                    .addOnSuccessListener(empty -> {
                                        if (getMvpView() != null) {
                                            getMvpView().onSuccessDeleteFriend(idFriend);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Timber.w("Error deleting document", e);
                                        if (getMvpView() != null) {
                                            getMvpView().onFailureDeleteFriend();
                                        }
                                    });
                        }
                    } else {
                        Timber.d("Listen failed");
                        if (getMvpView() != null) {
                            getMvpView().onFailureDeleteFriend();
                        }
                    }
                });
    }

    public String getId() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }

    public StorageReference getStorageReference(String avatar) {
        return mDataManager.getFirebaseService().getFirebaseStorage().getReference(String.format("%s/%s", AVATARS, avatar));
    }

    public void getUserAvatar() {
        String avatarPath = mDataManager.getPreferencesHelper().getUserInfo().getAvatar();
        if (avatarPath.equals(Const.STR_DEFAULT_AVATAR)) {
            ChatViewActivity.bitmapAvatarUser = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avatar);
        } else {
            mDataManager.getFirebaseService().getFirebaseStorage().getReference(String.format("%s/%s", AVATARS, avatarPath))
                    .getBytes(Const.FIVE_MEGABYTE)
                    .addOnSuccessListener(bytes -> {
                        Bitmap src = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ChatViewActivity.bitmapAvatarUser = src;
                    }).addOnFailureListener(exception -> {
                ChatViewActivity.bitmapAvatarUser = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avatar);
            });
        }
    }
}