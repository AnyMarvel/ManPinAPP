package com.google.android.libraries.social.licenses;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class License implements Comparable<License>, Parcelable {
    public static final Creator<License> CREATOR = new Creator<License>() {
        public License createFromParcel(Parcel in) {
            return new License(in);
        }

        public License[] newArray(int size) {
            return new License[size];
        }
    };
    private final String libraryName;
    private final int licenseLength;
    private final long licenseOffset;
    private final String path;

    static License create(String libraryName, long licenseOffset, int licenseLength) {
        return new License(libraryName, licenseOffset, licenseLength, "");
    }

    static License create(String libraryName, long licenseOffset, int licenseLength, String path) {
        return new License(libraryName, licenseOffset, licenseLength, path);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.libraryName);
        dest.writeLong(this.licenseOffset);
        dest.writeInt(this.licenseLength);
        dest.writeString(this.path);
    }

    public int compareTo(License o) {
        return this.libraryName.compareToIgnoreCase(o.getLibraryName());
    }

    public String toString() {
        return getLibraryName();
    }

    private License(String libraryName, long licenseOffset, int licenseLength, String path) {
        this.libraryName = libraryName;
        this.licenseOffset = licenseOffset;
        this.licenseLength = licenseLength;
        this.path = path;
    }

    private License(Parcel in) {
        this.libraryName = in.readString();
        this.licenseOffset = in.readLong();
        this.licenseLength = in.readInt();
        this.path = in.readString();
    }

    String getLibraryName() {
        return this.libraryName;
    }

    long getLicenseOffset() {
        return this.licenseOffset;
    }

    int getLicenseLength() {
        return this.licenseLength;
    }

    public String getPath() {
        return this.path;
    }
}
