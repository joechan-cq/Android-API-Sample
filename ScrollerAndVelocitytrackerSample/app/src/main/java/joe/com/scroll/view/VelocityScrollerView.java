package joe.com.scroll.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

/**
 * Description
 * Created by chenqiao on 2017/1/13.
 */
public class VelocityScrollerView extends View {

    private Scroller mScroller;
    private Paint linePaint;
    private Paint textPaint;
    private VelocityTracker velocityTracker;
    private float yVelocity;

    public VelocityScrollerView(Context context) {
        this(context, null);
    }

    public VelocityScrollerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VelocityScrollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(context);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.RED);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = -50; i < 50; i++) {
            int y = 50 * i;
            canvas.drawLine(0, y, getWidth(), y, linePaint);
            canvas.drawText(String.valueOf(y), getWidth() / 2, y, textPaint);
        }
    }

    private float startY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mScroller.computeScrollOffset()) {
                    //如果上次滚动未结束，则停止动画
                    mScroller.abortAnimation();
                }
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(event);
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                float currY = event.getY();
                scrollBy(0, (int) (startY - currY));
                startY = currY;
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                yVelocity = velocityTracker.getYVelocity();
                if (ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity() <= Math.abs(yVelocity)) {
                    //根据滚动需要的最低速度判定需不需要进行惯性移动
                    smoothFlingBy(0, (int) -yVelocity);
                }
                releaseVelocityTracker();
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

    private void smoothFlingBy(int xVelocity, int yVelocity) {
        mScroller.fling(mScroller.getFinalX(), mScroller.getFinalY(), xVelocity, yVelocity, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}