package org.pl.android.drively.repositories;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.injection.PerActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static org.pl.android.drively.util.FirebasePaths.FRIENDS;
import static org.pl.android.drively.util.FirebasePaths.GROUP;
import static org.pl.android.drively.util.FirebasePaths.USERS;

@PerActivity
public class UsersRepositoryImpl implements UsersRepository {

    @Inject
    DataManager dataManager;

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
                    for (DocumentSnapshot documentSnapshot: friends.getResult().getDocuments())
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
                    for (DocumentSnapshot documentSnapshot: friends.getResult().getDocuments())
                        friendsIds.add(documentSnapshot.getString("roomId"));
                    return friendsIds;
                });
    }

    @Override
    public Task<Void> addFriend(String userId, String friendId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", friendId);
        return dataManager.getFirebaseService()
                .getFirebaseFirestore()
                .collection(USERS).document(userId).collection(FRIENDS).document().set(map);
    }
}
