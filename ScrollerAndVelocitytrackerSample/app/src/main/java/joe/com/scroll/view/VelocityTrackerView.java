package joe.com.scroll.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * Description
 * Created by chenqiao on 2017/1/13.
 */
public class VelocityTrackerView extends View {
    private Paint textPaint;

    public VelocityTrackerView(Context context) {
        this(context, null);
    }

    public VelocityTrackerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VelocityTrackerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(30);
        textPaint.setColor(Color.RED);
    }

    private VelocityTracker velocityTracker;
    private float xVelocity, yVelocity;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(String.format("XVelocity:%f", xVelocity), 0, 100, textPaint);
        canvas.drawText(String.format("YVelocity:%f", yVelocity), 0, 200, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(event);
                //设置获取的速度的单位，1表示pixel/ms，1000表示pixel/s
                velocityTracker.computeCurrentVelocity(1000);
                xVelocity = velocityTracker.getXVelocity();
                yVelocity = velocityTracker.getYVelocity();
                //一定要记得释放
                releaseVelocityTracker();
                invalidate();
                break;
        }
        return true;
    }

    private void releaseVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }
}