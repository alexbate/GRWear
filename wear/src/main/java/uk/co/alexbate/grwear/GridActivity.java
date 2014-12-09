package uk.co.alexbate.grwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;


public class GridActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        final GridViewPager mGridPager = (GridViewPager) findViewById(R.id.pager);
        Intent intent = getIntent();
        String rawData = intent.getStringExtra("uk.co.alexbate.GRWear.API_DATA");


        mGridPager.setAdapter(new MenuGridPagerAdapter(rawData, getFragmentManager()));
    }
}