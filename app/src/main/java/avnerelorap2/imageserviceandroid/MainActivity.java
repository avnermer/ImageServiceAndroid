package avnerelorap2.imageserviceandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view)
    {
        Log.println(Log.DEBUG, "bla", "kaka");
        Intent intent = new Intent(this, ImageService.class);
        startService(intent);
    }
    public void stopService(View view)
    {
        Intent intent = new Intent(this, ImageService.class);
        stopService(intent);
    }

}
