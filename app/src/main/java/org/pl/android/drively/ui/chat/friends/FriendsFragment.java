package org.pl.android.drively.ui.chat.friends;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.Friend;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.data.FriendDB;
import org.pl.android.drively.ui.chat.friendsearch.FriendSearchDialogCompat;
import org.pl.android.drively.util.Const;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import ir.mirrajabi.searchdialog.core.BaseFilter;
import ir.mirrajabi.searchdialog.core.Searchable;
import timber.log.Timber;

public class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, FriendsMvpView {

    private RecyclerView recyclerListFrends;
    private ListFriendsAdapter adapter;
    public FragFriendClickFloatButton onClickFloatButton;
    private ListFriend dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private LovelyProgressDialog dialogFindAllFriend;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CountDownTimer detectFriendOnline;
    public static int ACTION_START_CHAT = 1;
    private LovelyProgressDialog dialogWait;
    LovelyProgressDialog dialogWaitDeleting;


    @Inject
    FriendsPresenter mFriendsPresenter;


    @BindView(R.id.fab_friends)
    FloatingActionButton fabFriendsButton;

    public static final String ACTION_DELETE_FRIEND = "com.android.rivchat.DELETE_FRIEND";

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
        View layout = inflater.inflate(R.layout.fragment_people, container, false);
        ButterKnife.bind(this, layout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrends = (RecyclerView) layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        dialogFindAllFriend = new LovelyProgressDialog(getContext());
        dialogWait = new LovelyProgressDialog((getContext()));
        dialogWaitDeleting = new LovelyProgressDialog((getContext()));
        adapter = new ListFriendsAdapter(getContext(), dataListFriend, this,dialogWaitDeleting);
        recyclerListFrends.setAdapter(adapter);

        if (listFriendID == null) {
            listFriendID = new ArrayList<>();
            dialogFindAllFriend.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Get all friend....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            getListFriendUId();
        }

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (Friend friend : dataListFriend.getListFriend()) {
                    if(idDeleted.equals(friend.id)){
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
        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);

        return layout;
    }

    @Override
    public void onDestroyView (){
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
                .setTitle("Add friend....")
                .setTopColorRes(R.color.colorPrimary)
                .show();

        //Check xem da ton tai id trong danh sach id chua
        if (listFriendID.contains(idFriend)) {
            dialogWait.dismiss();
            new LovelyInfoDialog(getContext())
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Friend")
                    .setMessage("ChatUser "+userInfo.getEmail() + " has been friend")
                    .show();
        } else {
            addFriend(idFriend, true);
            listFriendID.add(idFriend);
            dataListFriend.getListFriend().add(userInfo);
            FriendDB.getInstance(getContext()).addFriend(userInfo);
            adapter.notifyDataSetChanged();
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
            dialogWait.dismiss();
            new LovelyInfoDialog(getContext())
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Success")
                    .setMessage("Add friend success")
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
                .setTopColorRes(R.color.colorAccent)
                .setIcon(R.drawable.ic_add_friend)
                .setTitle("False")
                .setMessage("False to add friend success")
                .show();
    }

    @Override
    public void addFriendIsNotIdFriend() {
        addFriend(null, false);
    }


    @Override
    public void onSuccessDeleteFriend(String idFriend) {
        listFriendID.remove(idFriend);
        mFriendsPresenter.deleteFriendReference(idFriend);
    }

    @Override
    public void onFailureDeleteFriend() {
        dialogWaitDeleting.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.colorAccent)
                .setTitle("Error")
                .setMessage("Error occurred during deleting friend")
                .show();
    }

    @Override
    public void onSuccessDeleteFriendReference(String idFriend) {
        dialogWaitDeleting.dismiss();

        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.colorAccent)
                .setTitle("Success")
                .setMessage("Friend deleting successfully")
                .show();

        Intent intentDeleted = new Intent(FriendsFragment.ACTION_DELETE_FRIEND);
        intentDeleted.putExtra("idFriend", idFriend);
        getContext().sendBroadcast(intentDeleted);
    }

    public class FragFriendClickFloatButton implements View.OnClickListener {
        Context context;

        public FragFriendClickFloatButton() {
        }

