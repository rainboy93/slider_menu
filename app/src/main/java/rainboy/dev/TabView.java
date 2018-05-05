package rainboy.dev;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;

import java.util.ArrayList;
import java.util.Collections;

public class TabView extends RelativeLayout implements GestureDetector.OnGestureListener {
    private int numberOfTab = 5;
    private ArrayList<Tab> listView = new ArrayList<>();
    private int width = 0;
    private int scrollWidth = 0;

    private View mask;

    private int currentPos = 0;
    private int currentIndex = 0;
    private int gap = 0;

    private GestureDetector mDetector;

    private final int SCREEN_SCALE = 3;

    private int[] backgroundColors = new int[]{
            Color.BLUE, Color.CYAN, Color.RED, Color.YELLOW, Color.GREEN, Color.GRAY
    };

    private float[] realPositions;

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
        RelativeLayout container = v.findViewById(R.id.container);
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

        realPositions = new float[numberOfTab];

        scrollWidth = width - width / SCREEN_SCALE;
        gap = scrollWidth / SCREEN_SCALE / 5;

        for (int i = numberOfTab - 1; i >= 0; i--) {
            Tab tab = new Tab(context);
            tab.setX(i * width / (numberOfTab - 1) - i * width / ((numberOfTab - 1) * SCREEN_SCALE));
            tab.setCardBackground(backgroundColors[i]);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width / SCREEN_SCALE, -1);
            params.addRule(RelativeLayout.CENTER_VERTICAL);

            container.addView(tab, params);
            listView.add(tab);
        }
        Collections.reverse(listView);
        relayoutView(0);
    }

    private void relayoutView(int position) {
        if (position > scrollWidth) {
            position = scrollWidth;
        }

        for (int i = 0; i < numberOfTab; i++) {
            int basePos = i * width / (numberOfTab - 1)
                    - i * width / ((numberOfTab - 1) * SCREEN_SCALE);

            int dif = basePos - position;
            int absDif = Math.abs(dif);

            if (absDif < gap) {
                currentIndex = i;
                reorderView();
            }
            float scale = 1 - absDif / (2f * scrollWidth);
            float posScale = dif / (2f * scrollWidth);

            basePos += posScale * width / SCREEN_SCALE / 2;

            float margin = 0;
            if (i != 0 && i != numberOfTab - 1) {
                margin = (float) scrollWidth / 2 - position;
                margin = margin / (numberOfTab - 1);
            }

            listView.get(i).setX(basePos + margin);

            listView.get(i).setScaleX(scale);
            listView.get(i).setScale(scale);
        }
    }

    private void reorderView() {
        for (int i = 0; i < currentIndex; i++) {
            listView.get(i).bringToFront();
        }
        for (int i = numberOfTab - 1; i > currentIndex; i--) {
            listView.get(i).bringToFront();
        }
        listView.get(currentIndex).bringToFront();
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
        float tapPos = e.getRawX();

        Rect currentRect = new Rect();
        listView.get(currentIndex).getGlobalVisibleRect(currentRect);
        realPositions[currentIndex] = currentRect.left;

        for (int i = 0; i < currentIndex; i++) {
            Rect rect = new Rect();
            listView.get(i).getGlobalVisibleRect(rect);
            realPositions[i] = rect.left;
        }
        for (int i = numberOfTab - 1; i > currentIndex; i--) {
            Rect rect = new Rect();
            listView.get(i - 1).getGlobalVisibleRect(rect);
            realPositions[i] = rect.right;
        }

        for (int i = numberOfTab - 1; i >= 0; i--) {
            if (tapPos > realPositions[i]) {
                int basePos = i * width / (numberOfTab - 1)
                        - i * width / ((numberOfTab - 1) * SCREEN_SCALE);
                viewAnimator = ViewAnimator.animate(mask)
                        .custom(new AnimationListener.Update() {
                            @Override
                            public void update(View view, float value) {
                                currentPos = (int) value;
                                relayoutView(currentPos);
                            }
                        }, currentPos, basePos)
                        .decelerate()
                        .duration(300)
                        .start();

                break;
            }
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        currentPos += distanceX / 2;
        if (currentPos < 0) {
            currentPos = 0;
        } else if (currentPos > scrollWidth) {
            currentPos = scrollWidth;
        }
        relayoutView(currentPos);
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
                        } else if (currentPos > scrollWidth) {
                            currentPos = scrollWidth;
                        }
                        relayoutView(currentPos);
                    }
                }, 0, velo)
                .duration(300)
                .start();
        return false;
    }

    private ViewAnimator viewAnimator;
}
