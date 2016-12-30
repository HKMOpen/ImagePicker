/*
 * Created by Nguyen Hoang Lam
 * Date: ${DATE}
 */

package com.nguyenhoanglam.imagepicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.helper.Constants;
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
    private int layoutRes;
    private int peek_height;
    private String folderTitle;
    private String imageTitle;
    private String imageDirectory;
    private ArrayList<Image> selectedImages;
    private boolean folderMode;
    private boolean showCamera;

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

    public static class ImagePickerFragmentInsideDialogFragment extends ImagePicker {
        private Activity activity;
        private FragmentManager fm;

        public ImagePickerFragmentInsideDialogFragment(Fragment fragment) {
            this.activity = fragment.getActivity();
            init(activity);
        }

        public ImagePickerFragmentInsideDialogFragment(AppCompatActivity activity) {
            this.activity = activity;
            this.fm = activity.getSupportFragmentManager();
            init(activity);
        }

        @Override
        public void start(int requestCode) {
            startBottomSheetDialogFragment(fm);
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

    public static ImagePickerFragmentInsideDialogFragment createBottomSheetFragment(AppCompatActivity appCompatActivity) {
        return new ImagePickerFragmentInsideDialogFragment(appCompatActivity);
    }

    public static ImagePickerFragmentInsideDialogFragment createBottomSheetFragment(Fragment fragment) {
        return new ImagePickerFragmentInsideDialogFragment(fragment);
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

    public ImagePicker setBSPeekHeight(int h) {
        this.peek_height = h;
        return this;
    }

    public ImagePicker setLayout(int h) {
        this.layoutRes = h;
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

    private Bundle makeIntent() {
        Bundle bundleData = new Bundle();
        bundleData.putInt(INTENT_EXTRA_MODE, mode);
        bundleData.putInt(INTENT_EXTRA_LIMIT, limit);
        bundleData.putInt(INTENT_EXTRA_GRID_COLUMN, gridColumn);
        bundleData.putBoolean(INTENT_EXTRA_SHOW_CAMERA, showCamera);
        bundleData.putString(INTENT_EXTRA_FOLDER_TITLE, folderTitle);
        bundleData.putString(INTENT_EXTRA_IMAGE_TITLE, imageTitle);
        bundleData.putParcelableArrayList(INTENT_EXTRA_SELECTED_IMAGES, selectedImages);
        bundleData.putBoolean(INTENT_EXTRA_FOLDER_MODE, folderMode);
        bundleData.putString(INTENT_EXTRA_IMAGE_DIRECTORY, imageDirectory);
        bundleData.putInt(INTENT_EXTRA_SHEETPEEK_HEIGHT, peek_height);
        bundleData.putInt(INTENT_EXTRA_LAYOUT_RES, layoutRes);
        return bundleData;
    }

    private FragmentPickerv7 prepareV7() {
        FragmentPickerv7 interFragment = new FragmentPickerv7();
        interFragment.setArguments(makeIntent());
        return interFragment;
    }

    private FragmentPickerv4 prepareV4() {
        FragmentPickerv4 interFragment = new FragmentPickerv4();
        interFragment.setArguments(makeIntent());
        return interFragment;
    }

    protected void startFragment(Activity activity, @IdRes int framelayout_id) {
        activity.getFragmentManager().beginTransaction()
                .replace(framelayout_id, prepareV7())
                .commit();
    }

    protected void startBottomSheetDialogFragment(FragmentManager fm) {
        BottomSheetImagePicker bf = BottomSheetImagePicker.newInstance(makeIntent());
        bf.show(fm, BottomSheetImagePicker.info_tag);
    }

}
