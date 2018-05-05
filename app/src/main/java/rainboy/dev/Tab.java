package rainboy.dev;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Tab extends RelativeLayout {
    private ImageView icon;
    private CardView cardView;

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
        cardView = v.findViewById(R.id.card_view);
    }

    public void setScale(float scale) {
        icon.setScaleY(scale);
    }

    public void setCardBackground(int color) {
        cardView.setCardBackgroundColor(color);
    }
}
