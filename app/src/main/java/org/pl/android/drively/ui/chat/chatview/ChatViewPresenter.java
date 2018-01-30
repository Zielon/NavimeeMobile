package org.pl.android.drively.ui.chat.chatview;

import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.injection.ConfigPersistent;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import timber.log.Timber;

@ConfigPersistent
public class ChatViewPresenter extends BasePresenter<ChatViewMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;

    @Inject
    public ChatViewPresenter(DataManager dataManager) {
        mDataManager = dataManager;
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


    public void setMessageListener(String roomId) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("MESSAGES").document(roomId).collection("MESSAGES")
                .orderBy("timestamp")
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Timber.w("Listen failed.", e);
                        return;
                    }
                    List messageList = value.toObjects(Message.class);
                    if (getMvpView() != null) {
                        getMvpView().roomChangesListerSet(messageList);
                    }
                });

    }

    public void addMessage(String roomId, Message newMessage) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("MESSAGES")
                .document(roomId).collection("MESSAGES").add(newMessage)
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
        return mDataManager.getFirebaseService().getFirebaseStorage().getReference("AVATARS/" + avatar);
    }
}
