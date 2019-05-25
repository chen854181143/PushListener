package com.chenyang.newspushmonitor;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chenyang.newspushmonitor.util.SPUtils;


public class AppSettingActivity extends AppCompatActivity {

    private EditText edit_device_number;
    private EditText edit_url;
    private EditText edit_business_name;
    private EditText edit_time;
    private Button btn_commit;
    private Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);
        initView();
        initListener();
    }

    private void initListener() {
        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SPUtils.put(AppSettingActivity.this,"deviceNumber",edit_device_number.getText().toString());
                SPUtils.put(AppSettingActivity.this,"url",edit_url.getText().toString());
                SPUtils.put(AppSettingActivity.this,"businessName",edit_business_name.getText().toString());
                SPUtils.put(AppSettingActivity.this,"pollingTime",edit_time.getText().toString());
                Toast.makeText(AppSettingActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initView() {
        edit_device_number = findViewById(R.id.edit_device_number);
        edit_url = findViewById(R.id.edit_url);
        edit_business_name = findViewById(R.id.edit_business_name);
        edit_time = findViewById(R.id.edit_time);
        btn_commit = findViewById(R.id.btn_commit);
        btn_back = findViewById(R.id.btn_back);
        String deviceNumber= (String) SPUtils.get(AppSettingActivity.this,"deviceNumber","");
        String url= (String) SPUtils.get(AppSettingActivity.this,"url","");
        String businessName= (String) SPUtils.get(AppSettingActivity.this,"businessName","");
        String time= (String) SPUtils.get(AppSettingActivity.this,"pollingTime","");
        edit_device_number.setText(deviceNumber);
        edit_url.setText(url);
        edit_business_name.setText(businessName);
        edit_time.setText(time);
    }
}
