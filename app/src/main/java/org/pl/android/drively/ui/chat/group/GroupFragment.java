package org.pl.android.drively.ui.chat.group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.data.model.chat.RoomMember;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.chat.addgroup.AddGroupActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.data.FriendDB;
import org.pl.android.drively.ui.chat.data.GroupDB;
import org.pl.android.drively.util.Const;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class GroupFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, GroupMvpView {
    public static final int CONTEXT_MENU_DELETE = 1;
    public static final int CONTEXT_MENU_EDIT = 2;
    public static final int CONTEXT_MENU_LEAVE = 3;
    public static final int REQUEST_EDIT_GROUP = 0;
    public static final String CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos";
    public FragGroupClickFloatButton onClickFloatButton;
    @Inject
    GroupPresenter mGroupPresenter;
    @BindView(R.id.fab_group)
    FloatingActionButton fabGroupButton;
    LovelyProgressDialog progressDialog, waitingLeavingGroup, deleteGroupDialog;
    private RecyclerView recyclerListGroups;
    private ArrayList<Room> rooms;
    private ListGroupsAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_group, container, false);
        mGroupPresenter.attachView(this);
        ButterKnife.bind(this, layout);

        // TEMPORARY SOLUTION
        fabGroupButton.setVisibility(View.INVISIBLE);

        rooms = GroupDB.getInstance(getContext()).getListGroups();
        recyclerListGroups = (RecyclerView) layout.findViewById(R.id.recycleListGroup);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerListGroups.setLayoutManager(layoutManager);

        Collections.sort(rooms, (a, b) -> (a.getId().compareTo(b.getId())));
        adapter = new ListGroupsAdapter(getContext(), rooms, this);

        recyclerListGroups.setAdapter(adapter);
        onClickFloatButton = new FragGroupClickFloatButton();

        progressDialog = new LovelyProgressDialog(getContext())
                .setCancelable(false)
                .setIcon(R.drawable.ic_delete_white_24dp)
                .setTitle(getResources().getString(R.string.deleting))
                .setTopColorRes(R.color.primary_dark);

        waitingLeavingGroup = new LovelyProgressDialog(getContext())
                .setCancelable(false)
                .setIcon(R.drawable.ic_delete_white_24dp)
                .setTitle(getResources().getString(R.string.group_leaving))
                .setTopColorRes(R.color.primary_dark);

        if (rooms.size() == 0) {
            //Ket noi server hien thi group
            mSwipeRefreshLayout.setRefreshing(true);
            getListGroup();
        }
        fabGroupButton.setOnClickListener(onClickFloatButton.getInstance(getContext()));
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GroupDB.getInstance(getContext()).dropDB();
        mGroupPresenter.detachView();
    }

    private void getListGroup() {
        mGroupPresenter.getListGroup();
    }

    @Override
    public void setGroupList(List<String> rooms) {
        mGroupPresenter.getGroupInfo(rooms);
    }

    @Override
    public void getGroupError() {
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_GROUP && resultCode == Activity.RESULT_OK) {
            rooms.clear();
            ListGroupsAdapter.listFriend = null;
            GroupDB.getInstance(getContext()).dropDB();
            getListGroup();
        }
    }

    @SuppressLint("TimberArgCount")
    @Override
    public void setGroupInfo(List<Room> rooms) {
        Collections.sort(rooms, (a, b) -> a.getName().compareTo(b.getName()));

        for (Room room : rooms) {
            this.rooms.add(room);
            GroupDB.getInstance(getContext()).addGroup(room);
        }

        adapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        rooms.clear();
        ListGroupsAdapter.listFriend = null;
        GroupDB.getInstance(getContext()).dropDB();
        adapter.notifyDataSetChanged();
        getListGroup();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE:
                int posGroup = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if (rooms.get(posGroup).getAdmin().equals(mGroupPresenter.getId())) {
                    Room group = rooms.get(posGroup);
                    rooms.remove(posGroup);
                    if (group != null) {
                        progressDialog.show();
                        deleteGroup(group, 0);
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.not_admin), Toast.LENGTH_LONG).show();
                }
                break;
          /*  case CONTEXT_MENU_EDIT:
                int posGroup1 = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)rooms.get(posGroup1).groupInfo.get("admin")).equals(mGroupPresenter.getId())) {
                     Intent intent = new Intent(getContext(), AddGroupActivity.class);
                     intent.putExtra("groupId", rooms.get(posGroup1).id);
                     startActivityForResult(intent, REQUEST_EDIT_GROUP);
                }else{
                    Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
                }

                break;*/

            case CONTEXT_MENU_LEAVE:
                int position = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if (rooms.get(position).getAdmin().equals(mGroupPresenter.getId())) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.admin_cannot_leave), Toast.LENGTH_LONG).show();
                } else {
                    waitingLeavingGroup.show();
                    Room groupLeaving = rooms.get(position);
                    leaveGroup(groupLeaving);
                }
                break;
        }

        return super.onContextItemSelected(item);
    }

    public void deleteGroup(final Room group, final int index) {
        if (index == group.getMembers().size()) {
            mGroupPresenter.deleteGroup(group);
        } else {
            mGroupPresenter.deleteGroupReference(index, group);
        }
    }

    @Override
    public void deleteGroupSuccess(Room group) {
        progressDialog.dismiss();
        GroupDB.getInstance(getContext()).deleteGroup(group.getId());
        rooms.remove(group);
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), getResources().getString(R.string.deleted_group), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deleteGroupFailure() {
        progressDialog.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.primary)
                .setIcon(R.drawable.ic_delete_white_24dp)
                .setTitle(getResources().getString(R.string.failure))
                .setMessage(getResources().getString(R.string.delete_group_cannot_right_now))
                .setCancelable(false)
                .setConfirmButtonText("Ok")
                .show();
    }

    @Override
    public void onSuccessDeleteGroupReference(Room group, int index) {
        deleteGroup(group, index + 1);
    }

    @Override
    public void onFailureGroupReference() {
        progressDialog.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.primary)
                .setIcon(R.drawable.ic_dialog_delete_group)
                .setTitle(getResources().getString(R.string.failure))
                .setMessage(getResources().getString(R.string.cannot_connect_with_server))
                .setCancelable(false)
                .setConfirmButtonText("Ok")
                .show();
    }


    public void leaveGroup(final Room group) {
        mGroupPresenter.leaveGroup(group);
    }

    @Override
    public void onSuccessLeaveGroup(Room group) {
        mGroupPresenter.leaveGroupUserReference(group);
    }

    @Override
    public void onFailureLeaveGroup() {
        waitingLeavingGroup.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.primary)
                .setTitle(getResources().getString(R.string.failure))
                .setMessage(getResources().getString(R.string.error_during_leaveing_group))
                .show();
    }

    @Override
    public void onSuccessLeaveGroupReference(Room group) {
        waitingLeavingGroup.dismiss();
        rooms.remove(group);
        adapter.notifyDataSetChanged();
        GroupDB.getInstance(getContext()).deleteGroup(group.getId());
        Toast.makeText(getActivity(), getResources().getString(R.string.success_leaveing_group), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError() {

    }


    public class FragGroupClickFloatButton implements View.OnClickListener {

        Context context;

        public FragGroupClickFloatButton getInstance(Context context) {
            this.context = context;
            return this;
        }

        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(getContext(), AddGroupActivity.class), REQUEST_EDIT_GROUP);
        }
    }
}

class ListGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static ListFriend listFriend = null;
    private ArrayList<Room> listGroup;
    private Context context;
    private GroupFragment fragment;

    public ListGroupsAdapter(Context context, ArrayList<Room> listGroup, GroupFragment fragment) {
        this.context = context;
        this.listGroup = listGroup;
        this.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_group, parent, false);
        return new ItemGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final Room room = listGroup.get(position);
        final String groupName = room.getName();
        ItemGroupViewHolder holder = ((ItemGroupViewHolder) viewHolder);

        if (groupName != null && groupName.length() > 0) {
            holder.txtGroupName.setText(groupName);
            holder.iconGroup.setText((groupName.charAt(0) + "").toUpperCase());
        }

        if (!room.isEditable()) {
            holder.btnMore.setVisibility(View.INVISIBLE);
            Resources resource = context.getResources();
            Drawable shape = resource.getDrawable(R.drawable.circle_background);
            holder.iconGroup.setBackground(shape);
            holder.iconGroup.setTextColor(context.getResources().getColor(R.color.button_background));
        } else {
            holder.btnMore.setOnClickListener(view -> {
                view.setTag(new Object[]{groupName, position});
                view.getParent().showContextMenuForChild(view);
            });
            holder.iconGroup.setBackground(context.getResources().getDrawable(R.drawable.circle_background_default));
            holder.iconGroup.setTextColor(context.getResources().getColor(R.color.white));
            holder.btnMore.setVisibility(View.VISIBLE);
        }

        ((RelativeLayout) holder.txtGroupName.getParent()).setOnClickListener(view -> {
            if (listFriend == null) {
                listFriend = FriendDB.getInstance(context).getListFriend();
            }
            ArrayList<CharSequence> idFriend = new ArrayList<>();
            for (RoomMember member : listGroup.get(position).getMembers()) {
                String id = member.getMemberId();
                idFriend.add(id);
            }

            Intent intent = new Intent(context, ChatViewActivity.class);

            intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_NAME, room.getName());
            intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, room.getId());
            intent.putExtra(Const.INTENT_KEY_IS_GROUP_CHAT, true);
            intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listGroup.size();
    }
}

class ItemGroupViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    public TextView iconGroup, txtGroupName;
    public ImageButton btnMore;

    public ItemGroupViewHolder(View itemView) {
        super(itemView);
        itemView.setOnCreateContextMenuListener(this);
        iconGroup = (TextView) itemView.findViewById(R.id.icon_group);
        txtGroupName = (TextView) itemView.findViewById(R.id.txtName);
        btnMore = (ImageButton) itemView.findViewById(R.id.btnMoreAction);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        menu.setHeaderTitle((String) ((Object[]) btnMore.getTag())[0]);
        Intent data = new Intent();
        data.putExtra(GroupFragment.CONTEXT_MENU_KEY_INTENT_DATA_POS, (Integer) ((Object[]) btnMore.getTag())[1]);
        //menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_EDIT, Menu.NONE, "Edit group").setIntent(data);
        menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_DELETE, Menu.NONE, view.getContext().getResources().getString(R.string.delete_group)).setIntent(data);
        menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_LEAVE, Menu.NONE, view.getContext().getResources().getString(R.string.leave_group)).setIntent(data);
    }
}