package joe.com.accessibilityservicesample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView statusTv;

    private AccessibilityManager accessibilityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        statusTv = (TextView) findViewById(R.id.status_tv);
        findViewById(R.id.btn_jump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accessibilityManager.isEnabled()) {
            statusTv.setText("辅助服务已开启");
        } else {
            statusTv.setText("辅助服务未开启，请打开辅助服务");
        }
    }
}
