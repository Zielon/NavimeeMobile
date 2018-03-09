package org.pl.android.drively.ui.chat.chatview;

import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.data.model.chat.GroupMessage;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.data.model.chat.PrivateMessage;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.FirebasePaths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static org.pl.android.drively.util.FirebasePaths.AVATARS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_GROUPS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_PRIVATE;
import static org.pl.android.drively.util.ReflectionUtil.nameof;

public class ChatViewPresenter extends BasePresenter<ChatViewMvpView> {

    private final DataManager mDataManager;
    private final UsersRepository usersRepository;
    private Disposable mDisposable;

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

    public void setMessageListener(String roomId, boolean isGroupChat) {
        String messagePath = isGroupChat ? MESSAGES_GROUPS : MESSAGES_PRIVATE;
        mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(messagePath)
                .document(mDataManager.getPreferencesHelper().getCountry())
                .collection(roomId)
                .orderBy("timestamp")
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Timber.w("Listen failed.", e);
                        return;
                    }
                    List messageList = isGroupChat ? value.toObjects(GroupMessage.class) : value.toObjects(PrivateMessage.class);
                    if (getMvpView() != null) {
                        getMvpView().roomChangesListerSet(messageList);
                    }
                });
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
