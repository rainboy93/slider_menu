package rainboy.dev;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Tab extends RelativeLayout {
    private ImageView icon;
    private TextView title;
    private View border, background;

    private int iconWidth = 0;
    private int minX = 0;
    private int maxX = 0;

    public Tab(Context context) {
        super(context);
        init(context, null);
    }

    public Tab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Tab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.tab, this, true);

        icon = (ImageView) v.findViewById(R.id.icon);
        title = (TextView) v.findViewById(R.id.title);
        border = v.findViewById(R.id.border);
        background = v.findViewById(R.id.background);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setCardBackground(int id) {
        background.setBackgroundResource(id);
    }

    public void setIcon(int id) {
        icon.setImageResource(id);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setTabSelected(boolean selected) {
        if (selected) {
            icon.setColorFilter(Color.WHITE);
            title.setTextColor(Color.WHITE);
            border.setVisibility(VISIBLE);
            icon.setScaleX(1f);
            icon.setScaleY(1f);
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        } else {
            icon.setColorFilter(Color.BLACK);
            title.setTextColor(Color.BLACK);
            border.setVisibility(INVISIBLE);
            icon.setScaleX(0.8f);
            icon.setScaleY(0.8f);
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        }
    }

    public void setMinX(int minX) {
        this.minX = minX;
        Log.d("dungnt", "MinX " + minX);
    }

    public void setMaxX(int maxX) {
        maxX -= icon.getWidth();
        this.maxX = maxX;
        Log.d("dungnt", "MaxX " + maxX);
    }

    public void setXPosition(final float position) {
        if (iconWidth == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    iconWidth = icon.getWidth();
                    maxX -= iconWidth / 2;
                    icon.setX(maxX - position * (maxX - minX));
                }
            });
        } else {
            icon.setX(maxX - position * (maxX - minX));
        }
    }
}
