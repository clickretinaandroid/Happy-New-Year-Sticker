/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.wastickerapps.hnystickers;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.onesignal.OneSignal;
import com.wastickerapps.hnystickers.imagesliderbanner.API;
import com.wastickerapps.hnystickers.imagesliderbanner.MyBanner;
import com.wastickerapps.hnystickers.imagesliderbanner.ViewPagerAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class StickerPackListActivity extends AddStickerPackActivity {
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ArrayList<StickerPack> stickerPackList;


    private LinearLayout adView;


    //private InterstitialAd interstitialAd;

    //Top Banner Ads
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    LinearLayout sliderDotspanel;
    private int dotscount;
    private ImageView[] dots;
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();



    //Google Ads
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);

        //OneSignal
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();



        promoMyBannerAds();

        packRecyclerView = findViewById(R.id.sticker_pack_list);
        stickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
        showStickerPackList(stickerPackList);




        /*interstitialAd = new InterstitialAd(this, getResources().getString(R.string.fb_interstitial_ads_app_id));
        interstitialAd.loadAd();*/
        /*interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                // Show the ad
                //interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        });*/


        //Google Ads
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }



    private void promoMyBannerAds() {
        viewPager = findViewById(R.id.viewPager);
        sliderDotspanel = findViewById(R.id.SliderDots);

        jsonParsingUsingRetrofit();
    }

    private void jsonParsingUsingRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        API api = retrofit.create(API.class);
        Call<List<MyBanner>> myBannerCall = api.getMyBaners();
        myBannerCall.enqueue(new Callback<List<MyBanner>>() {
            @Override
            public void onResponse(Call<List<MyBanner>> call, Response<List<MyBanner>> response) {
                List<MyBanner> myBanners = response.body();
                setupViewPagerBannerAds(myBanners);
            }

            @Override
            public void onFailure(Call<List<MyBanner>> call, Throwable t) {
            }
        });
    }

    private void setupViewPagerBannerAds(List<MyBanner> myBanners) {

        List<MyBanner> myBannersRemoveElement = new ArrayList<>();

        for (MyBanner myBanner : myBanners) {
            if (myBanner.getAppName().contains(getString(R.string.app_name))) {
                myBannersRemoveElement.add(myBanner);
            }
        }

        myBanners.removeAll(myBannersRemoveElement);

        viewPagerAdapter = new ViewPagerAdapter(this, myBanners);

        viewPager.setAdapter(viewPagerAdapter);
        dotscount = viewPagerAdapter.getCount();
        dots = new ImageView[dotscount];

        startTimer();

        for (int i = 0; i < dotscount; i++) {

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            sliderDotspanel.addView(dots[i], params);

        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                for (int i = 0; i < dotscount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (viewPager.getCurrentItem() < viewPagerAdapter.getCount() - 1) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                        } else {
                            viewPager.setCurrentItem(0);
                        }
                    }
                });
            }
        };
        timer.schedule(timerTask, 10000, 10000);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showAlertDialog();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // builder.setCancelable(false);
        builder.setTitle("Rate us if u like this app");
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /*if (interstitialAd.isAdLoaded()) {
                    interstitialAd.show();
                }*/
                if (isFinishing()) {
                    timerTask.cancel();
                }

                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        });
        builder.setNeutralButton("Rate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isFinishing()) {
                    timerTask.cancel();
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(" http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ic_share) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String link = " https://play.google.com/store/apps/details?id=" + getPackageName();
                String message = getString(R.string.app_share_text) + link;
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(intent, "Share App"));
            } catch (Exception e) {

            }
        } else if (item.getItemId() == R.id.privacy_policy) {
            startActivity(new Intent(this, WebViewActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPackList.toArray(new StickerPack[stickerPackList.size()]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    private void showStickerPackList(List<StickerPack> stickerPackList) {
        allStickerPacksListAdapter = new StickerPackListAdapter(stickerPackList, onAddButtonClickedListener);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                packRecyclerView.getContext(),
                packLayoutManager.getOrientation()
        );
        packRecyclerView.addItemDecoration(dividerItemDecoration);
        packRecyclerView.setLayoutManager(packLayoutManager);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }


    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> {
        addStickerPackToWhatsApp(pack.identifier, pack.name);
    };

    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
        StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) packRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (viewHolder != null) {
            final int max = Math.max(viewHolder.imageRowView.getMeasuredWidth() / previewSize, 1);
            int numColumns = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            allStickerPacksListAdapter.setMaxNumberOfStickersInARow(numColumns);
        }
    }


    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return Arrays.asList(stickerPackArray);
            }
            for (StickerPack stickerPack : stickerPackArray) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return Arrays.asList(stickerPackArray);
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }




}