        public FragFriendClickFloatButton getInstance(Context context) {
            this.context = context;
            return this;
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

        @Override
        public void onClick(final View view) {

            FriendSearchDialogCompat searchDialogCompat =
                    new FriendSearchDialogCompat(view.getContext(), "Find friends...",
                    "What are you looking for...?", null, new ArrayList<>(),
                    (dialog, item, position) -> {
                        if (item.getId().equals(mFriendsPresenter.getId())) {
                            Toast.makeText(view.getContext(), "Email not valid",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Friend friend = new Friend();
                            friend.setName(item.getName());
                            friend.setEmail(item.getEmail());
                            friend.id = item.getId();
                            friend.idRoom = item.getId().compareTo(mFriendsPresenter.getId()) > 0 ? (mFriendsPresenter.getId() + item.getId()).hashCode() + "" : "" + (item.getId() + mFriendsPresenter.getId()).hashCode();
                            checkBeforAddFriend(item.getId(), friend);
                        }
                        dialog.dismiss();
                    });

            BaseFilter apiFilter = new BaseFilter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    return null;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mFriendsPresenter.findFriend(this, searchDialogCompat, charSequence.toString().trim(),listFriendID);
                }
            };

            searchDialogCompat.setFilter(apiFilter);
            searchDialogCompat.show();
        }
    }

    private void getListFriendUId() {
        mFriendsPresenter.getListFriendUId();
    }

    @Override
    public void listFriendFound(List<String> friendList) {
        listFriendID.clear();
        listFriendID.addAll(friendList);
        getAllFriendInfo(0);
    }

    @Override
    public void listFriendNotFound() {
        dialogFindAllFriend.dismiss();
    }

