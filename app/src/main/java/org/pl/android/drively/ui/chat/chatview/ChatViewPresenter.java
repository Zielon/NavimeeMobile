package org.pl.android.drively.ui.chat.chatview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.annimon.stream.Stream;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import org.apache.commons.collections4.ListUtils;
import org.pl.android.drively.contracts.repositories.UsersRepository;
import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.User;
import org.pl.android.drively.data.model.chat.Message;
import org.pl.android.drively.data.model.chat.PrivateMessage;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.Const;
import org.pl.android.drively.util.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static org.pl.android.drively.ui.main.MainActivity.DEFAULT_AVATAR;
import static org.pl.android.drively.util.FirebasePaths.AVATARS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_GROUPS;
import static org.pl.android.drively.util.FirebasePaths.MESSAGES_PRIVATE;

public class ChatViewPresenter extends BasePresenter<ChatViewMvpView> {

    private static final int MESSAGES_SLICE_QUANTITY = 20;

    private final DataManager mDataManager;
    private final UsersRepository usersRepository;
    private DocumentSnapshot lastSnapshot;
    private HashMap<String, Bitmap> friendsAvatars = new HashMap<>();

    private String roomId;

    private boolean isGroupChat;

    private ListenerRegistration newMessagesListener;

    private SchedulerProvider schedulerProvider;

    private CompositeDisposable compositeDisposable;

    private Query baseQuery;

    @Inject
    public ChatViewPresenter(DataManager dataManager, UsersRepository usersRepository,
                             CompositeDisposable compositeDisposable, SchedulerProvider schedulerProvider) {
        this.mDataManager = dataManager;
        this.usersRepository = usersRepository;
        this.compositeDisposable = compositeDisposable;
        this.schedulerProvider = schedulerProvider;
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

    public Bitmap getFriendAvatar(String friendId) {
        return friendsAvatars.get(friendId);
    }

    public void addFriend(String idFriend) {
        String userId = mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
        usersRepository.addFriend(userId, idFriend);
    }

    public void getSingleBatch() {
        Query messagesQuery = baseQuery;
        if (lastSnapshot != null)
            messagesQuery = messagesQuery.startAfter(lastSnapshot);

        messagesQuery.limit(MESSAGES_SLICE_QUANTITY).get()
                .addOnSuccessListener(snapshot -> {
                            List<Message> messageList = snapshot.toObjects(Message.class);
                            List<String> sendersAvatars = Stream.of(messageList)
                                    .map(message -> message.idSender)
                                    .filter(id -> !id.equals(getUserInfo().getId())).distinct().toList();
                            List<String> missing = ListUtils.subtract(sendersAvatars, new ArrayList<>(friendsAvatars.keySet()));

                            List<Task<byte[]>> tasks = Stream.of(missing).map(avatar ->
                                    FirebaseStorage.getInstance().getReference(getPath(avatar)).getBytes(Const.FIVE_MEGABYTE)
                                            .addOnSuccessListener(bytes -> friendsAvatars.put(avatar, BitmapFactory.decodeByteArray(bytes, 0, bytes.length)))
                                            .addOnFailureListener(failure -> friendsAvatars.put(avatar, DEFAULT_AVATAR))).toList();

                            Tasks.whenAllComplete(tasks).addOnSuccessListener(success -> {
                                int lastIndex = messageList.size() - 1;
                                if (lastIndex == MESSAGES_SLICE_QUANTITY - 1) {
                                    lastSnapshot = snapshot.getDocuments().get(messageList.size() - 1);
                                } else {
                                    getMvpView().setAllMessagesLoaded(true);
                                }
                                if (this.getMvpView() != null)
                                    filterAndSortDataBeforeAdding(messageList, false);
                            });
                        }
                );
    }

    private void filterAndSortDataBeforeAdding(List<Message> messages, boolean isNewMessages) {
        compositeDisposable.add(Observable.fromIterable(messages)
                .filter(message -> !getMvpView().getConversation().getListMessageData().contains(message))
                .sorted((message1, message2) -> Long.valueOf(message2.getTimestamp()).compareTo(message1.getTimestamp()))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(message -> {
                    if (isNewMessages) {
                        getMvpView().addMessagesAtTheBeginning(Collections.singletonList(message));
                    } else {
                        getMvpView().addNewBatch(Collections.singletonList(message));
                    }
                }));
    }

    public void setNewMessagesListenerTimestamp(Long firstMessageTimestamp) {
        Query messagesQuery = baseQuery.whereGreaterThanOrEqualTo("timestamp", firstMessageTimestamp);

        if (newMessagesListener != null) newMessagesListener.remove();

        newMessagesListener = messagesQuery.addSnapshotListener((result, e) -> {
            if (e != null) {
                Timber.d(e);
                return;
            }
            List<Message> newMessages = result.toObjects(Message.class);
            if (this.getMvpView() != null && !newMessages.isEmpty())
                filterAndSortDataBeforeAdding(newMessages, true);
        });
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

    public String getPath(String avatar) {
        return String.format("%s/%s", AVATARS, avatar);
    }
}
