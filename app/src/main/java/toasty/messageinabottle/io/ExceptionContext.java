package toasty.messageinabottle.io;

import java.util.Date;

public class ExceptionContext {

    private final Date date;
    private final String title;
    private final Exception exception;

    public ExceptionContext(String title, Exception exception) {
        this(new Date(), title, exception);
    }

    public ExceptionContext(Date date, String title, Exception exception) {
        this.date = date;
        this.title = title;
        this.exception = exception;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public Exception getException() {
        return exception;
    }
}
