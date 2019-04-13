package toasty.messageinabottle.exception;

public class MessageDoesNotExistException extends Throwable {

    private final String id;

    public MessageDoesNotExistException(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
