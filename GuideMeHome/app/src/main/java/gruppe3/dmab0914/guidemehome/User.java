package gruppe3.dmab0914.guidemehome;

import java.util.ArrayList;

/**
 * Created by Seagaard on 04-04-2016.
 */
public class User {
    private String mName;
    private String mPhone;
    private double mLongitude;
    private double mLatitude;
    private String mEmail;
    private boolean mGender;
    private String mToken;



    private ArrayList<Contact> mContacts = new ArrayList<>();


    public User(String name, String email){
        mName = name;
        mEmail = email;
    }

    public ArrayList<Contact> getContacts() {
        return mContacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.mContacts = contacts;
    }
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public boolean isGender() {
        return mGender;
    }

    public void setGender(boolean gender) {
        mGender = gender;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }
}
