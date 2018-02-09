package lzc.com.drawboard;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import zhangphil.iosdialog.widget.ActionSheetDialog;

public class Board extends AppCompatActivity {
    private ImageView ivUp,ivDraw,ivBoard,ivGrid;
    private boolean isDrawUp = false;
    private GridLayout gl;
    private static Bitmap mBitmap;
    private Canvas canvas;
    private Paint paint;
    private boolean isDone = false;
    private Timer timer;
    private Handler handler;
    private int screenWidth;
    private Bundle params;
    private IWXAPI api;
    private List<ImageView> itemList;
    private Tencent mTencent;
    private int columnCount;
    private VelocityTracker vTracker = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        //获取屏幕宽度
        WindowManager wm = (WindowManager) Board.this
                .getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();

        //微信api声明
        api = WXAPIFactory.createWXAPI(this, "wx2fa8f2142fe52feb", true);
        api.registerApp("wx2fa8f2142fe52feb");
        //qq api声明
        mTencent = Tencent.createInstance("1106083777", getApplicationContext());



        //初始化actionbar
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("文稿");

        //初始化控件
        ivDraw = (ImageView) findViewById(R.id.iv_b_draw);
        ivBoard = (ImageView) findViewById(R.id.iv_b_board);
        ivUp = (ImageView) findViewById(R.id.iv_b_up);

        ivGrid = (ImageView) findViewById(R.id.iv_grid);

        gl = (GridLayout) findViewById(R.id.glyout);
        ivBoard.getBackground().setAlpha(255);


        /**
         * gridLayout配置及初始化
         */
        columnCount = screenWidth/120;
        gl.setColumnCount(columnCount);

        //初始化imageView的list
        itemList = new ArrayList<ImageView>();


        /**
         * 初始化缓存
         */
        initCache();

        /**
         *处理绘制后生成bitmap的线程
         */
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {




                Bitmap bitmap = Bitmap.createScaledBitmap(mBitmap,120,120,true);
                ImageView iv = new ImageView(Board.this);
                iv.setBackgroundColor(Color.WHITE);
                //iv.setImageResource(R.mipmap.ic_draw);
                iv.setLayoutParams(new WindowManager.LayoutParams(120,120));

                iv.setLayoutParams(new ViewGroup.LayoutParams(120,120));

                iv.setImageBitmap(bitmap);
                itemList.add(iv);

                gl.addView(iv);
                ivBoard.setImageBitmap(null);
                canvas = new Canvas(mBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

               // bitmap.recycle();
                super.handleMessage(msg);
            }
        };
        timer = new Timer();
        showImage();




        //控件监听事件
        gl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                float curTranslationY3 = ivDraw.getTranslationY();
                ObjectAnimator animator3 = ObjectAnimator.ofFloat(ivDraw, "translationY", curTranslationY3,+0f);
                animator3.setDuration(500);
                animator3.start();


                float curTranslationY4 = ivBoard.getTranslationY();
                ObjectAnimator animator4 = ObjectAnimator.ofFloat(ivBoard, "translationY", curTranslationY4, +0f);
                animator4.setDuration(500);
                animator4.start();

                float curTranslationY5 = ivGrid.getTranslationY();
                ObjectAnimator animator5 = ObjectAnimator.ofFloat(ivGrid, "translationY", curTranslationY5, +0f);
                animator5.setDuration(500);
                animator5.start();

