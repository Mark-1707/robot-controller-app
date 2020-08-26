package com.example.arccontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import static com.example.arccontroller.R.color.colorAccent;

public class MainActivity0 extends AppCompatActivity {

    private static final String TAG = "ARC Controller";
    ImageView right, left, up, down;
    Window window;
    BluetoothAdapter adapter = null;
    BluetoothSocket socket = null;
    OutputStream outputStream = null;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static String address = "00:19:10:08:2D:63";

    @SuppressLint({"ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main0);

        this.getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity0.this, colorAccent));

        adapter=BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        right = findViewById(R.id.right);
        left = findViewById(R.id.left);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);

        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        up.setImageResource(R.drawable.ic_arrow_drop_up_faint);
                        goForward("1");
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        up.setImageResource(R.drawable.ic_arrow_drop_up);
                        offForward("5");
                        return true;
                    }
                }
                return false;
            }
        });


        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        down.setImageResource(R.drawable.ic_arrow_drop_down_faint);
                        goBackword("2");
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        down.setImageResource(R.drawable.ic_arrow_drop_down);
                        offBackword("6");
                        return true;
                    }
                }
                return false;
            }
        });


        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        left.setImageResource(R.drawable.ic_arrow_drop_left_faint);
                        goLeft("3");
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        left.setImageResource(R.drawable.ic_chevron_left_48px);
                        offLeft("7");
                        return true;
                    }
                }
                return false;
            }
        });


        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        right.setImageResource(R.drawable.ic_arrow_drop_right_faint);
                        goRight("4");
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        right.setImageResource(R.drawable.ic_chevron_right);
                        offRight("8");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = adapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            socket = createBluetoothSocket(device);
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        adapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            socket.connect();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try {
            socket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (adapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (adapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void goForward(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data goForward: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }

    private void goBackword(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data goBackword: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }

    private void goLeft(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data go Left: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }

    private void goRight(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data goRight: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }

    private void offForward(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data goRight: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }

    private void offBackword(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data goRight: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }

    private void offLeft(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data goRight: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }

    private void offRight(String message) {
        if (message != null) {
            byte[] msgBuffer = message.getBytes();

            Log.d(TAG, "...Send data goRight: " + message + "...");

            try {
                outputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                //errorExit("Fatal Error", msg);
            }
        } else {
            Log.d(TAG, "sent data is null !");
        }
    }
}


