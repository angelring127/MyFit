package com.example.angel.myfit.basichistoryapi.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.angel.myfit.basichistoryapi.R;
import com.example.angel.myfit.basichistoryapi.RemoteSensorManager;
import com.example.angel.myfit.basichistoryapi.data.Sensor;
import com.example.angel.myfit.basichistoryapi.events.BusProvider;
import com.example.angel.shared.GifView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by angel on 2016-06-09.
 */


public class AvatarActivity extends BaseActivity{
    public static final String TAG = "AvatarActivity";
    private RemoteSensorManager remoteSensorManager;
    GifView gifView;
    RelativeLayout avatarBackground;
    static TextView stepCountView;
    static Integer stepCount = 0;
    static Integer timeSec = 0;
    private TimerTask second;
    public String id;
    public String pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.heart_icon_gray);

        gifView = (GifView) findViewById(R.id.gif1);
        avatarBackground = (RelativeLayout) findViewById(R.id.avatar_background);
        stepCountView = (TextView) findViewById(R.id.stepCountView);
        remoteSensorManager = RemoteSensorManager.getInstance(this);

        Intent intent = getIntent();
        id = intent.getExtras().getString("id");
        pw = intent.getExtras().getString("pw");

        second = new TimerTask() {
            @Override
            public void run() {

                timeSec++;
                Log.i("TEST_Time", "Time :" + timeSec);
            }
        };
        Timer timer = new Timer();
        timer.schedule(second, 0, 1000);

        new Counter().start();

        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AvatarActivity.this,MainActivity.class);
                //intent.putExtra("key",remoteSensorManager);
                intent.putExtra("id",id);
                intent.putExtra("pw",pw);
                remoteSensorManager.stopMeasurement();

                startActivity(intent);
            }
        });
    }

    class Counter extends Thread{
        @Override
        public void run() {
            while(true){
                mHandler.sendEmptyMessage(stepCount);

                SystemClock.sleep(3000L);
            }
        }
    }
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG,"set stepcount: " + msg.what);
            stepCountView.setText("Point :"+stepCount);
            if(timeSec > 10){
                //운동을 30초 동안 안할 경우
                //avatarBackground.setBackground(getResources().getDrawable(R.mipmap.back));  //전체 배경화면 바꾸기
                gifView.setMovieResource(R.mipmap.gif10);                                   //gif이미지 바꾸기
                avatarBackground.setBackgroundColor(Color.rgb(171,231,219));
            }
            else if(stepCount>50)
            {

                gifView.setMovieResource(R.mipmap.gif12);
                avatarBackground.setBackgroundColor(Color.rgb(243,243,243));
                stepCountView.setTextColor(Color.rgb(0,0,0));

            }
            else if(stepCount>20)
            {
                //운동을 시작의 경우
                gifView.setMovieResource(R.mipmap.gif11);
                avatarBackground.setBackgroundColor(Color.rgb(0,0,9));
                stepCountView.setTextColor(Color.rgb(255,255,255));
            }
            else
            {
                // 운동을 시작하긴 했지만 적게 한 경우 stepcount가 20 이하
                gifView.setMovieResource(R.mipmap.gif13);
                avatarBackground.setBackgroundColor(Color.rgb(132,216,200));

            }

        }
    };



    @Override
    protected int getLayoutResource() {
        return R.layout.activity_avatar;
    }

    @Override
    protected void setActionBarIcon(int iconRes) {
        super.setActionBarIcon(iconRes);
    }

    public static void stepCount(){
        stepCount+= 2;
        timeSec = 0;
    }

    public static Integer getCount(){
        return stepCount;
    }



    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        List<Sensor> sensors = RemoteSensorManager.getInstance(this).getSensors();

        remoteSensorManager.startMeasurement();

    }


    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);

        remoteSensorManager.stopMeasurement();
    }


}
