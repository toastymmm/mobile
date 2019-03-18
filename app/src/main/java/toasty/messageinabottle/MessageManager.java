package toasty.messageinabottle;

import android.location.Location;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.List;

public class MessageManager implements IMyLocationConsumer {

    private BufferedBoundingBox bb;
    private List<BottleMarker> markers;

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        if (bb == null || !bb.innerContains(location)) {
            bb = new BufferedBoundingBox(location);
            fetchPoints();
        } else {
            informMarkers(location, source);
        }
    }

    private void fetchPoints() {
        // TODO download the messages and update the list
    }

    private void informMarkers(Location location, IMyLocationProvider source) {
        for (BottleMarker marker : markers) {
            marker.onLocationChanged(location, source);
        }
    }
}
