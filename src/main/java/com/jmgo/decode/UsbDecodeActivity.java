package com.jmgo.decode;

import android.app.Activity;
import android.app.HolatekOSConfig;
import android.app.HolatekOSManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebResourceError;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by ouxiaoqiang on 2018/8/16.
 */

public class UsbDecodeActivity extends Activity {

    private Button btnEncrypt, btnDecrypt;
    private TextView mTv1;
    FileDES fileDES;
    protected static HolatekOSManager holatekOSManager;
    String mDeviceSN;
    String mPath = "/storage/external_storage/sda1/keycipher.txt";

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(UsbDecodeActivity.this, "抱歉，文件不存在！", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(UsbDecodeActivity.this, "下标为：" + (int) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(UsbDecodeActivity.this, "加密成功！", Toast.LENGTH_SHORT).show();
                    break;
            }

        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        holatekOSManager = (HolatekOSManager) this.getSystemService("holatekos_mananger");
        setContentView(R.layout.usb_decode_layout);
        btnEncrypt = (Button) findViewById(R.id.btn_encrypt);
        btnDecrypt = (Button) findViewById(R.id.btn_decrypt);
        mTv1 = (TextView) findViewById(R.id.usb_decode_state);

        mDeviceSN = holatekOSManager.getString(HolatekOSConfig.Service.DLP, HolatekOSConfig.DLP_INDEX_SYS_SN_NUMBER);
        mTv1.setText(mDeviceSN);
        try {
            fileDES = new FileDES("spring.sky", this);
        } catch (Exception e) {
            e.printStackTrace();
        }


        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fileDES.doEncryptFile("/storage/external_storage/sda1/key.txt", "/storage/external_storage/sda1/keycipher.txt");  //加密
                            mHandler.sendEmptyMessage(3);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }


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
                        try {
                            if (fileIsExist(mPath)) {
                                StringBuilder sb = fileDES.doDecryptFile(mPath); //解密
                                int index = sb.indexOf(mDeviceSN);
                                if (index != -1) {
                                    //查找key成功，可以注册
                                    //holatekOSManager.set(HolatekOSConfig.Service.DLP, 0xB8, 5);
                                }

                                Message msg = mHandler.obtainMessage();
                                msg.what = 1;
                                msg.obj = index;
                                mHandler.sendMessage(msg);
                            } else {
                                mHandler.sendEmptyMessage(0);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }).start();
            }
        });

    }

    /**
     * Test file is exist
     *
     * @param path 文件路径
     * @return 文件存在返回true
     */
    public static boolean fileIsExist(String path) {
        File file = new File(path);
        return file.exists();
    }


}
