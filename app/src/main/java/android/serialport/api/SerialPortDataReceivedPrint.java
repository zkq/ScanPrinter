package android.serialport.api;

public interface SerialPortDataReceivedPrint {
	void onDataReceivedListener(final byte[] buffer, final int size);

}
