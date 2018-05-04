package rainboy.dev;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private ArrayList<ImageView> listImage = new ArrayList<>();
    private int width = 0;
    private int scrollWidth = 0;
    private RelativeLayout container;
    private HorizontalScrollViewExt scrollView;

    private final int SCREEN_SCALE = 3;
    private final int NUMBER_OF_OPTIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        scrollWidth = 3 * width / 4;

        container = findViewById(R.id.container);

        for (int i = NUMBER_OF_OPTIONS - 1; i >= 0; i--) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.mipmap.ic_launcher);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            imageView.setX(i * width / (NUMBER_OF_OPTIONS - 1) - i * width / ((NUMBER_OF_OPTIONS - 1) * SCREEN_SCALE));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width / SCREEN_SCALE, -1);
            params.addRule(RelativeLayout.CENTER_VERTICAL);

            imageView.setPivotX((float) (i / (NUMBER_OF_OPTIONS - 1)) * width / SCREEN_SCALE);

            container.addView(imageView, params);
            listImage.add(imageView);
        }
        Collections.reverse(listImage);
        relayoutImage(0);

        findViewById(R.id.view).getLayoutParams().width = 2 * width - width / 4;

        scrollView = findViewById(R.id.scroll_view);
        scrollView.setScrollViewListener(new HorizontalScrollViewExt.ScrollViewListener() {
            @Override
            public void onScrollChanged(HorizontalScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
//                Log.d("dungnt", "" + x);
                relayoutImage(x);
            }

            @Override
            public void onScrollStopped() {
//                Log.d("dungnt", "On stop");
                validateViews();
            }
        });
    }

    private void relayoutImage(int position) {
        if (position > scrollWidth) {
            position = scrollWidth;
        }

        for (int i = 0; i < NUMBER_OF_OPTIONS; i++) {
            int basePos = i * width / (NUMBER_OF_OPTIONS - 1)
                    - i * width / ((NUMBER_OF_OPTIONS - 1) * SCREEN_SCALE);
            int dif = position - basePos;

            if (Math.abs(dif) < 10) {
                listImage.get(i).bringToFront();
            }

            float scale = 1 - Math.abs(dif) / (width * 1.5f);

            if (i != 0 && i != NUMBER_OF_OPTIONS - 1) {
                listImage.get(i).setX(basePos - scale * dif / 3);
            }

            listImage.get(i).setScaleX(scale);
        }
    }

    private void validateViews() {
        int min = width;
        int index = 0;
        int position = scrollView.getScrollX();
        for (int i = NUMBER_OF_OPTIONS - 1; i >= 0; i--) {
            listImage.get(i).bringToFront();
            int basePos = i * width / (NUMBER_OF_OPTIONS - 1)
                    - i * width / ((NUMBER_OF_OPTIONS - 1) * SCREEN_SCALE);
            int dif = Math.abs(position - basePos);
            if (min > dif) {
                min = dif;
                index = i;
            }
        }
        listImage.get(index).bringToFront();
    }
}
