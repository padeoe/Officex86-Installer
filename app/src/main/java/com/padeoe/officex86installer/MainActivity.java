package com.padeoe.officex86installer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class MainActivity extends ActionBarActivity {
    private Toolbar toolbar;

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
        Button install_ppt = (Button) findViewById(R.id.install_ppt);
        Button install_word = (Button) findViewById(R.id.install_word);
        Button install_excel = (Button) findViewById(R.id.install_excel);
        install_ppt.setOnClickListener(new ButtonListener(R.raw.ppt_obb, R.raw.ppt, "ppt.apk", "com.microsoft.office.powerpoint", "main.1642013004.com.microsoft.office.powerpoint.obb"));
        install_word.setOnClickListener(new ButtonListener(R.raw.word_obb, R.raw.word, "word.apk", "com.microsoft.office.word", "main.1642013004.com.microsoft.office.word.obb"));
        install_excel.setOnClickListener(new ButtonListener(R.raw.excel_obb, R.raw.excel, "excel.apk", "com.microsoft.office.excel", "main.1642013004.com.microsoft.office.excel.obb"));
        //添加LeanCloud用户统计分析，下面一行代码中的key仅用于测试，发布的apk中使用的不同
        AVOSCloud.initialize(this, "pq3sqjul4anoev3fhxc99736s72jl6w0euuovi0tzfy35src", "i9mnvkzb53btg8nk22bmthraxwsfq71jdbatas5tueaggznj");

    }

    public class ButtonListener implements View.OnClickListener {
        int obbID;
        int apkID;
        String apkName;
        String dataPackageName;
        String obbName;

        ButtonListener(int obbID, int apkID, String apkName, String dataPackageName, String obbName) {
            this.obbID = obbID;
            this.apkID = apkID;
            this.apkName = apkName;
            this.dataPackageName = dataPackageName;
            this.obbName = obbName;
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, getResources().getText(R.string.please_wait), Toast.LENGTH_SHORT).show();
            install(obbID, apkID, apkName, dataPackageName, obbName);
            AVObject Like = new AVObject("Install");
            Like.put("apk", apkName);
            Like.saveInBackground();
        }

    }

    public void install(int obbID, int apkID, String apkName, String dataPackageName, String obbName) {
        String tmpPath = Environment.getExternalStorageDirectory() + "/office_installer/";

        final int apkID2 = apkID;
        final String tmpPath2 = tmpPath;
        final String apkName2 = apkName;
        final int obbID2 = obbID;
        final String obbName2 = obbName;
        final String dataPackageName2 = dataPackageName;

        //使用线程避免读取数据时导致的按钮点击反馈卡顿
        new Thread() {
            @Override
            public void run() {
                System.out.println("Environment.getExternalStorageDirectory():" + Environment.getExternalStorageDirectory());
                //临时文件读取
                saveAs(apkID2, tmpPath2, apkName2);
                //apk安装
                Uri path = Uri.fromFile(new File(tmpPath2 + apkName2));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/vnd.android.package-archive");
                startActivity(intent);
                //数据文件转移
                DataPackageManager.createFolder(Environment.getExternalStorageDirectory() + "/Android/obb/" + dataPackageName2 + "/");
                String dataPath = Environment.getExternalStorageDirectory() + "/Android/obb/" + dataPackageName2 + "/";
                Timer t = new Timer();
                t.start();
                try {
                    copyFileUsingBufferedReader(obbID2, dataPath, obbName2);
                    System.out.println("Copying file  takes " + t.end()
                            + " milliseconds");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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

    public boolean saveAs(int resSoundId, String path, String fileName) {
        byte[] buffer;
        InputStream fIn = getBaseContext().getResources().openRawResource(resSoundId);
        int size;

        try {
            size = fIn.available();
            buffer = new byte[size];
            fIn.read(buffer);
            fIn.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return false;
        }

        boolean exists = (new File(path)).exists();
        if (!exists) {
            new File(path).mkdirs();
        }

        FileOutputStream save;
        try {
            save = new FileOutputStream(path + fileName);
            save.write(buffer);
            save.flush();
            save.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return false;
        }
        return true;

    }

    private void copyFileUsingBufferedReader(int resSoundId, String path, String fileName) throws IOException {
        Log.i("写文件", "即将写数据");
        BufferedInputStream source = new BufferedInputStream(getBaseContext().getResources().openRawResource(resSoundId));
        BufferedOutputStream destination = new BufferedOutputStream(
                new FileOutputStream(new File(path + fileName)));
        int buffersize = 1024;
      /*  EditText buffersizeEdit=(EditText)findViewById(R.id.buffersize);
        buffersize=Integer.valueOf(buffersizeEdit.getText().toString());
        System.out.println("buffersize大小是："+buffersize);*/
        byte[] buffer = new byte[buffersize];
        try {
            int n = 0;
            while (-1 != (n = source.read(buffer))) {
                destination.write(buffer, 0, n);
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
