package org.pl.android.drively.ui.chat.friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.annimon.stream.Stream;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.R;
import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.chat.ChatUser;
import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.friendsearch.FriendModel;
import org.pl.android.drively.ui.chat.friendsearch.FriendSearchDialogCompat;
import org.pl.android.drively.util.ChatUtils;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.FirebasePaths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import ir.mirrajabi.searchdialog.core.BaseFilter;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;
import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class FriendsPresenter extends BasePresenter<FriendsMvpView> {

    private final DataManager mDataManager;
    private final UsersRepository usersRepository;
    private Context mContext;

    @Inject
    public FriendsPresenter(DataManager dataManager, @ActivityContext Context context, UsersRepository usersRepository) {
        this.mDataManager = dataManager;
        this.mContext = context;
        this.usersRepository = usersRepository;
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
        if (stringQuery == null || stringQuery.length() < 3) {
            searchDialogCompat.getItems().clear();
            searchDialogCompat.getFilterResultListener().onFilter(new ArrayList<>());
            return;
        }

        CollectionReference usersReference = mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS);
        friendList.add(getId());
        baseFilter.doBeforeFiltering();
        int length = stringQuery.length();
        String endcode = "";
        String upperCase = "";
        String encodeUpperCase = "";

        if (length > 0) {
            String strFrontCode = stringQuery.substring(0, length - 1);
            String strEndCode = stringQuery.substring(length - 1, length);

            endcode = strFrontCode + Character.toString((char) (strEndCode.charAt(0) + 1));
            upperCase = stringQuery.substring(0, 1).toUpperCase() + stringQuery.substring(1);
            encodeUpperCase = upperCase.substring(0, upperCase.length() - 1) + Character.toString((char) (strEndCode.charAt(0) + 1));
            if (encodeUpperCase.length() == 1) {
                encodeUpperCase = encodeUpperCase.toUpperCase();
            }
        }

        ArrayList<FriendModel> result = new ArrayList<>();
        searchDialogCompat.getItems().clear();
        searchDialogCompat.setLoading(true);

        String finalUpperCase = upperCase;
        String finalEncodeUpperCase = encodeUpperCase;
        String finalEndcode = endcode;
        try {
            String nameField = nameof(User.class, "name");
            String emailField = nameof(User.class, "email");
            List<Task<QuerySnapshot>> tasks = new ArrayList<>();

            tasks.add(usersReference.whereGreaterThanOrEqualTo(nameField, stringQuery).whereLessThan(nameField, finalEndcode).get());
            tasks.add(usersReference.whereGreaterThanOrEqualTo(nameField, finalUpperCase).whereLessThan(nameField, finalEncodeUpperCase).get());
            tasks.add(usersReference.whereGreaterThanOrEqualTo(emailField, stringQuery).whereLessThan(emailField, finalEndcode).get());

            Tasks.whenAllComplete(tasks).addOnSuccessListener(queries -> {

                HashSet<FriendModel> friendsSet = Stream.of(queries).map(task -> (QuerySnapshot) task.getResult())
                        .reduce(new HashSet<FriendModel>(), (friends, snapshot) -> {
                            List<FriendModel> models = Stream.of(snapshot.getDocuments())
                                    .map(document -> new FriendModel(document.toObject(ChatUser.class)))
                                    .filter(friend -> !friend.getId().equals(mDataManager.getPreferencesHelper().getUserId())).toList();
                            friends.addAll(models);
                            return friends;
                        });

                result.addAll(Stream.of(new ArrayList<>(friendsSet)).filter(friend -> !friendList.contains(friend.getId())).toList());

                List<Task<byte[]>> avatars = Stream.of(result).filter(user -> !user.getAvatar().equals(Const.STR_DEFAULT_AVATAR))
                        .map(user -> FirebaseStorage.getInstance().getReference("AVATARS/" + user.getAvatar())
                                .getBytes(Const.FIVE_MEGABYTE)
                                .addOnSuccessListener(bytes -> {
                                    Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    user.setAvatarImage(avatar);
                                })).toList();

                Tasks.whenAllComplete(avatars).addOnSuccessListener(bytes -> {
                    searchDialogCompat.getFilterResultListener().onFilter(result);
                    searchDialogCompat.setLoading(false);
                    baseFilter.doAfterFiltering();
                }).addOnFailureListener(failure -> searchDialogCompat.dismiss());

            }).addOnFailureListener(failure -> searchDialogCompat.dismiss());

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
                                if (avatar.isSuccessful()){
                                    friend.avatarBytes = avatar.getResult();
                                    mDataManager.getPreferencesHelper()
                                            .setFriendAvatar(friend.id, BitmapFactory.decodeByteArray(friend.avatarBytes, 0, friend.avatarBytes.length));
                                }
                                return friend;
                            });
                        }
                        return Tasks.forResult(null);
                    }));
        }

        Tasks.whenAllComplete(tasks).addOnSuccessListener(friends -> {
            List<Task<Friend>> friendsTasks = new ArrayList<>();
            for (Task<Task<Friend>> task : tasks)
                if (task.isSuccessful() && task.getResult() != null)
                    friendsTasks.add(task.getResult());

            Tasks.whenAllComplete(friendsTasks).addOnSuccessListener(avatars -> {
                for (Task<Friend> task : friendsTasks)
                    if (task.isSuccessful() && task.getResult() != null && getMvpView() != null)
                        getMvpView().addFriendInfo(task.getResult());

                if (getMvpView() != null)
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
                    if (task.getResult().size() == 0 && getMvpView() != null) {
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

    public void addFriend(String friendId) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        usersRepository.addFriend(userId, friendId).addOnSuccessListener(success -> {
            if (getMvpView() != null)
                getMvpView().addFriendSuccess(friendId);
        }).addOnFailureListener(error -> {
            if (getMvpView() != null)
                getMvpView().addFriendFailure();
        });
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

    public void deleteFriend(String friendId) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        usersRepository.deleteFriend(userId, friendId).addOnSuccessListener(success -> {
            if (getMvpView() != null)
                getMvpView().onSuccessDeleteFriend(friendId);
        }).addOnFailureListener(error -> {
            if (getMvpView() != null)
                getMvpView().onFailureDeleteFriend();
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