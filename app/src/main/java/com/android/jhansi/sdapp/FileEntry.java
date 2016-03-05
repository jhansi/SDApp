package com.android.jhansi.sdapp;

/**
 * Created by Jhansi Tavva on 3/3/16.
 * Copyright (c) 2016 Jhansi Tavva. All rights reserved.
 */
public class FileEntry {

    private long id;
    private String file_name;
    private long file_size;
    private String file_ext;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public long getFile_size() {
        return (file_size);
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public String getFile_ext() {
        return file_ext;
    }

    public void setFile_ext(String file_ext) {
        this.file_ext = file_ext;
    }

}
