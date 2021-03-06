package org.pl.android.drively.ui.chat.addgroup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.pl.android.drively.R;
import org.pl.android.drively.data.model.chat.ListFriend;
import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.data.model.chat.RoomMember;
import org.pl.android.drively.ui.base.BaseActivity;
import org.pl.android.drively.ui.chat.data.FriendDB;
import org.pl.android.drively.ui.chat.data.GroupDB;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;


public class AddGroupActivity extends BaseActivity implements AddGroupMvpView {

    @Inject
    AddGroupPresenter mAddGroupPresenter;
    private RecyclerView recyclerListFriend;
    private ListPeopleAdapter adapter;
    private ListFriend listFriend;
    private LinearLayout btnAddGroup;
    private Set<String> listIDChoose;
    private Set<String> listIDRemove;
    private EditText editTextGroupName;
    private TextView txtGroupIcon, txtActionName;
    private LovelyProgressDialog dialogWait;
    private boolean isEditGroup;
    private Room groupEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_add_group);
        mAddGroupPresenter.attachView(this);
        Intent intentData = getIntent();
        txtActionName = (TextView) findViewById(R.id.txtActionName);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listFriend = FriendDB.getInstance(this).getListFriend();
        listIDChoose = new HashSet<>();
        listIDRemove = new HashSet<>();
        listIDChoose.add(mAddGroupPresenter.getId());
        btnAddGroup = (LinearLayout) findViewById(R.id.btnAddGroup);
        editTextGroupName = (EditText) findViewById(R.id.editGroupName);
        txtGroupIcon = (TextView) findViewById(R.id.icon_group);
        dialogWait = new LovelyProgressDialog(this).setCancelable(false);
        editTextGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 1) {
                    txtGroupIcon.setText((charSequence.charAt(0) + "").toUpperCase());
                } else {
                    txtGroupIcon.setText("R");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnAddGroup.setOnClickListener(view -> {
            if (listIDChoose.size() < 3) {
                Toast.makeText(AddGroupActivity.this, getResources().getString(R.string.at_least_two), Toast.LENGTH_SHORT).show();
            } else {
                if (editTextGroupName.getText().length() == 0) {
                    Toast.makeText(AddGroupActivity.this, getResources().getString(R.string.enter_group_name), Toast.LENGTH_SHORT).show();
                } else {
                    if (isEditGroup) {
                        editGroup();
                    } else {
                        createGroup();
                    }
                }
            }
        });

        if (intentData.getStringExtra("groupId") != null) {
            isEditGroup = true;
            String idGroup = intentData.getStringExtra("groupId");
            txtActionName.setText("Save");
            btnAddGroup.setBackgroundColor(getResources().getColor(R.color.primary));
            groupEdit = GroupDB.getInstance(this).getGroup(idGroup);
            editTextGroupName.setText(groupEdit.getName());
        } else {
            isEditGroup = false;
        }

        recyclerListFriend = (RecyclerView) findViewById(R.id.recycleListFriend);
        recyclerListFriend.setLayoutManager(linearLayoutManager);
        adapter = new ListPeopleAdapter(this, listFriend, btnAddGroup, listIDChoose, listIDRemove, isEditGroup, groupEdit, this);
        recyclerListFriend.setAdapter(adapter);


    }

    private void editGroup() {
        //Show dialog wait
        dialogWait.setIcon(R.drawable.ic_add_group_dialog)
                .setTitle("Editing....")
                .setTopColorRes(R.color.primary)
                .show();
        //Delete group
        final String idGroup = groupEdit.getId();
        Room room = new Room();
        for (String id : listIDChoose) {
            room.getMembers().add(new RoomMember(id));
        }
        room.setName(editTextGroupName.getText().toString());
        room.setAdmin(mAddGroupPresenter.getId());

        mAddGroupPresenter.editGroup(idGroup, room);
    }

    private void createGroup() {
        //Show dialog wait
        dialogWait.setIcon(R.drawable.add_event_white_24dp)
                .setTitle(getResources().getString(R.string.registering))
                .setTopColorRes(R.color.primary)
                .show();

        final String idGroup = (mAddGroupPresenter.getId() + System.currentTimeMillis()).hashCode() + "";
        Room room = new Room();
        for (String id : listIDChoose) {
            room.getMembers().add(new RoomMember(id));
        }
        room.setName(editTextGroupName.getText().toString());
        room.setAdmin(mAddGroupPresenter.getId());
        mAddGroupPresenter.createGroup(idGroup, room);
    }

    private void deleteRoomForUser(final String roomId, final int userIndex) {
        if (userIndex == listIDRemove.size()) {
            dialogWait.dismiss();
            Toast.makeText(this, "Edit group success", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, null);
            AddGroupActivity.this.finish();
        } else {
            mAddGroupPresenter.deleteUserReference((String) listIDRemove.toArray()[userIndex], roomId, userIndex);
        }
    }

    @Override
    public void addRoomForUser(final String roomId, final int userIndex) {
        if (userIndex == listIDChoose.size()) {
            if (!isEditGroup) {
                dialogWait.dismiss();
                Toast.makeText(this, getResources().getString(R.string.create_group_success), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, null);
                this.finish();
            } else {
                deleteRoomForUser(roomId, 0);
            }
        } else {
            mAddGroupPresenter.addRoomForUser(roomId, userIndex, (String) listIDChoose.toArray()[userIndex]);
        }
    }

    @Override
    public void addRoomForUserFailure() {
        dialogWait.dismiss();
        new LovelyInfoDialog(AddGroupActivity.this) {
            @Override
            public LovelyInfoDialog setConfirmButtonText(String text) {
                findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(view -> dismiss());
                return super.setConfirmButtonText(text);
            }
        }
                .setTopColorRes(R.color.primary)
                .setIcon(R.drawable.ic_add_group_dialog)
                .setTitle(getResources().getString(R.string.failure))
                .setMessage(getResources().getString(R.string.create_group_failure))
                .setCancelable(false)
                .setConfirmButtonText("Ok")
                .show();
    }

    @Override
    public void editGroupSuccess(String idGroup) {
        addRoomForUser(idGroup, 0);
    }


    @Override
    public void editGroupFailure() {
        dialogWait.dismiss();
        new LovelyInfoDialog(AddGroupActivity.this) {
            @Override
            public LovelyInfoDialog setConfirmButtonText(String text) {
                findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(view -> dismiss());
                return super.setConfirmButtonText(text);
            }
        }
                .setTopColorRes(R.color.primary)
                .setIcon(R.drawable.ic_add_group_dialog)
                .setTitle(getResources().getString(R.string.failure))
                .setMessage("Cannot connect database")
                .setCancelable(false)
                .setConfirmButtonText("Ok")
                .show();
    }

    @Override
    public void onSuccessDeleteGroupReference(String roomId, int userIndex) {
        deleteRoomForUser(roomId, userIndex + 1);
    }

    @Override
    public void onFailureGroupReference() {
        dialogWait.dismiss();
        new LovelyInfoDialog(AddGroupActivity.this) {
            @Override
            public LovelyInfoDialog setConfirmButtonText(String text) {
                findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(view -> dismiss());
                return super.setConfirmButtonText(text);
            }
        }
                .setTopColorRes(R.color.primary)
                .setIcon(R.drawable.ic_add_group_dialog)
                .setTitle(getResources().getString(R.string.failure))
                .setMessage(getResources().getString(R.string.delete_group_failure))
                .setCancelable(false)
                .setConfirmButtonText("Ok")
                .show();
    }
}

class ListPeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ListFriend listFriend;
    private LinearLayout btnAddGroup;
    private Set<String> listIDChoose;
    private Set<String> listIDRemove;
    private boolean isEdit;
    private Room editGroup;
    private AddGroupActivity addGroupActivity;

    public ListPeopleAdapter(Context context, ListFriend listFriend, LinearLayout btnAddGroup, Set<String> listIDChoose, Set<String> listIDRemove, boolean isEdit, Room editGroup, AddGroupActivity addGroupActivity) {
        this.context = context;
        this.listFriend = listFriend;
        this.btnAddGroup = btnAddGroup;
        this.listIDChoose = listIDChoose;
        this.listIDRemove = listIDRemove;

        this.isEdit = isEdit;
        this.editGroup = editGroup;
        this.addGroupActivity = addGroupActivity;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_add_friend, parent, false);
        return new ItemFriendHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ItemFriendHolder) holder).txtName.setText(listFriend.getListFriend().get(position).name);
        ((ItemFriendHolder) holder).txtEmail.setText(listFriend.getListFriend().get(position).email);
        final String id = listFriend.getListFriend().get(position).id;
        if (listFriend.getListFriend().get(position).avatarBytes != null) {
            Bitmap src = BitmapFactory.decodeByteArray(listFriend.getListFriend().get(position).avatarBytes, 0, listFriend.getListFriend().get(position).avatarBytes.length);
            ((ItemFriendHolder) holder).avatar.setImageBitmap(src);
        } else {
            ((ItemFriendHolder) holder).avatar.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
        }
        ((ItemFriendHolder) holder).checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                listIDChoose.add(id);
                listIDRemove.remove(id);
            } else {
                listIDRemove.add(id);
                listIDChoose.remove(id);
            }
            if (listIDChoose.size() >= 3) {
                btnAddGroup.setBackgroundColor(context.getResources().getColor(R.color.primary));
            } else {
                btnAddGroup.setBackgroundColor(context.getResources().getColor(R.color.primary));
            }
        });
        if (isEdit && editGroup.getMembers().contains(new RoomMember(id))) {
            ((ItemFriendHolder) holder).checkBox.setChecked(true);
        } else if (editGroup != null && !editGroup.getMembers().contains(new RoomMember(id))) {
            ((ItemFriendHolder) holder).checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend().size();
    }
}

class ItemFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtName, txtEmail;
    public CircleImageView avatar;
    public CheckBox checkBox;

    public ItemFriendHolder(View itemView) {
        super(itemView);
        txtName = (TextView) itemView.findViewById(R.id.txtName);
        txtEmail = (TextView) itemView.findViewById(R.id.txtEmail);
        avatar = (CircleImageView) itemView.findViewById(R.id.icon_avata);
        checkBox = (CheckBox) itemView.findViewById(R.id.checkAddPeople);
    }
}

