package org.pl.android.drively.ui.chat.chatview;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import io.reactivex.disposables.Disposable;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lombok.Setter;
import timber.log.Timber;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_GROUPS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_PRIVATE;
import static org.pl.android.drively.util.FirebasePaths.USERS;

public class ChatViewPresenter extends BasePresenter<ChatViewMvpView> {

    public static final int MESSAGES_SLICE_QUANTITY = 20;

    private final DataManager mDataManager;
    private final UsersRepository usersRepository;
    private Disposable mDisposable;
    private Query messagesQuery;
    private DocumentSnapshot lastSnapshot;
    private Message lastMessage;

    @Setter
    String roomId;
    @Setter
    boolean isGroupChat;

    Long batchCounter = 1L;
    private ListenerRegistration newMessagesListener;

    @Inject
    public ChatViewPresenter(DataManager dataManager, UsersRepository usersRepository) {
        this.mDataManager = dataManager;
        this.usersRepository = usersRepository;
    }

    @Override
    public void attachView(ChatViewMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

    public void addFriend(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        usersRepository.addFriend(userId, idFriend);
    }

    public void getSingleBatch() {
        String messagePath = isGroupChat ? MESSAGES_GROUPS : MESSAGES_PRIVATE;
        messagesQuery = mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(messagePath)
                .document(mDataManager.getPreferencesHelper().getCountry())
                .collection(roomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        if (lastSnapshot != null) {
            messagesQuery = messagesQuery.startAfter(lastSnapshot);
        }
        messagesQuery = messagesQuery.limit(MESSAGES_SLICE_QUANTITY);
        messagesQuery.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<? extends Message> messageList = isGroupChat ? task.getResult().toObjects(GroupMessage.class) : task.getResult().toObjects(PrivateMessage.class);
                        int lastIndex = messageList.size() - 1;
                        if (lastIndex == MESSAGES_SLICE_QUANTITY - 1) {
                            lastSnapshot = task.getResult().getDocuments().get(messageList.size() - 1);
                            lastMessage = messageList.get(messageList.size() - 1);
                        } else {
                            getMvpView().setAllMessagesLoaded(true);
                        }
                        if (ChatViewPresenter.this.getMvpView() != null) {
                            ChatViewPresenter.this.getMvpView()
                                    .roomChangesListerSet(messageList);
                        }
                        batchCounter++;
                    } else {
                        Timber.d(task.getException());
                    }
                }
        );
    }

    public void updateNewMessagesListenerTimestamp(Long firstMessageTimestamp) {
        String messagePath = isGroupChat ? MESSAGES_GROUPS : MESSAGES_PRIVATE;
        messagesQuery = mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(messagePath)
                .document(mDataManager.getPreferencesHelper().getCountry())
                .collection(roomId)
                .whereGreaterThan("timestamp", firstMessageTimestamp)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MESSAGES_SLICE_QUANTITY * batchCounter);
        if (newMessagesListener != null) {
            newMessagesListener.remove();
        }
        newMessagesListener = messagesQuery.addSnapshotListener((result, e) -> {
            if (e != null) {
                Timber.d(e);
                return;
            }
            List<? extends Message> newMessages = isGroupChat ? result.toObjects(GroupMessage.class) : result.toObjects(PrivateMessage.class);
            if (ChatViewPresenter.this.getMvpView() != null && !newMessages.isEmpty()) {
                ChatViewPresenter.this.getMvpView()
                        .addMessagesAtTheBeginning(StreamSupport.stream(newMessages).sorted((message1, message2) ->
                                Integer.valueOf((int) message1.timestamp).compareTo((int) message2.timestamp)).collect(Collectors.toList()));
            }
        });
    }

    public void getAvatarPathFromFirebaseByUserEmail(String userId, AvatarPathCallback avatarPathCallback) {
        mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(USERS)
                .document(userId)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                mDataManager.getFirebaseService().getFirebaseStorage()
                        .getReference()
                        .child(String.format("%s/%s", AVATARS, user.getAvatar()))
                        .getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    avatarPathCallback.onAvatarPathReady(task.getResult().toString(), false);
                                } else {
                                    avatarPathCallback.onAvatarPathReady(null, true);
                                    Timber.d(task.getException());
                                }
                            }
                        });
            } else {
                Timber.d(task.getException());
            }
        });
    }

    @FunctionalInterface
    public interface AvatarPathCallback {
        void onAvatarPathReady(String path, boolean isDefault);
    }

    public void addMessage(String roomId, Message newMessage) {
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
