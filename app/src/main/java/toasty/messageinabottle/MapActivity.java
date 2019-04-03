package toasty.messageinabottle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.io.HeartbeatRunnable;
import toasty.messageinabottle.map.MessageManager;
import toasty.messageinabottle.map.WatchableMyLocationOverlay;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int LOGIN_REQUEST_CODE = 0;

    public static final String LOGGED_IN_STATE_KEY = "LOGGED_IN";

    private MapView mapView;

    private boolean loggedIn = false;
    private MenuItem loginMenuItem;
    private MenuItem logoutMenuItem;
    private MenuItem historyMenuItem;

    private IMyLocationProvider locationProvider;
    private MessageManager messageManager;
    private ScheduledExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loginMenuItem = navigationView.getMenu().findItem(R.id.login);
        logoutMenuItem = navigationView.getMenu().findItem(R.id.logout);
        historyMenuItem = navigationView.getMenu().findItem(R.id.history);

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        WatchableMyLocationOverlay locationOverlay = new WatchableMyLocationOverlay(mapView);
        locationProvider = locationOverlay.getMyLocationProvider();
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        messageManager = new MessageManager(mapView);
        locationOverlay.addConsumer(messageManager);

        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                mapView.zoomToBoundingBox(new BoundingBox(33.362, -78.343, 24.056, -84.276), false);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(LOGGED_IN_STATE_KEY, loggedIn);
        editor.apply();

        executor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        loggedIn = preferences.getBoolean(LOGGED_IN_STATE_KEY, false);
        updateLoginVisibility();

        executor = Executors.newSingleThreadScheduledExecutor();
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                Log.println(Log.INFO, "", "UPDATING MESSAGES"); // TODO remove debug log
                List<Message> messages = (List<Message>) msg.obj;
                messageManager.replaceActiveMarkers(messages);
            }
        };
        executor.scheduleAtFixedRate(new HeartbeatRunnable(handler, locationProvider),
                0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(LOGGED_IN_STATE_KEY, loggedIn);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loggedIn = savedInstanceState.getBoolean(LOGGED_IN_STATE_KEY);
        updateLoginVisibility();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
        } else if (id == R.id.logout) {
            loggedIn = false;
            updateLoginVisibility();
        } else if (id == R.id.history) {
            Intent intent = new Intent(this, MessageHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.saved) {
            Intent intent = new Intent(this, SavedMessagesActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String token = data.getStringExtra(LoginActivity.TOKEN_KEY);
            Toast.makeText(this, "LOGIN SUCCESS => token=" + token, Toast.LENGTH_LONG).show();
            loggedIn = true;
            getPreferences(Activity.MODE_PRIVATE).edit().putBoolean(LOGGED_IN_STATE_KEY, loggedIn).apply();
        }
    }

    private void updateLoginVisibility() {
        loginMenuItem.setVisible(!loggedIn);
        logoutMenuItem.setVisible(loggedIn);
        historyMenuItem.setVisible(loggedIn);
    }
}
