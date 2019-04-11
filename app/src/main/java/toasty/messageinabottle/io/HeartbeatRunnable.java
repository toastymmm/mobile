package toasty.messageinabottle.io;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.List;

import toasty.messageinabottle.data.Message;

public class HeartbeatRunnable implements Runnable {

    public static final int UPDATE_MESSAGE_MANAGER = 0;

    private final Handler handler;
    private final IMyLocationProvider locationProvider;
    private final LiveBackend backend;

    public HeartbeatRunnable(Context ctx, Handler handler, IMyLocationProvider locationProvider) {
        backend = LiveBackend.getInstance(ctx);
        this.handler = handler;
        this.locationProvider = locationProvider;
    }

    @Override
    public void run() {
        Location lastKnownLocation = locationProvider.getLastKnownLocation();
        if (lastKnownLocation == null)
            return;
        GeoPoint lastKnownGeoPoint = new GeoPoint(lastKnownLocation);

        try {
            Log.i("TOAST", "Updating messages...");
            List<Message> messages = backend.messages(lastKnownGeoPoint);
            Log.i("TOAST", "Received " + messages.size() + " messages.");
            android.os.Message message = handler.obtainMessage(UPDATE_MESSAGE_MANAGER, messages);
            message.sendToTarget();
        } catch (Exception e) {
            Log.i("TOAST", "Updating messages failed!", e);
        }
    }
}
