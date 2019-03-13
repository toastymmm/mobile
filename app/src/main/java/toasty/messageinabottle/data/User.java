package toasty.messageinabottle.data;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private final String username;

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public User(Parcel in) {
        this(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
    }
}
