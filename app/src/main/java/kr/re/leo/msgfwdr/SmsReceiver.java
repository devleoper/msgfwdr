package kr.re.leo.msgfwdr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2/11/15.
 */
public class SmsReceiver extends BroadcastReceiver {
    static final String logTag = "SmsReceiver";
    static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String PREFS_NAME = "SmsForwarderPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            // Check if Bundle is null.
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;

            Object[] pdusObj = (Object[]) bundle.get("pdus");
            if (pdusObj == null)
                return;

            HttpClient client = new DefaultHttpClient();
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            String post_url = settings.getString("postUri", "");

            if (post_url.equals(""))
            {
                Toast.makeText(context, "Please setup URL." , 0).show();
                return;
            }

            HttpPost httppost = new HttpPost(post_url);

            SmsMessage[] smsMessages = new SmsMessage[pdusObj.length];
            for (int i = 0; i < pdusObj.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                try {
                    List<NameValuePair> postData = new ArrayList<NameValuePair>(2);

                    SmsMessage message = smsMessages[i];
                    postData.add(new BasicNameValuePair("message[sender]", message.getOriginatingAddress()));

                    postData.add(new BasicNameValuePair("message[text]", message.getMessageBody()));
                    httppost.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));

                    HttpResponse response = client.execute(httppost);

                    Toast.makeText(context,
                            String.format("Query sent. code: %d", response.getStatusLine().getStatusCode())
                            , 0).show();
                } catch (IOException e) {
                    Toast.makeText(context, "Failed: " + e.toString() , 0).show();
                }
            }
        }
    }
}
