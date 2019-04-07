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

    public static final double IN_RANGE_LIMIT = 100000; // meters

    private final MapView mapView;
    private final List<BottleMarker> activeMarkers = new ArrayList<>();

    public MessageManager(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public synchronized void onLocationChanged(Location location, IMyLocationProvider source) {
        GeoPoint locationGeoPoint = new GeoPoint(location);
        for (BottleMarker marker : activeMarkers) {
            boolean inRange = locationGeoPoint.distanceToAsDouble(marker.getPosition()) <= IN_RANGE_LIMIT;
            marker.setInRange(inRange);
        }
    }

    public synchronized void replaceActiveMarkers(List<Message> messages) {
        mapView.getOverlays().removeAll(activeMarkers);
        activeMarkers.clear();

        for (Message message : messages) {
            activeMarkers.add(new BottleMarker(mapView, message));
        }

        mapView.getOverlays().addAll(activeMarkers);
    }
}
