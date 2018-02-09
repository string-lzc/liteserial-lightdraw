package lzc.com.drawboard;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by lzc on 2017/6/14.
 */

public class DrawPath {
    public Path dpath;
    public Paint dpaint;

    public DrawPath(){
        dpath = new Path();
        dpaint = new Paint();
    }
}
