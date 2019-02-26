package toasty.messageinabottle.data;

import org.osmdroid.util.BoundingBox;

import java.util.List;

import toasty.messageinabottle.exceptions.AuthenticationException;
import toasty.messageinabottle.exceptions.PermissionException;

public interface Backend {
    void login(String username, String password) throws AuthenticationException;

    void logout() throws AuthenticationException;

    void createMessage(Message message) throws AuthenticationException;

    void deleteMessage(Message message) throws PermissionException;

    void updateMessage(Message message) throws PermissionException;

    List<Message> getMessagesInBox(BoundingBox box);
}
