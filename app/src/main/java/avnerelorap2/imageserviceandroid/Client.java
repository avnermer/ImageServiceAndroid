package avnerelorap2.imageserviceandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Client {

    //members
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    public void connectToServer()
    {
        try {
            InetAddress serverAddr = InetAddress.getByName("192.168.1.105");
            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, 8200);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }


    public void sendImage(File image) throws IOException
    {
        try {
            //sends the message to the server
            FileInputStream fis = new FileInputStream(image);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            byte[] imageByte = getBytesFromBitmap(bm);
            byte[] received = new byte[10];

            //send image name
            outputStream.write(image.getName().getBytes());
            //wait for confirmation
            if((inputStream.read(received) <= 0)) {return;}

            //send image size in bytes
            outputStream.write((String.valueOf(imageByte.length)).getBytes());
            //wait for confirmation
            if((inputStream.read(received) <= 0)) {return;}

            //send image
            outputStream.write(imageByte, 0, imageByte.length);

            //wait for confirmation
            if((inputStream.read(received) <= 0)) {return;}
            outputStream.flush();
        } catch (Exception e) {
            Log.e("TCP", "S: Error", e);
        }
    }

    public void closeConnection ()
    {
        try {
            //notify server transfer is over
            String done = "DONE";
            outputStream.write(done.getBytes());
            //todo change to read confirmation?
            sleep(1);
            outputStream.close();
            socket.close();
        }
        catch(Exception e)
        {
            Log.e("TCP", "S: Error", e);
        }
    }
}