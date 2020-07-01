package com.dazone.crewchatoff.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.DrawImageItem;
import com.dazone.crewchatoff.dto.MenuDrawItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;

public class ImageUtils {
    public static String TAG = "ImageUtils";
    public static CropCircleTransformation mCropCircleTransformation = new CropCircleTransformation(CrewChatApplication.getInstance());

    @TargetApi(Build.VERSION_CODES.M)
    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static void setImgFromUrl(String url, final ImageView img) {
        Glide.with(CrewChatApplication.getInstance()).load(url).asBitmap().placeholder(R.drawable.loading).error(R.drawable.error_image).diskCacheStrategy(DiskCacheStrategy.ALL).override(200, 200).into(new BitmapImageViewTarget(img) {
            @Override
            protected void setResource(Bitmap resource) {
                img.setImageBitmap(resource);
            }
        });
    }

    public static void showRoundImage(DrawImageItem dto, ImageView view) {
        if (dto == null) {
            return;
        }

        try {
            ShowRoundImage(dto.getImageLink(), view);
        } catch (Exception e) {
            Log.d("lchTest", e.toString());
            Glide.with(CrewChatApplication.getInstance()).load(Constant.UriDefaultAvatar).bitmapTransform(mCropCircleTransformation).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
        }
    }

    public static void ShowRoundImage(String url, ImageView view) {
        String rootUrl = new Prefs().getServerSite() + url;
        Glide.with(CrewChatApplication.getInstance()).load(rootUrl).bitmapTransform(mCropCircleTransformation).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
    }

    public static void showBadgeImage(int count, ImageView view) {
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(String.valueOf(count), ImageUtils.getColor(view.getContext(), R.color.badge_bg_color));
        drawable.setPadding(1, 1, 1, 1);
        view.setImageDrawable(drawable);
    }

    public static void showImage(MenuDrawItem dto, ImageView view) {
        if (TextUtils.isEmpty(dto.getMenuIconUrl())) {
            view.setImageResource(dto.getIconResID());
        } else {
            showImage(dto.getMenuIconUrl(), view);
        }
    }

