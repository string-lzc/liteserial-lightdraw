package lzc.com.drawboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lzc.com.drawboard.util.StringRequestUTF8;

public class FunctionActivity extends AppCompatActivity {
    private ListView lv;
    private SwipeRefreshLayout srl;
    private List<Map<String,String>> mapList;
    private String url = "http://120.27.109.221/android/lightdraw_server/light_draw_server_api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function);

        lv = (ListView) findViewById(R.id.lv1);
        mapList = new ArrayList<>();
        srl = (SwipeRefreshLayout) findViewById(R.id.sl1);
        srl.setColorSchemeColors(Color.RED);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mapList.clear();
                requestData();
                srl.setRefreshing(false);
            }

        });
       // FunctionAdapter adapter = new FunctionAdapter(FunctionActivity.this,mapList);
       // lv.setAdapter(adapter);
        //setContentView(R.layout.activity_function);
        //初始化actionbar
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("反馈回复");

        requestData();
        
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                FunctionActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void requestData(){
        RequestQueue rq = Volley.newRequestQueue(FunctionActivity.this);

        StringRequestUTF8 sq = new StringRequestUTF8(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("response:"+response);

                System.out.println("respond："+response);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for(int i = 0; i<jsonArray.length(); i++){
                        JSONObject jb = jsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        int state = Integer.parseInt(jb.get("state").toString());
                        switch (state){
                            case 1:
                                map.put("state","功能开发中");
                                break;
                            case 2:
                                map.put("state","功能已加入");
                                break;
                            case 3:
                                map.put("state","建议被婉拒");
                                break;
                            case 4:
                                map.put("state","反馈问题回复");
                                break;
                        }
                        //map.put("state",jb.get("state").toString());
                        map.put("function",jb.get("sug").toString());
                        map.put("remark",jb.get("respond").toString());
                        mapList.add(map);
                    }

                    FunctionAdapter adapter = new FunctionAdapter(FunctionActivity.this,mapList);
                    lv.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //String[] from = new String[]{"content","contact","date"};
                //int[] to = new int[]{R.id.tv_item_content,R.id.tv_item_contact,R.id.tv_item_date};
//                FeedBackAdapter adapter = new FeedBackAdapter(LightDraw.this, mapList, new CallBack() {
//                    @Override
//                    public void getData(int id, int type, int state, String respond) {
//                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//
//
//                        commitData(id,type,state,respond);
//                        System.out.println(id+"/"+type+"/"+state+"/"+respond);
//                    }
//                });
//
//                // SimpleAdapter adapter = new SimpleAdapter(LightDraw.this,mapList,R.layout.item,from,to);
//                lv.setAdapter(adapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                new AlertDialog.Builder(FunctionActivity.this)
                        .setTitle("网络异常")
                        .setMessage("请检查网络状态后重新进入查看！")
                        .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FunctionActivity.this.finish();
                                startActivity(new Intent(FunctionActivity.this,FunctionActivity.class));
                            }
                        }).setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FunctionActivity.this.finish();
                    }
                }).show();

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> paramMap = new HashMap<>();
                paramMap.put("action","function");
                return paramMap;
            }
        };
        rq.add(sq);

    }
}
