package toasty.messageinabottle.map;

import android.location.Location;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class WatchableMyLocationOverlay extends MyLocationNewOverlay {

    private final List<IMyLocationConsumer> consumers = new ArrayList<>();

    public WatchableMyLocationOverlay(MapView mapView) {
        super(mapView);
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        super.onLocationChanged(location, source);
        for (IMyLocationConsumer consumer : consumers) {
            consumer.onLocationChanged(location, source);
        }
    }

    public void addConsumer(IMyLocationConsumer consumer) {
        consumers.add(consumer);
    }
}
