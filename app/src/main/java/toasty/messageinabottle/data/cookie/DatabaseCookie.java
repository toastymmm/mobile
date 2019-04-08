package toasty.messageinabottle.data.cookie;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import okhttp3.Cookie;

@Entity
public class DatabaseCookie {
    @PrimaryKey
    public int uid;
    @ColumnInfo
    public String name;
    @ColumnInfo
    public String value;
    @ColumnInfo(name = "expires_at")
    public long expiresAt;
    @ColumnInfo
    public String domain;
    @ColumnInfo
    public String path;
    @ColumnInfo
    public boolean secure;
    @ColumnInfo(name = "http_only")
    public boolean httpOnly;
    @ColumnInfo(name = "host_only")
    public boolean hostOnly;

    public DatabaseCookie() {
    }

    public DatabaseCookie(Cookie cookie) {
        name = cookie.name();
        value = cookie.value();
        expiresAt = cookie.expiresAt();
        domain = cookie.domain();
        path = cookie.path();
        secure = cookie.secure();
        httpOnly = cookie.httpOnly();
        hostOnly = cookie.hostOnly();
    }

    public Cookie toCookie() {
        Cookie.Builder builder = new Cookie.Builder()
                .name(name)
                .value(value)
                .expiresAt(expiresAt)
                .path(path);

        if (secure)
            builder.secure();

        if (httpOnly)
            builder.httpOnly();

        if (hostOnly)
            builder.hostOnlyDomain(domain);
        else
            builder.domain(domain);

        return builder.build();
    }
}
