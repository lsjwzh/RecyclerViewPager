package android.support.v7.widget;

import android.util.Log;

/**
 *  LogEx
 * @author Green
 * @since 15/1/19$ 下午7:00$
 */
public class LogEx {
    final static boolean DEBUG = true;
    public static void d(String tag,String msg){
        if(DEBUG){
            Log.d(tag,msg);
        }
    }
}
