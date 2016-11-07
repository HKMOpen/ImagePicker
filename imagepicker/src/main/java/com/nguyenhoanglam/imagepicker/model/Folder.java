package com.nguyenhoanglam.imagepicker.model;


import java.util.ArrayList;

/**
 * Created by boss1088 on 8/22/16.
 */
public class Folder {
    private String directory_base_path;
    private String folderName;
    private ArrayList<Image> images;

    public Folder(String bucket) {
        folderName = bucket;
        images = new ArrayList<>();
    }

    public void addBasePath(String _path) {
        int d = _path.lastIndexOf("/");
        String path = _path.substring(0, d);
        this.directory_base_path = path + "/";
    }

    public String getBaseDir() {
        return this.directory_base_path;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }
}
