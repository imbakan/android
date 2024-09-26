package balikbayan.box.toucheventsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

public class CustomView extends View {

    private final float radius = 150.0f;

    private Paint paint;
    private int count;
    private ArrayList<Element1> array = new ArrayList<>();

    public CustomView(Context context) {
        super(context);

        paint = new Paint();

        paint.setColor(0xffffff00);  // ARGB
        paint.setTextSize(70.0f);

        count = 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String str;

        canvas.save();

        str = String.format(Locale.US, "%d", count);
        canvas.drawText(str, 100, 100, paint);

        for(Element1 element1 : array)
            canvas.drawCircle(element1.getX(), element1.getY(), radius, paint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction() & MotionEvent.ACTION_MASK;

        switch (action){
            case MotionEvent.ACTION_DOWN:         onActionDown(event);        break;
            case MotionEvent.ACTION_UP:           onActionUp(event);          break;
            case MotionEvent.ACTION_MOVE:         onActionMove(event);        break
            case MotionEvent.ACTION_POINTER_DOWN: onActionPointerDown(event); break;
            case MotionEvent.ACTION_POINTER_UP:   onActionPointerUp(event);   break;
        }

        return true;
    }

    // iadd sa array ang position ng unang touch event
    private void onActionDown(MotionEvent event){

        int index = event.getActionIndex();
        float x = event.getX(index);
        float y = event.getY(index);
        int id = event.getPointerId(index);

        array.add(new Element1(x, y, id));
        count = array.size();

        invalidate();
    }

    // iremove sa array ang position ng huling touch event
    private void onActionUp(MotionEvent event){

        int index = event.getActionIndex();
        int id = event.getPointerId(index);

        Element1 element1 = findThis(array, id);

        array.remove(element1);
        count = array.size();

        invalidate();
    }

    // baguhin ang position ng mga element na nasa array
    private void onActionMove(MotionEvent event){
      
        float x, y;
        int id, index;

        for(Element1 element1 : array) {

            id = element1.getId();
            index = event.findPointerIndex(id);
            x = event.getX(index);
            y = event.getY(index);

            element1.setX(x);
            element1.setY(y);
        }

        invalidate();
    }

    // iadd sa array ang position ng susunod na touch event
    private void onActionPointerDown(MotionEvent event){

        int index = event.getActionIndex();
        float x = event.getX(index);
        float y = event.getY(index);
        int id = event.getPointerId(index);

        array.add(new Element1(x, y, id));
        count = array.size();

        invalidate();
    }

    // iremove sa array ang position ng iba pang touch event
    private void onActionPointerUp(MotionEvent event){

        int index = event.getActionIndex();
        int id = event.getPointerId(index);

        Element1 element1 = findThis(array, id);

        array.remove(element1);
        count = array.size();

        invalidate();
    }

    // hanapin ang position ng touch event sa array na may pointer id
    // ireturn ang object Element1
    private Element1 findThis(ArrayList<Element1> array, int id) {
        int i, n;
        Element1 element1, result;

        result = null;
        n = array.size();

        for(i=0; i<n; i++) {
            element1 = array.get(i);
            if(id == element1.getId()) {
                result = element1;
                break;
            }
        }

        return result;
    }
}
