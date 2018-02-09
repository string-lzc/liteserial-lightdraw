package lzc.com.drawboard;
//updated on 2017-12-10

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lzc.com.drawboard.secondview.LayerBoardActivity;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;
import zhangphil.iosdialog.widget.ActionSheetDialog;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_IMAGE = 6;
    private ImageView iv;//主画板imageview
    private ImageView ivUp;//左下角菜单按钮
    //private ImageView ivTab;//右上角跳转文稿按钮
    private LinearLayout llText;//文字输入linearlayout
    private LinearLayout llDel;//删除文字linearlayout
    private LineView lineView;  //直线辅助线蒙板
    private CircleView circleView;  //圆形辅助线蒙板
    private RectView rectView;  //矩形辅助线蒙板
    private Paint paint;//全局画笔
    private Bitmap baseBitmap;//全局bitmap
    private EditText etText; //文字输入输入框
    private FrameLayout fml;//外部总Framelayout
    private TextView tvCancel, tvConfirm;//文字输入界面取消|确认选项
    private Canvas canvas;//全局画布
    private Path path;//全局路径
    private static int p_color = Color.BLACK;//全局画笔颜色
    private static float p_strokeWidth = 10;//全局画笔宽度
    private Bundle params;//qq分享参数
    private DrawPath dp;//路径实例
    private List<DrawPath> savePath;//路径列表，用于缓存路径撤销使用
    private List<DragTextView> dtvs;//文字缓存列表
    private Tencent mTencent;//腾讯接口类实例
    private float preX;//手指接触画板的x轴坐标
    private float preY;//手指接触画板哪y轴坐标
    private ColorPickerDialog dialog;//颜色选择对话框实例
    private SeekBar seekBar;//笔粗选择滑杆实例
    private TextView tvPen;//笔粗标题
    private IWXAPI api;//微信api接口实例
    private LinearLayout llToolbar;
    private ListView lvDrawer;//抽屉布局的listview
    private ImageView ivPhoto;//照片背景
    private DrawerLayout mDrawerLayout;//抽屉layout
    private LinearLayout llReward;//打赏layout
    private ImageView ivShareApp;//分享app图标
    private TextView tvShareApp;//分享app文字
    private WechatShareManager mShareManager;//微信分享管理器实例
    private FrameLayout flWholeShare;//背景加画布
    private boolean isAddPic = false;


    static final String[] PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE,  //读取权限
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
            //读取设备信息
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取用户权限
        setPermissions();

        //初始化actionbar
        final ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("画板");

        //初始化分享api
        api = WXAPIFactory.createWXAPI(this, "wx2fa8f2142fe52feb", true);
        api.registerApp("wx2fa8f2142fe52feb");
        mShareManager = WechatShareManager.getInstance(MainActivity.this);
        mTencent = Tencent.createInstance("1106083777", getApplicationContext());


        //初始化组件
        iv = (ImageView) findViewById(R.id.imageView);
        lineView = (LineView) findViewById(R.id.shapeView);
        circleView = (CircleView) findViewById(R.id.circleView);
        rectView = (RectView) findViewById(R.id.rectView);

        tvPen = (TextView) findViewById(R.id.tv_pen);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        ivUp = (ImageView) findViewById(R.id.iv_up);
        //ivTab = (ImageView) findViewById(R.id.iv_tab);
        etText = (EditText) findViewById(R.id.et_text);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        //llToolbar = (LinearLayout) findViewById(R.id.ll_toobar);
        llText = (LinearLayout) findViewById(R.id.ll_text);
        llDel = (LinearLayout) findViewById(R.id.ll_del);
        fml = (FrameLayout) findViewById(R.id.fl_whole_share);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        llReward = (LinearLayout) findViewById(R.id.ll_reward);
        ivShareApp = (ImageView) findViewById(R.id.im_share_app);
        tvShareApp = (TextView) findViewById(R.id.tv_share_app);
        lvDrawer = (ListView) findViewById(R.id.lv_drawer);
        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
        flWholeShare = (FrameLayout) findViewById(R.id.fl_whole_share);


        //数据初始化
        seekBar.setVisibility(View.INVISIBLE);
        tvPen.setVisibility(View.INVISIBLE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        dtvs = new ArrayList<>();
        initDrawerLv();

        //检查更新
        AppManager app = new AppManager(MainActivity.this);
        app.checkUpdate();
        System.out.println("check instant run");


        //画板初始化
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(p_color);
        paint.setStrokeJoin(Paint.Join.ROUND);
        savePath = new ArrayList<DrawPath>();
        if (baseBitmap != null && baseBitmap.isRecycled() == false) //如果没有回收
            baseBitmap.recycle();

        // 创建一个可以被修改的bitmap
        WindowManager wm = this.getWindowManager();
        baseBitmap = Bitmap.createBitmap(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight() - 220,
                Bitmap.Config.ARGB_8888);
        canvas = new Canvas(baseBitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        if(!isAddPic) {
            canvas.drawColor(Color.WHITE);
        }


        //辅助线蒙板设置不可见
        lineView.setVisibility(View.GONE);
        circleView.setVisibility(View.GONE);
        rectView.setVisibility(View.GONE);

        /**
         * 分享app操作监听
         */
        ivShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareApp();
            }
        });
        tvShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareApp();
            }
        });


        /**
         * 打赏布局监听
         */
        llReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!AlipayZeroSdk.hasInstalledAlipayClient(MainActivity.this)){
                    Toast.makeText(MainActivity.this,"请安装支付宝后在打赏开发者吧～",Toast.LENGTH_SHORT).show();
                }else {
                    AlipayZeroSdk.startAlipayClient(MainActivity.this, getResources().getString(R.string.alipay));
                    new zhangphil.iosdialog.widget.AlertDialog(MainActivity.this)
                            .builder()
                            .setTitle("感谢！")
                            .setMsg("感谢您对开发者的慷慨打赏！有了您的鼓舞，「轻量画板」一定会越做越好！")
                            .setNegativeButton("加油！", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                }
            }
        });

        /**
         * 抽屉监听事件
         */
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_open);
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
                super.onDrawerClosed(drawerView);
            }
        });


        /**
         * 直线辅助线蒙板获取坐标回调
         */
        lineView.setCoordListener(new LineView.CoordListener() {

            @Override
            public void onCoord(float startX, float startY, float endX, float endY) {
                canvas.drawLine(startX, startY, endX, endY, paint);
                iv.setImageBitmap(baseBitmap);

                dp = new DrawPath();
                Path tempPath = new Path();
                tempPath.moveTo(startX, startY);
                tempPath.lineTo(endX, endY);
                dp.dpath = tempPath;

                dp.dpaint.setStyle(paint.getStyle());
                dp.dpaint.setAntiAlias(true);
                dp.dpaint.setDither(true);
                dp.dpaint.setStrokeWidth(paint.getStrokeWidth());
                dp.dpaint.setStrokeCap(Paint.Cap.ROUND);
                dp.dpaint.setColor(paint.getColor());

                savePath.add(dp);
            }
        });

        /**
         * 圆形辅助线蒙板获取坐标回调
         */
        circleView.setCoordListener(new CircleView.CoordListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCoord(float startX, float startY, float endX, float endY) {
                canvas.drawOval(startX, startY, endX, endY, paint);
                iv.setImageBitmap(baseBitmap);
                dp = new DrawPath();
                Path tempPath = new Path();
                tempPath.addOval(startX, startY, endX, endY, Path.Direction.CCW);
                dp.dpath = tempPath;
                dp.dpaint.setStyle(paint.getStyle());
                dp.dpaint.setAntiAlias(true);
                dp.dpaint.setDither(true);
                dp.dpaint.setStrokeWidth(paint.getStrokeWidth());
                dp.dpaint.setStrokeCap(Paint.Cap.ROUND);
                dp.dpaint.setColor(paint.getColor());
                savePath.add(dp);
            }
        });
        /**
         * 矩形辅助线蒙板获取坐标回调
         */
        rectView.setCoordListener(new RectView.CoordListener() {
            @Override
            public void onCoord(float startX, float startY, float endX, float endY) {
                canvas.drawRect(startX, startY, endX, endY, paint);
                iv.setImageBitmap(baseBitmap);
                dp = new DrawPath();
                Path tempPath = new Path();
                tempPath.addRect(startX, startY, endX, endY, Path.Direction.CCW);
                dp.dpath = tempPath;
                dp.dpaint.setStyle(paint.getStyle());
                dp.dpaint.setAntiAlias(true);
                dp.dpaint.setDither(true);
                dp.dpaint.setStrokeWidth(paint.getStrokeWidth());
                dp.dpaint.setStrokeCap(Paint.Cap.ROUND);
                dp.dpaint.setColor(paint.getColor());
                savePath.add(dp);
            }
        });
