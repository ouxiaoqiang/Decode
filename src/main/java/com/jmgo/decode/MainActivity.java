package com.jmgo.decode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static cn.bmob.v3.b.From.e;

public class MainActivity extends Activity {

    private static final String TAG = "oxq";
    Button mBt1, mBt2, mBtAdd, mBtDel, mBtLogin, mBtRegister;
    TextView mTv;
    private EditText etEncrypt, etDecrypt;
    private Button btnEncrypt, btnDecrypt;
  int count = 0;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTv.setText((String) msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bmob.initialize(this, "1d0b009363c8e37db91e40a0ef365056");
        //第二：自v3.4.7版本开始,设置BmobConfig,允许设置请求超时时间、文件分片上传时每片的大小、文件的过期时间(单位为秒)，
//        BmobConfig config =new BmobConfig.Builder(this)
//        //设置appkey
//        .setApplicationId("1d0b009363c8e37db91e40a0ef365056")
//        //请求超时时间（单位为秒）：默认15s
//        .setConnectTimeout(30)
//        //文件分片上传时每片的大小（单位字节），默认512*1024
//        .setUploadBlockSize(1024*1024)
//        //文件的过期时间(单位为秒)：默认1800s
//        .setFileExpiration(2500)
//        .build();
//        Bmob.initialize(config);

        setContentView(R.layout.activity_main);

        mBt1 = (Button) findViewById(R.id.create_file);
        mBt2 = (Button) findViewById(R.id.read_file);
        mTv = (TextView) findViewById(R.id.info);
        etEncrypt = (EditText) findViewById(R.id.et_encrypt);
        etDecrypt = (EditText) findViewById(R.id.et_decrypt);
        btnEncrypt = (Button) findViewById(R.id.btn_encrypt);
        btnDecrypt = (Button) findViewById(R.id.btn_decrypt);
        mBtAdd = (Button) findViewById(R.id.btn_add);
        mBtDel = (Button) findViewById(R.id.btn_del);
        mBtLogin = (Button) findViewById(R.id.btn_1);
        mBtRegister = (Button) findViewById(R.id.btn_2);
        mBt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mTv.setText(readExternal("1234.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        mBt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSuccess = CustomFileCipherUtil.encrypt("/storage/external_storage/sda1/1234.txt", new CustomFileCipherUtil.CipherListener() {
                            @Override
                            public void onProgress(long current, long total) {
                                Message msg = uiHandler.obtainMessage();
                                msg.obj = "加密：" + current + "/" + total + "(" + current * 100 / total + "%)";
                                uiHandler.sendMessage(msg);
                            }
                        });
                        Message msg = uiHandler.obtainMessage();
                        if (isSuccess) {
                            msg.obj = "加密成功";
                        } else {
                            msg.obj = "加密失败";
                        }
                        uiHandler.sendMessage(msg);
                    }
                }).start();
            }
        });
        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSuccess = CustomFileCipherUtil.decrypt("/storage/external_storage/sda1/1234.txt", new CustomFileCipherUtil.CipherListener() {
                            @Override
                            public void onProgress(long current, long total) {
                                Message msg = uiHandler.obtainMessage();
                                msg.obj = "解密：" + current + "/" + total + "(" + current * 100 / total + "%)";
                                uiHandler.sendMessage(msg);
                            }
                        });
                        Message msg = uiHandler.obtainMessage();
                        if (isSuccess) {
                            msg.obj = "解密成功";
                        } else {
                            msg.obj = "解密失败";
                        }
                        uiHandler.sendMessage(msg);
                    }
                }).start();
            }
        });



        mBtAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyID key = new KeyID();
                key.setId("qwe");
                key.setKey(10);
                key.save(new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        if (e == null) {
                            Toast.makeText(MainActivity.this, "添加数据成功，返回objectId为：" + objectId, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "创建数据失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //查找KeyID表里面id为06f22bdbaa的数据
//                BmobQuery<KeyID> bmobQuery = new BmobQuery<KeyID>();
//                bmobQuery.getObject("123456", new QueryListener<KeyID>() {
//                    @Override
//                    public void done(KeyID object, BmobException e) {
//                        if (e == null) {
//                            Toast.makeText(MainActivity.this, "查询成功!", Toast.LENGTH_SHORT).show();
//                            count = object.getKey();
//                            mTv.setText("注册剩余  "+count + "  次");
//
//
//                        } else {
//                            Toast.makeText(MainActivity.this, "查询失败!" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }
        });



        mBtDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //更新KeyID表里面id为123456的数据”
                KeyID key = new KeyID();
                key.setKey(2);
                key.update("123456", new UpdateListener() {

                    @Override
                    public void done(BmobException e) {
                        if(e==null){
                            Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "更新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                });

            }
        });


        mBtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String account,pwd;
//                account = etEncrypt.getText().toString().trim();
//
//                if(account.equals("")){
//
//                    return;
//                }
//
//
//
//                BmobUser user = new BmobUser();
//                user.setUsername(account);
//                user.login(MainActivity.this, new SaveListener() {
//
//                    @Override
//                    public void onSuccess() {
//                        // TODO Auto-generated method stub
////                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
////                        startActivity(intent);
//                        Toast.makeText(MainActivity.this, "登陆成功！" , Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(int arg0, String arg1) {
//                        // TODO Auto-generated method stub
//                       // toast("登陆失败："+arg1);
//                        Toast.makeText(MainActivity.this, "登陆失败："+arg1 , Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });
        mBtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String  account= etEncrypt.getText().toString().trim();
//                String   pwd = etDecrypt.getText().toString().trim();
//                if(account.equals("")){
//                    Toast.makeText(MainActivity.this, "填写你的用户名" , Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if(pwd.equals("")){
//                    Toast.makeText(MainActivity.this, "填写你的密码" , Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                BmobUser u = new BmobUser();
//                u.setUsername(account);
//                u.setPassword(pwd);
//                u.setEmail("791974431@qq.com");
//                u.signUp(MainActivity.this, new SaveListener() {
//
//                    @Override
//                    public void onSuccess() {
//                        // TODO Auto-generated method stub
//                        Toast.makeText(MainActivity.this, "注册成功！" , Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(int arg0, String arg1) {
//                        // TODO Auto-generated method stub
//                        Toast.makeText(MainActivity.this, "注册失败："+arg1 , Toast.LENGTH_SHORT).show();
//                    }
//                });

            }
        });


    }
//    static {
//        System.loadLibrary("bmob");
//    }

    void updateKey(){

        //更新KeyID表里面id为06f22bdbaa的数据，address内容更新为“北京朝阳”
        KeyID key = new KeyID();
        key.setKey(count -1);
        key.update("06f22bdbaa", new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "更新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
    public String readExternal(String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        // if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        //    filename = getExternalCacheDir().getAbsolutePath() + File.separator + filename;
        filename = "storage/external_storage/sda1" + File.separator + filename;
        //打开文件输入流
        FileInputStream inputStream = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        int len = inputStream.read(buffer);
        //读取文件内容
        while (len > 0) {
            sb.append(new String(buffer, 0, len));

            //继续将数据放到buffer中
            len = inputStream.read(buffer);
        }
        //关闭输入流
        inputStream.close();
        //   }
        return sb.toString();
    }


    /**
     * Description:得到U盘序列号
     *
     * @param drive
     * @return
     * @author liuwei  DateTime 2013-11-4 下午6:05:56
     */
    public static String getSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber";  // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input =
                    new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String mUsbPath = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action:" + action);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                mUsbPath = intent.getData().getPath();
                Log.i(TAG, "mUsbPath:" + mUsbPath);

            }
        }
    };

}
