package org.pl.android.drively.ui.chat.group;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;

import org.pl.android.drively.data.DataManager;
import org.pl.android.drively.data.model.chat.Group;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.injection.ActivityContext;
import org.pl.android.drively.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import timber.log.Timber;

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
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS")
                .document(getId()).collection("GROUP").get()
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


    public void getGroupInfo(int groupIndex, String id) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP").document(id)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Room room = new Room();
                    room.groupInfo.put("admin", task.getResult().getString("admin"));
                    room.groupInfo.put("name", task.getResult().getString("name"));
                    mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP")
                            .document(id).collection("MEMBERS")
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    room.member.add(document.getId());
                                }
                                if (getMvpView() != null) {
                                    getMvpView().setGroupInfo(groupIndex, room);
                                }
                            } else {
                                Timber.w("Error geting document");
                            }
                        }
                    });
                } else {
                    Timber.w("Error geting document");
                }
            } else {
                Timber.w("Error geting document");
            }
        });
    }


    public void deleteGroup(Group group) {
        deleteCollection(mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP")
                .document(group.id).collection("MEMBERS"), 1000, Executors.newSingleThreadExecutor());
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP").document(group.id)
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

    public void deleteGroupReference(int index, Group group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS")
                .document(group.member.get(index))
                .collection("GROUP").whereEqualTo("roomId", group.id)
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
                            mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(group.member.get(index)).collection("GROUP")
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

    public void leaveGroup(Group group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("GROUP")
                .document(group.id)
                .collection("MEMBERS")
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


    public void leaveGroupUserReference(Group group) {
        mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS")
                .document(getId())
                .collection("GROUP").whereEqualTo("roomId", group.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Timber.d(document.getId() + " => " + document.getData());
                            mDataManager.getFirebaseService().getFirebaseFirestore().collection("USERS").document(getId()).collection("GROUP")
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

    public StorageReference getStorageReference(String avatar) {
        return mDataManager.getFirebaseService().getFirebaseStorage().getReference("AVATARS/" + avatar);
    }

}
