package toasty.messageinabottle.data;

import org.osmdroid.util.GeoPoint;

import java.util.Objects;

public class Message extends GeoPoint {
    private final String msg;
    private final boolean owned;

    public Message(String msg, GeoPoint point) {
        this(msg, point, false);
    }

    public Message(String msg, GeoPoint point, boolean owned) {
        super(point);
        this.msg = msg;
        this.owned = owned;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isOwned() {
        return owned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Message message = (Message) o;
        return owned == message.owned &&
                Objects.equals(msg, message.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), msg, owned);
    }
}
