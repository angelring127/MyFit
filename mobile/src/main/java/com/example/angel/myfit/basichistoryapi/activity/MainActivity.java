package com.example.angel.myfit.basichistoryapi.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.angel.myfit.basichistoryapi.R;
import com.example.angel.myfit.basichistoryapi.RemoteSensorManager;
import com.example.angel.myfit.basichistoryapi.Utilities;
import com.example.angel.myfit.basichistoryapi.adapter.RecyclerViewAdapter;
import com.example.angel.myfit.basichistoryapi.data.Sensor;
import com.example.angel.myfit.basichistoryapi.database.CupboardSQLiteOpenHelper;
import com.example.angel.myfit.basichistoryapi.database.DataQueries;
import com.example.angel.myfit.basichistoryapi.events.BusProvider;
import com.example.angel.myfit.basichistoryapi.events.NewSensorEvent;
import com.example.angel.myfit.basichistoryapi.fitChart.FitChart;
import com.example.angel.myfit.basichistoryapi.model.UserPreferences;
import com.example.angel.myfit.basichistoryapi.model.Workout;
import com.example.angel.myfit.basichistoryapi.model.WorkoutReport;
import com.example.angel.myfit.basichistoryapi.model.WorkoutTypes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.wearable.Node;
import com.squareup.otto.Subscribe;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import common.logger.Log;
import common.logger.LogWrapper;
import common.logger.MessageOnlyLogFilter;
import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;


public class MainActivity extends ApiClientActivity implements RecyclerViewAdapter.OnItemClickListener {

    public static final String DATE_FORMAT = "MM.dd h:mm a";        //정적 날짜 수

    private RemoteSensorManager remoteSensorManager;
    private SQLiteDatabase db;
    private WorkoutReport report = new WorkoutReport();
    private RecyclerViewAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Node> mNodes;
    public FitChart fitChart;
    TabHost tabHost;
    TextView objectStep;
    TextView todayStep;
    Float value =100F;

    TextView a, b, c, d, e, f;

