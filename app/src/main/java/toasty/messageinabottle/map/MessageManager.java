package toasty.messageinabottle.map;

import android.location.Location;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.ArrayList;
import java.util.List;

import toasty.messageinabottle.data.Message;

public class MessageManager implements IMyLocationConsumer {

    public static final double IN_RANGE_LIMIT = 50; // meters
    private static final double USER_RANGE_LIMIT = 25; // meters

    private final MapView mapView;
    private final List<BottleMarker> activeMarkers = new ArrayList<>();
    private GeoPoint lastKnownLocation;
    private final WatchableMyLocationOverlay locationOverlay;

    public MessageManager(MapView mapView, WatchableMyLocationOverlay locationOverlay) {
        this.mapView = mapView;
        this.locationOverlay = locationOverlay;
    }

    @Override
    public synchronized void onLocationChanged(Location location, IMyLocationProvider source) {
        GeoPoint locationGeoPoint = new GeoPoint(location);
        // Store the location so that we can use it independent of the
        lastKnownLocation = locationGeoPoint;

        for (BottleMarker marker : activeMarkers) {
            checkRange(marker, locationGeoPoint);
        }
    }

    public synchronized void replaceActiveMarkers(List<Message> messages) {
        mapView.getOverlays().removeAll(activeMarkers);
        activeMarkers.clear();

        for (Message message : messages) {
            BottleMarker bottleMarker = new BottleMarker(mapView, message);
            activeMarkers.add(bottleMarker);

            // Do a range update so that the markers aren't waiting on the next GPS tick
            if (lastKnownLocation != null) {
                checkRange(bottleMarker, lastKnownLocation);
            }
        }

        mapView.getOverlays().addAll(activeMarkers);

        // Move the location overlay so that it is on top of the others
        mapView.getOverlays().remove(locationOverlay);
        mapView.getOverlays().add(locationOverlay);

        // Let the mapView know that it needs to redraw
        mapView.invalidate();
    }

    private void checkRange(BottleMarker bottleMarker, GeoPoint locationGeoPoint) {
        boolean inRange = locationGeoPoint.distanceToAsDouble(bottleMarker.getPosition()) <= IN_RANGE_LIMIT;
        bottleMarker.setInRange(inRange);
    }

    public boolean userIsTooClose(GeoPoint lastKnownLocation) {
        for (BottleMarker marker : activeMarkers) {
            if (lastKnownLocation.distanceToAsDouble(marker.getPosition()) <= USER_RANGE_LIMIT)
                return true;
        }
        return false;
    }
}
