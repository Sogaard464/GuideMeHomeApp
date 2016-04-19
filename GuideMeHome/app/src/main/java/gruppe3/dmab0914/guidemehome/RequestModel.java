package gruppe3.dmab0914.guidemehome;

import java.util.Objects;


public class RequestModel {
    private String mToken;
    private Object mObject;

    public RequestModel(String token, Object o){
        mToken = token;
        mObject = o;
    }
}
