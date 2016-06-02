package gruppe3.dmab0914.guidemehome.models;

import java.util.ArrayList;

public class User {
    private String mName;
    private String mPhone;
    private String mEmail;
    private String mToken;
    private ArrayList<Contact> mContacts = new ArrayList<>();

    public User(String name, String email) {
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

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }
}
