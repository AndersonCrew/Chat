package com.dazone.crewchatoff.TestMultiLevelListview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.activity.ChattingActivity;
import com.dazone.crewchatoff.activity.ProfileUserActivity;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AlertDialogView;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.fragment.CompanyFragment;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallbackWithJson;
import com.dazone.crewchatoff.interfaces.ICreateOneUserChatRom;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by DAZONE-XXX on 8/4/2016.
 */

// Custom recycle adapter to compacitice with the layout
public class NLevelRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    String TAG = "NLevelRecycleAdapter";

    public static int FILTER_TYPE_NORMAL = 0;
    public static int FILTER_TYPE_USER_SEARCH = 1;

    private final int TYPE_FOLDER = 0;
    private final int TYPE_USER = 1;
    private final int TYPE_EMPPY = 999;

    private ArrayList<TreeUserDTO> temp = new ArrayList<>();

    List<NLevelItem> list;
    List<NLevelListItem> filtered;

    private NLevelItem mTempDto;
    private int left20dp;
    boolean isToggle = false;
    private Context mContext;
    private OnGroupShowContextMenu mCallback;

    public void setFiltered(ArrayList<NLevelListItem> filtered) {
        this.filtered = filtered;

    }

    public NLevelRecycleAdapter(Context context, List<NLevelItem> list, int left20dp, OnGroupShowContextMenu callback) {
        this.list = list;
        this.filtered = filterItems();
        this.left20dp = left20dp;
        this.mContext = context;
        this.mCallback = callback;
    }

    @Override
    public void onClick(View v) {

    }

    private class UserViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView title, position, tv_work_phone, tv_personal_phone, tvPhone1, tvPhone2;
        public ImageView avatar_imv;
        ImageView status_imv;
        public LinearLayout lnItemWraper, lnPhone;
        public RelativeLayout main;

        public UserViewHolder(View currentView) {
            super(currentView);
            rootView = currentView;
            title = (TextView) currentView.findViewById(R.id.name);
            avatar_imv = (ImageView) currentView.findViewById(R.id.avatar);
            status_imv = (ImageView) currentView.findViewById(R.id.status_imv);
            position = (TextView) currentView.findViewById(R.id.position);
            lnItemWraper = (LinearLayout) currentView.findViewById(R.id.item_org_wrapper);
            //status_tv = (TextView) currentView.findViewById(R.id.status_tv);
            //checkBox = (CheckBox) currentView.findViewById(R.id.row_check);
            tv_work_phone = (TextView) currentView.findViewById(R.id.tv_work_phone);
            tv_personal_phone = (TextView) currentView.findViewById(R.id.tv_personal_phone);
            tvPhone1 = (TextView) currentView.findViewById(R.id.tv_phone_1);
            tvPhone2 = (TextView) currentView.findViewById(R.id.tv_phone_2);
            lnPhone = (LinearLayout) currentView.findViewById(R.id.ln_phone);
            main = (RelativeLayout) currentView.findViewById(R.id.mainParent);


        }
    }

    private class FolderViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView title;
        public ImageView icon;
        public CheckBox checkBox;
        public RelativeLayout main;
        public LinearLayout mLnTittle, lnl_child;

        public FolderViewHolder(View currentView) {
            super(currentView);
            rootView = currentView;
            title = (TextView) currentView.findViewById(R.id.office_title);

            title = (TextView) currentView.findViewById(R.id.office_title);
            icon = (ImageView) currentView.findViewById(R.id.ic_folder);
            main = (RelativeLayout) currentView.findViewById(R.id.mainParent);
            mLnTittle = (LinearLayout) currentView.findViewById(R.id.layout_title);
        }
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView noData;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            noData = (TextView) itemView.findViewById(R.id.tv_empty);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {

            case TYPE_USER:
                View userView = inflater.inflate(R.layout.tree_user_row, null);
                return new UserViewHolder(userView);

            case TYPE_EMPPY:
                View emptyView = inflater.inflate(R.layout.row_user_empty, null);
                return new EmptyViewHolder(emptyView);

            case TYPE_FOLDER:
            default:
                View defaultView = inflater.inflate(R.layout.tree_office_row_v2, null);
                return new FolderViewHolder(defaultView);
        }
    }

    private void setupStatusImage(UserViewHolder holder, TreeUserDTO dto) {

        switch (dto.getStatus()) {
            case Statics.USER_LOGIN:
                holder.status_imv.setImageResource(R.drawable.home_big_status_01);
                break;
            case Statics.USER_AWAY:
                holder.status_imv.setImageResource(R.drawable.home_big_status_02);
                break;
            case Statics.USER_LOGOUT:
                holder.status_imv.setImageResource(R.drawable.home_big_status_03);
                break;
            default:
                holder.status_imv.setImageResource(R.drawable.home_big_status_03);
                break;
        }
    }

    private void bindUserData(UserViewHolder holder, final TreeUserDTO dto) {
//        Log.d(TAG,new Gson().toJson(dto));

        holder.title.setText(dto.getName());

        String avatarUrl = new Prefs().getServerSite() + dto.getAvatarUrl();

        holder.avatar_imv.setImageResource(R.drawable.avatar_l);
        ImageUtils.showCycleImageFromLink(avatarUrl, holder.avatar_imv, R.dimen.button_height);
        setDutyOrPosition(holder.position,dto.DutyName,dto.getPosition());
       /* if (dto.DutyName.isEmpty())
            holder.position.setText(dto.getPosition());
        else
            holder.position.setText(dto.DutyName);*/

        if (TextUtils.isEmpty(dto.getPhoneNumber())) {
            holder.tvPhone1.setVisibility(View.GONE);
        } else {
            holder.tvPhone1.setVisibility(View.VISIBLE);
            holder.tvPhone1.setText(dto.getPhoneNumber());
        }
        if (TextUtils.isEmpty(dto.getCompanyNumber())) {
            holder.tvPhone2.setVisibility(View.GONE);
        } else {
            holder.tvPhone2.setVisibility(View.VISIBLE);
            holder.tvPhone2.setText(dto.getCompanyNumber());
        }
        setupStatusImage(holder, dto);
        if (dto.getId() == Utils.getCurrentId()) {
            holder.status_imv.setImageResource(R.drawable.home_status_me);
        }

        holder.avatar_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick 4");
                Intent intent = new Intent(BaseActivity.Instance, ProfileUserActivity.class);
                intent.putExtra(Constant.KEY_INTENT_USER_NO, dto.getId());
                BaseActivity.Instance.startActivity(intent);
            }
        });

        holder.rootView.setOnCreateContextMenuListener(this);
    }
    private void setDutyOrPosition(TextView tvPosition, String duty, String position) {
        if (isGetValueEnterAuto() && !duty.equals("")) {
            tvPosition.setText(duty);
        } else {
            tvPosition.setText(position);
        }
    }

    private boolean isGetValueEnterAuto() {
        boolean isEnable = false;
        isEnable = CrewChatApplication.getInstance().getPrefs().getBooleanValue(Statics.IS_ENABLE_ENTER_VIEW_DUTY_KEY, isEnable);
        return isEnable;
    }

    void setHideShowFolder(FolderViewHolder holder, TreeUserDTO dto) {
        if (dto.getId() != 0) {

            if (dto.getIsHide() == 1) {
                holder.icon.setImageResource(R.drawable.home_folder_close_ic);
                dto.setIsHide(0);
            } else {
                holder.icon.setImageResource(R.drawable.home_folder_open_ic);
                dto.setIsHide(1);
            }
        } else {
            if (dto.getName().equals(Constant.Favorites)) {
                if (dto.getIsHide() == 1) {
                    holder.icon.setImageResource(R.drawable.home_folder_close_ic);
                    dto.setIsHide(0);
                } else {
                    holder.icon.setImageResource(R.drawable.home_folder_open_ic);
                    dto.setIsHide(1);
                }
            }
        }
    }

    private void bindGroupData(FolderViewHolder holder, TreeUserDTO dto) {
        holder.title.setText(dto.getName());

        if (dto.getId() != 0) {
            holder.title.setText(dto.getItemName());

            if (dto.getIsHide() == 1) {
                holder.icon.setImageResource(R.drawable.home_folder_close_ic);
                //holder.lnl_child.setVisibility(View.GONE);
            } else {
                holder.icon.setImageResource(R.drawable.home_folder_open_ic);
                //holder.lnl_child.setVisibility(View.VISIBLE);
            }

        } else {
           /* holder.title.setVisibility(View.GONE);
            holder.icon.setVisibility(View.GONE);*/
        }
        holder.mLnTittle.setOnCreateContextMenuListener(this);
    }

    private int getMarginLeft(int level) {
        int marginLeft = level * this.left20dp;
        return marginLeft;
    }

    private void createChatRoom(final TreeUserDTO dto, int type) {
        if (type == TYPE_USER) {
//            if (dto.getId() != Utils.getCurrentId())
            HttpRequest.getInstance().CreateOneUserChatRoom(dto.getId(), new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                    intent.putExtra(Statics.TREE_USER_PC, dto);
                    intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                    BaseActivity.Instance.startActivity(intent);
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    Utils.showMessageShort("Fail");
                }
            });
