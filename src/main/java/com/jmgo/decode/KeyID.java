package com.jmgo.decode;

import cn.bmob.v3.BmobObject;

/**
 * Created by ouxiaoqiang on 2018/7/25.
 */

public class KeyID extends BmobObject {
    private String id;
    private int key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int  getKey() {
        return key;
    }

    public void setKey( int key) {
        this.key = key;
    }
}
