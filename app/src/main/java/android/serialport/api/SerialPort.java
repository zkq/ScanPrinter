package android.serialport.api;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.zkq.scanprinter.activity.MainActivity;
import com.example.zkq.scanprinter.util.BeepUtil;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SerialPort {

    final String TAG = "SerialPort";


    String deviceName = "/dev/ttyMT0";
    int baudrate = 115200;
    int flag = 0;
    int dataBits = 8;
    int stopBits = 1;
    int parity = 'n';
    int flowcontrol = 0;
    int spaceTime = 0;
    FileDescriptor descriptor;
    FileInputStream inputStream;
    FileOutputStream outputStream;
    int sendSize = 100;

    public enum PORT {comInit, comPrinter, comScan, comOutsize}

    private PORT currentPort = PORT.comInit;
    private byte[] bt_init = new byte[]{0x1b, 0x23};
    private byte[] bt_printer = new byte[]{0x1b, 0x26, 00};
    private byte[] bt_scan = new byte[]{0x1b, 0x26, 01};
    private byte[] bt_outside = new byte[]{0x1b, 0x26, 02};


    Thread readThread;
    boolean isReceiving = false;
    byte[] receivedData = new byte[4096];
    int receivedLen = 0;
    SerialPortDataReceivedScan scanListener;
    SerialPortDataReceivedPrint printerListener;

    public SerialPort(final MainActivity context) {
        scanListener = new SerialPortDataReceivedScan() {
            @Override
            public void onDataReceivedListener(byte[] buffer, int size) {
                if (size < 1) {
                    return;
                }

                System.arraycopy(buffer, 0, receivedData, receivedLen, size);
                receivedLen += size;

                if ((receivedLen>=2&&receivedData[receivedLen - 2] == 13 && receivedData[receivedLen - 1] == 10)
                        ||receivedLen>=2&&receivedData[receivedLen - 2] == 10 && receivedData[receivedLen - 1] == 13) {
                    try {
                        //放声音bee
                        BeepUtil.beep(context);
                        String barCodeStr = new String(receivedData, 0, receivedLen, "UTF-8").trim();

                        if (barCodeStr != "") {
                            Message message = Message.obtain();
                            message.what = 0xff;
                            Bundle bundle = new Bundle();
                            bundle.putString("barstr", barCodeStr);
                            message.setData(bundle);
                            context.handler.sendMessage(message);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    receivedLen = 0;
                }
            }
        };

        printerListener = new SerialPortDataReceivedPrint() {
            @Override
            public void onDataReceivedListener(byte[] buffer, int size) {
                String str = null;
                try {
                    str = new String(buffer, 0, size, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "Print received:" + str);
            }
        };

    }


    public boolean openPort() {
        if (isOpen()) {
            return true;
        }

        descriptor = open(deviceName, baudrate, flag,
                dataBits, stopBits, parity, flowcontrol);

        if (descriptor == null)
            return false;

        inputStream = new FileInputStream(descriptor);
        outputStream = new FileOutputStream(descriptor);

        //清除缓存
        try {
            outputStream.write(bt_init);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    public boolean closePort() {
        try {
            inputStream.close();
            outputStream.close();

            inputStream = null;
            outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isOpen() {
        return inputStream != null && outputStream != null;
    }


    public boolean switchFunc(PORT port) {
        if (!isOpen()) {
            return false;
        }
        if(currentPort == port)
        {
            return true;
        }

        try {
            Write(bt_init);
            Thread.sleep(200);
            switch (port) {
                case comInit:
                    Write(bt_init);
                    break;
                case comScan:
                    Write(bt_scan);
                    break;
                case comPrinter:
                    Write(bt_printer);
                    break;
                case comOutsize:
                    Write(bt_outside);
                    break;
                default:
                    break;
            }
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        currentPort = port;
        return true;
    }


    public boolean Write(byte[] buffer) {
        if (!isOpen() || buffer.length == 0) {
            return false;
        }

        for (int j = 0; j < buffer.length; j += sendSize) {
            int remainedLen = buffer.length - j;
            int sendLen = remainedLen <= sendSize ? remainedLen : sendSize;
            try {
                outputStream.write(buffer, j, sendLen);
                Thread.sleep(10);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean Write(String str) {
        byte[] buffer = str.getBytes();
        return Write(buffer);
    }

    public boolean startRead() {
        if (!isOpen()) {
            return false;
        }

        if (readThread != null && readThread.isAlive()) {
            return true;
        }

        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[64];

                while (isReceiving) {
                    int size;
                    try {
                        Arrays.fill(buffer, (byte) 0);
                        size = inputStream.read(buffer);
                        if (size > 0) {
                            Log.i(TAG, "Received Data:" + byteToString(buffer, size));
                            onDataReceived(buffer, size);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });

        isReceiving = true;
        readThread.start();

        return true;
    }


    public void stopRead()
    {
        if(readThread == null || !readThread.isAlive())
            return;

        isReceiving = false;
        readThread.interrupt();
        readThread = null;
    }

    public boolean isReading()
    {
        return isOpen() && isReceiving;
    }

    private void onDataReceived(byte[] buffer, int size) {
        switch (currentPort)
        {
            case comInit:
                break;
            case comScan:
                scanListener.onDataReceivedListener(buffer, size);
                break;
            case comPrinter:
                printerListener.onDataReceivedListener(buffer, size);
                break;
            case comOutsize:
                break;
            default:
                break;
        }
    }


    // JNI
    static {
        System.loadLibrary("serial_port");
    }

    private native static FileDescriptor open(String path, int baudrate,
                                              int flags, int databits2,
                                              int stopbits, int parity2,
                                              int flowcontrol);

    private native void close();


    private static String byteToString(byte[] b, int size) {
        byte high, low;
        byte maskHigh = (byte) 0xf0;
        byte maskLow = 0x0f;

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < size; i++) {
            high = (byte) ((b[i] & maskHigh) >> 4);
            low = (byte) (b[i] & maskLow);
            buf.append(findHex(high));
            buf.append(findHex(low));
            buf.append(" ");
        }

        return buf.toString();
    }

    private static char findHex(byte b) {
        int t = new Byte(b).intValue();
        t = t < 0 ? t + 16 : t;

        if ((0 <= t) && (t <= 9)) {
            return (char) (t + '0');
        }

        return (char) (t - 10 + 'A');
    }

    private static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }
}
