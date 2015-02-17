package kr.re.leo.msgfwdr;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2/11/15.
 */
public class SmsReceiver extends BroadcastReceiver {
    static final String logTag = "SmsReceiver";
    static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String PREFS_NAME = "SmsForwarderPrefs";

    private class ProcessPOSTRequest extends AsyncTask {
        private String content =  null;
        private boolean error = false;

        private Context mContext;
        private int NOTIFICATION_ID = 1;
        private Notification mNotification;
        private NotificationManager mNotificationManager;

        public ProcessPOSTRequest(Context context){
            this.mContext = context;
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        @Override
        protected String doInBackground(Object[] params) {
            String post_url = (String) params[0];
            List<NameValuePair> postData = (List<NameValuePair>) params[1];

            HttpClient client = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(post_url);

            try {
                httppost.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));
                HttpResponse response = client.execute(httppost);

                int res_code = response.getStatusLine().getStatusCode();

                if (res_code == 200){
                    Log.w("HTTP", "HTTP code 200");
                    content = "HTTP code 200";
                } else {
                    Log.e("HTTP", "HTTP code " + String.valueOf(res_code));

                    error = true;
                    content = "HTTP code " + String.valueOf(res_code);
                    cancel(true);
                }
            } catch (IOException e) {
                Log.e("HTTP", "HTTP Error: " + e.toString() );
                error = true;
                content = e.toString();
                cancel(true);
            }

            return content;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (error) {
                createNotification(mContext.getString(R.string.failed),content);
            } else {
                createNotification(mContext.getString(R.string.app_name), mContext.getString(R.string.successfully_sent));
            }
        }

        @Override
        protected void onCancelled() {
            createNotification(mContext.getString(R.string.failed),content);
        }

        private void createNotification(String contentTitle, String contentText) {
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
            mNotification = builder.setSmallIcon(R.drawable.ic_launcher).setTicker(content)
                    .setAutoCancel(true).setContentTitle(contentTitle)
                    .setContentText(contentText).setContentIntent(pendingIntent).build();

            mNotificationManager.notify(1, mNotification);
        }

    }

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


            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            String post_url = settings.getString("postUri", "");

            if (post_url.equals(""))
            {
                Toast.makeText(context, context.getString(R.string.please_url), 0).show();
                return;
            }



            SmsMessage[] smsMessages = new SmsMessage[pdusObj.length];
            for (int i = 0; i < pdusObj.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                List<NameValuePair> postData = new ArrayList<NameValuePair>(2);

                SmsMessage message = smsMessages[i];
                postData.add(new BasicNameValuePair("message[sender]", message.getOriginatingAddress()));

                postData.add(new BasicNameValuePair("message[text]", message.getMessageBody()));

                new ProcessPOSTRequest(context).execute(post_url, postData);

            }

        }




    }


}
