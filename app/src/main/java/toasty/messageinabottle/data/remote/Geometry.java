package toasty.messageinabottle.data.remote;

import org.osmdroid.api.IGeoPoint;

public class Geometry {
    public String type;
    public double[] coordinates;

    public static Geometry fromGeopoint(IGeoPoint geoPoint) {
        Geometry geometry = new Geometry();
        geometry.type = "Point";
        geometry.coordinates = new double[]{geoPoint.getLongitude(), geoPoint.getLatitude()};
        return geometry;
    }
}
