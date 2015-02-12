package kr.re.leo.msgfwdr;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.*;


public class MainActivity extends ActionBarActivity  {
    public static final String PREFS_NAME = "SmsForwarderPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String post_url = settings.getString("postUri", "");

        TextView t = (TextView)findViewById(R.id.post_url);
        t.setText(post_url);

        final Button button = (Button) findViewById(R.id.save_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

                SharedPreferences.Editor editor = settings.edit();
                TextView t = (TextView)findViewById(R.id.post_url);
                editor.putString("postUri", t.getText().toString());
                editor.commit();

                Toast.makeText(getApplicationContext(), getString(R.string.saved), 0).show();
            }
        });

    }
}
