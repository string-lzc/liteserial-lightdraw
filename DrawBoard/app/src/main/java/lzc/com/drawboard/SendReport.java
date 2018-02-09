package lzc.com.drawboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import lzc.com.drawboard.util.StringRequestUTF8;
import zhangphil.iosdialog.widget.AlertDialog;

public class SendReport extends AppCompatActivity {
    private EditText etSg,etCon;
    private String sg,con;
    private String url = "http://120.27.109.221/android/lightdraw_server/light_draw_server_api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);
        etSg = (EditText)findViewById(R.id.etSuggestion);
        etCon = (EditText)findViewById(R.id.etContact);

        ActionBar actionBar = this.getSupportActionBar();

        actionBar.setDisplayShowHomeEnabled(true);

        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("意见反馈");
        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sg = etSg.getText().toString();
                con = etCon.getText().toString();
                sendReport(sg,con);
            }
        });
    }

    public void sendReport(final String sug, final String con){
        RequestQueue rq = Volley.newRequestQueue(SendReport.this);

        StringRequestUTF8 sq = new StringRequestUTF8(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                etSg.setText("");
                etCon.setText("");
                new AlertDialog(SendReport.this).builder()
                        .setTitle("提交成功")
                        .setMsg("您的反馈会在【反馈回复】中及时得到回复，请注意查看，感谢您的支持！")
                        .setNegativeButton("返回", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SendReport.this.finish();
                            }
                        })
                        .setPositiveButton("再写一条", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                return;
                            }
                        }).setCancelable(true).show();
                    }







        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                new android.support.v7.app.AlertDialog.Builder(SendReport.this)
                        .setTitle("网络异常")
                        .setMessage("请检查网络状态后重新进入查看！")
                        .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SendReport.this.finish();
                                startActivity(new Intent(SendReport.this,SendReport.class));
                            }
                        }).setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SendReport.this.finish();
                    }
                }).show();

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> paramMap = new HashMap<>();
                paramMap.put("action","feedback");
                paramMap.put("sug",sug);
                paramMap.put("con",con);
                return paramMap;
            }
        };
        rq.add(sq);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
               // startActivity(new Intent(SendReport.this,MainActivity.class));
                SendReport.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