//            else
//                Utils.showMessage(Utils.getString(R.string.can_not_chat));
        } else if (type == TYPE_FOLDER) {

            if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                getUser(dto.getSubordinates());
                if (temp != null && temp.size() > 0) {

                    HttpRequest.getInstance().CreateGroupChatRoom(temp, new ICreateOneUserChatRom() {
                        @Override
                        public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                            Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                            intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                            intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                            BaseActivity.Instance.startActivity(intent);
                        }

                        @Override
                        public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                            Utils.showMessageShort("Fail");
                        }
                    }, "");
                }

            }
        }
    }

    // Get all user of this group to add to chat room
    public void getUser(List<TreeUserDTO> treeUserDTOs) {
        if (treeUserDTOs != null && treeUserDTOs.size() != 0) {
            for (TreeUserDTO dto : treeUserDTOs) {
                if (dto.getSubordinates() != null && dto.getSubordinates().size() > 0) {
                    if (dto.getType() == Statics.TYPE_USER)
                        temp.add(dto);
                    getUser(dto.getSubordinates());
                } else {
                    if (dto.getType() == Statics.TYPE_USER)
                        temp.add(dto);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        final NLevelListItem object = getItem(position);
        TreeUserDTO dto = null;
        if (object != null) {
            dto = object.getObject();
        }

        switch (holder.getItemViewType()) {
            case TYPE_USER:

                if (dto != null) {
                    UserViewHolder holder2 = ((UserViewHolder) holder);
                    bindUserData(holder2, dto);

                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder2.lnItemWraper.getLayoutParams();
                    params1.leftMargin = getMarginLeft(object.getLevel());

                    // Set event listener
                    final TreeUserDTO finalDto1 = dto;
                    holder2.rootView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick 3");
                            // Click to chat with current user
                            createChatRoom(finalDto1, TYPE_USER);
                        }
                    });

                    holder2.rootView.setTag(object);
                    holder2.rootView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Log.d(TAG, "  holder2.rootView");
                            v.showContextMenu();
                            return true;
                        }
                    });
                }

                break;
            case TYPE_EMPPY:

                EmptyViewHolder holderEmppty = ((EmptyViewHolder) holder);
                LinearLayout.LayoutParams paramsEmpty = (LinearLayout.LayoutParams) holderEmppty.noData.getLayoutParams();
                paramsEmpty.weight = 1.0f;
                paramsEmpty.gravity = Gravity.CENTER_HORIZONTAL;

                // Calculate
                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;
                float w = holderEmppty.noData.getPaint().measureText(CrewChatApplication.getInstance().getResources().getString(R.string.no_data));
                paramsEmpty.leftMargin = Math.round((width - w) / 2 - w / 2);
                paramsEmpty.topMargin = 1000;

                // Nothing to do now
                break;
            case TYPE_FOLDER:
            default:
                if (dto != null) {
                    FolderViewHolder holder3 = ((FolderViewHolder) holder);
                    bindGroupData(holder3, dto);

                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) holder3.mLnTittle.getLayoutParams();

                    params2.leftMargin = getMarginLeft(object.getLevel());

                    // Set event listener
                    final TreeUserDTO finalDto = dto;
                    holder3.mLnTittle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick 2");
                            createChatRoom(finalDto, TYPE_FOLDER);
                        }
                    });

                    holder3.mLnTittle.setTag(object);
                    holder3.mLnTittle.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Log.d(TAG, "  holder3.mLnTittle");
