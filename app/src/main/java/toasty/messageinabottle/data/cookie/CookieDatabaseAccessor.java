package toasty.messageinabottle.data.cookie;

import android.content.Context;

import androidx.room.Room;

public class CookieDatabaseAccessor {

    private static CookieDatabase db;

    public static CookieDatabase getCookieDatabase(Context ctx) {
        if (db == null)
            db = Room.databaseBuilder(ctx, CookieDatabase.class, "cookie-database").build();
        return db;
    }
}
