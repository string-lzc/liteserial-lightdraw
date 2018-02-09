package lzc.com.drawboard;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Created by lzc on 2017/12/8.
 */

public class LineView extends View {
    private int mov_x;//声明起点坐标
    private int mov_y;
    private Paint paint = new Paint();//声明画笔
    private Canvas canvas;//画布
    private Bitmap bitmap;//位图
    private Paint textPaint = new Paint();
    private boolean showText = false;
    private CoordListener coordListener;
    private int blcolor;
    DecimalFormat df = new DecimalFormat("#");
    private boolean fingerUp = false;
    float startX = 0;
    float startY = 0;

    float endX = 0;
    float endY = 0;

    public LineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LineView(Context context) {
        super(context);

        paint=new Paint(Paint.DITHER_FLAG);//创建一个画笔
        bitmap = Bitmap.createBitmap(480, 854, Bitmap.Config.ARGB_8888); //设置位图的宽高
        canvas=new Canvas();
        canvas.setBitmap(bitmap);


    }

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

            canvas.drawLine(startX,startY,endX,endY,paint);

            textPaint.reset();
            //textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(3);
            textPaint.setColor(Color.RED);
            textPaint.setAntiAlias(true);

            textPaint.setTextSize(50);

            if(showText) {
                canvas.drawText("长度：" + df.format(Math.sqrt((endX-startX)*(endX-startX)+(endY-startY)*(endY-startY))), 50, 100, textPaint);
            }
        } else {
            canvas.drawColor(Color.parseColor("#00f1f1f1"));
        }


        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                fingerUp = false;
                showText = true;
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
        void onCoord(float startX,float startY,float endX,float endY);


    }


}
