package com.homer.aircon;

import java.io.IOException;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.Handler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class LEDOnOff extends AppCompatActivity {

    private ConnectedThread mConnectedThread;

    private static final String TAG = "LEDOnOff";

    private int connected = 0;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler


    Button btnOn, btnOff;
    Button btnConnect, btnDisconnect;
    TextView txtArduino;
    Handler h;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
   // private OutputStream outStream = null;

    private StringBuilder sb = new StringBuilder();


    // Well known SPP UUID
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Insert your server's MAC address
   private static String address = "58:91:CF:45:FC:2B";
   //private static String address = "00:00:00:00:00:00";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "In onCreate()");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledon_off);


        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);

        txtArduino = (TextView) findViewById(R.id.txtArduino);

        btnOn = (Button) findViewById(R.id.btnOn);
        btnOff = (Button) findViewById(R.id.btnOff);

        btnOn.setEnabled(false);
        btnOff.setEnabled(false);



        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();


        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());                                      // and clear
                            txtArduino.setText("Data from Arduino: " + sbprint);            // update TextView
                            btnOff.setEnabled(true);
                            btnOn.setEnabled(true);
                        }
                        // Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                if (connected == 1)
                    mConnectedThread.write("AAA");

                /* Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked On", Toast.LENGTH_SHORT);
                msg.show(); */
            }
        });

        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (connected == 1)
                    mConnectedThread.write("BBB");

                /* Toast msg = Toast.makeText(getBaseContext(),
                        "You have clicked Off", Toast.LENGTH_SHORT);
                msg.show(); */
            }
        });

        btnDisconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                try  {

                    connected = 0;

                    btSocket.close();

                    btnOn.setEnabled(false);
                    btnOff.setEnabled(false);

                    btnConnect.setEnabled(true);
                    btnDisconnect.setEnabled(false);


                } catch (IOException e2) {
                    errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
                }

            }
        });

        btnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {


                Log.d(TAG, "...In onResume - Attempting client connect...");

                // Set up a pointer to the remote node using it's address.
                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                // Two things are needed to make a connection:
                //   A MAC address, which we got above.
                //   A Service ID or UUID.  In this case we are using the
                //     UUID for SPP.
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
                }

                // Discovery is resource intensive.  Make sure it isn't going on
                // when you attempt to connect and pass your message.
                btAdapter.cancelDiscovery();


                // Establish the connection.  This will block until it connects.
                Log.d(TAG, "...Connecting to Remote...");
                try {
                    btSocket.connect();
                    Log.d(TAG, "...Connection established and data link opened...");

                    mConnectedThread = new ConnectedThread(btSocket);
                    mConnectedThread.start();

                   connected = 1;

                    btnOn.setEnabled(true);
                    btnOff.setEnabled(true);

                    btnConnect.setEnabled(false);
                    btnDisconnect.setEnabled(true);




                } catch (IOException e) {
                    try {
                        btSocket.close();

                        btnOn.setEnabled(false);
                        btnOff.setEnabled(false);

                    } catch (IOException e2) {
                        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                    }
                }

            }
        });





    }

    @Override
    public void onResume() {
        super.onResume();





        // Create a data stream so we can talk to server.
      //  Log.d(TAG, "...Creating Socket...");

       /* try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }*/


    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

     /*   if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        } */

        try  {


            connected = 0;

            btSocket.close();

            btnOn.setEnabled(false);
            btnOff.setEnabled(false);

            btnConnect.setEnabled(true);
            btnDisconnect.setEnabled(false);

        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }



    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
                // Log.d(TAG, "Mesaj Geldi");

            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");

                Toast msg = Toast.makeText(getBaseContext(),
                        "ERROR:" + e.getMessage(), Toast.LENGTH_SHORT);
                msg.show();
            }
        }
    }

}
