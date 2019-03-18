package toasty.messageinabottle;

import android.location.Location;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

public class BufferedBoundingBox {

    public static final int INNER_OFFSET = 10;

    private BoundingBox inner;
    private BoundingBox outer;

    public BufferedBoundingBox(Location center) {
        this(new GeoPoint(center));
    }

    public BufferedBoundingBox(IGeoPoint center) {
        double north = center.getLatitude() + INNER_OFFSET;
        double east = center.getLongitude() + INNER_OFFSET;
        double south = center.getLatitude() - INNER_OFFSET;
        double west = center.getLongitude() - INNER_OFFSET;
        inner = new BoundingBox(north, east, south, west);
        outer = inner.increaseByScale();
    }

    public boolean innerContains(Location point) {
        return innerContains(new GeoPoint(point));
    }

    private boolean innerContains(IGeoPoint point) {
    }
}
