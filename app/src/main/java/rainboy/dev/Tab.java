package rainboy.dev;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Tab extends RelativeLayout {
    private ImageView icon;
    private TextView title;
    private View border;

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

        icon = v.findViewById(R.id.icon);
        title = v.findViewById(R.id.title);
        border = v.findViewById(R.id.border);
    }

    public void setCardBackground(int id) {
        setBackgroundResource(id);
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
            title.setTextSize(16);
        } else {
            icon.setColorFilter(null);
            title.setTextColor(Color.BLACK);
            border.setVisibility(INVISIBLE);
            icon.setScaleX(0.7f);
            icon.setScaleY(0.7f);
            title.setTextSize(12);
        }
    }
}
