package org.pl.android.drively.repositories;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.FRIENDS;
import static org.pl.android.drively.util.FirebasePaths.GROUP;
import static org.pl.android.drively.util.FirebasePaths.USERS;
import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class UsersRepositoryImpl implements UsersRepository {

    private DataManager dataManager;

    @Inject
    public UsersRepositoryImpl(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public Task<User> getUser(String userId) {
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS).document(userId).get()
                .continueWith(user -> user.getResult().toObject(User.class));
    }

    @Override
    public Task<List<String>> getUserFriends(String userId) {
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS).document(userId).collection(FRIENDS).get()
                .continueWith(friends -> {
                    List<String> friendsIds = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : friends.getResult().getDocuments())
                        friendsIds.add(documentSnapshot.getString("id"));
                    return friendsIds;
                });
    }

    @Override
    public Task<List<String>> getUserRooms(String userId) {
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS).document(userId).collection(GROUP).get()
                .continueWith(friends -> {
                    List<String> friendsIds = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : friends.getResult().getDocuments())
                        friendsIds.add(documentSnapshot.getString("roomId"));
                    return friendsIds;
                });
    }

    @Override
    public Task<Void> updateUser(User user) {
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS).document(user.getId()).set(user, SetOptions.merge());
    }

    @Override
    public Task<Void> updateUserField(String userId, String field, Object value) throws NoSuchFieldException {
        String filedToUpdate = nameof(User.class, field);
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS)
                .document(userId)
                .update(filedToUpdate, value);
    }

    @Override
    public Task<Void> addFriend(String userId, String friendId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", friendId);
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS).document(userId)
                .collection(FRIENDS)
                .document(friendId).set(map)
                .addOnSuccessListener(success -> addFriend(friendId, userId));
    }
}
