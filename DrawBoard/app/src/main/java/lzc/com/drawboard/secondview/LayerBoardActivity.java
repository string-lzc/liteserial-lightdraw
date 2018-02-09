package lzc.com.drawboard.secondview;

import android.animation.ObjectAnimator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.reflect.Method;

import lzc.com.drawboard.FunctionActivity;
import lzc.com.drawboard.R;

public class LayerBoardActivity extends AppCompatActivity {
    private LinearLayout llLayerCtrl;
    private LinearLayout llToolbar;
    private boolean isToolbarOpen = false;
    private boolean isLayerCtrlOpen = false;
    private ImageView ivLayerClose;
    private ImageView ivLayerAdd;
    private ImageView ivToolbarClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layer_board);
        final ActionBar actionBar = this.getSupportActionBar();

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.mipmap.);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        llToolbar = (LinearLayout) findViewById(R.id.ll_toolbar);
        llLayerCtrl = (LinearLayout) findViewById(R.id.ll_layer_ctrl);
        ivLayerAdd = (ImageView) findViewById(R.id.iv_layer_add);
        ivLayerClose = (ImageView) findViewById(R.id.iv_layer_close);
        ivToolbarClose = (ImageView) findViewById(R.id.iv_toobar_close);

        ivLayerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLayerCtrlOpen){
                    closeLayerCtrl();
                }
            }
        });

        ivToolbarClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isToolbarOpen){
                    closeToolbar();
                }
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu4, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                LayerBoardActivity.this.finish();
                break;
            case R.id.action_tool:
                if(!isToolbarOpen) {
                    openToolbar();
                }else {
                    closeToolbar();
                }
                break;
            case R.id.action_layer:
                if(!isLayerCtrlOpen) {
                   openLayerCtrl();
                }else {
                    closeLayerCtrl();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
    public void openToolbar(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(llToolbar, "translationX", 0f, 230f);
        animator.setDuration(300);
        animator.start();
        isToolbarOpen = true;
    }
    public void closeToolbar(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(llToolbar, "translationX", 230f, 0f);
        animator.setDuration(300);
        animator.start();
        isToolbarOpen = false;
    }
    public void openLayerCtrl(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(llLayerCtrl, "translationX", 0f, -300f);
        animator.setDuration(300);
        animator.start();
        isLayerCtrlOpen = true;
    }
    public void closeLayerCtrl(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(llLayerCtrl, "translationX", -300f, 0f);
        animator.setDuration(300);
        animator.start();
        isLayerCtrlOpen = false;
    }
}
