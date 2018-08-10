package com.jmgo.decode;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
//import android.util.Base64;

public class HolatekHelper {

    private static final String PRIVATE_STR = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private int DELTA = 0;
    
    public HolatekHelper(String key) {
        DELTA = cmdDelta(key);
        if (key.equals("Jmgo")) {
            DELTA = 0;
        }
    }

    public int realCmd(int cmd) {
        return cmd - DELTA;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HolatekHelper){
            return DELTA == ((HolatekHelper)obj).DELTA;
        }
        return false;
    }

    public static int cmdDelta(String rawkey) {
        int delta = 0;
        try {
            rawkey = getMD5String(rawkey);
            if (rawkey.length() < 16) {
                StringBuffer sb = new StringBuffer(rawkey);
                while (sb.length() < 16) {
                    sb.append("0");
                }
                rawkey = sb.toString();
            } else {
                rawkey = rawkey.substring(0, 8) + rawkey.substring(rawkey.length() - 8, rawkey.length());
            }
            SecretKeySpec secretKey = new SecretKeySpec(rawkey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            String encryptString = Base64.encodeToString(cipher.doFinal(PRIVATE_STR.getBytes()), Base64.NO_WRAP);
            char[] cc = encryptString.toCharArray();
            for (int i = 0; i < cc.length; i++) {
                int x = cc[i];
                delta += x * (cc.length - i);
            }
            return delta;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return delta;
    }

    private static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    private static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    protected static MessageDigest messagedigest = null;
    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            nsaex.printStackTrace();
        }
    }

    public static String getRandomStr(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int randomNum;
        char randomChar;
        Random random = new Random();
        StringBuffer str = new StringBuffer();

        for (int i = 0; i < length; i++) {
            randomNum = random.nextInt(base.length());
            randomChar = base.charAt(randomNum);
            str.append(randomChar);
        }
        return str.toString();
    }

    /* test */
    public static void main(String[] args) {
        try {
            HashMap<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < 1; i++) {
                String key ="hotel";
                int delta = cmdDelta(key);
                if (map.containsKey(delta)) {
                    int count = map.get(delta) + 1;
                    map.put(delta, count);
                } else {
                    map.put(delta, 1);
                }
            }

            Iterator<Integer> keyit = map.keySet().iterator();
            while (keyit.hasNext()) {
                int key = keyit.next();
                System.out.println(key + "=" + map.get(key));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
