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
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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
import toasty.messageinabottle.data.UserIDWrapper;
import toasty.messageinabottle.data.cookie.CookieDatabaseAccessor;
import toasty.messageinabottle.data.cookie.DatabaseCookieDao;
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
    public static final String USER_ID_KEY = "USER_ID";

    private UserIDWrapper userIDWrapper = new UserIDWrapper();

    private MapView mapView;

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
            if (userIDWrapper.isLoggedIn()) {
                Location lastKnownLocation = locationProvider.getLastKnownLocation();
                if (lastKnownLocation == null) {
                    Toast.makeText(ctx, "Wait for location service.", Toast.LENGTH_LONG).show();
                    return;
                }
                GeoPoint lastKnownGeoPoint = new GeoPoint(lastKnownLocation);

                if (messageManager.userIsTooClose(lastKnownGeoPoint)) {
                    Snackbar.make(view, "Too close to another bottle.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(ctx, CreateMessageActivity.class);
                intent.putExtra(CreateMessageActivity.LAST_KNOWN_LOCATION, (Parcelable) lastKnownGeoPoint);
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

        ProgressBar networkProgress = findViewById(R.id.network_progress);
        mapView = findViewById(R.id.map);
        locationOverlay = new WatchableMyLocationOverlay(mapView);
        locationProvider = locationOverlay.getMyLocationProvider();

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        GestureDetector doubleTapDetector = new GestureDetector(ctx, new DoubleTapGestureListener());
        mapView.setOnTouchListener((v, event) -> {
            if (doubleTapDetector.onTouchEvent(event))
                return true;
            return v.performClick();
        });

        messageManager = new MessageManager(mapView, locationOverlay, userIDWrapper);
        locationOverlay.addConsumer(messageManager);
        uiThreadMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case HeartbeatRunnable.UPDATE_MESSAGE_MANAGER:
                        @SuppressWarnings("unchecked")
                        List<Message> messages = (List<Message>) msg.obj;
                        Log.i("TOAST", "Updating the active markers.");
                        messageManager.replaceActiveMarkers(messages);
                        break;
                    case HeartbeatRunnable.EXCEPTION_ENCOUNTERED:
                        Toast.makeText(ctx, "An exception occurred while loading messages.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        throw new IllegalStateException("UI thread got an invalid message");
                }
            }
        };

        mapView.getOverlays().add(locationOverlay);
        Handler animationHandler = new Handler(Looper.getMainLooper());
        locationOverlay.runOnFirstFix(() -> {
            // Update the messages as soon as we get a location
            executor.execute(new HeartbeatRunnable(ctx, uiThreadMessageHandler, locationProvider, userIDWrapper));
            // Move the map to where the new location is
            animationHandler.post(() -> {
                mapView.getController().setCenter(locationOverlay.getMyLocation());
                mapView.getController().setZoom(19.0);
                networkProgress.setVisibility(View.GONE);
            });
        });

        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapView.addOnFirstLayoutListener((v, left, top, right, bottom) ->
                mapView.zoomToBoundingBox(new BoundingBox(33.362, -78.343, 24.056, -84.276), false));
    }

    @Override
    protected void onPause() {
        super.onPause();

        mapView.onPause();
        locationOverlay.disableMyLocation();
        locationOverlay.disableFollowLocation();

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID_KEY, userIDWrapper.getUserID());
        editor.apply();

        Log.i("TOAST", "Shutting down heartbeat thread.");
        executor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mapView.onResume();
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        locationOverlay.setEnableAutoStop(false);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        userIDWrapper.setUserID(preferences.getString(USER_ID_KEY, null));
        updateLoginVisibility();

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        Log.i("TOAST", "Setting up heartbeat thread.");
        executor.scheduleAtFixedRate(
                new HeartbeatRunnable(getApplicationContext(), uiThreadMessageHandler, locationProvider, userIDWrapper),
                0, 5, TimeUnit.SECONDS);

        ensureLocationPermission();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(USER_ID_KEY, userIDWrapper.getUserID());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        userIDWrapper.setUserID(savedInstanceState.getString(USER_ID_KEY));
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
        } else if (id == R.id.logout) {
            new Thread(() -> {
                DatabaseCookieDao cookieDao = CookieDatabaseAccessor.getCookieDatabase(this).databaseCookieDao();
                cookieDao.remove("username");
                cookieDao.remove("userid");
                cookieDao.remove("sid");
            }).start();

            userIDWrapper.logout();
            updateLoginVisibility();
        } else if (id == R.id.history) {
            Intent intent = new Intent(this, MessageHistoryActivity.class);
            intent.putExtra(MessageHistoryActivity.USER_ID_KEY, userIDWrapper.getUserID());
            startActivity(intent);
        } else if (id == R.id.saved) {
            Intent intent = new Intent(this, SavedMessagesActivity.class);
            intent.putExtra(SavedMessagesActivity.USER_ID_KEY, userIDWrapper.getUserID());
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

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == LoginActivity.LOGIN_SUCCESS) {
            if (data == null)
                throw new RuntimeException("Unable to get userID");
            userIDWrapper.setUserID(data.getStringExtra(USER_ID_KEY));
            Toast.makeText(this, "Logged in", Toast.LENGTH_LONG).show();
            getPreferences(Activity.MODE_PRIVATE).edit()
                    .putString(USER_ID_KEY, userIDWrapper.getUserID())
                    .apply();
        }
    }

    private void updateLoginVisibility() {
        boolean loggedIn = userIDWrapper.isLoggedIn();
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
