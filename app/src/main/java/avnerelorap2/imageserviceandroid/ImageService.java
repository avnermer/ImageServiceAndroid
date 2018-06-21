package avnerelorap2.imageserviceandroid;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageService extends Service {

    //members
    BroadcastReceiver receiver;
    ArrayList<File> images;
    Client client;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new Client();
    }

    public int onStartCommand(Intent intent, int flag, int startId)
    {
        Toast.makeText(this, "Service starting...", Toast.LENGTH_SHORT).show();

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.receiver = new BroadcastReceiver()
        {
            @Override
              public void onReceive(Context context, Intent intent)
            {
                WifiManager wifiManager =
                        (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null)
                {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                    {
                        //get the different network states
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            startTransfer(context);
                        }
                    }
                }
            }    };
        // Registers the receiver so that your service will listen for
        // broadcasts
        this.registerReceiver(this.receiver, theFilter);
        return START_STICKY;
    }

    private void startTransfer(Context context)
    {
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "default");
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Images Transfer");
        builder.setContentText("Transferring..");

        //start a transferring thread
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                int progressCount = 0;
                loadLocalImages(null, null);

                try
                {

                    client.connectToServer();
                    for(File image : images)
                    {
                        try {
                            client.sendImage(image);
                        } catch (Exception e)
                        {
                            Log.e("TCP", "S: Error", e);
                        }
                        progressCount = progressCount + 100/ images.size();
                        builder.setProgress(100, progressCount, false);
                        manager.notify(1, builder.build());
                    }

                    // in the end of transfer
                    builder.setProgress(0, 0, false);
                    builder.setContentTitle("Done");
                    builder.setContentText("Images transfer completed");
                    manager.notify(1, builder.build());
                    client.closeConnection();
                } catch (Exception e) {
                    Log.e("Connection Error", "Images transfer terminated", e);
                }
            }
        }).start();

    }

    private void loadLocalImages(ArrayList<File> itms, ArrayList<File> imgsList)
    {
        ArrayList<File> items = itms;
        ArrayList<File> imagesList = imgsList;

        //if it's first recursive call
        if(imgsList == null && itms == null)
        {
            // Getting the Camera Folder

            File dcim = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "Camera");
            if (dcim == null) {return;}

            File[] itemsArr =  dcim.listFiles();
            if ( itemsArr == null) {return;}

            items = new ArrayList<>(Arrays.asList( itemsArr));
            //initialize images temp list in order to avoid concurrent modification
            imagesList = new ArrayList<>();
        }
        for (File item : items)
        {
            if(item.isDirectory())
            {
                //prepare argument for recursion
                File[] newItemsArr =  item.listFiles();
                if ( newItemsArr == null) {return;}
                ArrayList<File> newDirItems = new ArrayList<>(Arrays.asList( newItemsArr));

                //recurse
                loadLocalImages(newDirItems, imagesList);
            }
            else if(item.isFile())
            {
                if(item.toString().contains(".jpg") || item.toString().contains(".png")
                        || item.toString().contains(".bmp") || item.toString().contains(".gif")) {imagesList.add(item);}
            }
        }
        this.images = imagesList;
    }

    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(this, "Service ending...", Toast.LENGTH_SHORT).show();
    }
}