    @SuppressLint("TimberArgCount")
    private void getAllFriendInfo(final int index) {
        if (index == listFriendID.size()) {
            //save list friend
            adapter.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            detectFriendOnline.start();
        } else {
            if(listFriendID.size() >= index) {
                try {
                    final String id = listFriendID.get(index);
                    mFriendsPresenter.getAllFriendInfo(index, id);
                } catch (IndexOutOfBoundsException ex) {
                    Timber.w("Exception occured.", ex);
                } catch (Exception ex) {
                    Timber.w("Exception occured.", ex);
                }
            } else {
                dialogFindAllFriend.dismiss();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }


    @Override
    public void friendInfoFound(int index, Friend friend) {
        if(!dataListFriend.getListFriend().contains(friend)) {
            dataListFriend.getListFriend().add(friend);
            FriendDB.getInstance(getContext()).addFriend(friend);
            getAllFriendInfo(index + 1);
        }
    }
}

class ListFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ListFriend listFriend;
    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DocumentReference> mapQueryOnline;
    public static Map<String, EventListener<QuerySnapshot>> mapChildListener;
    public static Map<String, EventListener<DocumentSnapshot>> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private FriendsFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;


    public ListFriendsAdapter(Context context, ListFriend listFriend, FriendsFragment fragment,LovelyProgressDialog dialogWaitDeleting) {
        this.listFriend = listFriend;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        this.fragment = fragment;
        this.dialogWaitDeleting = dialogWaitDeleting;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false);
        return new ItemFriendViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final String name = listFriend.getListFriend().get(position).getName();
        final String id = listFriend.getListFriend().get(position).id;
        final String idRoom = listFriend.getListFriend().get(position).idRoom;
        final String avata = listFriend.getListFriend().get(position).getAvatar();
        ((ItemFriendViewHolder) holder).txtName.setText(name);

        ((View) ((ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                        ((ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
                        Intent intent = new Intent(context, ChatViewActivity.class);
                        intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, name);
                        ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
                        idFriend.add(id);
                        intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
                        intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, idRoom);
                        ChatViewActivity.bitmapAvataFriend = new HashMap<>();
                        if (!avata.equals(Const.STR_DEFAULT_BASE64)) {
                            byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                            ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                        } else {
                            ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                        }

                        mapMark.put(id, null);
                        fragment.startActivityForResult(intent, FriendsFragment.ACTION_START_CHAT);
                    }
                });

        ((View) ((ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                .setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        String friendName = (String)((ItemFriendViewHolder) holder).txtName.getText();

                        new AlertDialog.Builder(context)
                                .setTitle("Delete Friend")
                                .setMessage("Are you sure want to delete "+friendName+ "?")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        final String idFriendRemoval = listFriend.getListFriend().get(position).id;
                                        dialogWaitDeleting.setTitle("Deleting...")
                                                .setCancelable(false)
                                                .setTopColorRes(R.color.colorAccent)
                                                .show();
                                        deleteFriend(idFriendRemoval);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();

                        return true;
                    }
                });


        if (listFriend.getListFriend().get(position).message.text.length() > 0) {
            ((ItemFriendViewHolder) holder).txtMessage.setVisibility(View.VISIBLE);
            ((ItemFriendViewHolder) holder).txtTime.setVisibility(View.VISIBLE);
            if (!listFriend.getListFriend().get(position).message.text.startsWith(id)) {
                ((ItemFriendViewHolder) holder).txtMessage.setText(listFriend.getListFriend().get(position).message.text);
                ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                ((ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
            } else {
                ((ItemFriendViewHolder) holder).txtMessage.setText(listFriend.getListFriend().get(position).message.text.substring((id + "").length()));
                ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT_BOLD);
                ((ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT_BOLD);
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
               mapChildListener.put(id, new EventListener<QuerySnapshot>() {
                    @SuppressLint("TimberArgCount")
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Timber.e("Listen failed.", e);
                            return;
                        }
                        try {
                            for (DocumentSnapshot documentSnapshot : snapshots) {
                                if (mapMark.get(id) != null) {
                                    if (!mapMark.get(id)) {
                                        if (listFriend.getListFriend().get(position).message.timestamp != documentSnapshot.getLong("timestamp")
                                                && listFriend.getListFriend().get(position).message.text != documentSnapshot.get("text")
                                                && listFriend.getListFriend().get(position).message.idReceiver != documentSnapshot.get("idReceiver")
                                                && listFriend.getListFriend().get(position).message.idSender != documentSnapshot.get("idSender")) {

                                            listFriend.getListFriend().get(position).message.text = id + documentSnapshot.get("text");
                                        }
                                    } else {
                                        listFriend.getListFriend().get(position).message.text = (String) documentSnapshot.get("text");
                                    }
                                    notifyDataSetChanged();
                                    mapMark.put(id, false);
                                } else {
                                    listFriend.getListFriend().get(position).message.text = (String) documentSnapshot.get("text");
                                    notifyDataSetChanged();
                                }
                                listFriend.getListFriend().get(position).message.timestamp = (long) documentSnapshot.get("timestamp");
                            }
                        } catch (IndexOutOfBoundsException ex) {
                            Timber.w("Exception occured.", ex);
                        } catch (Exception ex) {
                            Timber.w("Exception occured.", ex);
                        }
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
        if (listFriend.getListFriend().get(position).getAvatar().equals(Const.STR_DEFAULT_BASE64)) {
            ((ItemFriendViewHolder) holder).avata.setImageResource(R.drawable.default_avata);
        } else {
            byte[] decodedString = Base64.decode(listFriend.getListFriend().get(position).getAvatar(), Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((ItemFriendViewHolder) holder).avata.setImageBitmap(src);
        }


        if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
            mapQueryOnline.put(id,this.fragment.mFriendsPresenter.getStatus(id));
            mapChildListenerOnline.put(id, new EventListener<DocumentSnapshot>() {
                @SuppressLint("TimberArgCount")
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        Timber.w("Listen failed.", e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.get("isOnline") != null && listFriend.getListFriend().size() >= position) {
                        try {
                            listFriend.getListFriend().get(position).status.isOnline = (boolean) documentSnapshot.get("isOnline");
                            notifyDataSetChanged();
                        } catch (IndexOutOfBoundsException ex) {
                            Timber.w("Exception occured.", ex);
                        } catch (Exception ex) {
                            Timber.w("Exception occured.", ex);
                        }
                    }
                }

            });
            mapQueryOnline.get(id).addSnapshotListener(mapChildListenerOnline.get(id));
        }

        if (listFriend.getListFriend().get(position).status.isOnline) {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(10);
        } else {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(0);
        }
    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;
    }

    /**
     * Delete friend
     *
     * @param idFriend
     */
    private void deleteFriend(final String idFriend) {
        if (idFriend != null) {
            this.fragment.mFriendsPresenter.deleteFriend(idFriend);
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Error")
                    .setMessage("Error occurred during deleting friend")
                    .show();
        }
    }
}



class ItemFriendViewHolder extends RecyclerView.ViewHolder{
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

