package com.example.angel.myfit.basichistoryapi.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.angel.myfit.basichistoryapi.R;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by angel on 2016-05-25.
 */
public class LoginActivity extends ActionBarActivity {


    EditText id, pw;
    LinearLayout layout;
    ProgressDialog pDialog;

    //HttpClientBuilder builder


    HttpClient http = new DefaultHttpClient();
    //HttpPost httpPost = new HttpPost("http://");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CookieSyncManager.createInstance(this);

        //필요한 객체의 참조값 얻어오기(레이아웃 색 변경을 위한 변수도 생성함)
        id = (EditText) findViewById(R.id.edtId);
        pw = (EditText) findViewById(R.id.edtPaswd);

    }

    //로그인 버튼 클릭
    public void onClick(View v) {
        /*
        Intent intent=new Intent(LoginActivity.this,AvatarActivity.class);
        intent.putExtra("id",String.valueOf(id.getText()));
        intent.putExtra("pw",String.valueOf(pw.getText()));
        startActivity(intent);
        */
        loginProcess();
    }

    //네트워크 처리결과를 화면에 반영해주는 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pDialog.dismiss();
            String result = msg.getData().getString("RESULT");
            if (result.equals("success")) {

                Toast.makeText(LoginActivity.this, "success", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(LoginActivity.this,AvatarActivity.class);
                intent.putExtra("id",String.valueOf(id.getText()));
                intent.putExtra("pw",String.valueOf(pw.getText()));
                startActivity(intent);
                finish();
            } else {

                Toast.makeText(LoginActivity.this, "failed", Toast.LENGTH_LONG).show();
            }
        }
    };

    //서버에서 전송된 xml 데이터 파싱하는 메서드
    public String parsingData(InputStream input) {
        String result = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(input));
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                if (name != null && name.equals("result"))
                    result = parser.nextText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //httpClient 쿠키 가져오기
    public final Cookie getCookie() {
        List<Cookie> cookies = ((DefaultHttpClient) http).getCookieStore().getCookies();

        return cookies.get(0);
    }


    //로그인 버튼 클릭시 수행하는 메서드
    public void loginProcess() {


        final ResponseHandler<String> responseHandler =
                new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(org.apache.http.HttpResponse httpResponse) throws ClientProtocolException, IOException {
                        String result = null;
                        HttpEntity entity = httpResponse.getEntity();
                        result = parsingData(entity.getContent());
                        Message message = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        if (result.equals("success"))
                            bundle.putString("RESULT", "success");
                        else
                            bundle.putString("RESULT", "failed");
                        message.setData(bundle);
                        handler.sendMessage(message);
                        //로그 확인
                        Log.e("로그인", result);
                        return result;
                    }
                };
        //대화상자 출력
        pDialog = ProgressDialog.show(this, "", "로그인 처리중..");
        //스레드를 생성해 HTTP 요청 전달
        new Thread() {


            public void run() {
                String url = "http://172.20.10.3:8888/Test/logincheck.jsp";

                try {
                    //updateCookie(MainActivity.this);

                    //서버에 전달할 파라미터 세팅
                    ArrayList<NameValuePair> nameValuePairs =
                            new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("id", id.getText().toString()));
                    nameValuePairs.add(new BasicNameValuePair("pw", pw.getText().toString()));

                    //응답 시간 5초 초과시 타임아웃
                    //HttpParams params = http.getParams();
                    //HttpConnectionParams.setConnectionTimeout(params, 5000);
                    //HttpConnectionParams.setSoTimeout(params, 5000);

                    //http 서버 요청 전달
                    HttpPost httpPost = new HttpPost(url);
                    //HttpGet httpGet = new HttpGet("http://i");
                    //org.apache.http.HttpResponse response = http.execute(httpGet);
                    UrlEncodedFormEntity entityRequest =
                            new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
                    httpPost.setEntity(entityRequest);
                    http.execute(httpPost, responseHandler);
                    Cookie cookie = getCookie();
                    Log.e("쿠키 정보 확인", cookie.toString());
                    //builder=builder.build(getHttpClient());


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start(); //수행
    }
}


