package rainboy.dev;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class HorizontalScrollViewExt extends HorizontalScrollView {
    private ScrollViewListener scrollViewListener = null;

    private Runnable scrollerTask;
    private int initialPosition;

    private int newCheck = 50;

    private void initTimer() {
        scrollerTask = new Runnable() {
            public void run() {
                int newPosition = getScrollY();
                if (initialPosition - newPosition == 0) {//has stopped
                    if (scrollViewListener != null) {
                        scrollViewListener.onScrollStopped();
                    }
                } else {
                    initialPosition = getScrollY();
                    HorizontalScrollViewExt.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    startScrollerTask();
                }
                return false;
            }
        });
    }

    public HorizontalScrollViewExt(Context context) {
        super(context);
        initTimer();
    }

    public HorizontalScrollViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTimer();
    }

    public HorizontalScrollViewExt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTimer();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    public void startScrollerTask() {
        initialPosition = getScrollY();
        postDelayed(scrollerTask, newCheck);
    }

    public interface ScrollViewListener {
        void onScrollChanged(HorizontalScrollViewExt scrollView,
                             int x, int y, int oldx, int oldy);

        void onScrollStopped();
    }
}
