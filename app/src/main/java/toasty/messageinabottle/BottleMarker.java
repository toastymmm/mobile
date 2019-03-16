package toasty.messageinabottle;

import android.content.Context;
import android.content.Intent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class BottleMarker extends Marker {
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
}
