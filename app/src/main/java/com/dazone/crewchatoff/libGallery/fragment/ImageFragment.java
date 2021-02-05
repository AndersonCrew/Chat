/*
 * Copyright 2013 - learnNcode (learnncode@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package com.dazone.crewchatoff.libGallery.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.libGallery.MediaChooserConstants;
import com.dazone.crewchatoff.libGallery.MediaModel;
import com.dazone.crewchatoff.libGallery.adapter.GridViewAdapter;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.utils.CrewChatApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageFragment extends Fragment {
    private String TAG = "ImageFragment";
    private ArrayList<String> mSelectedItems = new ArrayList<String>();
    private ArrayList<MediaModel> mGalleryModelList;
    private GridView mImageGridView;
    private View mView;
    private OnImageSelectedListener mCallback;
    private GridViewAdapter mImageAdapter;
    private Cursor mImageCursor;


    // Container Activity must implement this interface
    public interface OnImageSelectedListener {
        void onImageSelected(int count);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnImageSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnImageSelectedListener");
        }
    }

    public ImageFragment() {
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.view_grid_layout_media_chooser, container, false);

            mImageGridView = mView.findViewById(R.id.gridViewFromMediaChooser);


            if (getArguments() != null) {
                initPhoneImages(getArguments().getString("name"));
            } else {
                initPhoneImages();
            }

        } else {
            ((ViewGroup) mView.getParent()).removeView(mView);
            if (mImageAdapter == null || mImageAdapter.getCount() == 0) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
            }
        }

        return mView;
    }


    private void initPhoneImages(String bucketName) {
        try {
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            String searchParams = null;
            String bucket = bucketName;
            searchParams = "bucket_display_name = \"" + bucket + "\"";

            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

            mImageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, searchParams, null, orderBy + " DESC");

            setAdapter(mImageCursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPhoneImages() {
        try {
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            mImageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");

            setAdapter(mImageCursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setAdapter(Cursor imageCursor) {
        if (imageCursor.getCount() > 0) {
            mGalleryModelList = new ArrayList<>();
            for (int i = 0; i < imageCursor.getCount(); i++) {
                imageCursor.moveToPosition(i);
                int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                MediaModel galleryModel = new MediaModel(imageCursor.getString(dataColumnIndex), false);
                mGalleryModelList.add(galleryModel);
            }

            mImageAdapter = new GridViewAdapter(getActivity(), 0, mGalleryModelList, false);
            mImageGridView.setAdapter(mImageAdapter);
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.no_media_file_available), Toast.LENGTH_SHORT).show();
        }

        mImageGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
                MediaModel galleryModel = adapter.getItem(position);
                File file = new File(galleryModel.url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                startActivity(intent);
                return true;
            }
        });

        mImageGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                // update the mStatus of each category in the adapter
                GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
                MediaModel galleryModel = adapter.getItem(position);

                if (!galleryModel.status) {
                    if (CrewChatApplication.getInstance().getPrefs().getDDSServer().contains(Statics.chat_jw_group_co_kr)) {
                        List<MediaModel> mGalleryModelList = adapter.getList();
                        int index = 0;
                        for (int i = 0; i < mGalleryModelList.size(); i++) {
                            MediaModel obj = mGalleryModelList.get(i);
                            if (obj.status) {
                                index++;
                            }
                        }
                        if (index >= Statics.chat_jw_group_co_kr_limit_image) {
                            Toast.makeText(getActivity(), "Limit " +
                                    Statics.chat_jw_group_co_kr_limit_image + " image", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }


                    long size = MediaChooserConstants.ChekcMediaFileSize(new File(galleryModel.url), false);
                    if (size != 0) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.file_size_exeeded) + "  " + MediaChooserConstants.SELECTED_IMAGE_SIZE_IN_MB + " " + getActivity().getResources().getString(R.string.mb), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ((MediaChooserConstants.MAX_MEDIA_LIMIT == MediaChooserConstants.SELECTED_MEDIA_COUNT)) {
                        if (MediaChooserConstants.SELECTED_MEDIA_COUNT < 2) {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.max_limit_file) + "  " + MediaChooserConstants.SELECTED_MEDIA_COUNT + " " + getActivity().getResources().getString(R.string.file), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.max_limit_file) + "  " + MediaChooserConstants.SELECTED_MEDIA_COUNT + " " + getActivity().getResources().getString(R.string.files), Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                }

                // inverse the status
                galleryModel.status = !galleryModel.status;

                adapter.notifyDataSetChanged();

                if (galleryModel.status) {
                    mSelectedItems.add(galleryModel.url);
                    MediaChooserConstants.SELECTED_MEDIA_COUNT++;

                } else {
                    mSelectedItems.remove(galleryModel.url.trim());
                    MediaChooserConstants.SELECTED_MEDIA_COUNT--;
                }

                if (mCallback != null) {
                    mCallback.onImageSelected(mSelectedItems.size());
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("list", mSelectedItems);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                }

            }
        });
    }

    public ArrayList<String> getSelectedImageList() {
        return mSelectedItems;
    }

    public void addItem(String item) {
        if (mImageAdapter != null) {
            MediaModel model = new MediaModel(item, false);
            mGalleryModelList.add(0, model);
            mImageAdapter.notifyDataSetChanged();
        } else {
            initPhoneImages();
        }
    }
}
