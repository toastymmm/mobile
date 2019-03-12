package toasty.messageinabottle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.data.User;

public class SavedMessagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_messages);

        recyclerView = findViewById(R.id.saved_messages);

        LinearLayoutManager layoutManger = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManger);

        String loremIpsum = getResources().getString(R.string.message_detail_lorem_ipsum);
        GeoPoint point = new GeoPoint(0.0, 0.0);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(loremIpsum, point, new User("Brad"), new Date()));
        messages.add(new Message(loremIpsum, point, new User("David"), new Date()));
        messages.add(new Message(loremIpsum, point, new User("Spencer"), new Date()));

        MessagePreviewAdapter messagePreviewAdapter = new MessagePreviewAdapter(messages);
        recyclerView.setAdapter(messagePreviewAdapter);
    }
}
