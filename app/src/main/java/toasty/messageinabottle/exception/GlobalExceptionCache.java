package toasty.messageinabottle.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import toasty.messageinabottle.io.ExceptionContext;

public class GlobalExceptionCache {

    private static final List<ExceptionContext> exceptions = new ArrayList<>();

    public static synchronized void post(String title, Exception e) {
        exceptions.add(new ExceptionContext(title, e));
    }

    public static synchronized List<ExceptionContext> get() {
        return Collections.unmodifiableList(new ArrayList<>(exceptions));
    }
}
