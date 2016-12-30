package com.nguyenhoanglam.imagepicker.helper;

/**
 * Created by hoanglam on 7/31/16.
 */
public class Constants {

    public static final int REQUEST_CODE_CAPTURE = 2000;

    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;
    public static final int ERROR = 2003;

    public static final int MAX_LIMIT = 99;

    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 23;
    public static final int PERMISSION_REQUEST_CAMERA = 24;

    public static final String PREF_WRITE_EXTERNAL_STORAGE_REQUESTED = "writeExternalRequested";
    public static final String PREF_CAMERA_REQUESTED = "cameraRequested";


    public static final int MODE_SINGLE = 1;
    public static final int MODE_MULTIPLE = 2;
    public static final String INTENT_EXTRA_SELECTED_IMAGES = "selectedImages";
    public static final String INTENT_EXTRA_LIMIT = "limit";
    public static final String INTENT_EXTRA_SHOW_CAMERA = "showCamera";
    public static final String INTENT_EXTRA_GRID_COLUMN = "ex_grid_preset_column";
    public static final String INTENT_EXTRA_MODE = "mode";
    public static final String INTENT_EXTRA_FOLDER_MODE = "folderMode";
    public static final String INTENT_EXTRA_FOLDER_TITLE = "folderTitle";
    public static final String INTENT_EXTRA_IMAGE_TITLE = "imageTitle";
    public static final String INTENT_EXTRA_IMAGE_DIRECTORY = "imageDirectory";
    public static final String INTENT_EXTRA_SHEETPEEK_HEIGHT = "ex_height_peek";
    public static final String INTENT_EXTRA_LAYOUT_RES = "ex_layout_res";

    public static final String EVENT_SELECT_SINGLE_IMAGE = "rx_event_image_pickerevent_s1";
    public static final String EVENT_FOLDER_SYSTEM_DETECTION = "rx_event_detect_progress";
    public static final String EVENT_SELECT_MULTI_IMAGES = "rx_event_image_pickerevent_m1";

}
