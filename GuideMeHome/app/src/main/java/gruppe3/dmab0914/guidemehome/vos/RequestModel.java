package gruppe3.dmab0914.guidemehome.vos;


public class RequestModel {
    private String mToken;
    private Object mObject;

    public RequestModel(String token, Object o){
        mToken = token;
        mObject = o;
    }
}
