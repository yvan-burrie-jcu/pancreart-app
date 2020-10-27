package jcu.cp3407.pancreart;

import android.bluetooth.*;
import android.os.Handler;

import java.io.*;
import java.util.Set;
import java.util.UUID;

public class Bluetooth {

    Set<BluetoothDevice> devices;

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    BluetoothHeadset headset;

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int ADAPTER_UNAVAILABLE = 5;
    public static final int ADAPTER_DISABLED = 6;
    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int CONNECTING = 2;
    public static final int CONNECTED = 3;
    public static final int NO_SOCKET_FOUND = 4;

    Handler handler;

    Bluetooth(Handler handler) {
        // register handler for bluetooth events
        this.handler = handler;

        // start accepting connections
        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();

        if (checkAdapter()) {
            return;
        }

        // todo: need to find out how to use proxy and profile
        /*
        BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int i, BluetoothProfile proxy) {
                if (i == BluetoothProfile.HEADSET) {
                    headset = (BluetoothHeadset) proxy;
                }
            }
            @Override
            public void onServiceDisconnected(int i) {
                if (i == BluetoothProfile.HEADSET) {
                    headset = null;
                }
            }
        };
        adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);
        adapter.closeProfileProxy(BluetoothProfile.HEALTH, headset);
        */

        updateDevices();

        // todo: use this when requesting to connect
//        ConnectThread connectThread = new ConnectThread(device);
//        connectThread.start();
    }

    private boolean checkAdapter() {
        if (adapter == null) {
            handler.obtainMessage(ADAPTER_UNAVAILABLE).sendToTarget();
            return false;
        }
        if (!adapter.isEnabled()) {
            handler.obtainMessage(ADAPTER_DISABLED).sendToTarget();
            return false;
        }
        return true;
    }

    private void updateDevices() {
        devices = adapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                // todo: append device into list
                System.out.println("DEVICE: " + deviceName);
            }
        }
    }

    public class AcceptThread extends Thread {

        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tempServerSocket = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tempServerSocket = adapter.listenUsingRfcommWithServiceRecord("NAME", MY_UUID);
            } catch (IOException e) {
            }
            serverSocket = tempServerSocket;
        }

        public void run() {
            while (true) {
                // Keep listening until exception occurs or a socket is returned
                BluetoothSocket socket;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    handler.obtainMessage(CONNECTED).sendToTarget();
                }
            }
        }
    }

    public boolean writeWithConnectedThread(BluetoothSocket socket, String data) {
        if (socket != null) {
            Bluetooth.ConnectedThread connectedThread = new ConnectedThread(socket);
            connectedThread.write(data.getBytes());
            return true;
        }
        return false;
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket socket;

        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            this.device = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            socket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                handler.obtainMessage(CONNECTING).sendToTarget();
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    socket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
//            bluetooth_message = "Initial message"
//            mHandler.obtainMessage(MESSAGE_WRITE,mmSocket).sendToTarget();
        }

        // Will cancel an in-progress connection, and close the socket
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    class ConnectedThread extends Thread {

        private final BluetoothSocket socket;

        private final InputStream inputStream;

        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[2];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
            }
        }

        // Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
