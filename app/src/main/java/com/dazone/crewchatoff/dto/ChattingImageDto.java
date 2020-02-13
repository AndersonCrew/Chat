package com.dazone.crewchatoff.dto;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.Serializable;

public class ChattingImageDto implements Serializable {
    private ImageView ivChatting;
    private Bitmap bmpResource;

    public ImageView getIvChatting() {
        return ivChatting;
    }

    public void setIvChatting(ImageView ivChatting) {
        this.ivChatting = ivChatting;
    }

    public Bitmap getBmpResource() {
        return bmpResource;
    }

    public void setBmpResource(Bitmap bmpResource) {
        this.bmpResource = bmpResource;
    }

    public ChattingImageDto(ImageView ivChatting, Bitmap bmpResource) {
        this.ivChatting = ivChatting;
        this.bmpResource = bmpResource;
    }
}