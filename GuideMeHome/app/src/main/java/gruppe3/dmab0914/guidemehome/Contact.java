package gruppe3.dmab0914.guidemehome;

import android.location.Location;

import java.util.ArrayList;

public class Contact {
    private String mName;
    private String mPhone;
    private Location mLocation;
    private boolean mContactVisible;
    private boolean mUserVisible;
    private boolean mOnlineStatus;

    public Contact(String name, String phone) {
        mName = name;
        mPhone = phone;
        mContactVisible = true;
        mUserVisible = true;
    }

    public String getName() {
        return mName;
    }

    public boolean isOnline() {
        return mOnlineStatus;
    }

    public void setmOnlineStatus(boolean mOnlineStatus) {
        this.mOnlineStatus = mOnlineStatus;
    }

    public boolean ismContactVisible() {
        return mContactVisible;
    }

    public void setmContactVisible(boolean mContactVisible) {
        this.mContactVisible = mContactVisible;
    }

    public boolean ismUserVisible() {
        return mUserVisible;
    }

    public void setmUserVisible(boolean mUserVisible) {
        this.mUserVisible = mUserVisible;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public Location getmLocation() {
        return mLocation;
    }

    public void setmLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    private static int lastContactId = 0;
}
