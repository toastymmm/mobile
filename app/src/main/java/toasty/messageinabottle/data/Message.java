package toasty.messageinabottle.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.Objects;

import toasty.messageinabottle.data.remote.Feature;
import toasty.messageinabottle.data.remote.Geometry;
import toasty.messageinabottle.data.remote.Properties;
import toasty.messageinabottle.data.remote.RemoteMessage;

public class Message extends GeoPoint implements Parcelable {

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

    private final String id;
    private String msg;
    private final User author;
    private final Date created;
    private boolean favorite;

    public Message(String id, String msg, GeoPoint point, User author, Date created, boolean favorite) {
        super(point);
        this.id = id;
        this.msg = msg;
        this.author = author;
        this.created = created;
        this.favorite = favorite;
    }

    public Message(Parcel in) {
        this(in.readString(), in.readString(), GeoPoint.CREATOR.createFromParcel(in), User.CREATOR.createFromParcel(in), (Date) in.readSerializable(), in.readByte() == (byte) 1);
    }

    public String getID() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
        return favorite == message.favorite &&
                Objects.equals(id, message.id) &&
                Objects.equals(msg, message.msg) &&
                Objects.equals(author, message.author) &&
                Objects.equals(created, message.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, msg, author, created, favorite);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(msg);
        super.writeToParcel(out, flags);
        author.writeToParcel(out, flags);
        out.writeSerializable(created);
        out.writeByte((byte) (favorite ? 1 : 0));
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String toRemoteJson() {
        RemoteMessage remoteMessage = new RemoteMessage();
        remoteMessage._id = id;
        remoteMessage.creator = author.getId();
        remoteMessage.feature = new Feature();
        remoteMessage.feature.type = "Feature";
        remoteMessage.feature.properties = new Properties();
        remoteMessage.feature.properties.text = msg;
        remoteMessage.feature.properties.category = "General"; // TODO categories
        remoteMessage.feature.properties.date = RemoteMessage.ISO8601.format(created);
        remoteMessage.feature.properties.numReports = 0; // TODO keep track of reports
        remoteMessage.feature.geometry = Geometry.fromGeoPoint(this);

        Gson gson = new Gson();
        return gson.toJson(remoteMessage);
    }
}
