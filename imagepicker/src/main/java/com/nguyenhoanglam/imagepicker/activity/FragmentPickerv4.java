package com.nguyenhoanglam.imagepicker.activity;

import android.Manifest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.adapter.FolderPickerAdapter;
import com.nguyenhoanglam.imagepicker.adapter.ImagePickerAdapter;
import com.nguyenhoanglam.imagepicker.helper.Constants;
import com.nguyenhoanglam.imagepicker.helper.Pickrx;
import com.nguyenhoanglam.imagepicker.listeners.OnFolderClickListener;
import com.nguyenhoanglam.imagepicker.listeners.OnImageClickListener;
import com.nguyenhoanglam.imagepicker.model.Folder;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.view.GridSpacingItemDecoration;
import com.nguyenhoanglam.imagepicker.view.ProgressWheel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.nguyenhoanglam.imagepicker.helper.Constants.EVENT_SELECT_SINGLE_IMAGE;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_FOLDER_MODE;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_FOLDER_TITLE;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_GRID_COLUMN;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_IMAGE_DIRECTORY;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_IMAGE_TITLE;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_LIMIT;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_MODE;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_SELECTED_IMAGES;
import static com.nguyenhoanglam.imagepicker.helper.Constants.INTENT_EXTRA_SHOW_CAMERA;
import static com.nguyenhoanglam.imagepicker.helper.Constants.MODE_MULTIPLE;

/**
 * Created by hesk on 16年12月30日.
 */

public class FragmentPickerv4 extends Fragment implements OnImageClickListener {
    private static final String TAG = "ImageFragmentPickerv13";

    private List<Folder> folders;
    private ArrayList<Image> images;
    private String currentImagePath;
    private String imageDirectory;

    private ArrayList<Image> selectedImages;
    private boolean showCamera;
    private int mode;
    private int column;
    private boolean folderMode;
    private int limit;
    private String folderTitle, imageTitle;
    private MenuItem menuDone, menuCamera;
    private final int menuDoneId = 100;
    private final int menuCameraId = 101;

    private RelativeLayout mainLayout;
    private ProgressWheel progressBar;
    private TextView emptyTextView;
    private RecyclerView recyclerView;

    private GridLayoutManager layoutManager;
    private GridSpacingItemDecoration itemOffsetDecoration;

    private int imageColumns;
    private int folderColumns;

    private ImagePickerAdapter imageAdapter;
    private FolderPickerAdapter folderAdapter;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
    private Parcelable foldersState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle intent = getArguments();
        if (intent == null) {
            return;
        }

        mainLayout = (RelativeLayout) view.findViewById(R.id.main);
        progressBar = (ProgressWheel) view.findViewById(R.id.progress_bar);
        emptyTextView = (TextView) view.findViewById(R.id.tv_empty_images);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        /** Get extras */
        limit = intent.getInt(INTENT_EXTRA_LIMIT, Constants.MAX_LIMIT);
        mode = intent.getInt(INTENT_EXTRA_MODE, MODE_MULTIPLE);
        column = intent.getInt(INTENT_EXTRA_GRID_COLUMN, 3);
        folderMode = intent.getBoolean(INTENT_EXTRA_FOLDER_MODE, false);

        folderTitle = intent.getString(INTENT_EXTRA_FOLDER_TITLE, getString(R.string.title_folder));
        imageTitle = intent.getString(INTENT_EXTRA_IMAGE_TITLE, getString(R.string.title_select_image));
        imageDirectory = intent.getString(INTENT_EXTRA_IMAGE_DIRECTORY);
        if (imageDirectory == null || TextUtils.isEmpty(imageDirectory)) {
            imageDirectory = getString(R.string.image_directory);
        }
        showCamera = intent.getBoolean(INTENT_EXTRA_SHOW_CAMERA, true);
        if (mode == MODE_MULTIPLE) {
            selectedImages = intent.getParcelableArrayList(INTENT_EXTRA_SELECTED_IMAGES);
        }
        if (selectedImages == null)
            selectedImages = new ArrayList<>();

