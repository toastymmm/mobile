package toasty.messageinabottle.data.cookie;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DatabaseCookie.class}, version = 1, exportSchema = false)
public abstract class CookieDatabase extends RoomDatabase {
    public abstract DatabaseCookieDao databaseCookieDao();
}
