package kr.re.leo.msgfwdr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2/11/15.
 */
public class SmsReceiver extends BroadcastReceiver {
    static final String logTag = "SmsReceiver";
    static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String PREFS_NAME = "SmsForwarderPrefs";

    private class ProcessPOSTrequest extends AsyncTask {

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

                if (res_code == 200)
                    return null;
                else
                    return String.valueOf(res_code);

            } catch (IOException e) {
                return e.toString();
            }

        }

        private void onPostExecute(String result) {
            //
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

                new ProcessPOSTrequest().execute(post_url, postData);



                    //Toast.makeText(context,
                    //        context.getString(R.string.successfully_sent), 0
                    //).show();
            }

        }




    }


}
