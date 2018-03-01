package org.pl.android.drively.ui.chat.group;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.chat.RoomMember;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.ui.base.BasePresenter;
import org.pl.android.drively.util.FirebasePaths;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import timber.log.Timber;

import static org.pl.android.drively.util.FirebasePaths.MEMBERS;
import static org.pl.android.drively.util.FirebasePaths.ROOM_DETAILS;

public class GroupPresenter extends BasePresenter<GroupMvpView> {

    private final DataManager mDataManager;

    private ListenerRegistration mListener;
    private Context mContext;

    @Inject
    public GroupPresenter(DataManager dataManager, @ActivityContext Context context) {
        mDataManager = dataManager;
        mContext = context;
    }

    @Override
    public void attachView(GroupMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }


    public void getListGroup() {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS)
                .document(getId()).collection(FirebasePaths.GROUP).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> rooms = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            if (document.get("roomId") != null && !rooms.contains(document.get("roomId"))) {
                                rooms.add(document.getString("roomId"));
                            }
                        }
                        if (getMvpView() != null) {
                            getMvpView().setGroupList(rooms);
                        }
                    } else {
                        if (getMvpView() != null) {
                            getMvpView().getGroupError();
                        }
                    }
                });
    }

    public void getGroupInfo(List<String> roomsIds) {
        List<Task<Task<Room>>> tasks = new ArrayList<>();
        DocumentReference groupRef = mDataManager.getFirebaseService().getFirebaseFirestore()
                .collection(FirebasePaths.GROUP).document(mDataManager.getPreferencesHelper().getCountry());

        for (String id : roomsIds) {
            tasks.add(groupRef.collection(id).document(ROOM_DETAILS).get()
                    .continueWith(task -> {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot != null && snapshot.exists()) {
                            Room room = snapshot.toObject(Room.class);
                            return groupRef.collection(id).document(MEMBERS).collection(MEMBERS).get().continueWith(members -> {
                                if(members.isSuccessful()){
                                    for(DocumentSnapshot doc : members.getResult().getDocuments())
                                        room.getMembers().add(doc.toObject(RoomMember.class));
                                }
                                return room;
                            });

                        }
                        return Tasks.forResult(null);
                    }));
        }

        Tasks.whenAll(tasks).addOnSuccessListener(friends -> {
            List<Task<Room>> friendsTasks = new ArrayList<>();
            for (Task<Task<Room>> task : tasks)
                if (task.isSuccessful() && task.getResult() != null)
                    friendsTasks.add(task.getResult());

            Tasks.whenAll(friendsTasks).addOnSuccessListener(avatars -> {
                List<Room> rooms = new ArrayList<>();
                for (Task<Room> task : friendsTasks)
                    if (task.isSuccessful() && task.getResult() != null && getMvpView() != null)
                        rooms.add(task.getResult());

                getMvpView().setGroupInfo(rooms);
            });
        });
    }

    public void deleteGroup(Room group) {
        deleteCollection(mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.GROUP)
                .document(group.getId()).collection(FirebasePaths.MEMBERS), 1000, Executors.newSingleThreadExecutor());
        mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.GROUP).document(group.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (getMvpView() != null) {
                        getMvpView().deleteGroupSuccess(group);
                    }
                })
                .addOnFailureListener(e -> getMvpView().deleteGroupFailure());

    }


    private Task<Void> deleteCollection(final CollectionReference collection,
                                        final int batchSize,
                                        Executor executor) {
        // Perform the delete operation on the provided Executor, which allows us to use
        // simpler synchronous logic without blocking the main thread.
        return Tasks.call(executor, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Get the first batch of documents in the collection
                Query query = collection.orderBy(FieldPath.documentId()).limit(batchSize);

                // Get a list of deleted documents
                List<DocumentSnapshot> deleted = deleteQueryBatch(query);

                // While the deleted documents in the last batch indicate that there
                // may still be more documents in the collection, page down to the
                // next batch and delete again
                while (deleted.size() >= batchSize) {
                    // Move the query cursor to start after the last doc in the batch
                    DocumentSnapshot last = deleted.get(deleted.size() - 1);
                    query = collection.orderBy(FieldPath.documentId())
                            .startAfter(last.getId())
                            .limit(batchSize);

                    deleted = deleteQueryBatch(query);
                }

                return null;
            }
        });

        // Logger.LOG(new Log(LogTypes.DELETION, collection.getPath(), deletedAll));
    }


    private List<DocumentSnapshot> deleteQueryBatch(final Query query) throws Exception {
        QuerySnapshot querySnapshot = Tasks.await(query.get());

        WriteBatch batch = query.getFirestore().batch();
        for (DocumentSnapshot snapshot : querySnapshot) {
            batch.delete(snapshot.getReference());
        }
        Tasks.await(batch.commit());
        return querySnapshot.getDocuments();
    }

    public void deleteGroupReference(int index, Room group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS)
                .document(group.getMembers().get(index).getMemberId())
                .collection(FirebasePaths.GROUP).whereEqualTo("roomId", group.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0) {
                            if (getMvpView() != null) {
                                getMvpView().onFailureGroupReference();
                                return;
                            }
                        }
                        for (DocumentSnapshot document : task.getResult()) {
                            Timber.d(document.getId() + " => " + document.getData());
                            mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(group.getMembers().get(index).getMemberId()).collection(FirebasePaths.GROUP)
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (getMvpView() != null) {
                                            getMvpView().onSuccessDeleteGroupReference(group, index);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Timber.w("Error deleting document", e);
                                        if (getMvpView() != null) {
                                            getMvpView().onFailureGroupReference();
                                        }
                                    });
                        }
                    } else {
                        Timber.d("Listen failed");
                        if (getMvpView() != null) {
                            getMvpView().onFailureGroupReference();
                        }
                    }
                });
    }

    public void leaveGroup(Room group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.GROUP)
                .document(group.getId())
                .collection(FirebasePaths.MEMBERS)
                .document(getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    if (getMvpView() != null) {
                        getMvpView().onSuccessLeaveGroup(group);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getMvpView() != null) {
                        getMvpView().onFailureLeaveGroup();
                    }
                });
    }


    public void leaveGroupUserReference(Room group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS)
                .document(getId())
                .collection(FirebasePaths.GROUP).whereEqualTo("roomId", group.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Timber.d(document.getId() + " => " + document.getData());
                            mDataManager.getFirebaseService().getFirebaseFirestore().collection(FirebasePaths.USERS).document(getId()).collection(FirebasePaths.GROUP)
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (getMvpView() != null) {
                                            getMvpView().onSuccessLeaveGroupReference(group);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Timber.w("Error deleting document", e);
                                        if (getMvpView() != null) {
                                            getMvpView().onFailureLeaveGroup();
                                        }
                                    });
                        }
                    } else {
                        Timber.d("Listen failed");
                        if (getMvpView() != null) {
                            getMvpView().onFailureGroupReference();
                        }
                    }
                });

    }


    public String getId() {
        return mDataManager.getFirebaseService().getFirebaseAuth().getCurrentUser().getUid();
    }


}
