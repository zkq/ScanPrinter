package com.smartdevicesdk.io;



public class ScanGpio {

	public static void openPower() {
		try {
			if (EmGpio.gpioInit()) {
				EmGpio.setGpioOutput(111);
				EmGpio.setGpioDataLow(111);
				Thread.sleep(100);

				EmGpio.setGpioOutput(111);
				EmGpio.setGpioDataHigh(111);
				Thread.sleep(100);
			}
			EmGpio.gpioUnInit();
		} catch (Exception e) {
		}
	}

	public static void closePower() {
		try {
			if (EmGpio.gpioInit()) {
				EmGpio.setGpioOutput(111);
				EmGpio.setGpioDataLow(111);
				Thread.sleep(100);
				EmGpio.setGpioInput(111);
			}
			EmGpio.gpioUnInit();
		} catch (Exception e) {
		}
	}

	public static void openScan() {
		try {
			if (EmGpio.gpioInit()) {
				EmGpio.setGpioOutput(110);
				EmGpio.setGpioDataHigh(110);
				Thread.sleep(100);
				EmGpio.setGpioDataLow(110);
			}
			EmGpio.gpioUnInit();
		} catch (Exception e) {
		}

	}

	public static void closeScan() {
		try {
			if (EmGpio.gpioInit()) {
				EmGpio.setGpioOutput(110);
				EmGpio.setGpioDataHigh(110);
			}
			EmGpio.gpioUnInit();
		} catch (Exception e) {
		}

	}
}
