package org.pl.android.drively.ui.chat.group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
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
import org.pl.android.drively.data.model.chat.Group;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.chat.addgroup.AddGroupActivity;
import org.pl.android.drively.ui.chat.chatview.ChatViewActivity;
import org.pl.android.drively.ui.chat.data.FriendDB;
import org.pl.android.drively.ui.chat.data.GroupDB;
import org.pl.android.drively.util.Const;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class GroupFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,GroupMvpView{
    private RecyclerView recyclerListGroups;
    public FragGroupClickFloatButton onClickFloatButton;
    private ArrayList<Group> listGroup;
    private ListGroupsAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final int CONTEXT_MENU_DELETE = 1;
    public static final int CONTEXT_MENU_EDIT = 2;
    public static final int CONTEXT_MENU_LEAVE = 3;
    public static final int REQUEST_EDIT_GROUP = 0;
    public static final String CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos";


    @Inject
    GroupPresenter mGroupPresenter;


    @BindView(R.id.fab_group)
    FloatingActionButton fabGroupButton;


    LovelyProgressDialog progressDialog, waitingLeavingGroup;

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

        listGroup = GroupDB.getInstance(getContext()).getListGroups();
        recyclerListGroups = (RecyclerView) layout.findViewById(R.id.recycleListGroup);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerListGroups.setLayoutManager(layoutManager);
        adapter = new ListGroupsAdapter(getContext(), listGroup);
        recyclerListGroups.setAdapter(adapter);
        onClickFloatButton = new FragGroupClickFloatButton();
        progressDialog = new LovelyProgressDialog(getContext())
                .setCancelable(false)
                .setIcon(R.drawable.ic_dialog_delete_group)
                .setTitle("Deleting....")
                .setTopColorRes(R.color.colorAccent);

        waitingLeavingGroup = new LovelyProgressDialog(getContext())
                .setCancelable(false)
                .setIcon(R.drawable.ic_dialog_delete_group)
                .setTitle("Group leaving....")
                .setTopColorRes(R.color.colorAccent);

        if(listGroup.size() == 0){
            //Ket noi server hien thi group
            mSwipeRefreshLayout.setRefreshing(true);
            getListGroup();
        }
        fabGroupButton.setOnClickListener(onClickFloatButton.getInstance(getContext()));
        return layout;
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();
        mGroupPresenter.detachView();
    }

    private void getListGroup(){
        mGroupPresenter.getListGroup();
    }

    @Override
    public void setGroupList(List<String> rooms) {
        for(String room :rooms) {
            Group newGroup = new Group();
            newGroup.id = room;
            listGroup.add(newGroup);
        }
        getGroupInfo(0);
    }

    @Override
    public void getGroupError() {
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_EDIT_GROUP && resultCode == Activity.RESULT_OK) {
            listGroup.clear();
            ListGroupsAdapter.listFriend = null;
            GroupDB.getInstance(getContext()).dropDB();
            getListGroup();
        }
    }

    private void getGroupInfo(final int indexGroup){
        if(indexGroup == listGroup.size()){
            adapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        }else {
            mGroupPresenter.getGroupInfo(indexGroup,listGroup.get(indexGroup).id);
        }
    }

    @SuppressLint("TimberArgCount")
    @Override
    public void setGroupInfo(int groupIndex, Room room) {
        for(String member : room.member) {
            listGroup.get(groupIndex).member.add(member);
        }
        listGroup.get(groupIndex).groupInfo.put("name",  room.groupInfo.get("name"));
        listGroup.get(groupIndex).groupInfo.put("admin",  room.groupInfo.get("admin"));
        GroupDB.getInstance(getContext()).addGroup(listGroup.get(groupIndex));
        Timber.d("GroupFragment", listGroup.get(groupIndex).id +": " + room.toString());
        getGroupInfo(groupIndex +1);
    }



    @Override
    public void onRefresh() {
        listGroup.clear();
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
                if(((String)listGroup.get(posGroup).groupInfo.get("admin")).equals(mGroupPresenter.getId())) {
                    Group group = listGroup.get(posGroup);
                    listGroup.remove(posGroup);
                    if(group != null){
                        deleteGroup(group, 0);
                    }
                }else{
                    Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
                }
                break;
          /*  case CONTEXT_MENU_EDIT:
                int posGroup1 = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(posGroup1).groupInfo.get("admin")).equals(mGroupPresenter.getId())) {
                     Intent intent = new Intent(getContext(), AddGroupActivity.class);
                     intent.putExtra("groupId", listGroup.get(posGroup1).id);
                     startActivityForResult(intent, REQUEST_EDIT_GROUP);
                }else{
                    Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
                }

                break;*/

            case CONTEXT_MENU_LEAVE:
                int position = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(position).groupInfo.get("admin")).equals(mGroupPresenter.getId())) {
                    Toast.makeText(getActivity(), "Admin cannot leave group", Toast.LENGTH_LONG).show();
                }else{
                    waitingLeavingGroup.show();
                    Group groupLeaving = listGroup.get(position);
                    leaveGroup(groupLeaving);
                }
                break;
        }

        return super.onContextItemSelected(item);
    }

    public void deleteGroup(final Group group, final int index){
        if(index == group.member.size()){
            mGroupPresenter.deleteGroup(group);
        }else{
            mGroupPresenter.deleteGroupReference(index, group);
        }
    }

    @Override
    public void deleteGroupSuccess(Group group) {
        progressDialog.dismiss();
        GroupDB.getInstance(getContext()).deleteGroup(group.id);
        listGroup.remove(group);
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Deleted group", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deleteGroupFailure() {
        progressDialog.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.colorAccent)
                .setIcon(R.drawable.ic_dialog_delete_group)
                .setTitle("False")
                .setMessage("Cannot delete group right now, please try again.")
                .setCancelable(false)
                .setConfirmButtonText("Ok")
                .show();
    }

    @Override
    public void onSuccessDeleteGroupReference(Group group, int index) {
        deleteGroup(group, index + 1);
    }

    @Override
    public void onFailureGroupReference() {
        progressDialog.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.colorAccent)
                .setIcon(R.drawable.ic_dialog_delete_group)
                .setTitle("False")
                .setMessage("Cannot connect server")
                .setCancelable(false)
                .setConfirmButtonText("Ok")
                .show();
    }



    public void leaveGroup(final Group group){
        mGroupPresenter.leaveGroup(group);
    }

    @Override
    public void onSuccessLeaveGroup(Group group) {
        mGroupPresenter.leaveGroupUserReference(group);
    }

    @Override
    public void onFailureLeaveGroup() {
        waitingLeavingGroup.dismiss();
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.colorAccent)
                .setTitle("Error")
                .setMessage("Error occurred during leaving group")
                .show();
    }

    @Override
    public void onSuccessLeaveGroupReference(Group group) {
        waitingLeavingGroup.dismiss();

        listGroup.remove(group);
        adapter.notifyDataSetChanged();
        GroupDB.getInstance(getContext()).deleteGroup(group.id);
        new LovelyInfoDialog(getContext())
                .setTopColorRes(R.color.colorAccent)
                .setTitle("Success")
                .setMessage("Group leaving successfully")
                .show();
    }

    @Override
    public void showError() {

    }



    public class FragGroupClickFloatButton implements View.OnClickListener{

        Context context;
        public FragGroupClickFloatButton getInstance(Context context){
            this.context = context;
            return this;
        }

        @Override
        public void onClick(View view) {
            startActivityForResult( new Intent(getContext(), AddGroupActivity.class),REQUEST_EDIT_GROUP);
        }
    }
}

class ListGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Group> listGroup;
    public static ListFriend listFriend = null;
    private Context context;

    public ListGroupsAdapter(Context context, ArrayList<Group> listGroup){
        this.context = context;
        this.listGroup = listGroup;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_group, parent, false);
        return new ItemGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final String groupName = listGroup.get(position).groupInfo.get("name");
        if(groupName != null && groupName.length() > 0) {
            ((ItemGroupViewHolder) holder).txtGroupName.setText(groupName);
            ((ItemGroupViewHolder) holder).iconGroup.setText((groupName.charAt(0) + "").toUpperCase());
        }
        ((ItemGroupViewHolder) holder).btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setTag(new Object[]{groupName, position});
                view.getParent().showContextMenuForChild(view);
            }
        });
        ((RelativeLayout)((ItemGroupViewHolder) holder).txtGroupName.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listFriend == null){
                   listFriend = FriendDB.getInstance(context).getListFriend();
                }
                Intent intent = new Intent(context, ChatViewActivity.class);
                intent.putExtra(Const.INTENT_KEY_CHAT_FRIEND, groupName);
                ArrayList<CharSequence> idFriend = new ArrayList<>();
                ChatViewActivity.bitmapAvataFriend = new HashMap<>();
                for(String id : listGroup.get(position).member) {
                    idFriend.add(id);
                    String avata = listFriend.getAvataById(id);
                    if(!avata.equals(Const.STR_DEFAULT_BASE64)) {
                        byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                        ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                    }else if(avata.equals(Const.STR_DEFAULT_BASE64)) {
                        ChatViewActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
                    }else {
                        ChatViewActivity.bitmapAvataFriend.put(id, null);
                    }
                }
                intent.putCharSequenceArrayListExtra(Const.INTENT_KEY_CHAT_ID, idFriend);
                intent.putExtra(Const.INTENT_KEY_CHAT_ROOM_ID, listGroup.get(position).id);
                context.startActivity(intent);
            }
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
        menu.setHeaderTitle((String) ((Object[])btnMore.getTag())[0]);
        Intent data = new Intent();
        data.putExtra(GroupFragment.CONTEXT_MENU_KEY_INTENT_DATA_POS, (Integer) ((Object[])btnMore.getTag())[1]);
        //menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_EDIT, Menu.NONE, "Edit group").setIntent(data);
        menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_DELETE, Menu.NONE, "Delete group").setIntent(data);
        menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_LEAVE, Menu.NONE, "Leave group").setIntent(data);
    }
}