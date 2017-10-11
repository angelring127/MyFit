package com.example.angel.myfit.basichistoryapi.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.angel.myfit.basichistoryapi.R;
import com.example.angel.myfit.basichistoryapi.Utilities;
import com.example.angel.myfit.basichistoryapi.fragment.ReportsFragment;
import com.example.angel.myfit.basichistoryapi.model.Workout;
import com.example.angel.myfit.basichistoryapi.model.WorkoutTypes;


/**
 * Created by chris.black on 5/2/15.
 */
public class DetailActivity extends BaseActivity {      // 그래프를 보여주는 activity


    private static final String EXTRA_TYPE = "DetailActivity:type";
    private static final String EXTRA_TITLE = "DetailActivity:title";
    private static final String EXTRA_IMAGE = "DetailActivity:image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView image = (ImageView) findViewById(R.id.image);
        ViewCompat.setTransitionName(image, EXTRA_IMAGE);
        image.setImageResource(getIntent().getIntExtra(EXTRA_IMAGE, R.drawable.heart_icon));

        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Palette palette = Palette.generate(bitmap);
        int vibrant = palette.getVibrantColor(0x000000);
        image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));

        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        container.setBackgroundColor(vibrant);

        getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_TITLE));

        toolbar.setBackgroundColor(vibrant);
        Toast.makeText(DetailActivity.this, "그래프 화면 입니다.", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(vibrant);
        }

        // TODO: Pass in workout type instead of title.
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.chart_container, ReportsFragment.newInstance(getIntent().getIntExtra(EXTRA_TYPE, 0)))
                .commit();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_detail;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    public static void launch(BaseActivity activity, View transitionView, Workout workout) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, EXTRA_IMAGE);
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(EXTRA_IMAGE, WorkoutTypes.getImageById(workout.type));
        intent.putExtra(EXTRA_TITLE, WorkoutTypes.getWorkOutTextById(workout.type));
        intent.putExtra(EXTRA_TYPE, workout.type);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
}