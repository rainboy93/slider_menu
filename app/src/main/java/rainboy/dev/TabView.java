package rainboy.dev;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;

import java.util.ArrayList;
import java.util.Collections;

public class TabView extends RelativeLayout implements GestureDetector.OnGestureListener {
    private int numberOfTab = 5;
    private ArrayList<ImageView> listImage = new ArrayList<>();
    private int width = 0;
    private int scrollWidth = 0;

    private View mask;
    private RelativeLayout container;

    private int currentPos = 0;
    private int currentIndex = 0;

    private GestureDetector mDetector;

    private final int SCREEN_SCALE = 3;

    public TabView(Context context) {
        super(context);
        init(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDetector = new GestureDetector(context, this);
        width = context.getResources().getDisplayMetrics().widthPixels;

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.tab_view, this, true);
        container = v.findViewById(R.id.container);
        mask = v.findViewById(R.id.mask);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TabView, 0, 0);
            try {
                numberOfTab = ta.getInteger(R.styleable.TabView_numberOfTab, 0);
                int margin = ta.getDimensionPixelSize(R.styleable.TabView_margin, 0);
                width -= 2 * margin;
            } finally {
                ta.recycle();
            }
        }

        if (numberOfTab <= 0) {
            return;
        }

        scrollWidth = width - width / SCREEN_SCALE;

        for (int i = numberOfTab - 1; i >= 0; i--) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.mipmap.ic_launcher);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            imageView.setX(i * width / (numberOfTab - 1) - i * width / ((numberOfTab - 1) * SCREEN_SCALE));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width / SCREEN_SCALE, -1);
            params.addRule(RelativeLayout.CENTER_VERTICAL);

//            imageView.setPivotX((float) (i / (numberOfTab - 1)) * width / SCREEN_SCALE);

            container.addView(imageView, params);
            listImage.add(imageView);
        }
        Collections.reverse(listImage);
        relayoutImage(0);
    }

    private void relayoutImage(int position) {
        if (position > scrollWidth) {
            position = scrollWidth;
        }

        for (int i = 0; i < numberOfTab; i++) {
            int basePos = i * width / (numberOfTab - 1)
                    - i * width / ((numberOfTab - 1) * SCREEN_SCALE);
            Log.d("dungnt", "Base pos " + basePos);
            int dif = position - basePos;

            if (Math.abs(dif) < 10) {
                currentIndex = i;
                reOrderImage();
            }

            float scale = 1 - Math.abs(dif) / (width * 1.5f);

            listImage.get(i).setX(basePos);

            listImage.get(i).setScaleX(scale);
        }
    }

    private void reOrderImage() {
        for (int i = 0; i < currentIndex; i++) {
            listImage.get(i).bringToFront();
        }
        for (int i = numberOfTab - 1; i > currentIndex; i--) {
            listImage.get(i).bringToFront();
        }
        listImage.get(currentIndex).bringToFront();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (viewAnimator != null) {
            viewAnimator.cancel();
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d("dungnt", "Single tap " + e.getX());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        currentPos += distanceX;
        if (currentPos < 0) {
            currentPos = 0;
        } else if (currentPos > scrollWidth) {
            currentPos = scrollWidth;
        }
        relayoutImage(currentPos);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final float velo = -velocityX / 200;
        viewAnimator = ViewAnimator.animate(mask)
                .custom(new AnimationListener.Update() {
                    @Override
                    public void update(View view, float value) {
                        currentPos += (velo - value);
                        if (currentPos < 0) {
                            currentPos = 0;
                            if (viewAnimator != null) {
                                viewAnimator.cancel();
                            }
                        } else if (currentPos > scrollWidth) {
                            currentPos = scrollWidth;
                            if (viewAnimator != null) {
                                viewAnimator.cancel();
                            }
                        }
                        relayoutImage(currentPos);
                    }
                }, 0, velo)
//                .decelerate()
                .duration(500)
                .start();
        return false;
    }

    private ViewAnimator viewAnimator;
}
