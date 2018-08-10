package com.jmgo.decode;

import android.app.Activity;
import android.app.HolatekOSConfig;
import android.app.HolatekOSManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by ouxiaoqiang on 2018/8/7.
 */

public class DecodeActivity extends Activity {

    private static final String TAG = "oxq";
    protected static HolatekOSManager holatekOSManager;
    private ConnectivityManager connectivityManager;

    private NetworkInfo info;

    private static final String DATE_PATH = "http://jmgo.holatek.cn/server/synt";

    private Context mContext;

    private int mCurrentPosition = 0;

    private int conn_error_code = 0;

    private static int try_count;

    private final int CONNECTED = 99;

    private boolean synchronization;


    private long gmt_millis_sec = -1;
    private static final String REQUEST_METHOD_GET = "GET";


    EditText mEdtKey;
    Button mBtnLogin, mBtnNetworking;
    TextView mTvRemainID;
    String mKeyID = null;
    int count = 0;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            final int data = msg.what;
            String str = (String) msg.obj;
            //更新KeyID表里面id为06f22bdbaa的数据”
            KeyID key = new KeyID();
            key.setKey(data - 1);
            key.update(str, new UpdateListener() {

                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        Toast.makeText(DecodeActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        // mTvRemainID.setText("注册成功，注册剩余  " + (data - 1) + "  次！");
                        holatekOSManager.set(HolatekOSConfig.Service.DLP, 0xB8, 5);
                        finish();
                    } else {
                        Toast.makeText(DecodeActivity.this, getString(R.string.login_fail) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mBtnLogin.setEnabled(true);
                    }
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "1d0b009363c8e37db91e40a0ef365056");//初始化
        holatekOSManager = (HolatekOSManager) this.getSystemService("holatekos_mananger");
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        setContentView(R.layout.decode_layout);
        mEdtKey = (EditText) findViewById(R.id.edt_value);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnNetworking = (Button) findViewById(R.id.btn_networking);
        mTvRemainID = (TextView) findViewById(R.id.remain_id);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //判断联网，更新系统时间
                if (isNetworkConnected()) {
                    new Thread(run).start();
                }
                //输入不能为空
                mKeyID = mEdtKey.getText().toString().trim();
                if (null == mKeyID || "".equals(mKeyID)) {
                    mTvRemainID.setText(getString(R.string.login_null));
                    return;
                }
                //连接服务器查询注册个数
                BmobQuery<KeyID> bmobQuery = new BmobQuery<KeyID>();
                bmobQuery.getObject(mKeyID, new QueryListener<KeyID>() {
                    @Override
                    public void done(KeyID object, BmobException e) {
                        if (e == null) {
                            count = object.getKey();
                            mTvRemainID.setText(getString(R.string.login_remain) + count);
                            if (count <= 0) {
                                mTvRemainID.setText(getString(R.string.login_remain) + count + getString(R.string.login_no));
                                return;
                            }
                            mBtnLogin.setEnabled(false);
                            Message msg = handler.obtainMessage();
                            msg.what = count;
                            msg.obj = mKeyID;
                            handler.sendMessage(msg);
                        } else if (e.getMessage().contains("object")) {
                            mTvRemainID.setText(getString(R.string.login_fail1));
                        } else if (e.getMessage().contains("network")) {
                            mTvRemainID.setText(getString(R.string.login_fail2));
                        } else if (e.getMessage().contains("ExtCertPathValidatorException") || e.getMessage().contains("sdk")) {
                            mTvRemainID.setText(getString(R.string.login_fail3));
                        } else {
                            mTvRemainID.setText(getString(R.string.login_fail) + e.getMessage());
                        }
                    }
                });


            }
        });

        mBtnNetworking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holatekOSManager.set(HolatekOSConfig.Service.DLP, 0xB8, 0);
                Intent intent = new Intent();
                intent.setAction("android.settings.WIRELESS_SETTINGS");
                startActivity(intent);
            }
        });

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, mFilter);

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d("oxq", "Network Stat Changed");
                if (isNetworkConnected()) {
                    new Thread(run).start();
                }
            }
        }
    };

    private Runnable run = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            getNetTimeInMillis();
        }
    };

    public boolean isNetworkConnected() {
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    private void getNetTimeInMillis() {
        try {
            URL url = new URL(DATE_PATH);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(REQUEST_METHOD_GET);
            conn.setConnectTimeout(5 * 1000);
            if (conn.getResponseCode() != 200) {
                //连接不了网络，用原始方法
                Log.i(TAG, "NetWork Connect Failed");
            } else {
                Log.i(TAG, "NetWork Connect Success");
                InputStream in = conn.getInputStream();
                byte[] buf = new byte[1024];
                int len = 0;
                StringBuilder sb = new StringBuilder();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((len = in.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                in.close();
                baos.close();
                String jsonTxt = baos.toString();

                //Json解析获取网络时间
                Log.i(TAG, jsonTxt);
                SimpleDateFormat ss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                JSONObject time = new JSONObject(jsonTxt);
                Calendar cal = Calendar.getInstance();
                //Log.i(TAG,cal.getTimeZone().getID()+" is "+ss.format(new Date(cal.getTimeInMillis())));
                long gmt_sec = time.getInt("timestamp");
                Log.i(TAG, "Beijing Time is " + ss.format(new Date(gmt_sec * 1000)));            //跟当前时区相关
                //gmt_millis_sec = gmt_sec*1000-8*3600*1000; //转换到GMT+0
                cal.setTimeInMillis(gmt_sec * 1000);
                Log.i(TAG, cal.getTimeZone().getID() + " is " + ss.format(new Date(cal.getTimeInMillis())));
                long when = cal.getTimeInMillis();
                if (when / 1000 < Integer.MAX_VALUE) {
                    SystemClock.setCurrentTimeMillis(when);
                }
                synchronization = true;
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            conn_error_code = -1;
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            conn_error_code = -2;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            conn_error_code = -3;
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            conn_error_code = -4;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            conn_error_code = -5;
        }

        switch (conn_error_code) {
            case -1:
                Log.i(TAG, "MalformedURLException");
                break;
            case -2:
                Log.i(TAG, "ProtocolException");
                break;
            case -3:
                Log.i(TAG, "IOException");
                break;
            case -4:
                Log.i(TAG, "NumberFormatException");
                break;
            case -5:
                Log.i(TAG, "JSONException");
                break;
        }
        if (conn_error_code != 0) {
            if (!synchronization) {
                Toast.makeText(mContext, getString(R.string.login_fail4), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
