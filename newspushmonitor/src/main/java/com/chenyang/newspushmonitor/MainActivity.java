package com.chenyang.newspushmonitor;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;

import com.chenyang.newspushmonitor.okHttp.ApiService;
import com.chenyang.newspushmonitor.ui.NewsInfoAdapter;
import com.chenyang.newspushmonitor.ui.NewsInfoItem;
import com.chenyang.newspushmonitor.util.SPUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity
        implements AccessibilityManager.AccessibilityStateChangeListener {
    private static final String TAG = "MainActivity";

    private AccessibilityManager accessibilityManager;
    private Button mServiceControlBtn;
    private Button btn_setting;
    private RecyclerView mNewsInfoRecycleView;
    private NewsInfoAdapter mAdapter;
    private Timer timer;
    private TimerTask task;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SPUtils.put(MainActivity.this, "pollingTime", "60");

//        SPUtils.put(getApplicationContext(),"time","");
//        SPUtils.put(getApplicationContext(),"title","");
//        SPUtils.put(getApplicationContext(),"text","");
//        SPUtils.put(getApplicationContext(),"packageName","");

        initView();
        initAccessibilityService();
        checkPermission();
        handleIntent(getIntent());
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        updateMonitorStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMonitorStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.setting) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.service_ctl_btn:
                    Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(accessibleIntent);
                    break;
                case R.id.btn_setting:
                    Intent intent = new Intent(MainActivity.this, AppSettingActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    private void initView() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mNewsInfoRecycleView = (RecyclerView) findViewById(R.id.newsinfo_recycler_view);
        mNewsInfoRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mNewsInfoRecycleView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new NewsInfoAdapter();
        mNewsInfoRecycleView.setAdapter(mAdapter);

        mServiceControlBtn = (Button) findViewById(R.id.service_ctl_btn);
        mServiceControlBtn.setOnClickListener(mOnClickListener);

        btn_setting = (Button) findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(mOnClickListener);
    }

    private void initAccessibilityService() {
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);
    }

    private void handleIntent(Intent intent) {
        String newsInfo = intent.getStringExtra("newsInfo");
        String packageName = intent.getStringExtra("packageName");
        if(count==4){
            count=0;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null) {
                task.cancel();
                task = null;
            }
            return;
        }
        if (!TextUtils.isEmpty(packageName)) {
            String time = (String) SPUtils.get(MainActivity.this, "time", "");
            if(NewsInfoAdapter.mNewsInfoList.size()>0){
                if(!time.equals(NewsInfoAdapter.mNewsInfoList.get(NewsInfoAdapter.mNewsInfoList.size()-1))){
                    count=0;
                }
            }

            if(NewsInfoAdapter.mNewsInfoList.size()>0){
                if(NewsInfoAdapter.mNewsInfoList.get(NewsInfoAdapter.mNewsInfoList.size()-1).equals(time)){
                    return;
                }else{
                    mAdapter.addNewsInfoItem(new NewsInfoItem(packageName, newsInfo, "",time));
                }
            }else{
                mAdapter.addNewsInfoItem(new NewsInfoItem(packageName, newsInfo, "",time));
            }

//            request();
        }

    }

    private void updateMonitorStatus() {
        String statusStr;
        if (isServiceEnabled()) {
            statusStr = getString(R.string.close_service);
        } else {
            statusStr = getString(R.string.start_service);
        }
        mServiceControlBtn.setText(statusStr);
    }

    private boolean isServiceEnabled() {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.PushMonitorAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            "android.permission.GET_INTENT_SENDER_INTENT"}, 0);
        }
    }

    private void request() {
        String pollingTime = (String) SPUtils.get(MainActivity.this, "pollingTime", "");//轮训时间
        final int pollingTimeInt = Integer.parseInt(pollingTime);
//        try{
        String deviceNumber = (String) SPUtils.get(MainActivity.this, "deviceNumber", "");
        String url = (String) SPUtils.get(MainActivity.this, "url", "");
        String businessName = (String) SPUtils.get(MainActivity.this, "businessName", "");
        String time = (String) SPUtils.get(MainActivity.this, "time", "");
        String title = (String) SPUtils.get(getApplicationContext(), "title", "");
        String text = (String) SPUtils.get(getApplicationContext(), "text", "");
        String packageName = (String) SPUtils.get(getApplicationContext(), "packageName", "");


        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(text)) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "暂未设置接口地址", Toast.LENGTH_SHORT).show();
            return;
        }
        String money;
        if (text.contains("微信")) {
            money = text.substring(8);
        } else {
            money = text.substring(9);
        }
        ApiService instance = ApiService.Instance();
        HashMap<String, String> map = new HashMap<>();
        map.put("userids", deviceNumber);
        map.put("mark", businessName);
        map.put("money", money);
        map.put("contentTitle", title);
        map.put("contentText", text);
        map.put("time", time);

        OkGo.<String>post(url)
                .tag(this)
                .params(map)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String msg=response.body();
                        if (msg.equals("success")) {
                            SPUtils.put(getApplicationContext(), "title", "");
                            SPUtils.put(getApplicationContext(), "text", "");
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.updateMsg("数据上传成功！");
                                }
                            });
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            if (task != null) {
                                task.cancel();
                                task = null;
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "网络请求异常", Toast.LENGTH_SHORT).show();
                            if(!mAdapter.getMsg().contains("数据上传失败")){
                                mAdapter.updateMsg("数据上传失败！\n");
                            }

                            if (timer != null) {
                                return;
                            }
                            if (task != null) {
                                return;
                            }
                            timer = new Timer();
                            task = new TimerTask() {
                                @Override
                                public void run() {
                                    count++;
                                    if (count == 4) {
                                        count = 0;

                                        if (timer != null) {
                                            timer.cancel();
                                            timer = null;
                                        }
                                        if (task != null) {
                                            task.cancel();
                                            task = null;
                                        }
                                        return;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.updateMsg("第" + count + "次轮询 数据上传失败(时间:" + getDate() + ")\n");
                                        }
                                    });
                                    request();
                                }
                            };
                            timer.schedule(task, pollingTimeInt * 1000, pollingTimeInt * 1000);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Toast.makeText(MainActivity.this, "网络请求异常", Toast.LENGTH_SHORT).show();
                        if(!mAdapter.getMsg().contains("数据上传失败")){
                            mAdapter.updateMsg("数据上传失败！\n");
                        }

                        if (timer != null) {
                            return;
                        }
                        if (task != null) {
                            return;
                        }
                        timer = new Timer();
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                count++;
                                if (count == 4) {
                                    count = 0;
                                    if (timer != null) {
                                        timer.cancel();
                                        timer = null;
                                    }
                                    if (task != null) {
                                        task.cancel();
                                        task = null;
                                    }
                                    return;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.updateMsg("第" + count + "次轮询 数据上传失败(时间:" + getDate() + ")\n");
                                    }
                                });
                                request();
                            }
                        };
                        timer.schedule(task, pollingTimeInt * 1000, pollingTimeInt * 1000);
                    }
                });
        /*instance.okHttpPost(url, map, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                try {
                    Toast.makeText(MainActivity.this, "网络请求异常", Toast.LENGTH_SHORT).show();
                    mAdapter.updateMsg("数据上传失败！\n");

                    if (timer != null) {
                        return;
                    }
                    if (task != null) {
                        return;
                    }
                    timer = new Timer();
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            count++;
                            if (count == 4) {
                                count = 0;
                                return;
                            }
                            mAdapter.updateMsg("第" + count + "次轮询 数据上传失败(时间:" + getDate() + ")\n");
                            request();
                        }
                    };
                    timer.schedule(task, pollingTimeInt * 1000, pollingTimeInt * 1000);

                } catch (Exception exception) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(String response) {
                try {
                    if (response.equals("success")) {
                        SPUtils.put(getApplicationContext(), "title", "");
                        SPUtils.put(getApplicationContext(), "text", "");
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        mAdapter.updateMsg("数据上传成功！");
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        if (task != null) {
                            task.cancel();
                            task = null;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "网络请求异常", Toast.LENGTH_SHORT).show();
                        mAdapter.updateMsg("数据上传失败！\n");

                        if (timer != null) {
                            return;
                        }
                        if (task != null) {
                            return;
                        }
                        timer = new Timer();
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                count++;
                                if (count == 4) {
                                    count = 0;
                                    return;
                                }
                                mAdapter.updateMsg("第" + count + "次轮询 数据上传失败(时间:" + getDate() + ")\n");
                                request();
                            }
                        };
                        timer.schedule(task, pollingTimeInt * 1000, pollingTimeInt * 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
        /*}catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "网络请求异常", Toast.LENGTH_SHORT).show();
            mAdapter.updateMsg("数据上传失败！\n");

            if (timer != null) {
                return;
            }
            if (task != null) {
                return;
            }
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    count++;
                    if (count == 4) {
                        count = 0;
                        return;
                    }
                    mAdapter.updateMsg("第" + count + "次轮询 数据上传失败(时间:" + getDate() + ")\n");
                    request();
                }
            };
            timer.schedule(task, pollingTimeInt * 1000, pollingTimeInt * 1000);
        }*/
    }

    public String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

}