        images = new ArrayList<>();
        /** Init folder and image adapter */
        imageAdapter = new ImagePickerAdapter(getActivity(), images, selectedImages, this);
        folderAdapter = new FolderPickerAdapter(getActivity(), new OnFolderClickListener() {
            @Override
            public void onFolderClick(Folder bucket) {
                foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
                setImageAdapter(bucket.getImages());
            }
        });
        orientationBasedUI(getResources().getConfiguration().orientation);
    }

    /**
     * Config recyclerView when configuration changed
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }


    /**
     * Set item size, column size base on the screen orientation
     */
    private void orientationBasedUI(int orientation) {
        imageColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? column : column + 2;
        folderColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? column - 1 : column + 2;
        int columns = isDisplayingFolderView() ? folderColumns : imageColumns;
        layoutManager = new GridLayoutManager(getActivity(), columns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        setItemDecoration(columns);
    }

    /**
     * Set folder adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setFolderAdapter() {
        folderAdapter.setData(folders);
        setItemDecoration(folderColumns);
        recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
        updateTitle();
    }

    private int selectedImagePosition(Image image) {
        for (int i = 0; i < selectedImages.size(); i++) {
            if (selectedImages.get(i).getPath().equals(image.getPath())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Init handler to handle loading data results
     */
    @Override
    public void onStart() {
        super.onStart();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.FETCH_STARTED: {
                        showLoading();
                        break;
                    }
                    case Constants.FETCH_COMPLETED: {
                        ArrayList<Image> temps = new ArrayList<>();
                        temps.addAll(selectedImages);

                        ArrayList<Image> newImages = new ArrayList<>();
                        newImages.addAll(images);


                        if (folderMode) {
                            setFolderAdapter();
                            if (folders.size() != 0)
                                hideLoading();
                            else
                                showEmpty();

                        } else {
                            setImageAdapter(newImages);
                            if (images.size() != 0)
                                hideLoading();
                            else
                                showEmpty();
                        }

                        break;
                    }
                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };

        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                getData();
            }
        };
        getActivity().getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
    }

    /**
     * Stop loading data task
     */
    private void abortLoading() {
        if (thread == null)
            return;
        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if displaying folders view
     */
    private boolean isDisplayingFolderView() {
        return (folderMode &&
                (recyclerView.getAdapter() == null || recyclerView.getAdapter() instanceof FolderPickerAdapter));
    }

    /**
     * Update activity title
     * If we're displaying folder, set folder title
     * If we're displaying images, show number of selected images
     */
    private void updateTitle() {
        if (menuDone != null && menuCamera != null) {
            if (isDisplayingFolderView()) {
                //  actionBar.setTitle(folderTitle);
                menuDone.setVisible(false);
            } else {
                if (selectedImages.size() == 0) {
                    //actionBar.setTitle(imageTitle);
                    if (menuDone != null)
                        menuDone.setVisible(false);
                } else {
                    if (mode == MODE_MULTIPLE) {
                        if (limit == Constants.MAX_LIMIT) {
                            //actionBar.setTitle(String.format(getString(R.string.selected), selectedImages.size()));
                        } else {
                            // actionBar.setTitle(String.format(getString(R.string.selected_with_limit), selectedImages.size(), limit));
                        }
                    }
                    if (menuDone != null)
                        menuDone.setVisible(true);
                }
            }
        }
    }

    /**
     * Show progessbar when loading data
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
    }

    /**
     * Hide progressbar when data loaded
     */
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    /**
     * Show empty data
     */
    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        abortLoading();
        getActivity().getContentResolver().unregisterContentObserver(observer);
        observer = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

    }


    @Override
    public void onClick(View view, int position) {
        clickImage(position);
    }

    /**
     * Loading data task
     */
    private class ImageLoaderRunnable implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Message message;
            if (recyclerView.getAdapter() == null) {
                /*
                If the adapter is null, this is first time this activity's view is
                being shown, hence send FETCH_STARTED message to show progress bar
                while images are loaded from phone
                 */
                message = handler.obtainMessage();
                message.what = Constants.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                message = handler.obtainMessage();
                message.what = Constants.ERROR;
                message.sendToTarget();
                return;
            }

            ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
            File file;
            folders = new ArrayList<>();

            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));

                    file = new File(path);
                    if (file.exists()) {
                        Image image = new Image(id, name, path, false);
                        temp.add(image);

                        if (folderMode) {
                            Folder folder = getFolder(bucket);
                            if (folder == null) {
                                folder = new Folder(bucket);
                                folders.add(folder);
                            }

                            folder.getImages().add(image);
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();
            if (images == null) {
                images = new ArrayList<>();
            }
            images.clear();
            images.addAll(temp);

            if (handler != null) {
                message = handler.obtainMessage();
                message.what = Constants.FETCH_COMPLETED;
                message.sendToTarget();
            }

            Thread.interrupted();

        }
    }


    /**
     * Return folder base on folder name
     *
     * @param name g
     * @return g
     */
    public Folder getFolder(String name) {
        for (Folder folder : folders) {
            if (folder.getFolderName().equals(name)) {
                return folder;
            }
        }
        return null;
    }


    /**
     * Handle image selection event: add or remove selected image, change title
     */
    private void clickImage(int position) {
        int selectedItemPosition = selectedImagePosition(images.get(position));
        if (mode == MODE_MULTIPLE) {
            if (selectedItemPosition == -1) {
                if (selectedImages.size() < limit) {
                    imageAdapter.addSelected(images.get(position));
                } else {
                    Toast.makeText(getActivity(), R.string.msg_limit_images, Toast.LENGTH_SHORT).show();
                }
            } else {
                imageAdapter.removeSelectedPosition(selectedItemPosition, position);
            }
        } else {
            if (selectedItemPosition != -1)
                imageAdapter.removeSelectedPosition(selectedItemPosition, position);
            else {
                if (selectedImages.size() > 0) {
                    imageAdapter.removeAllSelectedSingleClick();
                }
                imageAdapter.addSelected(images.get(position));
            }
        }
        Pickrx.get().post(EVENT_SELECT_SINGLE_IMAGE, images.get(position));
        updateTitle();
    }


    /**
     * Check permission
     */
    private void getDataWithPermission() {
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED)
            getData();
        else
            requestWriteExternalPermission();
    }


    /**
     * Request for permission
     * If permission denied or app is first launched, request for permission
     * If permission denied and user choose 'Nerver Ask Again', show snackbar with an action that navigate to app settings
     */
    private void requestWriteExternalPermission() {
        Log.w(TAG, "Write External permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            if (isPermissionRequested(Constants.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED) == false) {
                ActivityCompat.requestPermissions(getActivity(), permissions, Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                setPermissionRequested(Constants.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED);
            } else {
                Snackbar snackbar = Snackbar.make(mainLayout, R.string.msg_no_write_external_permission,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAppSettings();
                    }
                });
                snackbar.show();
            }
        }

    }

    /**
     * Open app settings screen
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getActivity().getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Set a permission is requested
     */
    private void setPermissionRequested(String permission) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(permission, true);
        editor.commit();
    }


    /**
     * Check if a permission is requestted or not (false by default)
     */
    private boolean isPermissionRequested(String permission) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getBoolean(permission, false);
    }

    /**
     * Get data
     */
    private void getData() {
        abortLoading();
        ImageLoaderRunnable runnable = new ImageLoaderRunnable();
        thread = new Thread(runnable);
        thread.start();
    }


    /**
     * Set item decoration
     */
    private void setItemDecoration(int columns) {
        layoutManager.setSpanCount(columns);
        if (itemOffsetDecoration != null)
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        itemOffsetDecoration = new GridSpacingItemDecoration(columns, getResources().getDimensionPixelSize(R.dimen.item_padding), false);
        recyclerView.addItemDecoration(itemOffsetDecoration);
    }

    @Override
    public void onPause() {
        Pickrx.get().unregister(this);
        super.onPause();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {@Tag(Constants.EVENT_FOLDER_SYSTEM_DETECTION)}
    )
    public void getImageAdapterUpdate(ArrayList<Image> im) {
        setImageAdapter(im);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataWithPermission();
        Pickrx.get().register(this);
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setImageAdapter(ArrayList<Image> images) {
        imageAdapter.setData(images);
        setItemDecoration(imageColumns);
        recyclerView.setAdapter(imageAdapter);
        updateTitle();
    }

}
