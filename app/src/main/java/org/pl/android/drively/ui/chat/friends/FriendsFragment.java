package org.pl.android.drively.ui.chat.friends;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.data.model.chat.PrivateMessage;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.data.FriendDB;
import org.pl.android.drively.ui.chat.friendsearch.FriendModel;
import org.pl.android.drively.ui.chat.friendsearch.FriendSearchDialogCompat;
import org.pl.android.drively.util.ChatUtils;
import org.pl.android.drively.util.Const;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import ir.mirrajabi.searchdialog.core.BaseFilter;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.Searchable;
import timber.log.Timber;

public class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FriendsMvpView {

    public static final String ACTION_DELETE_FRIEND = "com.android.rivchat.DELETE_FRIEND";
    public static int ACTION_START_CHAT = 1;
    public FragFriendClickFloatButton onClickFloatButton;
    LovelyProgressDialog dialogWaitDeleting;
    @Inject
    FriendsPresenter mFriendsPresenter;
    @BindView(R.id.fab_friends)
    FloatingActionButton fabFriendsButton;
    Friend mUserInfo;
    String mIdFriend;
    private RecyclerView recyclerListFrends;
    private ListFriendsAdapter adapter;
    private ListFriend dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CountDownTimer detectFriendOnline;
    private LovelyProgressDialog dialogWait;
    private View layout;
    private BroadcastReceiver deleteFriendReceiver;

