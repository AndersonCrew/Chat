package com.dazone.crewchatoff.adapter;


import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachImageList;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.TimeUtils;
import com.dazone.crewchatoff.utils.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by maidinh on 7/2/2017.
 */

public class AttachFileBoxAdapter extends RecyclerView.Adapter<AttachFileBoxAdapter.RecyclerViewHolders> {
    private List<AttachImageList> imagesURL;
    private Context context;
    String TAG = "AttachFileBoxAdapter";
    List<TreeUserDTOTemp> allUser;


    public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public TextView tvFileName, tvUserName, tvSize, tvDate;
        public RelativeLayout btnDetails;

        // add event  click
        public RecyclerViewHolders(View itemView) {
            super(itemView);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDetails.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            Resources res = CrewChatApplication.getInstance().getResources();
            MenuItem roomOut = contextMenu.add(0, Statics.MENU_DOWNLOAD, 0, res.getString(R.string.download));
            roomOut.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case Statics.MENU_DOWNLOAD:
                    if (objTemp != null) {
                        String url = String.format("/UI/CrewChat/MobileAttachDownload.aspx?session=%s&no=%s",
                                new Prefs().getaccesstoken(), objTemp.getAttachNo());
                        String urlDownload = new Prefs().getServerSite() + url;
                        Utils.displayDownloadFileDialog(context, urlDownload, objTemp.getFileName());
                    }
                    break;
            }
            return false;
        }
    }

    public void updateList(List<AttachImageList> imagesURL) {
        if (imagesURL != null) {
            if (imagesURL.size() > 0) {
                this.imagesURL = imagesURL;
                this.notifyDataSetChanged();
            }
        }
    }

    public AttachFileBoxAdapter(Context context, List<AttachImageList> imagesURL, List<TreeUserDTOTemp> allUser) {
        this.context = context;
        this.imagesURL = imagesURL;
        this.allUser = allUser;
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_attach_file_box_layout, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    AttachImageList objTemp;

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, final int position) {
        final AttachImageList obj = imagesURL.get(position);
        holder.tvFileName.setText(obj.getFileName());
        String userName;
        userName = Constant.getUserName(allUser, obj.getUserNo());
        if (userName.length() == 0) {
            userName = context.getResources().getString(R.string.unknown);
        }
        holder.tvUserName.setText(userName);
        holder.tvSize.setText("" + obj.getSize() + "KB");
        holder.tvDate.setText(TimeUtils.displayTimeWithoutOffset(context, obj.getRegDate(), 0, TimeUtils.KEY_FROM_SERVER));
        holder.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRow(obj);
            }
        });

        holder.btnDetails.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                objTemp = obj;
                view.showContextMenu();
                return true;
            }
        });
    }

    void clickRow(AttachImageList obj) {
        String url = String.format("/UI/CrewChat/MobileAttachDownload.aspx?session=%s&no=%s",
                new Prefs().getaccesstoken(), obj.getAttachNo());
        String urlDownload1 = new Prefs().getServerSite() + url;
        Log.d(TAG, "urlDownload1:" + urlDownload1);
        String path = "";
        path = Environment.getExternalStorageDirectory() + Constant.pathDownload + "/" + obj.getFileName();
        Log.d(TAG, "path:" + path);

        if (obj != null) {
            Utils.displayDownloadFileDialog(context, urlDownload1, obj.getFileName());
        }
    }

    @Override
    public int getItemCount() {
        return this.imagesURL.size();
    }
}
