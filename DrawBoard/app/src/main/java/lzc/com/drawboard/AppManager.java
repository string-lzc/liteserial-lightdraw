package lzc.com.drawboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Objects;

/**
 * Created by lzc on 2017/5/18.
 */

public class AppManager {
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;



    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context context;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private static final String serverIni = "http://120.27.109.221/android/lightdraw_server/version/update.ini";
    private static final String serverApk = "http://120.27.109.221/android/lightdraw_server/version/app-release.apk";
    private Dialog mDownloadDialog;
    private AppInfo info;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        };
    };

    public AppInfo getInfo() {
        return info;
    }

    public AppManager(Context context){
        super();

        this.context = context;
    }

    public int getVersionCode() throws Exception{
        //获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packInfo.versionCode;
    }

    public String getVersionName() throws Exception{
        //获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packInfo.versionName;
    }


    public void checkUpdate(){


        new AsyncTask<String,Void,String>(){
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++"+s);
                String[] app = s.split("#");

                try {
                    if(getVersionCode()<Integer.parseInt(app[0])){

                        showNoticeDialog(app[1],app[2]);
                    }else{
                        Toast.makeText(context, "当前为最新版本", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            @Override
            protected String doInBackground(String... strings) {
                String result2="";
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os,"utf-8");
                    BufferedWriter bfw = new BufferedWriter(osw);
                    //bfw.write("sug="+sg+"&con="+con);
                    bfw.flush();
                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is,"utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null){


                        result2 = line;
                    }
                    br.close();
                    isr.close();
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("getOutPut:",result2);
                return result2;
            }
        }.execute(serverIni);



    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog(String versionname,String instruction)
    {

        // 构造对话框
        String[] updateMsgArray = instruction.split(",");
        String updateMsg = "\n";
        for(String s:updateMsgArray){
            updateMsg+=s+"\n";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("软件更新");
        builder.setMessage("检测到新版本"+versionname+"\n\n【版本特性】"+updateMsg+"\n立即更新吗?");
        // 更新
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        // 稍后更新
        builder.setNegativeButton("稍后更新", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog()
    {
        // 构造软件下载对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("正在更新");
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }

    private void downloadApk()
    {
        // 启动新线程下载软件
        new AppManager.downloadApkThread().start();
    }

    /**
     * 下载文件线程
     *
     * @author coolszy
     *@date 2012-4-26
     *@blog http://blog.92coding.com
     */
    private class downloadApkThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "DrawBoard/";
                    URL url = new URL(serverApk);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists())
                    {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, "轻量画板"+getVersionName()+".apk");
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do
                    {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0)
                        {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    };

    /**
     * 安装APK文件
     */
    private void installApk()
    {
        File apkfile = null;
        try {
            apkfile = new File(mSavePath, "轻量画板"+getVersionName()+".apk");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!apkfile.exists())
        {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        context.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
