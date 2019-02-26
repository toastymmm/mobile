package toasty.messageinabottle.data;

import org.osmdroid.util.GeoPoint;

import java.util.Objects;

public class Message {
    private final String msg;
    private final GeoPoint point;
    private final boolean owned;

    public Message(String msg, GeoPoint point) {
        this.msg = msg;
        this.point = point;
        this.owned = false;
    }

    public Message(String msg, GeoPoint point, boolean owned) {
        this.msg = msg;
        this.point = point;
        this.owned = owned;
    }

    public String getMsg() {
        return msg;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public boolean isOwned() {
        return owned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return isOwned() == message.isOwned() &&
                Objects.equals(getMsg(), message.getMsg()) &&
                Objects.equals(getPoint(), message.getPoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMsg(), getPoint(), isOwned());
    }
}
