package rainboy.dev;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class Tab extends RelativeLayout {
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
    }
}