    HttpClient http = new DefaultHttpClient();
    ArrayList<String> itemTwo = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.heart_icon_gray);

        initializeLogging();           //로깅 초기화

        CookieSyncManager.createInstance(this);


        //인텐트로 id,pw 받아옴
        Intent intent = getIntent();
        remoteSensorManager = RemoteSensorManager.getInstance(this);
        remoteSensorManager.startMeasurement();
        final String id = intent.getExtras().getString("id");
        final String pw = intent.getExtras().getString("pw");
        tabHost = (TabHost)findViewById(R.id.tabhost);
        setTabhost();
        //테스트
        fitChart = (FitChart)findViewById(R.id.fitChart);
        fitChart.setMinValue(0f);
        fitChart.setMaxValue(100f);
        objectStep = (TextView) findViewById(R.id.objectStep);      // 목표 걸음 설정 텍스트뷰
        todayStep = (TextView) findViewById(R.id.todayStep);        // 오늘 할당량 걸음 텍스트뷰


        // 테스트 소스
              // 센서 관리


        CupboardSQLiteOpenHelper dbHelper = new CupboardSQLiteOpenHelper(this);     //디비 설정
        db = dbHelper.getWritableDatabase();



        List<Workout> items = new ArrayList<>(report.getWorkoutData());             // 종목 별 운동 구조 ArrayList

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new RecyclerViewAdapter(items, this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.contentView);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Grabs 30 days worth of data
                populateHistoricalData();
                populateReport();
            }
        });



        //server name

        CookieSyncManager.createInstance(this);
        //table=(TableLayout)findViewById(R.id.table);
        //동적 텍스트뷰를 나타내기 위한 객체들을 생성
        a = (TextView) findViewById(R.id.a);
        b = (TextView) findViewById(R.id.b);
        c = (TextView) findViewById(R.id.c);
        d = (TextView) findViewById(R.id.d);
        e = (TextView) findViewById(R.id.e);
        f = (TextView) findViewById(R.id.f);
        //ApacheHttpTransport.Builder builder = new ApacheHttpTransport.Builder();
        //builder.build(HttpClient);

        //인텐트로 id,pw 받아옴

        //스레드 동작
        new Thread() {
            //ArrayList로 핸들러 생성해서 itemTwo 리턴
            ResponseHandler<ArrayList> responseHandler =
                    new ResponseHandler<ArrayList>() {
                        @Override
                        public ArrayList handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                            try {
                                //응답 받음
                                HttpEntity entity = httpResponse.getEntity();
                                //파서 객체 생성
                                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                                XmlPullParser parser = factory.newPullParser();
                                //읽어올 값 스트림 저장
                                parser.setInput(new InputStreamReader(entity.getContent(), "utf-8"));
                                parser.next();
                                Boolean bSet = false;
                                //xml문서의 요소를 구분해주는 정수
                                int parserEvent = parser.getEventType();
                                String rec = null;
                                //데이터 읽어오기
                                while (parserEvent != XmlPullParser.END_DOCUMENT) {

                                    switch (parserEvent) {
                                        case XmlPullParser.START_DOCUMENT: //문서의 시작
                                            break;
                                        case XmlPullParser.END_DOCUMENT:  //문서의 끝
                                            break;
                                        case XmlPullParser.START_TAG:  //태그 시작
                                            String tag = parser.getName();
                                            if (tag.equals("name")) {
                                                rec = parser.nextText();
                                                itemTwo.add(0, rec);
                                                bSet = true;
                                                android.util.Log.e("이름", itemTwo.get(0).toString());
                                            } else if (tag != null && tag.equals("id")) {
                                                rec = parser.nextText();
                                                itemTwo.add(1, rec);
                                                bSet = true;
                                                android.util.Log.e("아이디", itemTwo.get(1).toString());

                                            } else if (tag != null && tag.equals("sex")) {
                                                rec = parser.nextText();
                                                itemTwo.add(2, rec);
                                                bSet = true;
                                                android.util.Log.e("성별", itemTwo.get(2).toString());
                                            } else if (tag != null && tag.equals("email")) {
                                                rec = parser.nextText();
                                                itemTwo.add(3, rec);
                                                bSet = true;
                                                android.util.Log.e("이메일", itemTwo.get(3).toString());
                                            } else if (tag != null && tag.equals("phone")) {
                                                rec = parser.nextText();
                                                itemTwo.add(4, rec);
                                                bSet = true;
                                                android.util.Log.e("휴대폰번호", itemTwo.get(4).toString());
                                            } else if (tag != null && tag.equals("pw")) {
                                                rec = parser.nextText();
                                                itemTwo.add(5, rec);
                                                bSet = true;
                                                android.util.Log.e("패스워드", itemTwo.get(5).toString());
                                            }
                                            /*else if(a!=null&&a.equals("person")) {
                                                Log.e("xml오류", a);

                                                parserEvent = parser.next();
                                            }*/
                                            break;
                                        case XmlPullParser.END_TAG: //태그 끝
                                            rec = null;
                                            parser.nextTag();
                                            break;
                                        case XmlPullParser.TEXT: //텍스트
                                            if (bSet) {
                                                //item = itemContents + " : " + parser.getText() + "\n";
                                                bSet = false;
                                            }
                                            break;
                                    }
                                    parserEvent = parser.next(); //다음으로 이동

                                }
                                //edtResult.setText(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return itemTwo;
                        }
                    };

            public void run() {
                String url = "http://172.20.10.3:8888/Test/userinfo.jsp";

                try {
                    //서버에 전달할 파라미터 세팅
                    ArrayList<NameValuePair> nameValuePairs =
                            new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("id", id));
                    nameValuePairs.add(new BasicNameValuePair("pw", pw));

                    //응답 시간 5초 초과시 타임아웃
                    //HttpParams params = http.getParams();
                    //HttpConnectionParams.setConnectionTimeout(params, 5000);
                    //HttpConnectionParams.setSoTimeout(params, 5000);

                    //http 서버 요청 전달
                    HttpPost httpPost = new HttpPost(url);
                    UrlEncodedFormEntity entityRequest =
                            new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
                    httpPost.setEntity(entityRequest);
                    //HttpGet httpGet = new HttpGet("http://i");
                    //org.apache.http.HttpResponse response = http.execute(httpGet);
                    http.execute(httpPost, responseHandler);
                    android.util.Log.e("아이템 왔니", itemTwo.get(0).toString());
                    String[] arr = itemTwo.toArray(new String[itemTwo.size()]);
                    android.util.Log.e("아이템 배열이니", arr[0]);

                    //a=.setText(arr[0]);
                    //String z = null;
                    //z = itemTwo.get(0).toString();

                    //텍스트뷰에 문자열 표시
                    a.setText(arr[0]);
                    b.setText(arr[1]);
                    c.setText(arr[2]);
                    d.setText(arr[3]);
                    e.setText(arr[4]);
                    f.setText(arr[5]);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start(); //스레드 수행
    }

    Utilities.TimeFrame timeFrame = Utilities.TimeFrame.BEGINNING_OF_DAY;
    Utilities.TimeFrame lastPosition;


    //대화상자 오버로딩


    @Override
    public void initdialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setTitle("목표");
        builder.setMessage("당신의 목표");
        final EditText input = new EditText(this);
        input.setId(0);
        builder.setView(input);

        builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                value = Float.parseFloat(input.getText().toString());
                fitChart.setMaxValue(value);
                objectStep.setText("당신의 목표 걸음은 :"+value +"입니다.");
            }
        });
        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void setTabhost()             //호스트 탭 호출 함수
    {
        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab1").setContent(R.id.tab1).setIndicator("그래프");
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab2").setContent(R.id.tab2).setIndicator("목표");
        tabHost.addTab(spec2);

        TabHost.TabSpec spec3 = tabHost.newTabSpec("Tab3").setContent(R.id.tab3).setIndicator("랭킹");
        tabHost.addTab(spec3);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                if(tabId.equals("Tab1")){
                    Toast toast = Toast.makeText(getApplicationContext(), "그래프" , Toast.LENGTH_SHORT);
                    toast.show();
                } else if(tabId.equals("Tab2")){
                    Integer stepCount = AvatarActivity.getCount();
                    fitChart.setValue(stepCount.floatValue());
                    todayStep.setText(stepCount+"steps today");
                    if(stepCount >= fitChart.getMaxValue())
                    {
                        Toast.makeText(MainActivity.this, "축하하니다 목표를 당성했습니다.", Toast.LENGTH_LONG).show();
                    }
                } else if(tabId.equals("Tab3")){
                    Toast toast = Toast.makeText(getApplicationContext(), "랭킹" , Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });
    }
