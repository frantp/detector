package es.uvigo.fran.detector2;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TransparentToolbar extends Toolbar {
    public TransparentToolbar(Context context) {
        super(context);
    }

    public TransparentToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransparentToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return false;
    }
}
