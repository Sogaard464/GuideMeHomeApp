package gruppe3.dmab0914.guidemehome.models;

import android.location.Location;

public class Contact {
    private String mName;
    private String mPhone;
    private boolean mCan_see;
    private boolean mWill_see;

    public Contact(String name, String phone) {
        mName = name;
        mPhone = phone;
        mWill_see = true;
        mCan_see = true;
    }

    public boolean will_see() {
        return mWill_see;
    }

    public void setmWill_see(boolean mWill_see) {
        this.mWill_see = mWill_see;
    }

    public boolean can_see() {
        return mCan_see;
    }

    public void setmCan_see(boolean mCan_see) {
        this.mCan_see = mCan_see;
    }

    public String getName() {
        return mName;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

}
