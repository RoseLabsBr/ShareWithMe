package edu.rosehulman.roselabs.sharewithme.Profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserProfile implements Parcelable {

    private String name;
    private String userID;
    private String picture;
    private String phone;

    @JsonIgnore
    private String key;

    public UserProfile() {
        this.picture = null;
        this.phone = null;
        this.name = null;
        this.userID = null;
    }

    protected UserProfile(Parcel in) {
        key = in.readString();
        name = in.readString();
        userID = in.readString();
        picture = in.readString();
    }

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
        @Override
        public UserProfile createFromParcel(Parcel in) {
            return new UserProfile(in);
        }

        @Override
        public UserProfile[] newArray(int size) {
            return new UserProfile[size];
        }
    };

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(userID);
        dest.writeString(picture);
        dest.writeString(phone);
    }
}
