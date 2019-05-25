package com.chenyang.newspushmonitor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.chenyang.newspushmonitor.parser.BaseNotificationParser;
import com.chenyang.newspushmonitor.parser.NotificationParserFactory;
import com.chenyang.newspushmonitor.util.LogWriter;
import com.chenyang.newspushmonitor.util.SPUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by wangmingxing on 17-12-25.
 */

public class PushMonitorAccessibilityService extends AccessibilityService {
    private static final String TAG = "PushMonitorService";

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = getServiceInfo();
        info.packageNames = GlobalConfig.getMonitorApps();
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int type = accessibilityEvent.getEventType();
        if (type != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            return;
        }

        String pkg = accessibilityEvent.getPackageName().toString();
        long time = accessibilityEvent.getEventTime();
        Notification notification = (Notification) accessibilityEvent.getParcelableData();
        List<CharSequence> texts = accessibilityEvent.getText();

        StringBuilder sb = new StringBuilder();
        for (CharSequence text : texts) {
            sb.append(text).append("###");
        }

        LogWriter.i(TAG, "onAccessibilityEvent "
                + "package=" + pkg
                + ",time=" + time
                + ",text=" + sb.toString());

        LogWriter.i(TAG, "Parse notification for " + pkg + " begin!");
        BaseNotificationParser notificationParser = NotificationParserFactory.getNotificationParser(pkg);
        BaseNotificationParser.NewsInfo newsInfo = notificationParser.parse(notification);
        LogWriter.i(TAG, "when:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newsInfo.when));
        LogWriter.i(TAG, "ContentTitle:" + newsInfo.contentTitle);
        LogWriter.i(TAG, "ContentText:" + newsInfo.contentText);
        LogWriter.i(TAG, "Url:" + newsInfo.url);
        LogWriter.i(TAG, "Parse notification for " + pkg + " end!");
        LogWriter.i(TAG, "##################################################################");

        if(TextUtils.isEmpty(newsInfo.contentTitle)||TextUtils.isEmpty(newsInfo.contentText)){
            return;
        }
        if(newsInfo.contentText.equals("点击了解详情或停止应用。")||newsInfo.contentText.equals("贵州农信黔农商户宝正在运行")){
            return;
        }
        SPUtils.put(getApplicationContext(),"time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newsInfo.when));
        SPUtils.put(getApplicationContext(),"title",newsInfo.contentTitle);
        SPUtils.put(getApplicationContext(),"text",newsInfo.contentText);
        SPUtils.put(getApplicationContext(),"packageName",newsInfo.packageName);
        commitNewsInfoToServer(newsInfo);
        showNewsInfo(newsInfo);
    }

    @Override
    public void onInterrupt() {

    }

    private void commitNewsInfoToServer(final BaseNotificationParser.NewsInfo newsInfo) {
        // TODO upload notification infos to server
    }

    private void showNewsInfo(BaseNotificationParser.NewsInfo newsInfo) {
        if (GlobalConfig.mIsSettingMode) {
            return;
        }

        StringBuilder sb = new StringBuilder(512);
        if (!TextUtils.isEmpty(newsInfo.extras)) {
            sb.append(newsInfo.extras);
        }

        sb.append("PackageName:").append(newsInfo.packageName)
            .append("\nTime:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newsInfo.when))
            .append("\nTitle:").append(newsInfo.contentTitle)
            .append("\nText:").append(newsInfo.contentText);

        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.putExtra("newsInfo", sb.toString());
        intent.putExtra("packageName", newsInfo.packageName);
        intent.putExtra("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newsInfo.when));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
