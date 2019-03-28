package toasty.messageinabottle.distance;

import android.location.Location;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.ArrayList;
import java.util.List;

import toasty.messageinabottle.data.Message;

public class MessageManager implements IMyLocationConsumer {

    public static final double IN_RANGE_LIMIT = 100000; // meters

    private final MapView mapView;
    private final List<BottleMarker> markers = new ArrayList<>();

    public MessageManager(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        GeoPoint locationGeoPoint = new GeoPoint(location);

        if (false) { // TODO check if user is out of range
            fetchPoints();
        } else {
            for (BottleMarker marker : markers) {
                boolean inRange = locationGeoPoint.distanceToAsDouble(marker.getPosition()) <= IN_RANGE_LIMIT;
                marker.setInRange(inRange);
            }
        }
    }

    private void fetchPoints() {
        // TODO download the messages and update the list
    }

    public void addMessage(Message message) {
        BottleMarker marker = new BottleMarker(mapView, message);
        markers.add(marker);
        mapView.getOverlays().add(marker);
    }
}
