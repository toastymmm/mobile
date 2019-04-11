package toasty.messageinabottle.data.cookie;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface DatabaseCookieDao {
    @Query("SELECT * FROM DatabaseCookie")
    List<DatabaseCookie> loadAll();

    @Query("SELECT * FROM DatabaseCookie WHERE name=:name")
    List<DatabaseCookie> find(String name);

    @Insert
    void insert(DatabaseCookie cookies);

    @Delete
    void remove(DatabaseCookie dbCookie);
}
