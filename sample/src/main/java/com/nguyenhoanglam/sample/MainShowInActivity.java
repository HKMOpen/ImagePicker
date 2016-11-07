package com.nguyenhoanglam.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.helper.Constants;
import com.nguyenhoanglam.imagepicker.helper.Pickrx;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hesk on 16年10月19日.
 */

public class MainShowInActivity extends AppCompatActivity {
    private ImageView image_holder;
    private Button buttonPickImage;

    private ArrayList<Image> images = new ArrayList<>();

    private int REQUEST_CODE_PICKER = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);
        image_holder = (ImageView) findViewById(R.id.image_holder);
        start();
        Pickrx.get().register(this);
    }

    @Override
    protected void onDestroy() {
        Pickrx.get().unregister(this);
        super.onDestroy();
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {@Tag(Constants.EVENT_SELECT_SINGLE_IMAGE)}
    )
    public void selectImageSingle(Image item) {
        Glide.with(this)

                .load(new File(item.getPath()))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image_holder);
    }

    // Recomended builder
    public void start() {
        ImagePicker.createFragment(this, R.id.area_load)
                .folderMode(false) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select") // image selection title
                .single() // single mode
                .setGridColumn(4)
                // .multi()  multi mode (default mode)
                .limit(10) // max images can be selected (99 by default)
                .showCamera(false) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(REQUEST_CODE_PICKER); // start image picker activity with request code
    }


}
