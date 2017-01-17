package joe.com.scroll.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * Description
 * Created by chenqiao on 2017/1/12.
 */
public class ScrollerView extends View {

    private Scroller mScroller;

    private Paint linePaint;

    private Paint textPaint;

    public ScrollerView(Context context) {
        this(context, null);
    }

    public ScrollerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.RED);
        PathEffect effect = new DashPathEffect(new float[]{3, 2}, 1);
        linePaint.setPathEffect(effect);
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(22);
        textPaint.setTextAlign(Paint.Align.CENTER);

        mScroller = new Scroller(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 50; i++) {
            int y = 50 * i;
            canvas.drawLine(0, y, getWidth(), y, linePaint);
            canvas.drawText(String.valueOf(y), getWidth() / 2, y, textPaint);
        }
    }

    private float startY = 0f;
    private float endY = 0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mScroller.computeScrollOffset()) {
                    //如果上次滑动动画未结束，强行停止
                    mScroller.abortAnimation();
                }
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                endY = event.getY();
                //进行滚动
                smoothScrollBy(0, (int) (startY - endY));
                break;
        }
        return true;
    }

    //调用此方法设置滚动的相对偏移
    public void smoothScrollBy(int dx, int dy) {
        //设置mScroller的滚动偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();//一定要调用，否则不会生效
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            //如果滚动动画在继续，则滚动到即将到达的位置（不是最终位置）
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}