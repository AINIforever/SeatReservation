package com.mycompany.urltest7;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
//import android.widget.Toast;

//import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Calendar;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import static android.R.id.message;

public class MainActivity extends AppCompatActivity{

    //用户名及密码
    private EditText editNum, editPawd;
    //六个id编辑框
    private EditText[] edt_id = {null,null,null,null,null,null};
    //六个id字符串
    private String[] id_str = {null,null,null,null,null,null};
    //两个时间编辑框
    private EditText edt_startTime, edt_endTime;
    //两个时间字符串
    private String startTime_str, endTime_str;
    //提交按钮
    private Button btnLogin;
    //返回字符串
    private TextView textResult;
    //默认id组
    private int[] seat_ids = {9209,9219,9267,9211,9273,9283};
    //默认时间
    private int startTime = 0;
    private int endTime = 1320;
    //日期
    private String date_str;

    private final int SUCCESS = 1;
    String obj_string;
    private String token;
    public static String yuyue_URL = "http://seat.lib.whu.edu.cn/rest/v2/freeBook";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化UI控件
        initView();



        //按钮点击事件：1.登陆并获取token   2.通过token来发post请求，输出流为预约信息
        btnLogin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){


                //获得需要post的信息流
                setPostInfomationByEdit();

                Thread_Login thread_login = new Thread_Login();
                thread_login.start();
                //给登录请求的线程“插队”，即只有该线程执行完毕才继续往下执行逻辑
                try {
                    thread_login.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                getDataAboutToken();


                //开启多线程，分别按照座位id请求预约
                for(int i=0; i<6; i++) {
                    final int index = i;
                      new Thread() {
                        public void run() {
                            //发送登录请求，获取响应流并解析token
//                            getDataAboutToken();
                            //给预约助手发送请求
                            try {
                                //定义一个URL对象
                                URL url1 = new URL(yuyue_URL);
                                //建立URLConnection连接
                                HttpURLConnection yzmURL = (HttpURLConnection) url1.openConnection();
                                //必要设置
                                yzmURL.setDoInput(true);
                                yzmURL.setRequestMethod("POST");
                                yzmURL.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                                yzmURL.setConnectTimeout(8000);
                                yzmURL.connect();

                                String data = "token=" + URLEncoder.encode(token, "UTF-8") + "&startTime=" + startTime + "&endTime=" + endTime + "&seat=" + seat_ids[index] + "&date=" + date_str;
                                //获取输出流
                                OutputStream out = yzmURL.getOutputStream();
                                out.write(data.getBytes("utf8"));
                                out.flush();
                                String frame_code = getStringFromResponse(yzmURL, "gbk");
                                textResult.setText(frame_code);

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    }.start();

                }
            }
        }
        );

        //定时设置





    }

    private void initView() {
        textResult = (TextView) findViewById(R.id.textResult);
        editNum = (EditText) findViewById(R.id.userName);
        editPawd = (EditText) findViewById(R.id.password);
        edt_id[0] = (EditText) findViewById(R.id.edt_id_1);
        edt_id[1] = (EditText) findViewById(R.id.edt_id_2);
        edt_id[2] = (EditText) findViewById(R.id.edt_id_3);
        edt_id[3] = (EditText) findViewById(R.id.edt_id_4);
        edt_id[4] = (EditText) findViewById(R.id.edt_id_5);
        edt_id[5] = (EditText) findViewById(R.id.edt_id_6);
        edt_startTime = (EditText) findViewById(R.id.edt_startTime);
        edt_endTime = (EditText) findViewById(R.id.edt_endTime);
        btnLogin = (Button) findViewById(R.id.submit);

    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    /**
                     * 获取信息成功后，对该信息进行JSON解析
                     */
                    obj_string = msg.obj.toString();
                    JSONAnalysis(obj_string);
                    break;
                default:
                    break;
            }
        };
    };

