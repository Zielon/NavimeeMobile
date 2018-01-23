package org.pl.android.drively.ui.chat.chatview;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.pl.android.drively.data.DataManager;
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
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @SuppressLint("TimberArgCount")
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Timber.w( "Listen failed.", e);
                                return;
                            }
                            List messageList = value.toObjects(Message.class);
                            if (getMvpView() != null) {
                                getMvpView().roomChangesListerSet(messageList);
                            }
                        }
                    });

    }

    public void addMessage(String roomId, Message newMessage) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("MESSAGES")
                    .document(roomId).collection("MESSAGES").add(newMessage)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Timber.w( "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("TimberArgCount")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Timber.w( "Error writing document", e);
                        }
                    });

    }

    public String getId() {
        return  mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }
}
