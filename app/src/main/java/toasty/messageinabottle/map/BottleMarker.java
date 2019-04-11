package toasty.messageinabottle.map;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import toasty.messageinabottle.MessageDetailActivity;
import toasty.messageinabottle.R;
import toasty.messageinabottle.data.Message;

public class BottleMarker extends Marker {

    private final Message message;
    private boolean inRange = false;

    public BottleMarker(MapView mapView, Message message) {
        super(mapView);
        this.message = message;
        setAnchor(ANCHOR_CENTER, ANCHOR_CENTER);
        setIcon(mapView.getContext().getDrawable(R.mipmap.ic_launcher_round));
        setPosition(message);
        setAlpha(0.5f);
    }

    @Override
    protected boolean onMarkerClickDefault(Marker marker, MapView mapView) {
        if (inRange) {
            Context ctx = mapView.getContext();
            Intent intent = new Intent(ctx, MessageDetailActivity.class);
            intent.putExtra(MessageDetailActivity.MESSAGE_KEY, (Parcelable) message);
            ctx.startActivity(intent);
            return true;
        }
        return false;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
        if (inRange) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.5f);
        }
    }
}
