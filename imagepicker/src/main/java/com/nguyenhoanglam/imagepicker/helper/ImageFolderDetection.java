package com.nguyenhoanglam.imagepicker.helper;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;

import com.nguyenhoanglam.imagepicker.model.Folder;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesk on 16年11月7日.
 */
public class ImageFolderDetection {

    private final String[] projection;
    private List<Folder> folders;
    private ArrayList<Image> images;
    private ContentObserver observer;
    private Handler handler;
    private Thread thread;
    private Activity resource;
    private Message message;

    public interface FolderGet {
        void onFolder(List<Folder> list, ArrayList<Image> imageList);
    }

    public ImageFolderDetection(Activity rsc, final FolderGet listen) {
        this.folders = new ArrayList<>();
        this.images = new ArrayList<>();
        this.resource = rsc;
        this.projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.FETCH_STARTED:
                        // abortLoading();
                        // showLoading();
                        break;

                    case Constants.FETCH_COMPLETED:
                        // ArrayList<Image> temps = new ArrayList<>();
                        //  temps.addAll(selectedImages);

                        ArrayList<Image> newImages = new ArrayList<>();
                        newImages.addAll(images);

                        ArrayList<Folder> newFolders = new ArrayList<>();
                        newFolders.addAll(folders);

                        listen.onFolder(newFolders, newImages);

                        /*if (folderMode) {
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
                        }*/

                        break;

                    default:
                        super.handleMessage(msg);

                }
            }
        };

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
     * Get data
     */
    public void detect() {
        abortLoading();
        ImageLoaderRunnable runnable = new ImageLoaderRunnable();
        thread = new Thread(runnable);
        thread.start();
    }


    /**
     * Return folder base on folder name
     *
     * @param name another name is in here
     * @return g
     */
    private Folder getFolder(String name) {
        for (Folder folder : folders) {
            if (folder.getFolderName().equals(name)) {
                return folder;
            }
        }
        return null;
    }

    public void onDestroy() {
        abortLoading();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    private void postMessage(int status) {
        message = handler.obtainMessage();
        message.what = status;
        message.sendToTarget();
    }

    private class ImageLoaderRunnable implements Runnable {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            /*if (recyclerView.getAdapter() == null) {
                message = handler.obtainMessage();
                message.what = Constants.FETCH_STARTED;
                message.sendToTarget();
            }*/

            /*If the adapter is null, this is first time this activity's view is
              being shown, hence send FETCH_STARTED message to show progress bar
              while images are loaded from phone*/
            postMessage(Constants.FETCH_STARTED);
            if (Thread.interrupted()) {
                return;
            }

            Cursor cursor = resource.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                postMessage(Constants.ERROR);
                return;
            }

            ArrayList<Image> temp = new ArrayList<>(cursor.getCount());

            File file;
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

                        Folder ff = getFolder(bucket);
                        if (ff == null) {
                            ff = new Folder(bucket);
                            ff.addBasePath(path);
                            folders.add(ff);
                        }
                        ff.getImages().add(image);

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
                postMessage(Constants.FETCH_COMPLETED);
            }
            Thread.interrupted();
        }
    }

}
