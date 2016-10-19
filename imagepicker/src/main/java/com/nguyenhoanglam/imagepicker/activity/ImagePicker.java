/*
 * Created by Nguyen Hoang Lam
 * Date: ${DATE}
 */

package com.nguyenhoanglam.imagepicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.helper.Constants;
import com.nguyenhoanglam.imagepicker.listeners.OnFragmentItemSelected;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.ArrayList;

import static com.nguyenhoanglam.imagepicker.helper.Constants.*;

/**
 * Created by hoanglam on 8/4/16.
 */
public abstract class ImagePicker {

    private int mode;
    private int limit;
    private int gridColumn;
    private boolean showCamera;
    private String folderTitle;
    private String imageTitle;
    private ArrayList<Image> selectedImages;
    private boolean folderMode;
    private String imageDirectory;

    public abstract void start(int requestCode);

    public static class ImagePickerFragmentFromActivity extends ImagePicker {

        private Activity activity;
        @IdRes
        private int layoutId;

        public ImagePickerFragmentFromActivity(Activity activity, @IdRes int layoutId) {
            this.activity = activity;
            this.layoutId = layoutId;
            init(activity);
        }

        @Override
        public void start(int requestCode) {
            startFragment(activity, layoutId);
        }
    }

    public static class ImagePickerFragmentFromFragment extends ImagePicker {

        private Fragment fragment;
        @IdRes
        private int layoutId;

        public ImagePickerFragmentFromFragment(Fragment fragment, @IdRes int layoutId) {
            this.fragment = fragment;
            this.layoutId = layoutId;
            init(fragment.getActivity());
        }

        @Override
        public void start(int requestCode) {
            startFragment(fragment.getActivity(), layoutId);
        }
    }


    public static class ImagePickerWithActivity extends ImagePicker {

        private Activity activity;

        public ImagePickerWithActivity(Activity activity) {
            this.activity = activity;
            init(activity);
        }

        @Override
        public void start(int requestCode) {
            Intent intent = getIntent(activity);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static class ImagePickerWithFragment extends ImagePicker {

        private Fragment fragment;

        public ImagePickerWithFragment(Fragment fragment) {
            this.fragment = fragment;
            init(fragment.getActivity());
        }

        @Override
        public void start(int requestCode) {
            Intent intent = getIntent(fragment.getActivity());
            fragment.startActivityForResult(intent, requestCode);
        }
    }


    public void init(Activity activity) {
        this.mode = MODE_SINGLE;
        this.limit = Constants.MAX_LIMIT;
        this.showCamera = false;
        this.folderMode = true;
        this.folderTitle = activity.getString(R.string.title_folder);
        this.imageTitle = activity.getString(R.string.title_select_image);
        this.selectedImages = new ArrayList<>();
        this.imageDirectory = activity.getString(R.string.image_directory);
    }


    public static ImagePickerWithActivity create(Activity activity) {
        return new ImagePickerWithActivity(activity);
    }

    public static ImagePickerWithFragment create(Fragment fragment) {
        return new ImagePickerWithFragment(fragment);
    }

    public static ImagePickerFragmentFromActivity createFragment(Activity activity, @IdRes int fragmentFrameLayoutId) {
        return new ImagePickerFragmentFromActivity(activity, fragmentFrameLayoutId);
    }

    public static ImagePickerFragmentFromFragment createFragment(Fragment fragment, @IdRes int fragmentFrameLayoutId) {
        return new ImagePickerFragmentFromFragment(fragment, fragmentFrameLayoutId);
    }

    public ImagePicker single() {
        mode = MODE_SINGLE;
        return this;
    }

    public ImagePicker multi() {
        mode = MODE_MULTIPLE;
        return this;
    }


    public ImagePicker limit(int count) {
        limit = count;
        return this;
    }

    public ImagePicker showCamera(boolean show) {
        showCamera = show;
        return this;
    }

    public ImagePicker folderTitle(String title) {
        this.folderTitle = title;
        return this;
    }

    public ImagePicker setGridColumn(int gridColumn) {
        this.gridColumn = gridColumn;
        return this;
    }

    public ImagePicker imageTitle(String title) {
        this.imageTitle = title;
        return this;
    }

    public ImagePicker origin(ArrayList<Image> images) {
        selectedImages = images;
        return this;
    }

    public ImagePicker folderMode(boolean folderMode) {
        this.folderMode = folderMode;
        return this;
    }

    public ImagePicker imageDirectory(String directory) {
        this.imageDirectory = directory;
        return this;
    }

    public Intent getIntent(Activity activity) {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(INTENT_EXTRA_MODE, mode);
        intent.putExtra(INTENT_EXTRA_LIMIT, limit);
        intent.putExtra(INTENT_EXTRA_GRID_COLUMN, gridColumn);
        intent.putExtra(INTENT_EXTRA_SHOW_CAMERA, showCamera);
        intent.putExtra(INTENT_EXTRA_FOLDER_TITLE, folderTitle);
        intent.putExtra(INTENT_EXTRA_IMAGE_TITLE, imageTitle);
        intent.putExtra(INTENT_EXTRA_SELECTED_IMAGES, selectedImages);
        intent.putExtra(INTENT_EXTRA_FOLDER_MODE, folderMode);
        intent.putExtra(INTENT_EXTRA_IMAGE_DIRECTORY, imageDirectory);
        return intent;
    }

    public void startFragment(Activity activity, @IdRes int framelayout_id) {
        FragmentArea interFragment = new FragmentArea();
        Bundle intent = new Bundle();
        intent.putInt(INTENT_EXTRA_MODE, mode);
        intent.putInt(INTENT_EXTRA_LIMIT, limit);
        intent.putInt(INTENT_EXTRA_GRID_COLUMN, gridColumn);
        intent.putBoolean(INTENT_EXTRA_SHOW_CAMERA, showCamera);
        intent.putString(INTENT_EXTRA_FOLDER_TITLE, folderTitle);
        intent.putString(INTENT_EXTRA_IMAGE_TITLE, imageTitle);
        intent.putParcelableArrayList(INTENT_EXTRA_SELECTED_IMAGES, selectedImages);
        intent.putBoolean(INTENT_EXTRA_FOLDER_MODE, folderMode);
        intent.putString(INTENT_EXTRA_IMAGE_DIRECTORY, imageDirectory);
        interFragment.setArguments(intent);

        activity.getFragmentManager().beginTransaction()
                .replace(framelayout_id, interFragment)
                .commit();


    }

}
