package com.disnak.puas.adapter;

/**
 * Created by Muh. Zadi on 6/18/2017.
 */

import android.support.v7.appcompat.BuildConfig;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static void TRACE(String TAG, String message)
    {
        if(BuildConfig.DEBUG){
            Log.d(TAG, message);
        }
    }
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try{
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }catch (Exception e){

        }
    }
}
