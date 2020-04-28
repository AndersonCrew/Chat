package com.dazone.crewchatoff.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dazone.crewchatoff.dto.MenuDrawItem;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.ImageUtils;

import java.util.List;

/**
 * Created by david on 12/18/15.
 */
public class SelectListAdapter extends DrawerListAdapter<MenuDrawItem> {

    public SelectListAdapter(Context context, List<MenuDrawItem> navItems) {
        super(context, navItems);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MenuDrawItem item = mMenuItems.get(position);
        if (item.isHide())
        {
            view = inflater.inflate(R.layout.row_null,null);
        }
        else {
            view = inflater.inflate(R.layout.row_menu, null);
            TextView titleView = view.findViewById(R.id.title);
            ImageView iconView = view.findViewById(R.id.ic_folder);

            titleView.setText(item.getStringTitle());
            titleView.setTextColor(Color.BLACK);
            ImageUtils.showImage(item, iconView);
        }
        return view;
    }

    @Override
    public long getItemId(int position) {
        return mMenuItems.get(position).getItemID();
    }
}
