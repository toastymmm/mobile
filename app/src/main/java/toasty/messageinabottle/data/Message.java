package toasty.messageinabottle.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.Objects;

public class Message extends GeoPoint implements Parcelable {

    private final String msg;
    private final User author;
    private final Date created;
    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    private boolean favorite = false; // TODO take from inputs

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
        return isFavorite() == message.isFavorite() &&
                Objects.equals(getMsg(), message.getMsg()) &&
                Objects.equals(getAuthor(), message.getAuthor()) &&
                Objects.equals(getCreated(), message.getCreated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMsg(), getAuthor(), getCreated(), isFavorite());
    }

    public Message(Parcel in) {
        this(in.readString(), GeoPoint.CREATOR.createFromParcel(in), User.CREATOR.createFromParcel(in), new Date(in.readLong()));

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(msg);
        super.writeToParcel(out, flags);
        author.writeToParcel(out, flags);
        out.writeSerializable(created);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
