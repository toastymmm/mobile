package toasty.messageinabottle.data;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import toasty.messageinabottle.exceptions.AuthenticationException;

public class MockBackend implements Backend {

    private List<Message> messages = new ArrayList<>();
    private boolean loggedIn = false;

    private static MockBackend instance;

    @Override
    public void login(String username, String password) throws AuthenticationException {
        if (username.startsWith("z"))
            throw new AuthenticationException();
        loggedIn = true;
    }

    @Override
    public void logout() {
        loggedIn = false;
    }

    @Override
    public void createMessage(Message message) {
        if (!messages.contains(message))
            messages.add(message);
    }

    @Override
    public void deleteMessage(Message message) {
        messages.remove(message);
    }

    @Override
    public void updateMessage(Message message) {
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public List<Message> getMessagesInBox(BoundingBox box) {
        return Collections.unmodifiableList(messages);
    }

    public static MockBackend getInstance() {
        return instance==null?instance=new MockBackend():instance;
    }

}
