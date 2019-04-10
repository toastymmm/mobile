package toasty.messageinabottle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import toasty.messageinabottle.data.Message;
import toasty.messageinabottle.io.HeartbeatRunnable;
import toasty.messageinabottle.map.DoubleTapGestureListener;
import toasty.messageinabottle.map.MessageManager;
import toasty.messageinabottle.map.WatchableMyLocationOverlay;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int LOGIN_REQUEST_CODE = 0;
    public static final int MY_PERMISSION_REQUEST_CODE = 1;

    public static final String LOGGED_IN_STATE_KEY = "LOGGED_IN";

    private MapView mapView;

    private boolean loggedIn = false;
    private MenuItem loginMenuItem;
    private MenuItem logoutMenuItem;
    private MenuItem savedMenuItem;
    private MenuItem historyMenuItem;

    private WatchableMyLocationOverlay locationOverlay;
    private IMyLocationProvider locationProvider;

    private Handler uiThreadMessageHandler;
    private MessageManager messageManager;
    private ScheduledExecutorService executor;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        executor = Executors.newSingleThreadScheduledExecutor();

        setContentView(R.layout.activity_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (loggedIn) {
                Intent intent = new Intent(ctx, CreateMessageActivity.class);
                Location lastKnownLocation = locationProvider.getLastKnownLocation();
                if (lastKnownLocation == null) {
                    Toast.makeText(ctx, "Wait for location service.", Toast.LENGTH_LONG).show();
                    return;
                }
                intent.putExtra(CreateMessageActivity.LAST_KNOWN_LOCATION, (Parcelable) new GeoPoint(lastKnownLocation));
                startActivity(intent);
            } else {
                Snackbar.make(view, "Log in to create messages.", Snackbar.LENGTH_LONG).show();
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
        savedMenuItem = navigationView.getMenu().findItem(R.id.saved);
        historyMenuItem = navigationView.getMenu().findItem(R.id.history);

        mapView = findViewById(R.id.map);
        locationOverlay = new WatchableMyLocationOverlay(mapView);
        locationProvider = locationOverlay.getMyLocationProvider();

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        GestureDetector doubleTapDetector = new GestureDetector(ctx, new DoubleTapGestureListener());
        mapView.setOnTouchListener((v, event) -> {
            if (doubleTapDetector.onTouchEvent(event))
                return true;
            // TODO ensure that we can still click on map messages
            return v.performClick();
        });

        messageManager = new MessageManager(mapView);
        locationOverlay.addConsumer(messageManager);
        uiThreadMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                @SuppressWarnings("unchecked")
                List<Message> messages = (List<Message>) msg.obj;
                Log.i("TOAST", "Updating the active markers.");
                messageManager.replaceActiveMarkers(messages);
            }
        };

        mapView.getOverlays().add(locationOverlay);
        Handler animationHandler = new Handler(Looper.getMainLooper());
        locationOverlay.runOnFirstFix(() -> {
            // Update the messages as soon as we get a location
            executor.execute(new HeartbeatRunnable(ctx, uiThreadMessageHandler, locationProvider));
            // Move the map to where the new location is
            animationHandler.post(() -> {
                mapView.getController().setCenter(locationOverlay.getMyLocation());
                mapView.getController().setZoom(19.0);
            });
        });

        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapView.addOnFirstLayoutListener((v, left, top, right, bottom) ->
                mapView.zoomToBoundingBox(new BoundingBox(33.362, -78.343, 24.056, -84.276), false));
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationOverlay.disableMyLocation();
        locationOverlay.disableFollowLocation();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(LOGGED_IN_STATE_KEY, loggedIn);
        editor.apply();

        Log.i("TOAST", "Shutting down heartbeat thread.");
        executor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        locationOverlay.setEnableAutoStop(false);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        loggedIn = preferences.getBoolean(LOGGED_IN_STATE_KEY, false);
        updateLoginVisibility();

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        Log.i("TOAST", "Setting up heartbeat thread.");
        executor.scheduleAtFixedRate(new HeartbeatRunnable(getApplicationContext(), uiThreadMessageHandler, locationProvider),
                0, 5, TimeUnit.SECONDS);

        ensureLocationPermission();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
        savedMenuItem.setVisible(loggedIn);
        historyMenuItem.setVisible(loggedIn);
    }

    private void ensureLocationPermission() {
        if (Build.VERSION.SDK_INT < 23)
            return;
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Message in a bottle requires location permission!", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != MY_PERMISSION_REQUEST_CODE || grantResults.length != 1 || grantResults[0] != PERMISSION_GRANTED) {
            Toast.makeText(this, "Message in a bottle requires location permission!", Toast.LENGTH_LONG).show();
        }
    }
}
