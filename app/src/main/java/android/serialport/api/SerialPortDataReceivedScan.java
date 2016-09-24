package android.serialport.api;

public interface SerialPortDataReceivedScan {
	void onDataReceivedListener(final byte[] buffer, final int size);

}
