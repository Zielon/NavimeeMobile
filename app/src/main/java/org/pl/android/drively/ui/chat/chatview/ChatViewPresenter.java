package org.pl.android.drively.ui.chat.chatview;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.chat.GroupMessage;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.data.model.chat.PrivateMessage;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_GROUPS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_PRIVATE;

public class ChatViewPresenter extends BasePresenter<ChatViewMvpView> {

    private static final int MESSAGES_SLICE_QUANTITY = 20;

    private final DataManager mDataManager;
    private final UsersRepository usersRepository;
    private DocumentSnapshot lastSnapshot;

    private String roomId;

    private boolean isGroupChat;

    private ListenerRegistration newMessagesListener;

    private Query baseQuery;

    @Inject
    public ChatViewPresenter(DataManager dataManager, UsersRepository usersRepository) {
        this.mDataManager = dataManager;
        this.usersRepository = usersRepository;
    }

    @Override
    public void attachView(ChatViewMvpView mvpView) {
        super.attachView(mvpView);
    }

    public void setIntentDataAndBuildBaseQuery(boolean isGroupChat, String roomId) {
        this.roomId = roomId;
        this.isGroupChat = isGroupChat;
        String messagePath = isGroupChat ? MESSAGES_GROUPS : MESSAGES_PRIVATE;
        baseQuery = mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(messagePath)
                .document(mDataManager.getPreferencesHelper().getCountry())
                .collection(roomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    public void addFriend(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        usersRepository.addFriend(userId, idFriend);
    }

    public void getSingleBatch() {
        Query messagesQuery = baseQuery;
        if (lastSnapshot != null) {
            messagesQuery = messagesQuery.startAfter(lastSnapshot);
        }
        messagesQuery = messagesQuery.limit(MESSAGES_SLICE_QUANTITY);
        messagesQuery.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List messageList = isGroupChat ? task.getResult().toObjects(GroupMessage.class) : task.getResult().toObjects(PrivateMessage.class);
                        int lastIndex = messageList.size() - 1;
                        if (lastIndex == MESSAGES_SLICE_QUANTITY - 1) {
                            lastSnapshot = task.getResult().getDocuments().get(messageList.size() - 1);
                        } else {
                            getMvpView().setAllMessagesLoaded(true);
                        }
                        if (ChatViewPresenter.this.getMvpView() != null) {
                            ChatViewPresenter.this.getMvpView().addNewBatch(messageList);
                        }
                    } else {
                        Timber.d(task.getException());
                    }
                }
        );
    }

    public void updateNewMessagesListenerTimestamp(Long firstMessageTimestamp) {
        Query messagesQuery = baseQuery
                .whereGreaterThan("timestamp", firstMessageTimestamp);
        if (newMessagesListener != null) {
            newMessagesListener.remove();
        }
        newMessagesListener = messagesQuery.addSnapshotListener((result, e) -> {
            if (e != null) {
                Timber.d(e);
                return;
            }
            List newMessages = isGroupChat ? result.toObjects(GroupMessage.class) : result.toObjects(PrivateMessage.class);
            if (ChatViewPresenter.this.getMvpView() != null && !newMessages.isEmpty()) {
                ChatViewPresenter.this.getMvpView().addMessagesAtTheBeginning(newMessages);
            }
        });
    }

    public Bitmap getFriendAvatar(String friendId){
        return mDataManager.getPreferencesHelper().getFriendAvatar(friendId);
    }

    public void addMessage(Message newMessage) {
        String messagePath = newMessage instanceof PrivateMessage ? MESSAGES_PRIVATE : MESSAGES_GROUPS;
        mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(messagePath)
                .document(mDataManager.getPreferencesHelper().getCountry())
                .collection(roomId).add(newMessage)
                .addOnSuccessListener(documentReference -> Timber.w("DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Timber.w("Error writing document", e));
    }

    public String getId() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }

    public User getUserInfo() {
        return mDataManager.getPreferencesHelper().getUserInfo();
    }

    public StorageReference getStorageReference(String avatar) {
        return mDataManager.getFirebaseService().getFirebaseStorage().getReference(String.format("%s/%s", AVATARS, avatar));
    }
}