    public FriendsFragment() {
        onClickFloatButton = new FragFriendClickFloatButton();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), Const.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                mFriendsPresenter.updateUserStatus();
                mFriendsPresenter.updateFriendStatus(dataListFriend);
            }

            @Override
            public void onFinish() {

            }
        };
        if (dataListFriend == null) {
            dataListFriend = FriendDB.getInstance(getContext()).getListFriend();
            if (dataListFriend.getListFriend().size() > 0) {
                listFriendID = new ArrayList<>();
                for (Friend friend : dataListFriend.getListFriend()) {
                    listFriendID.add(friend.id);
                }
                detectFriendOnline.start();
            }
        }
        layout = inflater.inflate(R.layout.fragment_people, container, false);
        ButterKnife.bind(this, layout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrends = (RecyclerView) layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        dialogWait = new LovelyProgressDialog((getContext()));
        dialogWaitDeleting = new LovelyProgressDialog((getContext()));
        adapter = new ListFriendsAdapter(getContext(), dataListFriend, this, dialogWaitDeleting, mFriendsPresenter.getId());
        recyclerListFrends.setAdapter(adapter);

        if (listFriendID == null) {
            listFriendID = new ArrayList<>();
            mSwipeRefreshLayout.setRefreshing(true);
            getListFriendUId();
        }

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (Friend friend : dataListFriend.getListFriend()) {
                    if (idDeleted.equals(friend.id)) {
                        ArrayList<Friend> friends = dataListFriend.getListFriend();
                        friends.remove(friend);
                        FriendDB.getInstance(getContext()).deleteFriend(friend);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };
        fabFriendsButton.setOnClickListener(onClickFloatButton.getInstance(getContext()));
        mFriendsPresenter.attachView(this);
        mFriendsPresenter.getUserAvatar();
        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFriendsPresenter.detachView();
        detectFriendOnline.cancel();
        dataListFriend.getListFriend().clear();
        FriendDB.getInstance(getContext()).dropDB();
        listFriendID = null;
        getContext().unregisterReceiver(deleteFriendReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_START_CHAT == requestCode && data != null && ListFriendsAdapter.mapMark != null) {
            ListFriendsAdapter.mapMark.put(data.getStringExtra("idFriend"), false);
        }
    }

    @Override
    public void onRefresh() {
        listFriendID.clear();
        dataListFriend.getListFriend().clear();
        adapter.notifyDataSetChanged();
        FriendDB.getInstance(getContext()).dropDB();
        detectFriendOnline.cancel();
        ListFriendsAdapter.mapMark.clear();
        ListFriendsAdapter.mapChildListener.clear();
        ListFriendsAdapter.mapQuery.clear();
        ListFriendsAdapter.mapQueryOnline.clear();
        ListFriendsAdapter.mapChildListenerOnline.clear();
        getListFriendUId();
    }

    @Override
    public void showError() {

    }

    private void checkBeforAddFriend(final String idFriend, Friend userInfo) {
        dialogWait.setCancelable(false)
                .setIcon(R.drawable.ic_add_friend)
                .setTitle(getResources().getString(R.string.add_friend))
                .setTopColorRes(R.color.primary)
                .show();

        //Check xem da ton tai id trong danh sach id chua
        if (listFriendID.contains(idFriend)) {
            dialogWait.dismiss();
        } else {
            addFriend(idFriend, true);
            mUserInfo = userInfo;
            mIdFriend = idFriend;
        }
    }

    private void addFriend(final String idFriend, boolean isIdFriend) {
        if (idFriend != null) {
            if (isIdFriend) {
                mFriendsPresenter.addFriend(idFriend);
            } else {
                mFriendsPresenter.addFriendForFriendId(idFriend);
            }
        } else {
            listFriendID.add(mIdFriend);
            dataListFriend.getListFriend().add(mUserInfo);
            Collections.sort(dataListFriend.getListFriend());
            FriendDB.getInstance(getContext()).addFriend(mUserInfo);
            adapter.notifyDataSetChanged();
            dialogWait.dismiss();
            new LovelyInfoDialog(getContext())
                    .setTopColorRes(R.color.primary)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle(getResources().getString(R.string.success))
                    .setMessage(getResources().getString(R.string.add_friend_success))
                    .show();
        }
    }

    @Override
    public void addFriendSuccess(String idFriend) {
        addFriend(idFriend, false);
    }

    @Override
    public void addFriendFailure() {
        dialogWait.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.primary)
                .setIcon(R.drawable.ic_add_friend)
                .setTitle(getResources().getString(R.string.failure))
                .setMessage(getResources().getString(R.string.add_friend_failure))
                .show();
    }

    @Override
    public void addFriendIsNotIdFriend() {
        addFriend(null, false);
    }


    @Override
    public void onSuccessDeleteFriend(String idFriend) {
        listFriendID.remove(idFriend);
        dialogWaitDeleting.dismiss();

        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.primary)
                .setTitle(getResources().getString(R.string.success))
                .setIcon(getResources().getDrawable(R.drawable.ic_delete_black_24dp))
                .setIconTintColor(getResources().getColor(R.color.white))
                .setMessage(getResources().getString(R.string.delete_friend_success))
                .show();

        Intent intentDeleted = new Intent(FriendsFragment.ACTION_DELETE_FRIEND);
        intentDeleted.putExtra("idFriend", idFriend);
        getContext().sendBroadcast(intentDeleted);
    }

    @Override
    public void onFailureDeleteFriend() {
        dialogWaitDeleting.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.primary)
                .setTitle(getResources().getString(R.string.failure))
                .setIcon(getResources().getDrawable(R.drawable.ic_delete_black_24dp))
                .setIconTintColor(getResources().getColor(R.color.white))
                .setMessage(getResources().getString(R.string.delete_friend_failure))
                .show();
    }

    private void getListFriendUId() {
        mFriendsPresenter.getListFriendUId();
    }

    @Override
    public void listFriendFound(List<String> friendList) {
        listFriendID.clear();
        listFriendID.addAll(friendList);
        mFriendsPresenter.getAllFriendInfo(listFriendID);
    }

    @Override
    public void listFriendNotFound() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void allFriendsFound() {
        Collections.sort(dataListFriend.getListFriend());
        FriendDB.getInstance(getContext()).addListFriend(dataListFriend);
        adapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
        detectFriendOnline.start();
    }

    @Override
    public void addFriendInfo(Friend friend) {
        if (!dataListFriend.getListFriend().contains(friend)) {
            dataListFriend.getListFriend().add(friend);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public class FragFriendClickFloatButton implements View.OnClickListener {
        Context context;

        public FragFriendClickFloatButton() {
        }

        public FragFriendClickFloatButton getInstance(Context context) {
            this.context = context;
            return this;
        }

        @Override
        public void onClick(final View view) {

            FriendSearchDialogCompat searchDialogCompat =
                    new FriendSearchDialogCompat(view.getContext(), getResources().getString(R.string.find_friends),
                            getResources().getString(R.string.find_friends_who), null, new ArrayList<>(),
                            (BaseSearchDialogCompat dialog, FriendModel item, int position) -> {
                                if (item.getId().equals(mFriendsPresenter.getId())) {
                                    Toast.makeText(view.getContext(), getResources().getString(R.string.email_not_valid),
                                            Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Friend friend = new Friend();
                                    friend.name = item.getName();
                                    friend.email = item.getEmail();
                                    friend.id = item.getId();
                                    friend.avatar = item.getAvatar();
                                    friend.idRoom = ChatUtils.getRoomId(item.getId(), mFriendsPresenter.getId());
                                    mFriendsPresenter.getStorageReference(friend.avatar)
                                            .getBytes(Const.FIVE_MEGABYTE)
                                            .addOnSuccessListener(bytes -> {
                                                friend.avatarBytes = bytes;
                                                checkBeforAddFriend(item.getId(), friend);
                                                dialog.dismiss();
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    checkBeforAddFriend(item.getId(), friend);
                                                    dialog.dismiss();
                                                }
                                            });
                                }
                            });

            BaseFilter apiFilter = new BaseFilter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    return null;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mFriendsPresenter.findFriend(this, searchDialogCompat, charSequence.toString().trim(), listFriendID);
                }
            };

            searchDialogCompat.setFilter(apiFilter);
            searchDialogCompat.show();
            searchDialogCompat.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        public class SampleSearchModel implements Searchable {
            private String mTitle;

            public SampleSearchModel(String title) {
                mTitle = title;
            }

            @Override
            public String getTitle() {
                return mTitle;
            }

            public SampleSearchModel setTitle(String title) {
                mTitle = title;
                return this;
            }
        }
    }
}

class ListFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static Map<String, Query> mapQuery;
    public static Map<String, DocumentReference> mapQueryOnline;
    public static Map<String, EventListener<QuerySnapshot>> mapChildListener;
    public static Map<String, EventListener<DocumentSnapshot>> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    LovelyProgressDialog dialogWaitDeleting;
    private ListFriend listFriend;
    private Context context;
    private FriendsFragment fragment;
    private String currentUser;

    public ListFriendsAdapter(Context context, ListFriend listFriend, FriendsFragment fragment, LovelyProgressDialog dialogWaitDeleting, String currentUser) {
        this.listFriend = listFriend;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        this.fragment = fragment;
        this.dialogWaitDeleting = dialogWaitDeleting;
        this.currentUser = currentUser;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false);
        return new ItemFriendViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final String name = listFriend.getListFriend().get(position).name;
        final String id = listFriend.getListFriend().get(position).id;
        final String idRoom = listFriend.getListFriend().get(position).idRoom;
        final String avata = listFriend.getListFriend().get(position).avatar;
        ((ItemFriendViewHolder) holder).txtName.setText(name);

        ((View) ((ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                .setOnClickListener(view -> {
                    ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                    ((ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
                    Intent intent = new Intent(context, ChatViewActivity.class);
                    intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, name);
                    ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
                    idFriend.add(id);
                    intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
                    intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, idRoom);
                    ChatViewActivity.bitmapAvataFriend = new HashMap<>();
                    if (!avata.equals(Const.STR_DEFAULT_AVATAR)) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) ((ItemFriendViewHolder) holder).avata.getDrawable();
                        if (bitmapDrawable != null)
                            ChatViewActivity.bitmapAvataFriend.put(id, bitmapDrawable.getBitmap());
                    } else {
                        ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
                    }

                    mapMark.put(id, null);
                    fragment.startActivityForResult(intent, FriendsFragment.ACTION_START_CHAT);
                });

        ((View) ((ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                .setOnLongClickListener(view -> {
                    String friendName = (String) ((ItemFriendViewHolder) holder).txtName.getText();

                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.delete_friend))
                            .setMessage(context.getResources().getString(R.string.delete_friend_sure) + " " + friendName + " ?")
                            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                final String idFriendRemoval = listFriend.getListFriend().get(position).id;
                                dialogWaitDeleting.setTitle(context.getResources().getString(R.string.deleting))
                                        .setCancelable(false)
                                        .setIcon(context.getResources().getDrawable(R.drawable.ic_delete_black_24dp))
                                        .setIconTintColor(context.getResources().getColor(R.color.white))
                                        .setTopColorRes(R.color.primary)
                                        .show();
                                deleteFriend(idFriendRemoval);
                            })
                            .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show();

                    return true;
                });

        if (listFriend.getListFriend().get(position).message != null) {
            ((ItemFriendViewHolder) holder).txtMessage.setVisibility(View.VISIBLE);
            ((ItemFriendViewHolder) holder).txtTime.setVisibility(View.VISIBLE);

            ((ItemFriendViewHolder) holder).txtMessage.setText(listFriend.getListFriend().get(position).message.text);

            if (!listFriend.getListFriend().get(position).message.idSender.equals(currentUser)) {
                ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT_BOLD);
            }

            String time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(listFriend.getListFriend().get(position).message.timestamp));
            String today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));
            if (today.equals(time)) {
                ((ItemFriendViewHolder) holder).txtTime.setText(new SimpleDateFormat("HH:mm").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
            } else {
                ((ItemFriendViewHolder) holder).txtTime.setText(new SimpleDateFormat("MMM d").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
            }
        } else {
            ((ItemFriendViewHolder) holder).txtMessage.setVisibility(View.GONE);
            ((ItemFriendViewHolder) holder).txtTime.setVisibility(View.GONE);
            if (mapQuery.get(id) == null && mapChildListener.get(id) == null) {
                mapQuery.put(id, this.fragment.mFriendsPresenter.getLastMessage(idRoom));
                mapChildListener.put(id, (snapshots, e) -> {
                    if (e != null) {
                        Timber.e("Listen failed.", e);
                        return;
                    }
                    try {
                        for (DocumentSnapshot documentSnapshot : snapshots.getDocuments()) {
                            PrivateMessage message = new PrivateMessage();
                            message.idSender = (String) documentSnapshot.getData().get("idSender");
                            message.text = (String) documentSnapshot.getData().get("text");
                            message.timestamp = (long) documentSnapshot.getData().get("timestamp");
                            listFriend.getListFriend().get(position).message = message;
                            notifyDataSetChanged();
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        Timber.w("Exception occured.", ex);
                    } catch (Exception ex) {
                        Timber.w("Exception occured.", ex);
                    }
                });
                mapQuery.get(id).addSnapshotListener(mapChildListener.get(id));
                mapMark.put(id, true);
            } else {
                // mapQuery.get(id).removeEventListener(mapChildListener.get(id));
                mapQuery.get(id).addSnapshotListener(mapChildListener.get(id));
                mapMark.put(id, true);
            }
        }
        if (listFriend.getListFriend().get(position).avatar.equals(Const.STR_DEFAULT_AVATAR) || listFriend.getListFriend().get(position).avatarBytes == null) {
            ((ItemFriendViewHolder) holder).avata.setImageResource(R.drawable.default_avatar);
        } else {
            Bitmap src = BitmapFactory.decodeByteArray(listFriend.getListFriend().get(position).avatarBytes, 0, listFriend.getListFriend().get(position).avatarBytes.length);
            ((ItemFriendViewHolder) holder).avata.setImageBitmap(src);
        }

        if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
            mapQueryOnline.put(id, this.fragment.mFriendsPresenter.getStatus(id));
            mapChildListenerOnline.put(id, (documentSnapshot, e) -> {
                if (e != null) {
                    Timber.w("Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists() && documentSnapshot.get("isOnline") != null && listFriend.getListFriend().size() >= position) {
                    try {
                        listFriend.getListFriend().get(position).status.isOnline = (boolean) documentSnapshot.get("isOnline");
                        notifyDataSetChanged();
                    } catch (IndexOutOfBoundsException ex) {
                        Timber.w("Exception occured.", ex);
                    } catch (Exception ex) {
                        Timber.w("Exception occured.", ex);
                    }
                }
            });
            mapQueryOnline.get(id).addSnapshotListener(mapChildListenerOnline.get(id));
        }

        if (listFriend.getListFriend().get(position).status.isOnline) {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(7);
            ((ItemFriendViewHolder) holder).avata.setBorderColor(context.getResources().getColor(R.color.button_background));
        } else {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(0);
        }
    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;
    }

    private void deleteFriend(final String idFriend) {
        if (idFriend != null) {
            this.fragment.mFriendsPresenter.deleteFriend(idFriend);
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.primary)
                    .setIcon(context.getResources().getDrawable(R.drawable.ic_delete_black_24dp))
                    .setIconTintColor(context.getResources().getColor(R.color.white))
                    .setTitle(context.getResources().getString(R.string.failure))
                    .setMessage(context.getResources().getString(R.string.delete_friend_failure))
                    .show();
        }
    }
}


class ItemFriendViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView avata;
    public TextView txtName, txtTime, txtMessage;
    private Context context;

    ItemFriendViewHolder(Context context, View itemView) {
        super(itemView);
        avata = (CircleImageView) itemView.findViewById(R.id.icon_avata);
        txtName = (TextView) itemView.findViewById(R.id.txtName);
        txtTime = (TextView) itemView.findViewById(R.id.txtTime);
        txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
        this.context = context;
    }
}