                isDrawUp=false;
            }
        });
        ivUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new ActionSheetDialog(Board.this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem("发送到QQ好友/空间",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        formatView();
                                        if(itemList.size()>0) {
                                            gl.setDrawingCacheEnabled(true);
                                            saveBitmap(convertViewToBitmap(gl), "/temp.png");
                                            shareToQQ();
                                            gl.setDrawingCacheEnabled(false);
                                            gl.destroyDrawingCache();
                                        }else{
                                            Toast.makeText(Board.this,"啥都没有你想分享什么？",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .addSheetItem("发送给微信好友",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {

                                    @Override
                                    public void onClick(int which) {
                                        formatView();
                                        if(itemList.size()>0) {
                                            gl.setDrawingCacheEnabled(true);
                                            share2weixin(0,convertViewToBitmap(gl));
                                            gl.setDrawingCacheEnabled(false);
                                            gl.destroyDrawingCache();
                                        }else{
                                            Toast.makeText(Board.this,"啥都没有你想分享什么？",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .addSheetItem("分享到微信朋友圈",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {

                                    @Override
                                    public void onClick(int which) {
                                        formatView();
                                        if(itemList.size()>0) {
                                            gl.setDrawingCacheEnabled(true);
                                            share2weixin(1,convertViewToBitmap(gl));
                                            gl.setDrawingCacheEnabled(false);
                                            gl.destroyDrawingCache();
                                        }else{
                                            Toast.makeText(Board.this,"啥都没有你想分享什么？",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .addSheetItem("保存到手机",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {

                                    @Override
                                    public void onClick(int which) {
                                        formatView();
                                        if(itemList.size()>0) {
                                            System.out.println("save");
                                            gl.setDrawingCacheEnabled(true);
                                            saveBitmap(convertViewToBitmap(gl),"/lightdraw_"+getRandomString(8)+".png");
                                            Toast.makeText(Board.this,"图片已存储至sd卡：／DrawBoard/ 下",Toast.LENGTH_SHORT).show();
                                            gl.setDrawingCacheEnabled(false);
                                            gl.destroyDrawingCache();
                                        }else{
                                            Toast.makeText(Board.this,"啥都没有你想保存什么？",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).show();

            }
        });
        ivDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("click");
                if(!isDrawUp) {

                    float curTranslationY = ivDraw.getTranslationY();
                    ObjectAnimator animator = ObjectAnimator.ofFloat(ivDraw, "translationY", curTranslationY, -1000f);
                    animator.setDuration(500);
                    animator.start();

                    float curTranslationY2 = ivBoard.getTranslationY();
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(ivBoard, "translationY", curTranslationY2, -1000f);
                    animator2.setDuration(500);
                    animator2.start();

                    float curTranslationY3 = ivGrid.getTranslationY();
                    ObjectAnimator animator3 = ObjectAnimator.ofFloat(ivGrid, "translationY", curTranslationY3, -1000f);
                    animator3.setDuration(500);
                    animator3.start();
                }else{

                    float curTranslationY = ivDraw.getTranslationY();
                    ObjectAnimator animator = ObjectAnimator.ofFloat(ivDraw, "translationY", curTranslationY,+0f);
                    animator.setDuration(500);
                    animator.start();


                    float curTranslationY2 = ivBoard.getTranslationY();
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(ivBoard, "translationY", curTranslationY2, +0f);
                    animator2.setDuration(500);
                    animator2.start();

                    float curTranslationY3 = ivGrid.getTranslationY();
                    ObjectAnimator animator3 = ObjectAnimator.ofFloat(ivGrid, "translationY", curTranslationY3, +0f);
                    animator3.setDuration(500);
                    animator3.start();
                }
                isDrawUp=!isDrawUp;
            }
        });




    }

    //分享到qq
    private void shareToQQ() {
        params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "轻量画板");// 标题
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "轻量画板，轻松表达！");// 摘要
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, "https://www.baidu.com");// 内容地址
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, Environment.getExternalStorageDirectory().getPath() + "/DrawBoard/temp.png");// 网络图片地址　　params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "应用名称");// 应用名称
        params.putString(QQShare.SHARE_TO_QQ_EXT_INT, "");
        // 分享操作要在主线程中完成
        ThreadManager.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                mTencent.shareToQQ(Board.this, params, new IUiListener() {
                    @Override
                    public void onComplete(Object o) {
                        Toast.makeText(getApplicationContext(), "分享成功",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(UiError uiError) {

                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });
    }

    //分享到微信
    private void share2weixin(int shareType ,Bitmap bitmap) {

        WXImageObject imgObj = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        Bitmap thumbBitmap =  Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        //bitmap.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBitmap, true);  //设置缩略图
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        //req.transaction = buildTransaction("imgshareappdata");
        req.message = msg;
        req.scene = shareType;
        api.sendReq(req);
    }

    //layout转位图
    public static Bitmap convertViewToBitmap(View view){
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

         return bitmap;
    }

    //获取随机字符串
    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 保存图片到本地
     */
    public void saveBitmap(Bitmap bm, String picName) {
        File file;
        file = Environment.getExternalStorageDirectory();

        file = new File(file.getPath() + "/DrawBoard/");
        File file_temp = new File(file.getPath() + "/temp/");
        file_temp.mkdir();
        if (!file_temp.isDirectory()) {
            file_temp.delete();
            file_temp.mkdirs();
        }
        if (!file_temp.exists()) {
            file_temp.mkdirs();
        }
        file.mkdir();
        if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        writeBitmap(file.getPath(), picName, bm);

    }
    /**
     * 保存图片
     *
     * @param path
     * @param name
     * @param bitmap
     */
    public static void writeBitmap(String path, String name, Bitmap bitmap) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        File _file = new File(path + name);
        if (_file.exists()) {
            _file.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(_file);
            if (name != null && !"".equals(name)) {
                int index = name.lastIndexOf(".");
                if (index != -1 && (index + 1) < name.length()) {
                    String extension = name.substring(index + 1).toLowerCase();
                    if ("png".equals(extension)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } else if ("jpg".equals(extension)
                            || "jpeg".equals(extension)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    //绘制手写区域
    private void showImage() {

        WindowManager wm = (WindowManager) Board.this
                .getSystemService(Context.WINDOW_SERVICE);
        // 创建一张空白图片
        if(mBitmap!=null && mBitmap.isRecycled()==false) //如果没有回收
            mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(wm.getDefaultDisplay().getWidth(),1000, Bitmap.Config.ARGB_8888);
        // 创建一张画布
        canvas = new Canvas(mBitmap);
        // 画布背景为白色
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // 创建画笔
        paint = new Paint();
        // 画笔颜色为黑色
        paint.setColor(Color.BLACK);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        // 宽度30个像素
        paint.setStrokeWidth(30);
        // 先将白色背景画上
        canvas.drawBitmap(mBitmap, new Matrix(), paint);
        ivBoard.setImageBitmap(mBitmap);

        ivBoard.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取手按下时的坐标
                        isDone = false;
                        timer.cancel();
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        if(vTracker == null){
                            vTracker = VelocityTracker.obtain();
                        }else{
                            vTracker.clear();
                        }
                        vTracker.addMovement(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取手移动后的坐标
                        int endX = (int) event.getX();
                        int endY = (int) event.getY();
                        vTracker.addMovement(event);
                        vTracker.computeCurrentVelocity(1);
                        double vxy = Math.sqrt(vTracker.getXVelocity()*vTracker.getXVelocity()+vTracker.getYVelocity()*vTracker.getYVelocity());
                       // System.out.println("v="+Math.sqrt(vTracker.getXVelocity()*vTracker.getXVelocity()+vTracker.getYVelocity()*vTracker.getYVelocity()));
                        paint.setStrokeWidth((float) (30-1.5*vxy));

                        // 在开始和结束坐标间画一条线

                        canvas.drawLine(startX, startY, endX, endY, paint);
                        // 刷新开始坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        ivBoard.setImageBitmap(mBitmap);
                        break;
                    case MotionEvent.ACTION_UP:
                        isDone = true;
                       // vTracker.recycle();
                        timer = new Timer();

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(0x00);
                                System.out.println("test");
                            }
                        }, 1000);

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        vTracker.recycle();
                        break;
                }
                return true;
            }
        });

    }
    class MakeThread extends Thread{

        @Override
        public void run() {
            if(isDone) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


                super.run();
            }
        }
    }

    /**
     * 格式化分享|保存的view
     */
    public void formatView(){

        int viewCount = itemList.size();
        if(viewCount<=columnCount){
            return;
        }
        int addViewCount = columnCount-viewCount%columnCount;

        for(int i = 0;i<addViewCount; i++){
            ImageView iv = new ImageView(Board.this);
            iv.setBackgroundColor(Color.WHITE);
            //iv.setImageResource(R.mipmap.ic_draw);
            iv.setLayoutParams(new WindowManager.LayoutParams(120,120));
            iv.setLayoutParams(new ViewGroup.LayoutParams(120,120));
            iv.setImageBitmap(null);
            itemList.add(iv);
            gl.addView(iv);
        }
    }
    /**
     * 初始化缓存
     */
    public void initCache(){
        File file;
        file = Environment.getExternalStorageDirectory();

        file = new File(file.getPath() + "/DrawBoard/");
        File file_temp = new File(file.getPath() + "/temp/");
        file_temp.mkdir();
        if (!file_temp.isDirectory()) {
            file_temp.delete();
            file_temp.mkdirs();
        }
        if (!file_temp.exists()) {
            file_temp.mkdirs();
        }
        file.mkdir();
        if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] files = file_temp.listFiles();
        Log.i("files_length",files.length+"");
        if(files.length<1){
            return;
        }
        //将缓存文件读入itemList
        FileHelper fp = new FileHelper();
        for(File f1:files){
            Bitmap bmpPic = null;
            if(bmpPic==null){
                if(fp.getFileOrFilesSize(f1,1)>100) {
                    Log.i("files_name", file_temp.getPath() + f1.getName());
                    Log.i("files_size", fp.getFileOrFilesSize(f1, 1) + "");
                    bmpPic = BitmapFactory.decodeFile(file_temp.getPath() + "/" + f1.getName(), null);
                    ImageView imageView = new ImageView(Board.this);
                    imageView.setImageBitmap(bmpPic);
                    itemList.add(imageView);
                }else{
                    ImageView iv = new ImageView(Board.this);
                    iv.setBackgroundColor(Color.WHITE);
                    //iv.setImageResource(R.mipmap.ic_draw);
                    iv.setLayoutParams(new WindowManager.LayoutParams(120,120));
                    iv.setLayoutParams(new ViewGroup.LayoutParams(120,120));
                    iv.setImageBitmap(null);
                    itemList.add(iv);
                }
            }
        }
        //缓存植入
        for (ImageView i : itemList) {
            gl.addView(i);
        }

        //清空缓存
        for(File f:files){
            f.delete();
        }
    }

    /**
     * 菜单点击监听
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo:
                if(itemList.size()>0) {
                    itemList.remove(itemList.size() - 1);
                    gl.removeAllViews();
                    for (ImageView i : itemList) {
                        gl.addView(i);

                    }
                }
                break;
            case R.id.action_tab:
                ImageView iv = new ImageView(Board.this);
                iv.setBackgroundColor(Color.WHITE);
                //iv.setImageResource(R.mipmap.ic_draw);
                iv.setLayoutParams(new WindowManager.LayoutParams(120,120));
                iv.setLayoutParams(new ViewGroup.LayoutParams(120,120));
                iv.setImageBitmap(null);
                itemList.add(iv);
                gl.addView(iv);
                break;
            case R.id.action_del:
                gl.removeAllViews();
                itemList.clear();
                break;
            case R.id.action_sav:
                if(itemList.size()<1) {
                    Toast.makeText(Board.this,"当前无文稿，无法保存并离开！",Toast.LENGTH_SHORT).show();
                    break;
                }
                new zhangphil.iosdialog.widget.AlertDialog(Board.this).builder()
                        .setTitle("保存草稿")
                        .setMsg("是否保存草稿并暂时离开？")
                        .setPositiveButton("是", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int i = 0;
                                for(ImageView iv:itemList){
                                    System.out.println("save_temp");
                                    iv.setDrawingCacheEnabled(true);
                                    saveBitmap(convertViewToBitmap(iv),"/temp/lightdraw_temp"+i+".png");
                                    iv.setDrawingCacheEnabled(false);
                                    iv.destroyDrawingCache();
                                    i++;
                                }
                                Board.this.finish();
                            }
                        })
                        .setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                return;
                            }
                        }).show();

                break;
            case android.R.id.home:
                if(itemList.size()<1) {
                    Board.this.finish();
                    return true;
                }
                new ActionSheetDialog(Board.this)
                        .builder()
                        .setTitle("是否保存草稿？")
                        .addSheetItem("保存", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                int i = 0;
                                for(ImageView iv:itemList){
                                    System.out.println("save_temp");
                                    iv.setDrawingCacheEnabled(true);
                                    saveBitmap(convertViewToBitmap(iv),"/temp/lightdraw_temp"+i+".png");
                                    iv.setDrawingCacheEnabled(false);
                                    iv.destroyDrawingCache();
                                    i++;
                                }
                                Board.this.finish();
                            }
                        })
                        .addSheetItem("不保存", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                Board.this.finish();
                            }
                        })
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .show();

                break;

        }
        return super.onOptionsItemSelected(item);
    }
    /**
     *加载菜单资源文件
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Overflow菜单选项显示icon
     */

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try{
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(itemList.size()<1) {
                Board.this.finish();
                return true;
            }
            new ActionSheetDialog(Board.this)
                    .builder()
                    .setTitle("是否保存草稿？")
                    .addSheetItem("保存", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            int i = 0;
                            for(ImageView iv:itemList){
                                System.out.println("save_temp");
                                iv.setDrawingCacheEnabled(true);
                                saveBitmap(convertViewToBitmap(iv),"/temp/lightdraw_temp"+i+".png");
                                iv.setDrawingCacheEnabled(false);
                                iv.destroyDrawingCache();
                                i++;
                            }
                            Board.this.finish();
                        }
                    })
                    .addSheetItem("不保存", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() {
                        @Override
                        public void onClick(int which) {
                            Board.this.finish();
                        }
                    })
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true)
                    .show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
