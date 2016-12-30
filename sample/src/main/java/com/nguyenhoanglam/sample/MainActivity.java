/*
 * Created by Nguyen Hoang Lam
 * Date: ${DATE}
 */

package com.nguyenhoanglam.sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.helper.ImageFolderDetection;
import com.nguyenhoanglam.imagepicker.helper.Pickrx;
import com.nguyenhoanglam.imagepicker.model.Folder;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.activity.BottomSheetImagePicker;

import java.util.ArrayList;
import java.util.List;

import static com.nguyenhoanglam.imagepicker.helper.Constants.*;

/**
 * Created by hoanglam on 8/4/16.
 */
public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Button buttonPickImage, buttonPickImage2;
    private ImageFolderDetection im;
    private ArrayList<Image> images = new ArrayList<>();
    private int REQUEST_CODE_PICKER = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_view);
        buttonPickImage = (Button) findViewById(R.id.button_pick_image);
        buttonPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        buttonPickImage2 = (Button) findViewById(R.id.button_bottom_sheet);
        buttonPickImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.createBottomSheetFragment(MainActivity.this)
                        .folderMode(false) // set folder mode (false by default)
                        .folderTitle("Folder") // folder selection title
                        .imageTitle("Tap to select") // image selection title
                        .single() // single mode
                        .setGridColumn(4)
                        // .multi()  multi mode (default mode)
                        .limit(10) // max images can be selected (99 by default)
                        .showCamera(false) // show camera or not (true by default)
                        .imageDirectory("Camera") // captured image directory name ("Camera" folder by default)
                        .origin(images) // original selected images, used in multi mode
                        .setBSPeekHeight(getResources().getDimensionPixelOffset(R.dimen.filter_start))
                        .start(202); // start image picker activity with request code
            }
        });
        im = new ImageFolderDetection(this, new ImageFolderDetection.FolderGet() {
            @Override
            public void onFolder(List<Folder> list, ArrayList<Image> imageList) {
                if (list.size() > 0) {
                    Folder f = list.get(3);
                    Pickrx.get().post(EVENT_FOLDER_SYSTEM_DETECTION, f.getImages());
                }
            }
        });
    }

    // Recomended builder
    private void start() {
        ImagePicker.create(this)
                .folderMode(false) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select") // image selection title
                .single() // single mode
                //.multi()  multi mode (default mode)
                .limit(10) // max images can be selected (99 by default)
                .showCamera(false) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(REQUEST_CODE_PICKER); // start image picker activity with request code

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                im.detect();
            }
        }, 2000);

    }

    // Traditional intent
    public void startWithIntent() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(INTENT_EXTRA_FOLDER_MODE, true);
        intent.putExtra(INTENT_EXTRA_MODE, MODE_MULTIPLE);
        intent.putExtra(INTENT_EXTRA_LIMIT, 10);
        intent.putExtra(INTENT_EXTRA_SHOW_CAMERA, true);
        intent.putExtra(INTENT_EXTRA_SELECTED_IMAGES, images);
        intent.putExtra(INTENT_EXTRA_FOLDER_TITLE, "Album");
        intent.putExtra(INTENT_EXTRA_IMAGE_TITLE, "Tap to select images");
        intent.putExtra(INTENT_EXTRA_IMAGE_DIRECTORY, "Camera");
        startActivityForResult(intent, REQUEST_CODE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = data.getParcelableArrayListExtra(INTENT_EXTRA_SELECTED_IMAGES);
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0, l = images.size(); i < l; i++) {
                stringBuffer.append(images.get(i).getPath() + "\n");
            }
            textView.setText(stringBuffer.toString());
        }
    }
}
