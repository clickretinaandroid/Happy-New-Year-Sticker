package fb.adstest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import com.wastickerapps.hnystickers.R;

public class FbAdsTest extends AppCompatActivity {

    private final String TAG = FbAdsTest.class.getSimpleName();

    private LinearLayout adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_ads_test);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the Audience Network SDK


    }


}
