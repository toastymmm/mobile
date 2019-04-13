package toasty.messageinabottle.data;

public class UserIDWrapper {

    private String userID;

    public synchronized String getUserID() {
        return userID;
    }

    public synchronized void setUserID(String userID) {
        this.userID = userID;
    }

    public synchronized boolean isLoggedIn() {
        return userID != null;
    }

    public synchronized boolean logout() {
        boolean result = userID != null;
        userID = null;
        return result;
    }
}
