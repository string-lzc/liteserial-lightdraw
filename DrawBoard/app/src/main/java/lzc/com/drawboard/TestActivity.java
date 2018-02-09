package lzc.com.drawboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LineView sv = new LineView(TestActivity.this);
        setContentView(sv);

    }
}
