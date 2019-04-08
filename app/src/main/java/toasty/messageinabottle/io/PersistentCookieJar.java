package toasty.messageinabottle.io;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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
            DatabaseCookie databaseCookie = new DatabaseCookie(cookie);
            cookieDatabase.databaseCookieDao().insert(databaseCookie);
        }
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        List<Cookie> result = new ArrayList<>();
        for (DatabaseCookie dbCookie : cookieDatabase.databaseCookieDao().loadAll()) {
            if (dbCookie.expiresAt > System.currentTimeMillis()) {
                cookieDatabase.databaseCookieDao().remove(dbCookie);
                continue;
            }

            Cookie cookie = dbCookie.toCookie();
            if (cookie.matches(url))
                result.add(cookie);
        }
        return result;
    }
}