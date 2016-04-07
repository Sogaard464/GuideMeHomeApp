package gruppe3.dmab0914.guidemehome;

/**
 * Created by Lasse on 07-04-2016.
 */
public class LoginUser {
    private String mName;
    private String mPhone;
    private String mPassword;

    public LoginUser(String name, String phone, String password){
        mName = name;
        mPhone = phone;
        mPassword = password;
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

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }
}
