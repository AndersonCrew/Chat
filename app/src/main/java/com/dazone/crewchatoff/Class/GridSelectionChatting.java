package com.dazone.crewchatoff.Class;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dazone.crewchatoff.adapter.SelectionPlusAdapter;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.GridDecoration;
import com.dazone.crewchatoff.dto.SelectionPlusDto;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.CrewChatApplication;

import java.util.ArrayList;
import java.util.List;

public class GridSelectionChatting extends BaseViewClass {
    private RecyclerView selection_rcl;
    private List<SelectionPlusDto> dataSet;

    public GridSelectionChatting(Context context) {
        super(context);
        setupView();
    }

    @Override
    protected void setupView() {
        currentView = inflater.inflate(R.layout.grid_selection_chatting, null);
        selection_rcl = currentView.findViewById(R.id.selection_rcl);
        initView();
    }

    private void initView() {
        dataSet = new ArrayList<>();


        if (CrewChatApplication.getInstance().getPrefs().getDDSServer().contains(Statics.chat_jw_group_co_kr)) {
            dataSet.add(new SelectionPlusDto(1));
            dataSet.add(new SelectionPlusDto(3));
            dataSet.add(new SelectionPlusDto(6));
        } else {
            dataSet.add(new SelectionPlusDto(1));
            dataSet.add(new SelectionPlusDto(2));
            dataSet.add(new SelectionPlusDto(3));
            dataSet.add(new SelectionPlusDto(4));
            dataSet.add(new SelectionPlusDto(5));
            dataSet.add(new SelectionPlusDto(6));
        }

        dataSet.add(new SelectionPlusDto(7));
        selection_rcl.setHasFixedSize(true);
        selection_rcl.setLayoutManager(new GridLayoutManager(context, 3));
        selection_rcl.addItemDecoration(new GridDecoration(0));
        SelectionPlusAdapter adapter = new SelectionPlusAdapter(dataSet);
        selection_rcl.setAdapter(adapter);
    }
}