//    private void getDataAboutToken() {
//
//                try{
//                    //定义一个URL对象
//                    URL url1 = new URL("http://seat.lib.whu.edu.cn/rest/auth?username=" + editNum.getText().toString() + "&password=" + editPawd.getText().toString());
//                    HttpURLConnection yzmURL = (HttpURLConnection) url1.openConnection();
//                    //必要设置
//                    yzmURL.setDoInput(true);
//                    yzmURL.setRequestMethod("GET");
//                    yzmURL.setRequestProperty("content-type","application/x-www-form-urlencoded");
//                    yzmURL.setConnectTimeout(8000);
//                    int resCode = yzmURL.getResponseCode();
//                    if(resCode==200){
//                        InputStream is = yzmURL.getInputStream();
//                        String result = HttpUtils.readMyInputStream(is);
//
//                        /**
//                         * 子线程发送消息到主线程，并将获取的结果带到主线程，让主线程来更新UI。
//                         */
//                        Message msg = new Message();
//                        msg.obj = result;
//                        msg.what = SUCCESS;
//                        handler.sendMessage(msg);
//                    }
//
//                }catch (MalformedURLException e){
//                    e.printStackTrace();
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                }
//
//    }


    /**
     * JSON解析方法
     */
    protected void JSONAnalysis(String string) {
        JSONObject object = null;
        try {
            object = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /**
         * 在你获取的string这个JSON对象中，提取你所需要的信息。
         */

        JSONObject ObjectInfo = object.optJSONObject("data");

//        JSONObject jsonObject = ObjectInfo.optJSONObject("data");
        token = ObjectInfo.optString("token");




    }

    //从响应流中获取并解析输入
    public String getStringFromResponse(URLConnection connection, String encoding) {
        if (connection == null || encoding == null || encoding.isEmpty()) {
            return "";
        }
        InputStream in = null;
        try {
            in = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in, encoding);
        /* 获取返回的字符串 */
            char[] buff = new char[1024];
            int len = 0;
            String str = "";
            while ((len = isr.read(buff)) != -1) {
                str += (new String(buff, 0, len));
            }
            in.close();
            return str;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    //设置需要post的信息流
    protected void setPostInfomationByEdit(){
        //获取输入框中所有信息(字符串)
        for(int i=0;i<6;i++){
            id_str[i] = edt_id[i].getText().toString();
        }
        startTime_str = edt_startTime.getText().toString();
        endTime_str = edt_endTime.getText().toString();

        //id字符串转换成数字
        for(int i=0;i<seat_ids.length;i++){
            seat_ids[i] = Integer.parseInt(id_str[i]);
        }
        //将小时转换成分钟数（post需要的格式）
        String DELIM = ":";
        StringTokenizer start_tokenizer = new StringTokenizer(startTime_str,DELIM);
        int start_hours = Integer.parseInt(start_tokenizer.nextToken());
        int start_minutes = Integer.parseInt(start_tokenizer.nextToken());
        StringTokenizer end_tokenizer = new StringTokenizer(endTime_str,DELIM);
        int end_hours = Integer.parseInt(end_tokenizer.nextToken());
        int end_minutes = Integer.parseInt(end_tokenizer.nextToken());

        startTime = start_hours * 60 + start_minutes;
        endTime = end_hours * 60 + end_minutes;

        //获取明天的日期，并转换成post需要的格式
        Date date=new Date();//取时间
        Calendar calendar = new java.util.GregorianCalendar();
        calendar.setTime(date);
        if(calendar.get(Calendar.HOUR_OF_DAY)>=22 && calendar.get(Calendar.MINUTE)>=30){ //如果十点半之后预约
            calendar.add(calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动
        }
        date=calendar.getTime(); //这个时间就是日期往后推一天的结果
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        date_str = formatter.format(date);



    }

    public class Thread_Login extends Thread{
        public void run(){
            try{
                //定义一个URL对象
                URL url1 = new URL("http://seat.lib.whu.edu.cn/rest/auth?username=" + editNum.getText().toString() + "&password=" + editPawd.getText().toString());
                HttpURLConnection yzmURL = (HttpURLConnection) url1.openConnection();
                //必要设置
                yzmURL.setDoInput(true);
                yzmURL.setRequestMethod("GET");
                yzmURL.setRequestProperty("content-type","application/x-www-form-urlencoded");
                yzmURL.setConnectTimeout(8000);
                int resCode = yzmURL.getResponseCode();
                if(resCode==200){
                    InputStream is = yzmURL.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);

                    /**
                     * 子线程发送消息到主线程，并将获取的结果带到主线程，让主线程来更新UI。
                     */
                    Message msg = new Message();
                    msg.obj = result;
                    msg.what = SUCCESS;
                    handler.sendMessage(msg);
                }

            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}