    public static void showImageFull(final Context context, String url, final ImageView imageView) {
        String fullUrl = new Prefs().getServerSite() + url;
        Log.d(TAG, "fullUrl:" + fullUrl);
        Glide.with(context)
                .load(fullUrl)
                .placeholder(R.drawable.loading)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.loading)
                .fallback(R.drawable.loading)
                .into(imageView);

    }

    public static void loadImageNormal(String url, final ImageView view) {
        Glide.with(CrewChatApplication.getInstance())
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        view.setImageBitmap(resource);
                    }
                });
    }

    public static void loadImageNormalNoCache(String url, final ImageView view) {
        Glide.with(CrewChatApplication.getInstance())
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        view.setImageBitmap(resource);
                    }
                });
    }

    public static void showImage(final String url, final ImageView view) {
        if (TextUtils.isEmpty(url)) {
            ImageLoader.getInstance().displayImage("http://www.blogto.com/upload/2009/02/20090201-dazone.jpg", view, Statics.options);
        } else {
            if (url.contains("file")) {
                ImageUtils.loadImageNormalNoCache(url, view);

            } else if (url.contains("content") || url.contains("storage")) {
                File f = new File(url);
                if (f.exists()) {
                    ImageUtils.loadImageNormal(url, view);
                } else {
                    ImageUtils.loadImageNormal(url, view);
                }
            } else {
                ImageLoader.getInstance().displayImage(new Prefs().getServerSite() + url, view, Statics.options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view2, FailReason failReason) {
                        ImageUtils.loadImageNormal("http://www.blogto.com/upload/2009/02/20090201-dazone.jpg", view);
                    }

                    @Override
                    public void onLoadingComplete(String s, View view2, Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
            }
        }
    }

    public static void imageFileType(ImageView imageView, String fileType) {
        String uri = "";
        if (fileType.equalsIgnoreCase(".apk")) {
            uri = "drawable://" + R.drawable.android;
        } else if (fileType.equalsIgnoreCase(".doc") || fileType.equalsIgnoreCase(".docx")) {
            uri = "drawable://" + R.drawable.word;
        } else if (fileType.equalsIgnoreCase(".xls") || fileType.equalsIgnoreCase(".xlsx")) {
            uri = "drawable://" + R.drawable.excel;
        } else if (fileType.equalsIgnoreCase(".ppt") || fileType.equalsIgnoreCase(".pptx")) {
            uri = "drawable://" + R.drawable.power_point;
        } else if (fileType.equalsIgnoreCase(".hwp")) {
            uri = "drawable://" + R.drawable.hwp;
        } else if (fileType.equalsIgnoreCase(".pdf")) {
            uri = "drawable://" + R.drawable.pdf;
        } else if (fileType.equalsIgnoreCase(".exe")) {
            uri = "drawable://" + R.drawable.exe;
        } else if (fileType.equalsIgnoreCase(".txt") || fileType.equalsIgnoreCase(".log") || fileType.equalsIgnoreCase(".sql")) {
            uri = "drawable://" + R.drawable.file;
        } else if (fileType.equalsIgnoreCase(".zip") || fileType.equalsIgnoreCase(".zap") || fileType.equalsIgnoreCase(".alz")) {
            uri = "drawable://" + R.drawable.compressed;
        } else if (fileType.equalsIgnoreCase(".html") || fileType.equalsIgnoreCase(".htm")) {
            uri = "drawable://" + R.drawable.html;
        } else if (fileType.equalsIgnoreCase(".avi") || fileType.equalsIgnoreCase(".mov") || fileType.equalsIgnoreCase(".mp4")) {
            uri = "drawable://" + R.drawable.movie;
        } else if (fileType.equalsIgnoreCase(Statics.AUDIO_MP3) || fileType.equalsIgnoreCase(".wav") || fileType.equalsIgnoreCase(Statics.AUDIO_AMR)
                || fileType.equalsIgnoreCase(Statics.AUDIO_WMA) || fileType.equalsIgnoreCase(Statics.AUDIO_M4A)) {
            uri = "drawable://" + R.drawable.play_icon;
        }
        if (TextUtils.isEmpty(uri)) {
            uri = "drawable://" + R.drawable.file;
        }
        ImageLoader.getInstance().displayImage(uri, imageView);

    }

    public static void showCycleImageFromLink(String link, final ImageView imageview, int dimen_id) {
        final int size = (Utils.getDimenInPx(dimen_id));
        Glide.with(CrewChatApplication.getInstance()).load(link).asBitmap().placeholder(R.drawable.avatar_l).error(R.drawable.avatar_l).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).override(size, size).into(new BitmapImageViewTarget(imageview) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(CrewChatApplication.getInstance().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageview.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public static void showCycle(String link, final ImageView imageview, int dimen_id) {
        final int size = (Utils.getDimenInPx(dimen_id));

        Picasso.with(CrewChatApplication.getInstance())
                .load(link)
                .resize(size, size).onlyScaleDown()
                .error(R.drawable.avatar_l)
                .placeholder(R.drawable.avatar_l)

                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        //Set it in the ImageView
                        imageview.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

    }

    public static void showCycleImageFromLinkScale(Context context, String link, final ImageView imageview, int dimen_id) {
        final int size = (Utils.getDimenInPx(dimen_id));
        Glide.with(context)
                .load(link)
                .asBitmap()
                .override(size, size)
                .placeholder(R.drawable.avatar_l)
                .fallback(R.drawable.avatar_l)
                .error(R.drawable.avatar_l)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .transform(new CircleTransform(CrewChatApplication.getInstance()))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageview.setImageBitmap(resource);
                    }
                });
    }
}