//                            for(NLevelItem ob:list)
//                            {
//                                TreeUserDTO obj=ob.getObject();
//                                Log.d(TAG,new Gson().toJson(obj));
//                            }


                            TreeUserDTO dto = null;
                            if (object != null) {
                                dto = object.getObject();
//                                Log.d(TAG,"dto:"+new Gson().toJson(dto));

                                MultilLevelListviewFragment.idFolder = dto.getId();
//                                Log.d(TAG, dto.getId() + "\t" + new Gson().toJson(dto));
                            }


                            v.showContextMenu();
                            return true;
                        }
                    });
                    final TreeUserDTO dtoFolder = dto;
                    holder3.icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (holder instanceof FolderViewHolder) {
                                FolderViewHolder holder3 = ((FolderViewHolder) holder);
                                setHideShowFolder(holder3, dtoFolder);
                                Log.d(TAG, "FolderViewHolder");
                            } else {
                                Log.d(TAG, "not FolderViewHolder");
                            }

                            Log.d(TAG, "onClick 1");
                            isToggle = true;
                            toggle(position);
                            getFilter().filter();
                        }
                    });
                }
                break;

        }
    }

    public NLevelListItem getItem(int position) {
        if (getItemCount() == 1 && filtered.size() == 0) {
            return null;
        }
        return filtered.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) == null && getItemCount() == 1)
            return TYPE_EMPPY;

        if (getItem(position).getObject().getType() == Statics.TYPE_USER)
            return TYPE_USER;

        return TYPE_FOLDER;
    }

    @Override
    public int getItemCount() {
        if (filtered.size() == 0)
            return 1;
        return filtered.size();
    }

    public NLevelFilter getFilter() {
        return new NLevelFilter();
    }

    class NLevelFilter {

        public void filter() {
            new AsyncFilter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        // Filter user when receive text string
        public void filterUser(String text) {
            new SearchUserFilter().execute(text);
        }

        // Class filter when collapse or expand
        class AsyncFilter extends AsyncTask<Void, Void, ArrayList<NLevelListItem>> {

            @Override
            protected ArrayList<NLevelListItem> doInBackground(Void... arg0) {

                return (ArrayList<NLevelListItem>) filterItems();
            }

            @Override
            protected void onPostExecute(ArrayList<NLevelListItem> result) {
                setFiltered(result);
                NLevelRecycleAdapter.this.notifyDataSetChanged();
            }
        }

        // Class filter when search user
        class SearchUserFilter extends AsyncTask<String, Void, ArrayList<NLevelListItem>> {
            @Override
            protected ArrayList<NLevelListItem> doInBackground(String... params) {
                // Just filter user
                ArrayList<NLevelListItem> items = (ArrayList<NLevelListItem>) filterUsers(params[0]);
                return items;
            }

            @Override
            protected void onPostExecute(ArrayList<NLevelListItem> nLevelListItems) {
                setFiltered(nLevelListItems);
                NLevelRecycleAdapter.this.notifyDataSetChanged();
            }
        }

    }

    // Filter user by UserName or User ID
    public List<NLevelListItem> filterUsers(String textString) {
        List<NLevelListItem> tempfiltered = new ArrayList<NLevelListItem>();
        if (TextUtils.isEmpty(textString)) {
            tempfiltered.addAll(list);
        } else {

            for (NLevelListItem item : list) {
                //add expanded items and top level items
                //if parent is null then its a top level item
                if (item.getParent() == null && item.getObject() != null && item.getObject().getName() != null && item.getObject().getName().equals("Dazone")) {
                    tempfiltered.add(item);
                } else {
                    TreeUserDTO object = item.getObject();
                    if (object != null && object.getType() == Statics.TYPE_USER) {
                        if (object.getName().contains(textString)) {
                            tempfiltered.add(item);
                        }
                    }
                }
            }
        }

        return tempfiltered;
    }
// Filter user by UserName or User ID
  /*  public List<NLevelListItem> filterUsers(String textString) {
        List<NLevelListItem> tempfiltered = new ArrayList<NLevelListItem>();
        if (TextUtils.isEmpty(textString)) {
            tempfiltered.addAll(list);
        } else {

            for (NLevelListItem item : list) {
                //add expanded items and top level items
                //if parent is null then its a top level item
                if (item.getParent() == null && item.getObject() != null && item.getObject().getName() != null && item.getObject().getName().equals("Dazone")) {
                    tempfiltered.add(item);
                } else {
                    TreeUserDTO object = item.getObject();
                    if (object != null && object.getType() == Statics.TYPE_USER) {
                        if (
                                ((object.getName().toUpperCase().contains(textString.toUpperCase()))
                                        || (object.getPhoneNumber() != null && object.getPhoneNumber().toUpperCase().contains(textString.toUpperCase()))
                                        || (object.getPhoneNumber() != null && object.getPhoneNumber().toUpperCase().replace("-", "").contains(textString.toUpperCase()))
                                        || (object.getCompanyNumber() != null && object.getCompanyNumber().toUpperCase().contains(textString.toUpperCase()))
                                        || (object.getCompanyNumber() != null && object.getCompanyNumber().toUpperCase().replace("-", "").contains(textString.toUpperCase())))
                                ||(object.getPosition().toUpperCase().contains(textString.toUpperCase()))) {
                            tempfiltered.add(item);
                        }
                    }
                }
            }
        }

        return tempfiltered;
    }*/

    public List<NLevelListItem> filterItems() {

        // sort list
//        Collections.sort(list, new Comparator<NLevelItem>() {
//            @Override
//            public int compare(NLevelItem s1, NLevelItem s2) {
//                return s1.getObject().getName().compareTo(s2.getObject().getName());
//            }
//        });


        List<NLevelListItem> tempfiltered = new ArrayList<NLevelListItem>();
        OUTER:
        for (NLevelListItem item : list) {
            //add expanded items and top level items
            //if parent is null then its a top level item
            if (item.getParent() == null) {
                tempfiltered.add(item);
            } else {
                //go through each ancestor to make sure they are all expanded
                NLevelListItem parent = item;
                while ((parent = parent.getParent()) != null) {
                    if (!parent.isExpanded()) {
                        //one parent was not expanded
                        //skip the rest and continue the OUTER for loop
                        continue OUTER;
                    }
                }

                tempfiltered.add(item);
            }
        }

        return tempfiltered;
    }

    public void toggle(int arg2) {
        filtered.get(arg2).toggle();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu");
        NLevelItem dto = null;
        try {
            dto = (NLevelItem) v.getTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dto != null) {
            mTempDto = dto;

            if (dto.getObject().getType() == Statics.TYPE_USER) {
                if (dto.getParent() != null) {
                    if (dto.getParent().getObject() != null) {
                        if (dto.getParent().getObject().getType() == 0) {
                            return;
                        }
                    }
                }
                if (menu.size() == 0) {
                    Resources res = CrewChatApplication.getInstance().getResources();
                    MenuItem removeFavorite = menu.add(0, Statics.MENU_REMOVE_FROM_FAVORITE, 0, res.getString(R.string.remove_from_favorite));
                    MenuItem openChatRoom = menu.add(0, Statics.MENU_OPEN_CHAT_ROOM, 0, res.getString(R.string.open_chat_room));

                    removeFavorite.setOnMenuItemClickListener(this);
                    openChatRoom.setOnMenuItemClickListener(this);
                }
            } else {

                if (dto.getObject().getType() == 0) {
                    return;
                }

                if (menu.size() == 0) {
                    Resources res = CrewChatApplication.getInstance().getResources();
                    MenuItem openChatRoom = menu.add(0, Statics.MENU_OPEN_CHAT_GROUP, 0, res.getString(R.string.open_chat_room));
                    MenuItem registedUser = menu.add(0, Statics.MENU_REGISTERED_USERS, 0, res.getString(R.string.registered_users));
                    MenuItem modifyGroup = menu.add(0, Statics.MENU_MODIFYING_GROUP, 0, res.getString(R.string.modifying_group));

                    openChatRoom.setOnMenuItemClickListener(this);
                    registedUser.setOnMenuItemClickListener(this);
                    modifyGroup.setOnMenuItemClickListener(this);
                    if (dto.getObject().getId() != Statics.ID_GROUP) {
                        MenuItem deleteGroup = menu.add(0, Statics.MENU_DELETE_GROUP, 0, res.getString(R.string.delete_group));
                        deleteGroup.setOnMenuItemClickListener(this);
                    }

                }
            }
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final TreeUserDTO dto = mTempDto.getObject();
        switch (item.getItemId()) {
            case Statics.MENU_REMOVE_FROM_FAVORITE:
                // Call API to remove an user from favorite list
                HttpRequest.getInstance().deleteFavoriteUser(dto.getParent(), dto.getId(), new BaseHTTPCallbackWithJson() {
                    @Override
                    public void onHTTPSuccess(String jsonData) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FavoriteUserDBHelper.deleteFavoriteUser(dto.getParent(), dto.getId());
                            }
                        }).start();

                        list.remove(mTempDto);
                        reloadData();
                    }

                    @Override
                    public void onHTTPFail(ErrorDto errorDto) {
                        Toast.makeText(CrewChatApplication.getInstance(), "Has failed", Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case Statics.MENU_OPEN_CHAT_ROOM:

                if (dto.getId() != Utils.getCurrentId())
                    HttpRequest.getInstance().CreateOneUserChatRoom(dto.getId(), new ICreateOneUserChatRom() {
                        @Override
                        public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                            Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                            intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                            intent.putExtra(Statics.TREE_USER_PC, dto);
                            intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                            BaseActivity.Instance.startActivity(intent);
                        }

                        @Override
                        public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                            Utils.showMessageShort("Fail");
                        }
                    });
                else
                    Utils.showMessage(Utils.getString(R.string.can_not_chat));

                break;

            case Statics.MENU_REGISTERED_USERS:

                ArrayList<Integer> uNos = new ArrayList<>();
                for (NLevelItem u : list) {
                    if (u.getParent() != null && u.getParent().equals(mTempDto))
                        uNos.add(u.getObject().getId());
                }

                // Callback to modify user
                if (mCallback != null) {
                    mCallback.onShow(mTempDto, uNos);
                }

                break;
            case Statics.MENU_MODIFYING_GROUP:

                Resources res = CrewChatApplication.getInstance().getResources();
                String groupName = res.getString(R.string.group_name);
                String confirm = res.getString(R.string.confirm);
                String cancel = res.getString(R.string.cancel);

                AlertDialogView.alertDialogConfirmWithEditText(mContext, groupName, groupName, dto.getName(), confirm, cancel, new AlertDialogView.onAlertDialogViewClickEventData() {
                    @Override
                    public void onOkClick(String groupName) {
                        // Call API to add group
                        int sortNo = 0;
                        updateFavoriteGroup(mTempDto, groupName, sortNo);
                    }

                    @Override
                    public void onCancelClick() {
                        // Dismiss dialog
                    }
                });

                break;
            case Statics.MENU_DELETE_GROUP:
                AlertDialogView.normalAlertDialogWithCancel(mContext, Utils.getString(R.string.app_name), Utils.getString(R.string.favorite_group_delete_warning), Utils.getString(R.string.no), Utils.getString(R.string.yes), new AlertDialogView.OnAlertDialogViewClickEvent() {
                    @Override
                    public void onOkClick(DialogInterface alertDialog) {
                        onDeleteGroup(dto.getId());
                    }

                    @Override
                    public void onCancelClick() {

                    }
                });

                break;
            case Statics.MENU_OPEN_CHAT_GROUP:

                if (dto.getId() == Statics.ID_GROUP) {
                    int temp = 0;
                    for (int i = 0; i < list.size(); i++) {
                        NLevelListItem obj = list.get(i);
                        if (obj.getObject().getType() == 1 && obj.getObject().getId() == Statics.ID_GROUP) {
                            temp = i;
                            break;
                        }
                    }
                    if (temp + 1 < list.size()) {
                        ArrayList<TreeUserDTO> lst = new ArrayList<>();
                        for (int i = temp + 1; i < list.size(); i++) {
                            NLevelListItem obj = list.get(i);
                            TreeUserDTO t = obj.getObject();
                            if (t.getType() == 2) {
                                //get list user
                                if (isAdd(lst, t))
                                    lst.add(t);
                            }
                        }
                        if (lst.size() > 0) {
//                            for(TreeUserDTO obj:lst)
//                            {
//                                Log.d(TAG,new Gson().toJson(obj));
//                            }
                            createChatRoom(lst);
                        }
                    }

                } else {
                    int temp = 0;
                    for (int i = 0; i < list.size(); i++) {
                        NLevelListItem obj = list.get(i);
                        if (obj.getObject().getType() == 1 && obj.getObject().getId() == dto.getId()) {
                            temp = i;
                            break;
                        }
                    }
                    if (temp + 1 < list.size()) {
                        ArrayList<TreeUserDTO> lst = new ArrayList<>();
                        for (int i = temp + 1; i < list.size(); i++) {
                            NLevelListItem obj = list.get(i);
                            TreeUserDTO t = obj.getObject();
                            if (t.getType() == 2 && t.getParent() == dto.getId()) {
                                //get list user
                                if (isAdd(lst, t))
                                    lst.add(t);
                            }
                        }
                        if (lst.size() > 0) {
//                            for(TreeUserDTO obj:lst)
//                            {
//                                Log.d(TAG,new Gson().toJson(obj));
//                            }
                            createChatRoom(lst);
                        }
                    }

                }


                break;
        }

        return false;
    }

    boolean isAdd(List<TreeUserDTO> lst, TreeUserDTO dto) {
        for (TreeUserDTO obj : lst) {
            if (obj.getId() == dto.getId())
                return false;
        }
        return true;
    }

    private void createChatRoom(final ArrayList<TreeUserDTO> selectedPersonList) {
        if (selectedPersonList.size() == 1) {
            if (selectedPersonList.get(0).getId() == Utils.getCurrentId()) {
                Utils.showMessage(Utils.getString(R.string.can_not_chat));
            } else {
                HttpRequest.getInstance().CreateOneUserChatRoom(selectedPersonList.get(0).getId(), new ICreateOneUserChatRom() {
                    @Override
                    public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                        Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                        intent.putExtra(Statics.TREE_USER_PC, selectedPersonList.get(0));
                        intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                        intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                        BaseActivity.Instance.startActivity(intent);
                    }

                    @Override
                    public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                        Utils.showMessageShort("Fail");
                    }
                });
            }
        } else if (selectedPersonList.size() > 1) {
            HttpRequest.getInstance().CreateGroupChatRoom(selectedPersonList, new ICreateOneUserChatRom() {
                @Override
                public void onICreateOneUserChatRomSuccess(ChattingDto chattingDto) {
                    Intent intent = new Intent(BaseActivity.Instance, ChattingActivity.class);
                    intent.putExtra(Statics.CHATTING_DTO, chattingDto);
                    intent.putExtra(Constant.KEY_INTENT_ROOM_NO, chattingDto.getRoomNo());
                    BaseActivity.Instance.startActivity(intent);
                }

                @Override
                public void onICreateOneUserChatRomFail(ErrorDto errorDto) {
                    Utils.showMessageShort("Fail");
                }
            }, "");
        }
    }

    public void reloadData() {

        filtered = filterItems();
        Log.d(TAG, "reloadData");
        ArrayList<TreeUserDTOTemp> lst = null;
        if (CompanyFragment.instance != null) lst = CompanyFragment.instance.getUser();
        if (lst == null) lst = new ArrayList<>();
        Log.d("sssDebug", "filtered" + filtered.toString());

        if (lst != null) {
            if (lst.size() > 0) {
                for (NLevelListItem obj : filtered) {
                    TreeUserDTO dto = obj.getObject();
//                    Log.d(TAG, new Gson().toJson(dto));
                    Log.d("sssDebug", "dto.getName()/dto.getCompanyNumber()" + dto.getName() + "/" + dto.getCompanyNumber());
                    Log.d("sssDebug", "dto" + dto.toString());
                    if (dto.getType() == 2) {
                        for (TreeUserDTOTemp temp : lst) {
                            int status = temp.getStatus();
                            int userNo = temp.getUserNo();
                            if (dto.getId() == userNo) {
                                dto.setStatus(status);

//                                Log.d(TAG, dto.getName() + ":" + dto.getId() + ":" + status);
                            }
                        }
                    }
                }
            }
        }

        this.notifyDataSetChanged();
    }

    /* Function call API delete favorite group */
    /*
    * Delete group and delete all user of this group
    * */
    private void onDeleteGroup(final long groupNo) {
        HttpRequest.getInstance().deleteFavoriteGroup(groupNo, new BaseHTTPCallBack() {
            @Override
            public void onHTTPSuccess() {
                // Delete favorite group in new thread

                // notify local data
                for (Iterator<NLevelItem> iterator = list.iterator(); iterator.hasNext(); ) {
                    NLevelItem item = iterator.next();
                    if (item.getParent() != null && item.getParent().getObject().getId() == mTempDto.getObject().getId()) {
                        iterator.remove();
                    }
                }
                list.remove(mTempDto);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FavoriteGroupDBHelper.deleteFavoriteGroup(groupNo);
                        CrewChatApplication.listFavoriteGroup = FavoriteGroupDBHelper.getFavoriteGroup();
                    }
                }).start();

                reloadData();
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {

            }
        });
    }

    /* Function to request to server to update favorite group */
    private void updateFavoriteGroup(final NLevelItem item, final String groupNam, int sortNo) {
        final TreeUserDTO dto = item.getObject();
        HttpRequest.getInstance().updateFavoriteGroup(dto.getId(), groupNam, sortNo, new BaseHTTPCallbackWithJson() {
            @Override
            public void onHTTPSuccess(String jsonData) {
                // refresh view again, find item to update
                int indexof = list.indexOf(item);
                if (indexof != -1) {

                    NLevelItem foundItem = list.get(indexof);

                    // perform update by new thread
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FavoriteGroupDBHelper.updateGroup(dto.getId(), groupNam);
                        }
                    }).start();

                    // Set current dataset value
                    foundItem.getObject().setName(groupNam);
                    foundItem.getObject().setNameEN(groupNam);
                    // notify data set
                    reloadData();
                }

            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {
            }
        });
    }
}