//        /**
//         * 右上角文稿功能跳转
//         */
//        ivTab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, Board.class));
//                //MainActivity.this.finish();
//            }
//        });

        /**
         * 右下角功能按钮事件
         */
        ivUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ActionSheetDialog(MainActivity.this)
                        .builder()
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem("发送到QQ好友/空间",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        saveBitmap(convertViewToBitmap(flWholeShare), "/temp.png");

                                        shareToQQ();
                                        //shareImg();
                                    }
                                })
                        .addSheetItem("发送给微信好友",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {

                                    @Override
                                    public void onClick(int which) {
                                        if(isAddPic){
                                            Bitmap bitmap = convertViewToBitmap(flWholeShare);
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            int quality = 8;
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                                            byte[] bytes = baos.toByteArray();
                                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            Log.i("wechat", "压缩后图片的大小" + (bm.getByteCount() / 1024 / 1024)
                                                    + "M宽度为" + bm.getWidth() + "高度为" + bm.getHeight()
                                                    + "bytes.length= " + (bytes.length / 1024) + "KB"
                                                    + "quality=" + quality);
                                            share2weixin(0, bm);
                                        }else {
                                            share2weixin(0, convertViewToBitmap(flWholeShare));
                                        }




                                    }
                                })
                        .addSheetItem("分享到微信朋友圈",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {

                                    @Override
                                    public void onClick(int which) {
                                        if(isAddPic){
                                            Bitmap bitmap = convertViewToBitmap(flWholeShare);
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            int quality = 8;
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                                            byte[] bytes = baos.toByteArray();
                                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            Log.i("wechat", "压缩后图片的大小" + (bm.getByteCount() / 1024 / 1024)
                                                    + "M宽度为" + bm.getWidth() + "高度为" + bm.getHeight()
                                                    + "bytes.length= " + (bytes.length / 1024) + "KB"
                                                    + "quality=" + quality);
                                            share2weixin(1, bm);
                                        }else {
                                            share2weixin(1, convertViewToBitmap(flWholeShare));
                                        }


                                    }
                                })
                        .addSheetItem("保存到手机",
                                ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {

                                    @Override
                                    public void onClick(int which) {

                                    saveBitmap(convertViewToBitmap(flWholeShare), "/lightdraw_" + getRandomString(8) + ".png");


                                        Toast.makeText(MainActivity.this, "图片已存储至sd卡：／DrawBoard/ 下", Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
            }
        });


        /**
         * 文字取消按钮事件
         */
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager manager = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                etText.setText("");
                llText.setVisibility(View.GONE);
            }
        });

        /**
         * 文字确定按钮事件
         */
        tvConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                Toast.makeText(MainActivity.this, "拖动文字以移动", Toast.LENGTH_SHORT).show();
                final DragTextView dtv = new DragTextView(MainActivity.this);
                dtv.setText(etText.getText().toString().trim());
                dtv.setTextColor(p_color);
                final float textSize = p_strokeWidth * 3;
                dtv.setTextSize(textSize);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 0);
                lp.gravity = Gravity.CENTER;
                dtv.setLayoutParams(lp);
                /**
                 * 文字拖动回调监听
                 */
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                float SCREEN_WIDTH = dm.widthPixels;
                final float SCREEN_HEIGHT = dm.heightPixels;
                final float dtvSize = dtv.getTextSize();
                dtv.setViewSelectClickListener(new DragTextView.ViewSelectClickListener() {

                    @Override
                    public void onViewTouchDown(View view) {

                        llDel.setVisibility(View.VISIBLE);
                        dtv.setTextSize(textSize - 8);
                    }

                    @Override
                    public void onViewMove(View view, float x, float y) {

                        if (y > SCREEN_HEIGHT - (llDel.getBottom() - llDel.getTop())) {

                            llDel.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));

                        } else {
                            llDel.setBackgroundColor(Color.parseColor("#e0e0e0"));
                        }
                    }

                    @Override
                    public void onViewTouchUp(final View tview, float x, float y) {
                        if (y > SCREEN_HEIGHT - (llDel.getBottom() - llDel.getTop())) {
                            fml.removeView(tview);
                            dtvs.remove(tview);
                        }
                        llDel.setBackgroundColor(Color.parseColor("#e0e0e0"));
                        llDel.setVisibility(View.GONE);
                        dtv.setTextSize(textSize);
                    }
                });
                fml.addView(dtv);
                dtvs.add(dtv);
                InputMethodManager manager = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                etText.setText("");
                llText.setVisibility(View.GONE);
            }
        });


        /**
         * 画板手指触摸监听
         */
        iv.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // 手指第一次接触屏幕

                        seekBar.setVisibility(View.INVISIBLE);
                        tvPen.setVisibility(View.INVISIBLE);
                        path = new Path();
                        path.moveTo(x, y);
                        preX = x;
                        preY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:// 手指在屏幕上滑动
                        if (Math.abs(preX - x) < 3 && Math.abs(preY - y) < 3) {
                        } else {
                            path.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);
                            preX = x ;
                            preY = y ;
                        }
                        canvas.drawPath(path, paint);
                        iv.setImageBitmap(baseBitmap);

                        break;
                    case MotionEvent.ACTION_UP: // 手指离开屏幕
                        dp = new DrawPath();
                        dp.dpath = path;
                        dp.dpaint.setStyle(paint.getStyle());
                        dp.dpaint.setAntiAlias(true);
                        dp.dpaint.setDither(true);
                        dp.dpaint.setStrokeWidth(paint.getStrokeWidth());
                        dp.dpaint.setStrokeCap(Paint.Cap.ROUND);
                        dp.dpaint.setColor(paint.getColor());
                        savePath.add(dp);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });


        /**
         * 拖动条更改画笔宽度
         */
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                p_strokeWidth = i;
                paint.setStrokeWidth(i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 顶部菜单操作
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //画笔图标
            case R.id.action_pen:
                lineView.setVisibility(View.GONE);
                circleView.setVisibility(View.GONE);
                rectView.setVisibility(View.GONE);
                paint.setColor(p_color);
                paint.setStrokeWidth(p_strokeWidth);
                seekBar.setVisibility(View.VISIBLE);
                tvPen.setVisibility(View.VISIBLE);
                break;
            //撤销图标
            case R.id.action_undo0:
                if (savePath.size() > 0) {
                    savePath.remove(savePath.size() - 1);
                    iv.setImageBitmap(null);
                    if (baseBitmap != null && baseBitmap.isRecycled() == false) //如果没有回收
                        baseBitmap.recycle();

                    // 创建一个可以被修改的bitmap
                    WindowManager wm = this.getWindowManager();
                    baseBitmap = Bitmap.createBitmap(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight() - 220,
                            Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(baseBitmap);
                    if(!isAddPic) {
                        canvas.drawColor(Color.WHITE);
                    }

                    for (DrawPath dp : savePath) {

                        canvas.drawPath(dp.dpath, dp.dpaint);
                        iv.setImageBitmap(baseBitmap);
                    }
                }
                break;
            //删除图标
            case R.id.action_delete:
                iv.setImageBitmap(null);
                ivPhoto.setImageBitmap(null);
                for (DragTextView dt : dtvs) {
                    fml.removeView(dt);
                }
                dtvs.clear();
                savePath.clear();
                if (baseBitmap != null && baseBitmap.isRecycled() == false) //如果没有回收
                    baseBitmap.recycle();

                // 创建一个可以被修改的bitmap
                WindowManager wm = this.getWindowManager();
                baseBitmap = Bitmap.createBitmap(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight() - 220,
                        Bitmap.Config.ARGB_8888);
                canvas = new Canvas(baseBitmap);
                isAddPic = false;
                if(!isAddPic) {
                    canvas.drawColor(Color.WHITE);
                }


                break;
            //橡皮图标
            case R.id.action_rube:
                lineView.setVisibility(View.GONE);
                circleView.setVisibility(View.GONE);
                rectView.setVisibility(View.GONE);
                paint.setStrokeWidth(40);
                paint.setColor(Color.WHITE);
                break;
            //颜色图标
            case R.id.action_color:
                Toast.makeText(MainActivity.this, "取色完毕后，点击中间圆形区域完成取色！", Toast.LENGTH_LONG).show();
                dialog = new ColorPickerDialog(MainActivity.this, "颜色选择",
                        new ColorPickerDialog.OnColorChangedListener() {

                            @Override
                            public void colorChanged(int color) {
                                p_color = color;
                                paint.setColor(p_color);
                            }
                        });
                dialog.show();
                break;
            //添加图形
            case R.id.action_shape:
                ListView lv = new ListView(MainActivity.this);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, new String[]{"直线", "圆形", "矩形"});
                lv.setAdapter(adapter);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("选择形状");
                builder.setCancelable(true);
                builder.setView(lv);
                final Dialog dialog = builder.show();
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i) {
                            case 0://直线
                                paint.setColor(p_color);
                                paint.setStrokeWidth(p_strokeWidth);
                                lineView.setVisibility(View.VISIBLE);
                                circleView.setVisibility(View.GONE);
                                rectView.setVisibility(View.GONE);
                                dialog.dismiss();
                                break;
                            case 1://圆形
                                paint.setColor(p_color);
                                paint.setStrokeWidth(p_strokeWidth);
                                lineView.setVisibility(View.GONE);
                                circleView.setVisibility(View.VISIBLE);
                                rectView.setVisibility(View.GONE);
                                dialog.dismiss();
                                break;
                            case 2://矩形
                                paint.setColor(p_color);
                                paint.setStrokeWidth(p_strokeWidth);
                                lineView.setVisibility(View.GONE);
                                circleView.setVisibility(View.GONE);
                                rectView.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                break;
            //添加文字
            case R.id.action_text:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lineView.setVisibility(View.GONE);
                        circleView.setVisibility(View.GONE);
                        rectView.setVisibility(View.GONE);
                        llText.setVisibility(View.VISIBLE);
                        etText.setFocusable(true);
                        etText.setFocusableInTouchMode(true);
                        etText.requestFocus();
                        InputMethodManager manager = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
                        manager.showSoftInput(etText, 0);

                    }
                }, 100);
                break;
            //编辑照片
            case R.id.action_photo:
                choosePhoto();
                break;
            //跳转到手写文稿
            case R.id.action_board:
                startActivity(new Intent(MainActivity.this, Board.class));
                break;
            //左上角图标
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);

                }else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                    //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 分享到qq
     */
    private void shareToQQ() {
        params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, "轻量画板");// 标题
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "轻量画板，轻松表达！");// 摘要
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, "http://sj.qq.com/myapp/detail.htm?apkName=lzc.com.drawboard");// 内容地址
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, Environment.getExternalStorageDirectory().getPath() + "/DrawBoard/temp.png");// 网络图片地址　　params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "应用名称");// 应用名称
        params.putString(QQShare.SHARE_TO_QQ_EXT_INT, "");
        // 分享操作要在主线程中完成
        ThreadManager.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                mTencent.shareToQQ(MainActivity.this, params, new IUiListener() {
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

    /**
     * 分享app
     */
    private void shareApp(){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent.putExtra(Intent.EXTRA_TEXT, "我发现了一款轻量级的画板应用，功能多多哦，快来试试吧！\n[下载地址]：http://app.xiaomi.com/detail/549122");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    /**
     * 分享到微信
     */
    private void share2weixin(int shareType, Bitmap bitmap) {
        WXImageObject imgObj = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        Bitmap thumbBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
        //bitmap.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBitmap, true);  //设置缩略图
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        //req.transaction = buildTransaction("imgshareappdata");
        req.message = msg;
        req.scene = shareType;
        api.sendReq(req);
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

    /**
     * 保存图片到本地
     */
    public void saveBitmap(Bitmap bm, String picName) {
        File file;
        file = Environment.getExternalStorageDirectory();

        file = new File(file.getPath() + "/DrawBoard/");
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
     * 初始化drawerlayout的listview
     */
    private void initDrawerLv(){
        String[] from = new String[]{"img","text"};
        int[] to = new int[]{R.id.iv_item_icon,R.id.tv_item_opption};
        List<Map<String,Object>> list = new ArrayList<>();

        Map<String,Object> map7 = new HashMap<>();
        map7.put("img",R.mipmap.ic_advise);
        map7.put("text","意见／建议反馈");

        Map<String,Object> map2 = new HashMap<>();
        map2.put("img",R.mipmap.ic_update);
        map2.put("text","检查软件更新");

        Map<String,Object> map3 = new HashMap<>();
        map3.put("img",R.mipmap.ic_info);
        map3.put("text","关于「轻量画板」");

        Map<String,Object> map4 = new HashMap<>();
        map4.put("img",R.mipmap.ic_board_white);
        map4.put("text","手写文稿");
//
        Map<String,Object> map5 = new HashMap<>();
        map5.put("img",R.mipmap.ic_help);
        map5.put("text","常见问题");
//
        Map<String,Object> map6 = new HashMap<>();
        map6.put("img",R.mipmap.ic_question_answer);
        map6.put("text","反馈回复");
//
        Map<String,Object> map1 = new HashMap<>();
        map1.put("img",R.mipmap.ic_layer);
        map1.put("text","高级画板");
//
//        Map<String,Object> map8 = new HashMap<>();
//        map8.put("img",R.mipmap.ic_info);
//        map8.put("text","关于「轻量画板」");
        list.add(map1); //手写文稿
        list.add(map2); //常见问题
        list.add(map3); //意见反馈
        list.add(map4); //功能性反馈回复
        list.add(map5); //检查软件更新
        list.add(map6); //关于
        list.add(map7);
//        list.add(map5);
//        list.add(map6);
//        list.add(map7);
//        list.add(map8);



        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,list,R.layout.drawer_item,from,to);
        lvDrawer.setAdapter(adapter);
        lvDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                switch (pos){
                    case 3://手写文稿
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        startActivity(new Intent(MainActivity.this, Board.class));
                        break;
                    case 4://常见问题
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        startActivity(new Intent(MainActivity.this, HelpActivity.class));
                        break;
                    case 6://意见反馈
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        startActivity(new Intent(MainActivity.this, SendReport.class));
                        break;
                    case 5://功能性反馈回复
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        startActivity(new Intent(MainActivity.this, FunctionActivity.class));
                        break;
                    case 1://检查软件更新
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        AppManager app = new AppManager(MainActivity.this);
                        app.checkUpdate();
                        break;
                    case 2://关于
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        new AlertDialog.Builder(MainActivity.this).setTitle("关于")
                                .setIcon(R.drawable.ic_info_black_24dp)
                                .setMessage(R.string.app_version_message)
                                .setNegativeButton("关闭", null).create().show();
                        break;
                    case 0://高级画板
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        startActivity(new Intent(MainActivity.this, LayerBoardActivity.class));
                        break;
                }
            }
        });
    }

    /**
     * 获取随机字符串
     */
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
     * 申请权限
     */
    private void setPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Android 6.0申请权限
            ActivityCompat.requestPermissions(this, PERMISSION, 1);

        } else {

        }
    }

    /**
     * Overflow菜单选项显示icon
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
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
    /**
     * 从相册选取图片
     */
    private void choosePhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * activity回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null)
            return;
        switch (requestCode){
            case REQUEST_CODE_PICK_IMAGE:
                Uri uri = data.getData();
                iv.setImageBitmap(null);
                //ivPhoto.setImageBitmap(null);
                for (DragTextView dt : dtvs) {
                    fml.removeView(dt);
                }
                dtvs.clear();
                savePath.clear();
                if (baseBitmap != null && baseBitmap.isRecycled() == false) //如果没有回收
                    baseBitmap.recycle();

                // 创建一个可以被修改的bitmap
                WindowManager wm = this.getWindowManager();
                baseBitmap = Bitmap.createBitmap(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight() - 220,
                        Bitmap.Config.ARGB_8888);
                canvas = new Canvas(baseBitmap);
                isAddPic = true;
                try {
                    setImage(uri);
                }catch (Exception e){
                    try {
                        Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri)).copy(Bitmap.Config.ARGB_8888,true);
                        ivPhoto.setImageBitmap(bit);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }


                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 设置背景图(从相册)
     * @param mImageCaptureUri
     */

    private void setImage(Uri mImageCaptureUri) {

        // 不管是拍照还是选择图片每张图片都有在数据中存储也存储有对应旋转角度orientation值
        // 所以我们在取出图片是把角度值取出以便能正确的显示图片,没有旋转时的效果观看

        ContentResolver cr = this.getContentResolver();
        Cursor cursor = cr.query(mImageCaptureUri, null, null, null, null);// 根据Uri从数据库中找
        if (cursor != null) {
            cursor.moveToFirst();// 把游标移动到首位，因为这里的Uri是包含ID的所以是唯一的不需要循环找指向第一个就是了
            String filePath = cursor.getString(cursor.getColumnIndex("_data"));// 获取图片路
            String orientation = cursor.getString(cursor
                    .getColumnIndex("orientation"));// 获取旋转的角度
            cursor.close();
            if (filePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);//根据Path读取资源图片
                int angle = 0;
                if (orientation != null && !"".equals(orientation)) {
                    angle = Integer.parseInt(orientation);
                }
                if (angle != 0) {
                    // 下面的方法主要作用是把图片转一个角度，也可以放大缩小等
                    Matrix m = new Matrix();
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    m.setRotate(angle); // 旋转angle度
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                            m, true);// 从新生成图片

                }
                ivPhoto.setImageBitmap(bitmap);
            }
        }
    }
    /**
     * layout转位图
     */
    public static Bitmap convertViewToBitmap(View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.setDrawingCacheEnabled(true);
        view.draw(canvas);

        return bitmap;

    }
    /**
     * 分享图片（系统接口）//微信不可用
     */
    private void shareImg(){
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        ComponentName comp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.tools.ShareToTimeLineUI");
//        shareIntent.setComponent(comp);
//        Uri imageUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/DrawBoard/temp.png");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hello");
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share");
//        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        shareIntent.setType("image/*");
//        startActivity(shareIntent);

        Intent intent = new Intent();
        Uri imageUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/DrawBoard/temp.png");
        ComponentName comp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);//uri为你要分享的图片的uri
        startActivity(intent);
    }

    /**
     * 覆写返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

