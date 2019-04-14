package toasty.messageinabottle;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import toasty.messageinabottle.exception.ErrorAdapter;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        RecyclerView errors = findViewById(R.id.error_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        errors.setLayoutManager(linearLayoutManager);

        ErrorAdapter errorAdapter = new ErrorAdapter();
        errors.setAdapter(errorAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(errors.getContext(), linearLayoutManager.getOrientation());
        errors.addItemDecoration(dividerItemDecoration);
    }
}
