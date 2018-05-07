package rainboy.dev;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        TabView tabView = findViewById(R.id.tab_view);
        tabView.setTabListener(new TabView.TabListener() {
            @Override
            public void onTabSelected(int position) {
                Log.d("dungnt", "On selected " + position);
            }
        });
    }
}
