package toasty.messageinabottle.map;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class DoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }
}
