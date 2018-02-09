package lzc.com.drawboard;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by lzc on 2017/12/10.
 */

public class DragTextView extends  android.support.v7.widget.AppCompatTextView {
    private float startx;// down事件发生时，手指相对于view左上角x轴的距离
    private float starty;// down事件发生时，手指相对于view左上角y轴的距离
    private float endx; // move事件发生时，手指相对于view左上角x轴的距离
    private float endy; // move事件发生时，手指相对于view左上角y轴的距离
    private int left; // DragTV左边缘相对于父控件的距离
    private int top; // DragTV上边缘相对于父控件的距离
    private int right; // DragTV右边缘相对于父控件的距离
    private int bottom; // DragTV底边缘相对于父控件的距离
    private int hor; // 触摸情况下，手指在x轴方向移动的距离
    private int ver; // 触摸情况下，手指在y轴方向移动的距离

    private ViewSelectClickListener viewSelectClickListener;

    public DragTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public DragTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public DragTextView(Context context) {
        this(context, null);


        // TODO Auto-generated constructor stub
    }


    public void setViewSelectClickListener(ViewSelectClickListener viewSelectClickListener){
        this.viewSelectClickListener = viewSelectClickListener;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                viewSelectClickListener.onViewTouchDown(DragTextView.this);
                startx = event.getX();
                starty = event.getY();



                break;
            case MotionEvent.ACTION_MOVE:
               // System.out.println(event.getX()+"/"+event.getY());
                viewSelectClickListener.onViewMove(DragTextView.this,event.getRawX(),event.getRawY());

                // 手指停留在屏幕或移动时，手指相对与View左上角水平和竖直方向的距离:endX endY
                endx = event.getX();
                endy = event.getY();
                // 获取此时刻 View的位置。
                left = getLeft();
                top = getTop();
                right = getRight();
                bottom = getBottom();
                // 手指移动的水平距离
                hor = (int) (endx - startx);
                // 手指移动的竖直距离
                ver = (int) (endy - starty);
                // 当手指在水平或竖直方向上发生移动时，重新设置View的位置（layout方法）
                if (hor != 0 || ver != 0) {
                    layout(left + hor, top + ver, right + hor, bottom + ver);
                }
                break;
            case MotionEvent.ACTION_UP:

                viewSelectClickListener.onViewTouchUp(DragTextView.this,event.getRawX(),event.getRawY());
                int lastMoveDx = Math.abs((int) event.getRawX() - (int)startx);
                int lastMoveDy = Math.abs((int) event.getRawY() - (int)starty);

                // 每次移动都要设置其layout，不然由于父布局可能嵌套listview，当父布局发生改变冲毁（如下拉刷新时）则移动的view会回到原来的位置
                FrameLayout.LayoutParams lpFeedback = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lpFeedback.leftMargin = getLeft();
                lpFeedback.topMargin = getTop();
                lpFeedback.setMargins(getLeft(), getTop(), 0, 0);
                setLayoutParams(lpFeedback);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        System.out.println("test");
        super.setOnClickListener(l);
    }

    public interface ViewSelectClickListener{
        void onViewTouchDown(View view);
        void onViewMove(View view,float x,float y);
        void onViewTouchUp(View view,float x,float y);
    }

}
