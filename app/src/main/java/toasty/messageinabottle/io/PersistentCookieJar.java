package toasty.messageinabottle.io;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import toasty.messageinabottle.data.cookie.CookieDatabase;
import toasty.messageinabottle.data.cookie.CookieDatabaseAccessor;
import toasty.messageinabottle.data.cookie.DatabaseCookie;

class PersistentCookieJar implements CookieJar {

    private final CookieDatabase cookieDatabase;

    public PersistentCookieJar(Context ctx) {
        cookieDatabase = CookieDatabaseAccessor.getCookieDatabase(ctx);
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            List<DatabaseCookie> foundCookies = cookieDatabase.databaseCookieDao().find(cookie.name());
            for (DatabaseCookie foundCookie : foundCookies)
                cookieDatabase.databaseCookieDao().remove(foundCookie);

            DatabaseCookie databaseCookie = new DatabaseCookie(cookie);
            cookieDatabase.databaseCookieDao().insert(databaseCookie);
        }
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        List<Cookie> result = new ArrayList<>();
        for (DatabaseCookie dbCookie : cookieDatabase.databaseCookieDao().loadAll()) {
            Cookie cookie = dbCookie.toCookie();
            if (cookie.matches(url))
                result.add(cookie);
        }
        return result;
    }
}
