package lzc.com.drawboard;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Created by lzc on 2017/12/8.
 */

public class RectView extends View {
    private int mov_x;//声明起点坐标
    private int mov_y;
    private Paint paint = new Paint();//声明画笔
    private Paint textPaint = new Paint();
    private Canvas canvas;//画布
    private Bitmap bitmap;//位图
    private CoordListener coordListener;
    private int blcolor;
    private boolean showText = false;
    private boolean fingerUp = false;
    DecimalFormat df = new DecimalFormat("#");
    float startX = 0;
    float startY = 0;

    float endX = 0;
    float endY = 0;

    public RectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RectView(Context context) {
        super(context);

        paint=new Paint(Paint.DITHER_FLAG);//创建一个画笔
        bitmap = Bitmap.createBitmap(480, 854, Bitmap.Config.ARGB_8888); //设置位图的宽高
        canvas=new Canvas();
        canvas.setBitmap(bitmap);


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {


        if(!fingerUp){
            DashPathEffect pathEffect = new DashPathEffect(new float[] { 10,3 }, 1);

            paint.reset();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);
            paint.setPathEffect(pathEffect);
            paint.setTextSize(30);

            textPaint.reset();
            //textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(3);
            textPaint.setColor(Color.RED);
            textPaint.setAntiAlias(true);

            textPaint.setTextSize(50);

            if(showText) {
                canvas.drawText("尺寸：" + df.format(Math.abs(endX - startX)) + "X" + df.format(Math.abs(endY - startY)), 50, 100, textPaint);
            }
            canvas.drawRect(startX,startY,endX,endY,paint);


        } else {
            canvas.drawColor(Color.parseColor("#00f1f1f1"));
        }


        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showText = true;
                fingerUp = false;

                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                endY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                coordListener.onCoord(startX,startY,endX,endY);

                fingerUp = true;


                break;
        }
        return true;
    }

    public void setCoordListener(CoordListener coordListener){
        this.coordListener = coordListener;
    }


    interface CoordListener{
        void onCoord(float startX, float startY, float endX, float endY);


    }


}
