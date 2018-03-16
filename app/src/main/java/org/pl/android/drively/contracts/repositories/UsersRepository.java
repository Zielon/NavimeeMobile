package org.pl.android.drively.contracts.repositories;

import com.google.android.gms.tasks.Task;

import org.pl.android.drively.data.model.User;

import java.util.List;

public interface UsersRepository {
    Task<User> getUser(String userId);

    Task<List<String>> getUserFriends(String userId);

    Task<List<String>> getUserRooms(String userId);

    Task<Void> updateUser(User user);

    Task<Void> updateUserField(String userId, String field, Object value) throws NoSuchFieldException;

    Task<Void> deleteUserField(String userId, String field) throws NoSuchFieldException;

    Task<Void> addFriend(String userId, String friendId);
}