/*
    public void openFragment() {
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        GifFragment fragmentLocal = new GifFragment().newInstance();
        fragmentLocal.setHasOptionsMenu(true);
        trans.replace(R.id.frame, fragmentLocal, fragmentLocal.getTAG());
        trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        trans.addToBackStack(fragmentLocal.getTAG());
        trans.commitAllowingStateLoss();
    }
*/
    protected void populateReport() {
        new ReadTodayDataTask().execute();
        lastPosition = timeFrame;
    }


    private void notifyUSerForNewSensor(Sensor sensor) {
        Toast.makeText(this, "New Sensor!\n" + sensor.getName(), Toast.LENGTH_SHORT).show();
    }
    @Subscribe
    public void onNewSensorEvent(final NewSensorEvent event) {
       // ((ScreenSlidePagerAdapter) pager.getAdapter()).addNewSensor(event.getSensor());
       // pager.getAdapter().notifyDataSetChanged();
       // emptyState.setVisibility(View.GONE);
        notifyUSerForNewSensor(event.getSensor());
    }

    private void populateHistoricalData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Run on executer to allow both tasks to run at the same time.
            // This task writes to the DB and the other reads so we shouldn't run into any issues.
            new ReadHistoricalDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else {
            new ReadHistoricalDataTask().execute();
        }
    }

    Timer timer;

    @Override
    public void onConnect() {
        if(initialDisplay) {
            // Grabs 30 days worth of data
            populateHistoricalData();
            // Data load not complete, could take a while so lets show some data
            populateReport();
        } else {
            cancelTimer();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    // Grabs 30 days worth of data
                    populateHistoricalData();
                    // Data load not complete, could take a while so lets show some data
                    populateReport();
                    timer.cancel();
                }
            }, 750);
        }
    }

    private void cancelTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTimer();
    }

    @Override
    public void onItemClick(View view, Workout viewModel) {
        if(viewModel.type == WorkoutTypes.TIME.getValue()) {
            cancelTimer();
            timeFrame = timeFrame.next();
            adapter.setNeedsAnimate();
            populateReport();
        } else {
            DetailActivity.launch(MainActivity.this, view.findViewById(R.id.image), viewModel);
        }

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    // Show partial data on first run to make the app feel faster
    private boolean initialDisplay = true;

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        List<Sensor> sensors = RemoteSensorManager.getInstance(this).getSensors();

        remoteSensorManager.startMeasurement();
        fitChart.setMaxValue(value);


    }


    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);

        remoteSensorManager.stopMeasurement();
    }







    // TODO: Move these AsyncTask's to another class and call them via RetroFit
    private class ReadTodayDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            report.clearWorkoutData();

            long endTime = Utilities.getTimeFrameEnd(timeFrame);

            // Get data prior to today from cache
            long reportStartTime = Utilities.getTimeFrameStart(timeFrame);

            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            Log.i(TAG, "Range Start: " + dateFormat.format(reportStartTime));
            Log.i(TAG, "Range End: " + dateFormat.format(endTime));


            QueryResultIterable<Workout> itr = cupboard().withDatabase(db).query(Workout.class).withSelection("start > ?", "" + reportStartTime).query();
            for (Workout workout : itr) {
                if(workout.start > reportStartTime && workout.start < endTime) {
                    report.addWorkoutData(workout);
                }
            }
            itr.close();
            if(initialDisplay) {
                // TODO: Make sure the app is still in focus or this will crash.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update the UI
                        List<Workout> items = report.getWorkoutData();
                        adapter.setItems(items, Utilities.getTimeFrameText(timeFrame));
                    }
                });
            }

            // We don't write the activity duration from the past two hours to the cache.
            // Grab past two hours worth of data.
            if(timeFrame != Utilities.TimeFrame.LAST_MONTH) {
                Calendar cal = Calendar.getInstance();
                Date now = new Date();
                cal.setTime(now);
                cal.add(Calendar.HOUR_OF_DAY, -2);
                long startTime = cal.getTimeInMillis();

                // Estimated duration by Activity within the past two hours
                DataReadRequest activitySegmentRequest = DataQueries.queryActivitySegmentBucket(startTime, endTime);
                DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, activitySegmentRequest).await(1, TimeUnit.MINUTES);
                writeActivityDataToWorkout(dataReadResult);
            } else {
                // We don't need this data when reporting on last month.
            }

            // Estimated steps by bucket is more accurate than the step count by activity.
            // Replace walking step count total with this number to more closely match Google Fit.
            DataReadRequest stepCountRequest = DataQueries.queryStepEstimate(reportStartTime, endTime);
            DataReadResult stepCountReadResult = Fitness.HistoryApi.readData(mClient, stepCountRequest).await(1, TimeUnit.MINUTES);
            int stepCount = countStepData(stepCountReadResult);
            Workout workout = new Workout();
            workout.start = reportStartTime;
            workout.type = WorkoutTypes.WALKING.getValue();
            workout.stepCount = stepCount;
            report.setStepData(workout);

            // TODO: Make sure the app is still in focus or this will crash.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update the UI
                    List<Workout> items = new ArrayList<>(report.getWorkoutData());
                    adapter.setItems(items, Utilities.getTimeFrameText(timeFrame));
                    initialDisplay = false;
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });

            return null;
        }
    }

    // TODO: Move these AsyncTask's to another class and call them via RetroFit
    private class ReadHistoricalDataTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {

            // Setting a start and end date using a range of 1 month before this moment.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            // You might be in the middle of a workout, don't cache the past two hours of data.
            // This could be an issue for workouts longer than 2 hours. Special case for that?
            cal.add(Calendar.HOUR_OF_DAY, -2);
            long endTime = cal.getTimeInMillis();
            long startTime = endTime;
            if(UserPreferences.getBackgroundLoadComplete(MainActivity.this)) {
                Workout w = cupboard().withDatabase(db).query(Workout.class).orderBy("start DESC").get();
                startTime = w.start - 1000*60*60*8; // Go back eight hours just to be safe
            } else {
                cal.add(Calendar.DAY_OF_YEAR, -45);
                startTime = cal.getTimeInMillis();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
            Log.i(TAG, "Range End: " + dateFormat.format(endTime));

            // Estimated steps and duration by Activity
            DataReadRequest activitySegmentRequest = DataQueries.queryActivitySegment(startTime, endTime);
            DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, activitySegmentRequest).await(1, TimeUnit.MINUTES);
            writeActivityDataToCache(dataReadResult);

            UserPreferences.setBackgroundLoadComplete(MainActivity.this, true);

            // Read cached data and calculate real time step estimates
            populateReport();

            return null;
        }
    }


    private void printActivityData() {
        WorkoutReport report = new WorkoutReport();

        QueryResultIterable<Workout> itr = cupboard().withDatabase(db).query(Workout.class).query();
        for (Workout workout : itr) {
            report.addWorkoutData(workout);
        }
        itr.close();
        Log.i(TAG, report.toString());
    }

    private boolean writeActivityDataToCache(DataReadResult dataReadResult) {
        boolean wroteDataToCache = false;
        for (DataSet dataSet : dataReadResult.getDataSets()) {
            wroteDataToCache = wroteDataToCache || writeDataSetToCache(dataSet);
        }
        return wroteDataToCache;
    }

    private void writeActivityDataToWorkout(DataReadResult dataReadResult) {
        for (Bucket bucket : dataReadResult.getBuckets()) {
            for (DataSet dataSet : bucket.getDataSets()) {
                parseDataSet(dataSet);
            }
        }
    }

    /**
     * Count step data for a bucket of step count deltas.
     *
     * @param dataReadResult Read result from the step count estimate Google Fit call.
     * @return Step count for data read.
     */
    private int countStepData(DataReadResult dataReadResult) {
        int stepCount = 0;
        for (Bucket bucket : dataReadResult.getBuckets()) {
            for (DataSet dataSet : bucket.getDataSets()) {
                stepCount += parseDataSet(dataSet);
            }
        }
        return stepCount;
    }

    /**
     * Walk through all activity fields in a segment dataset and writes them to the cache. Used to
     * store data to display in reports and graphs.
     *
     * @param dataSet
     */
    private boolean writeDataSetToCache(DataSet dataSet) {
        boolean wroteDataToCache = false;
        for (DataPoint dp : dataSet.getDataPoints()) {
            // Populate db cache with data
            for(Field field : dp.getDataType().getFields()) {
                if(field.getName().equals("activity") && dp.getDataType().getName().equals("com.google.activity.segment")) {
                    long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                    int activity = dp.getValue(field).asInt();
                    Workout workout = cupboard().withDatabase(db).get(Workout.class, startTime);

                    // When the workout is null, we need to cache it. If the background task has completed,
                    // then we have at most 8 - 12 hours of data. Recent data is likely to change so over-
                    // write it.
                    if(workout == null || UserPreferences.getBackgroundLoadComplete(MainActivity.this)) {
                        long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
                        DataReadRequest readRequest = DataQueries.queryStepCount(startTime, endTime);
                        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
                        int stepCount = countStepData(dataReadResult);
                        workout = new Workout();
                        workout._id = startTime;
                        workout.start = startTime;
                        workout.duration = endTime - startTime;
                        workout.stepCount = stepCount;
                        workout.type = activity;
                        //Log.v("MainActivity", "Put Cache: " + WorkoutTypes.getWorkOutTextById(workout.type) + " " + workout.duration);
                        cupboard().withDatabase(db).put(workout);
                        wroteDataToCache = true;
                    } else {
                        // Do not overwrite data if the initial load is in progress. This would take too
                        // long and prevent us from accumulating a base set of data.
                    }
                }
            }
        }
        return wroteDataToCache;
    }

    /**
     * Walk through all fields in a step_count dataset and return the sum of steps. Used to
     * calculate step counts.
     *
     * @param dataSet
     */
    private int parseDataSet(DataSet dataSet) {
        int dataSteps = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            // Accumulate step count for estimate

            if(dp.getDataType().getName().equals("com.google.step_count.delta")) {
                for (Field field : dp.getDataType().getFields()) {
                    if (dp.getValue(field).asInt() > 0) {
                        dataSteps += dp.getValue(field).asInt();
                    }
                }
            }else {
                Workout workout = new Workout();
                workout.start = 0;
                workout.stepCount = 0;
                for (Field field : dp.getDataType().getFields()) {

                    String fieldName = field.getName();
                    if(fieldName.equals("activit")) {
                        workout.type = dp.getValue(field).asInt();
                    }else if(fieldName.equals("duration")) {
                        workout.duration = dp.getValue(field).asInt();
                    }
                }
                report.addWorkoutData(workout);
            }
        }
        return dataSteps;
    }



    // 메뉴 선택
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh_data) {
            if(connected) {
                UserPreferences.setBackgroundLoadComplete(MainActivity.this, false);
                populateHistoricalData();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *  Initialize a custom log class that outputs both to in-app targets and logcat.
     */
    private void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        // On screen logging via a customized TextView.
       // LogView logView = (LogView) findViewById(R.id.sample_logview);
        //logView.setTextAppearance(this, R.style.Log);
        //logView.setBackgroundColor(Color.WHITE);
      //  msgFilter.setNext(logView);
        Log.i(TAG, "Ready");
    }
}
