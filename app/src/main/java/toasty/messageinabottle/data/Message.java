package toasty.messageinabottle.data;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.Objects;

public class Message extends GeoPoint {

    private final String msg;
    private final User author;
    private final Date created;

    public Message(String msg, GeoPoint point, User author, Date created) {
        super(point);
        this.msg = msg;
        this.author = author;
        this.created = created;
    }

    public String getMsg() {
        return msg;
    }

    public User getAuthor() {
        return author;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Message message = (Message) o;
        return Objects.equals(msg, message.msg) &&
                Objects.equals(author, message.author) &&
                Objects.equals(created, message.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), msg, author, created);
    }
}
