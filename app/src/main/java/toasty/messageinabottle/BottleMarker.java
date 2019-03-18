package toasty.messageinabottle;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

public class BottleMarker extends Marker implements IMyLocationConsumer {

    public BottleMarker(MapView mapView) {
        super(mapView);
        setIcon(mapView.getContext().getDrawable(R.mipmap.ic_launcher_round));
        setAnchor(ANCHOR_CENTER, ANCHOR_CENTER);
    }

    @Override
    protected boolean onMarkerClickDefault(Marker marker, MapView mapView) {
        Context ctx = mapView.getContext();
        Intent intent = new Intent(ctx, MessageDetailActivity.class);
        ctx.startActivity(intent);
        return false;
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        // TODO check distance and modify visibility or icon or w/e
    }
}
