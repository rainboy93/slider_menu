package rainboy.dev;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
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

    private int initialIndex = -1;
    private int currentPos = 0;
    private int currentIndex = -1;
    private int gap = 0;

    private GestureDetector mDetector;

    private final int SCREEN_SCALE = 3;

    private int[] backgrounds = new int[]{
            R.drawable.tab_deposit, R.drawable.tab_payment,
            R.drawable.tab_qr, R.drawable.tab_game,
            R.drawable.tab_gift, R.drawable.tab_withdraw,
    };

    private String[] titles = new String[]{
            "NẠP TIỀN", "THANH TOÁN", "MY QR CODE", "CHƠI GAME", "QUÀ TẶNG", "RÚT TIỀN"
    };

    private float[] realPositions;
    private int[] minPos;
    private int[] maxPos;

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
        minPos = new int[numberOfTab];
        maxPos = new int[numberOfTab];
        int realWidth = 2 * width / SCREEN_SCALE / 3;
        for (int i = 0; i < numberOfTab - 1; i++) {
            if (i != 0) {
                minPos[i] = (int) (minPos[i - 1] + (float) realWidth * i / (numberOfTab - 1));
            }
        }
        for (int i = numberOfTab - 1; i >= 0; i--) {
            if (i != numberOfTab - 1) {
                maxPos[i] = (int) (maxPos[i + 1] - (float) realWidth * (numberOfTab - i - 1) / (numberOfTab - 1));
            } else {
                maxPos[i] = (numberOfTab - 1) * (width - width / SCREEN_SCALE) / (numberOfTab - 1);
            }
        }

        scrollWidth = width - width / SCREEN_SCALE;
        gap = scrollWidth / SCREEN_SCALE / 5;

        for (int i = numberOfTab - 1; i >= 0; i--) {
            Tab tab = new Tab(context);
            tab.setX(i * width / (numberOfTab - 1) - i * width / ((numberOfTab - 1) * SCREEN_SCALE));
            tab.setCardBackground(backgrounds[i]);
            tab.setTitle(titles[i]);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width / SCREEN_SCALE, -1);
            params.addRule(RelativeLayout.CENTER_VERTICAL);

            container.addView(tab, params);
            listView.add(tab);
        }
        Collections.reverse(listView);
        relayoutView(0);
        invalidateView();
    }

    private void relayoutView(int position) {
        if (position > scrollWidth) {
            position = scrollWidth;
        }

        for (int i = 0; i < numberOfTab; i++) {
            int basePos = i * (width - width / SCREEN_SCALE) / (numberOfTab - 1);

            int dif = basePos - position;
            int absDif = Math.abs(dif);

            if (absDif < gap) {
                if (currentIndex != i) {
                    currentIndex = i;
                    reorderView();
                    invalidateView();
                }
            }

            if (i == 0) {
                listView.get(i).setX(0);
            } else if (i == numberOfTab - 1) {
                listView.get(i).setX(basePos);
            } else {
                float path = (float) position / scrollWidth;
                listView.get(i).setX(maxPos[i] - path * (maxPos[i] - minPos[i]));
            }
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

    private void invalidateView() {
        if (currentIndex != initialIndex) {
            for (int i = 0; i < numberOfTab; i++) {
                if (currentIndex == i) {
                    listView.get(i).setTabSelected(true);
                } else {
                    listView.get(i).setTabSelected(false);
                }
            }
            initialIndex = currentIndex;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mDetector.onTouchEvent(ev)) {
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null) {
                listener.onTabSelected(currentIndex);
            }
            return true;
        }
        return false;
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
                        .onStop(new AnimationListener.Stop() {
                            @Override
                            public void onStop() {
                                if (listener != null) {
                                    listener.onTabSelected(currentIndex);
                                }
                            }
                        })
                        .decelerate()
                        .duration(300)
                        .start();
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        currentPos += distanceX / 3;
        if (currentPos < 0) {
            currentPos = 0;
        } else if (currentPos > scrollWidth) {
            currentPos = scrollWidth;
        }
        relayoutView(currentPos);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final float velo = -velocityX / 100;
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
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        if (listener != null) {
                            listener.onTabSelected(currentIndex);
                        }
                    }
                })
                .duration(300)
                .decelerate()
                .start();
        return true;
    }

    private ViewAnimator viewAnimator;

    // Handle tab event
    private TabListener listener;

    public void setTabListener(TabListener listener) {
        this.listener = listener;
    }

    public interface TabListener {
        void onTabSelected(int position);
    }
}
