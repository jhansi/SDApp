package com.android.jhansi.sdapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jhansi Tavva on 3/4/16.
 * Copyright (c) 2016 Jhansi Tavva. All rights reserved.
 */
public class ParcelableData implements Parcelable {


    List<FileEntry> listLargeFiles;
    List<Map<String, String>> listFrequentFiles;
    long avgFileSize;

    public List<FileEntry> getListLargeFiles() {
        return listLargeFiles;
    }

    public void setListLargeFiles(List<FileEntry> listLargeFiles) {
        this.listLargeFiles = listLargeFiles;
    }

    public List<Map<String, String>> getListFrequentFiles() {
        return listFrequentFiles;
    }

    public void setListFrequentFiles(List<Map<String, String>> listFrequentFiles) {
        this.listFrequentFiles = listFrequentFiles;
    }

    public long getAvgFileSize() {
        return avgFileSize;
    }

    public void setAvgFileSize(long avgFileSize) {
        this.avgFileSize = avgFileSize;
    }

    public ParcelableData() {
        listLargeFiles = new ArrayList<FileEntry>();
        listFrequentFiles = new ArrayList<Map<String, String>>(5);
        long avgFileSize = 0;

    }

    public ParcelableData(Parcel in) {
        this.avgFileSize = in.readLong();
        in.readList(this.listLargeFiles, null);
        in.readList(this.listFrequentFiles, null);

    }

    public static final Creator<ParcelableData> CREATOR = new Creator<ParcelableData>() {
        @Override
        public ParcelableData createFromParcel(Parcel in) {
            return new ParcelableData(in);
        }

        @Override
        public ParcelableData[] newArray(int size) {
            return new ParcelableData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(avgFileSize);

        dest.writeList(listLargeFiles);
        dest.writeList(listFrequentFiles);
    }
}
