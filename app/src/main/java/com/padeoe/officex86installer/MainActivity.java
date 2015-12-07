package com.padeoe.officex86installer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;


public class MainActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private ProgressDialog progress;
    Handler updateBarHandler;
    static int writingSize = 0;
    //更新时代码只需要更新以下三个对象信息
    final InstallItem ppt = new InstallItem(44529615, R.raw.ppt_obb, R.raw.ppt,
            "ppt.apk", "main.1663263009.com.microsoft.office.powerpoint.obb", "com.microsoft.office.powerpoint","16.0.6326.1009",R.id.install_ppt,R.id.ppt_version);
    final InstallItem excel = new InstallItem(48549300, R.raw.excel_obb, R.raw.excel,
            "excel.apk", "main.1663263009.com.microsoft.office.excel.obb", "com.microsoft.office.excel","16.0.6326.1009",R.id.install_excel,R.id.excel_version);
    final InstallItem word = new InstallItem(47031102, R.raw.word_obb, R.raw.word,
            "word.apk", "main.1663263009.com.microsoft.office.word.obb", "com.microsoft.office.word","16.0.6326.1009",R.id.install_word,R.id.word_version);

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取toolbar对象，设置为ActionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        this.setSupportActionBar(toolbar);
        if (Utils.isAndroid5()) {
            this.getWindow().setNavigationBarColor(getResources().getColor(R.color.ColorPrimary));
        }
        initInstallItem(word);
        initInstallItem(ppt);
        initInstallItem(excel);

        //添加leancloud用户统计，以下秘钥仅用于测试，发布时不同
        AVOSCloud.initialize(this, "rfdbmj8hpdbo3dwx2unrqmvhfb2y8r6d3xrsaiwwoewr2bc4", "c6n60q7onyffn97vey1jywk3bje590xlntp8ddasdo0hnvcy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initInstallItem(word);
        initInstallItem(ppt);
        initInstallItem(excel);
    }

    private void initInstallItem(InstallItem installItem){
        initButton(installItem);
        initVersionTextview(installItem);
    }
    private void initButton(InstallItem installItem){
        Button install_ppt = (Button) findViewById(installItem.getButtonId());
        if(appInstalledOrNot(installItem.getPackageFolderName())){
            if(isNewestVersion(installItem)){
                install_ppt.setText((String) getResources().getText(R.string.reinstall));
            }
            else{
                install_ppt.setText((String) getResources().getText(R.string.update));
            }
        }
    }
    private void initVersionTextview(InstallItem installItem){
        TextView versionTextView=(TextView)findViewById(installItem.getVersionTextViewId());
        versionTextView.setText(getResources().getText(R.string.version)+installItem.getVersionName());
    }
    public void install(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.install_ppt:
                install(ppt);
                break;
            case R.id.install_excel:
                install(excel);
                break;
            case R.id.install_word:
                install(word);
                break;
        }
    }

    public void install(final InstallItem installItem) {
        if(isRunning(installItem)){
            FragmentManager fm = getSupportFragmentManager();
            new AppRunnningAlert().show(fm, "pptRunning");
        }
        else{
            writingSize = 0;
            progress = new ProgressDialog(MainActivity.this);
            progress.setTitle("写入数据");
            progress.setMessage("写入中");
            progress.setProgressStyle(progress.STYLE_HORIZONTAL);
            progress.setProgress(0);
            progress.setMax(installItem.getObbsize());
            progress.setCancelable(false);
            progress.show();
            updateBarHandler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Here you should write your time consuming task...
                        while (progress.getProgress() <= progress.getMax()) {
                            Thread.sleep(200);
                            updateBarHandler.post(new Runnable() {
                                public void run() {
                                    progress.setProgress(writingSize);
                                }
                            });
                            if (progress.getProgress() == progress.getMax()) {
                                progress.dismiss();
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }).start();

            final String tmpPath = Environment.getExternalStorageDirectory() + "/office_installer/";
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        //拷贝obb文件
                        String obbFolderPath=Environment.getExternalStorageDirectory() + "/Android/obb/" + installItem.getPackageFolderName() + "/";
                        copyFileUsingBufferedReader(installItem.getObbID(),obbFolderPath,installItem.getObbName());
                        //拷贝apk到临时目录
                        copyFileUsingBufferedReader(installItem.getApkID(), tmpPath, installItem.getApkName());
                        //apk安装
                        Uri path = Uri.fromFile(new File(tmpPath + installItem.getApkName()));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(path, "application/vnd.android.package-archive");
                        startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        startIntroAnimation();
        return true;
    }

    private void startIntroAnimation() {

        int actionbarSize = Utils.dpToPx(56);
        toolbar.setTranslationY(-actionbarSize);

        toolbar.animate()
                .translationY(0)
                .setDuration(200)
                .setStartDelay(200);
        CardView cardView = (CardView) findViewById(R.id.card_view_ppt);
        cardView.animate()
                .translationY(0)
                .setDuration(200)
                .setStartDelay(200);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.About) {
            FragmentManager fm = getSupportFragmentManager();
            new AboutDialogFragment().show(fm, "s");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 通过BufferedReader将raw资源拷贝到指定路径
     *
     * @param resSoundId
     * @param path
     * @param fileName
     * @throws IOException
     */
    private void copyFileUsingBufferedReader(int resSoundId, String path, String fileName) throws IOException {
        Log.i("写文件", "即将写数据");
        DataPackageManager.createFolder(path);
        System.out.println(path);
        BufferedInputStream source = new BufferedInputStream(getBaseContext().getResources().openRawResource(resSoundId));
        File file=new File(path+fileName);
        BufferedOutputStream destination = new BufferedOutputStream(new FileOutputStream(file));
        int buffersize = 1024;
        byte[] buffer = new byte[buffersize];
        try {
            int count = 0;
            while ((count = source.read(buffer)) != -1) {
                destination.write(buffer, 0, count);
                writingSize += count;
            }

            destination.flush();
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return progress;
    }
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public boolean isNewestVersion(InstallItem installItem){
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(installItem.getPackageFolderName(), 0);
            String installedVersion = pInfo.versionName;
            if(installedVersion.equals(installItem.getVersionName())){
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isRunning(InstallItem installItem){
        ActivityManager activityManager = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++)
        {
            if(procInfos.get(i).processName.equals(installItem.getPackageFolderName()))
            {
                return true;
            }
        }
        return false;

    }

}

/**
 * 用于实验缓存多大时文件读写速度最快，测量所用时间
 */
class Timer {
    long s;

    public long start() {
        s = System.currentTimeMillis();
        return s;
    }

    public long end() {
        return System.currentTimeMillis() - s;
    }
}



