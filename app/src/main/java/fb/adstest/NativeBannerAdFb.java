package fb.adstest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.wastickerapps.hnystickers.R;

public class NativeBannerAdFb extends AppCompatActivity {

    private final String TAG = NativeBannerAdFb.class.getSimpleName();

    private LinearLayout adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_banner_ad_fb);
       }
}
