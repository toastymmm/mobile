package toasty.messageinabottle.data;

public class LoginResult {

    public static final int ACCOUNT_SUCCESSFULLY_CREATED = 0;
    public static final int LOGIN_SUCCESSFUL = 1;
    public static final int USERNAME_ALREADY_EXISTS = 2;
    public static final int INCORRECT_PASSWORD = 3;
    public static final int EXCEPTION_FAILURE = 4;

    private final int resultType;
    private final String userID;

    public LoginResult(int resultType) {
        this(resultType, null);
    }

    public LoginResult(int resultType, String userID) {
        this.resultType = resultType;
        this.userID = userID;
    }

    public int getResultType() {
        return resultType;
    }

    public String getUserID() {
        return userID;
    }
}
