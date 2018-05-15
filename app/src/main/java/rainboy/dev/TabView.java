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
import java.util.List;

public class TabView extends RelativeLayout implements GestureDetector.OnGestureListener {
    private int numberOfTab = 5;
    private ArrayList<Tab> listView = new ArrayList<>();
    private int width = 0;
    private int scrollWidth = 0;
    private int tabWidth = 0;

    private View mask;

    private int initialIndex = -1;
    private int currentPos = 0;
    private int currentIndex = -1;
    private int gap = 0;

    private boolean animating = false;

    private GestureDetector mDetector;

    private final int SCREEN_SCALE = 3;

    private int[] targetPosition;

    private int[] backgrounds = new int[]{
            R.drawable.tab_deposit, R.drawable.tab_payment,
            R.drawable.tab_qr, R.drawable.tab_game,
            R.drawable.tab_gift, R.drawable.tab_withdraw,
    };

    private int[] icons = new int[]{
            R.drawable.tab_icon_deposit, R.drawable.tab_icon_payment,
            R.drawable.tab_icon_qr, R.drawable.tab_icon_game,
            R.drawable.tab_icon_gift, R.drawable.tab_icon_withdraw,
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
        RelativeLayout container = (RelativeLayout) v.findViewById(R.id.container);
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
        targetPosition = new int[numberOfTab];
        minPos = new int[numberOfTab];
        maxPos = new int[numberOfTab];
        tabWidth = width / SCREEN_SCALE;
        int realWidth = 2 * tabWidth / 3;

        scrollWidth = width - tabWidth;
        gap = scrollWidth / SCREEN_SCALE / 5;

        for (int i = 0; i < numberOfTab - 1; i++) {
            if (i != 0) {
                minPos[i] = (int) (minPos[i - 1] + (float) realWidth * i / (numberOfTab - 1));
            }
        }
        for (int i = numberOfTab - 1; i >= 0; i--) {
            if (i != numberOfTab - 1) {
                maxPos[i] = (int) (maxPos[i + 1] - (float) realWidth * (numberOfTab - i - 1) / (numberOfTab - 1));
            } else {
                maxPos[i] = (numberOfTab - 1) * scrollWidth / (numberOfTab - 1);
            }

            Tab tab = new Tab(context);
            tab.setX(i * width / (numberOfTab - 1) - i * tabWidth / (numberOfTab - 1));
            tab.setCardBackground(backgrounds[i]);
            tab.setTitle(titles[i]);
            tab.setIcon(icons[i]);
            tab.setMinX((int) (tabWidth / 2f - tabWidth / 2f * (numberOfTab - 1 - i) / (numberOfTab - 1)));
            tab.setMaxX((int) (tabWidth / 2f + tabWidth / 2f * (float) i / (numberOfTab - 1)));

            LayoutParams params = new LayoutParams(tabWidth, -1);
            params.addRule(RelativeLayout.CENTER_VERTICAL);

            container.addView(tab, params);
            listView.add(tab);
        }

        for (int i = 0; i < numberOfTab; i++) {
            if (i == 0) {
                targetPosition[i] = 0;
            } else if (i == numberOfTab - 1) {
                targetPosition[i] = scrollWidth;
            } else {
                targetPosition[i] = maxPos[i] - (maxPos[i] - minPos[i]) / (numberOfTab - 1);
            }
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
            int basePos = i * scrollWidth / (numberOfTab - 1);

            int dif = basePos - position;
            int absDif = Math.abs(dif);

            if (absDif < gap) {
                if (currentIndex != i) {
                    currentIndex = i;
                    reorderView();
                    invalidateView();
                }
            }

            float path = (float) position / scrollWidth;
            if (i == 0) {
                listView.get(i).setX(0);
            } else if (i == numberOfTab - 1) {
                listView.get(i).setX(basePos);
            } else {

                listView.get(i).setX(maxPos[i] - path * (maxPos[i] - minPos[i]));
            }
//            listView.get(i).setXPosition(path);
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
        return mDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (animating) {
            return false;
        } else {
            if (viewAnimator != null) {
                viewAnimator.cancel();
            }
            return true;
        }
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (animating) {
            return false;
        }
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
                if (i == currentIndex) {
                    if (listener != null) {
                        listener.onTabSelected(currentIndex);
                    }
                } else {
                    List<Tab> tabs = new ArrayList<>();
                    if (i == 0) {
                        tabs.add(listView.get(numberOfTab - 1));
                    } else {
                        tabs.add(listView.get(i - 1));
                    }

                    if (i == numberOfTab - 1) {
                        tabs.add(listView.get(0));
                    } else {
                        tabs.add(listView.get(i + 1));
                    }

                    for (int n = 1; n < numberOfTab - 1; n++) {
                        int t = i + n;
                        if (t > numberOfTab - 1) {
                            t -= numberOfTab - 1;
                        }
                        tabs.add(listView.get(t));
                    }
                    listView = new ArrayList<>(tabs);

                    int basePos = scrollWidth / (numberOfTab - 1);
                    relayoutView(basePos);

//                    int basePos = i * width / (numberOfTab - 1)
//                            - i * tabWidth / (numberOfTab - 1);
//                    viewAnimator = ViewAnimator.animate(mask)
//                            .custom(new AnimationListener.Update() {
//                                @Override
//                                public void update(View view, float value) {
//                                    currentPos = (int) value;
//                                    relayoutView(currentPos);
//                                }
//                            }, currentPos, basePos)
//                            .onStart(new AnimationListener.Start() {
//                                @Override
//                                public void onStart() {
//                                    animating = true;
//                                }
//                            })
//                            .onStop(new AnimationListener.Stop() {
//                                @Override
//                                public void onStop() {
//                                    animating = false;
//                                }
//                            })
//                            .decelerate()
//                            .duration(300)
//                            .start();
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        currentPos += distanceX;
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
        final float velocity = -velocityX / 100;
        viewAnimator = ViewAnimator.animate(mask)
                .custom(new AnimationListener.Update() {
                    @Override
                    public void update(View view, float value) {
                        currentPos += (velocity - value);
                        if (currentPos < 0) {
                            currentPos = 0;
                        } else if (currentPos > scrollWidth) {
                            currentPos = scrollWidth;
                        }
                        relayoutView(currentPos);
                    }
                }, 0, velocity)
                .onStart(new AnimationListener.Start() {
                    @Override
                    public void onStart() {
                        animating = true;
                    }
                })
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        animating = false;
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
