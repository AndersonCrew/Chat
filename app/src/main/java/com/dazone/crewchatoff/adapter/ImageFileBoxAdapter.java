package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.ChatViewImageActivity;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.dto.AttachImageList;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.interfaces.Urls;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.ImageUtils;
import com.dazone.crewchatoff.utils.Prefs;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maidinh on 6/2/2017.
 */

public class ImageFileBoxAdapter extends RecyclerView.Adapter<ImageFileBoxAdapter.RecyclerViewHolders> {
    private List<AttachImageList> imagesURL;
    private Context context;
    String TAG = "ImageFileBoxAdapter";
    List<TreeUserDTOTemp> allUser;

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {
        public ImageView iv;
        public LinearLayout btnDetails;

        // add event  click
        public RecyclerViewHolders(View itemView) {
            super(itemView);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            iv = itemView.findViewById(R.id.iv);
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

    public ImageFileBoxAdapter(Context context, List<AttachImageList> imagesURL, List<TreeUserDTOTemp> allUser) {
        this.context = context;
        this.imagesURL = imagesURL;
        this.allUser = allUser;
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_iv_file_box_layout, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, final int position) {
        AttachImageList obj = imagesURL.get(position);
        String urlTemp = new Prefs().getServerSite() + Urls.URL_DOWNLOAD_THUMBNAIL + "session=" + CrewChatApplication.getInstance().getPrefs().getaccesstoken() + "&no=" + obj.getAttachNo();
        ImageUtils.setImgFromUrl(urlTemp, holder.iv);
        holder.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ChattingDto> urls = new ArrayList<>();

                for (AttachImageList attachImageList : imagesURL) {
                    ChattingDto dto = new ChattingDto();
                    AttachDTO AttachInfo = new AttachDTO();

                    AttachInfo.setFileName(attachImageList.getFileName());

                    dto.setAttachNo(attachImageList.getAttachNo());
                    dto.setRegDate(attachImageList.getRegDate());
                    dto.setMessageNo(attachImageList.getMessageNo());
                    dto.setRoomNo(attachImageList.getRoomNo());
                    dto.setUserNo(attachImageList.getUserNo());
                    dto.setAttachFileName(attachImageList.getFileName());
                    dto.setAttachFilePath(attachImageList.getFullPath());

                    dto.setAttachFileSize(attachImageList.getSize());
                    dto.setAttachFileType(attachImageList.getType());
                    dto.setAttachInfo(AttachInfo);

                    urls.add(dto);
                }

                Prefs prefs = CrewChatApplication.getInstance().getPrefs();
                if (urls.size() > 0)
                    prefs.setIMAGE_LIST(new Gson().toJson(urls));
                else
                    prefs.setIMAGE_LIST("");

                Intent intent = new Intent(context, ChatViewImageActivity.class);
                intent.putExtra(Statics.CHATTING_DTO_GALLERY_POSITION, position);
                intent.putExtra(Statics.get_user_name_from_db, true);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.imagesURL.size();
    }
}
