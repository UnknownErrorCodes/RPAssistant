package com.limelight.binding.input.driver;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import java.nio.ByteBuffer;

public class Xbox360Controller extends AbstractXboxController {
    private static final int XB360_IFACE_SUBCLASS = 93;
    private static final int XB360_IFACE_PROTOCOL = 1; // Wired only

    private static final int[] SUPPORTED_VENDORS = {
            0x0079, // GPD Win 2
            0x044f, // Thrustmaster
            0x045e, // Microsoft
            0x046d, // Logitech
            0x056e, // Elecom
            0x06a3, // Saitek
            0x0738, // Mad Catz
            0x07ff, // Mad Catz
            0x0e6f, // Unknown
            0x0f0d, // Hori
            0x1038, // SteelSeries
            0x11c9, // Nacon
            0x12ab, // Unknown
            0x1430, // RedOctane
            0x146b, // BigBen
            0x1532, // Razer Sabertooth
            0x15e4, // Numark
            0x162e, // Joytech
            0x1689, // Razer Onza
            0x1bad, // Harmonix
            0x24c6, // PowerA
    };

    public static boolean canClaimDevice(UsbDevice device) {
        for (int supportedVid : SUPPORTED_VENDORS) {
            if (device.getVendorId() == supportedVid &&
                    device.getInterfaceCount() >= 1 &&
                    device.getInterface(0).getInterfaceClass() == UsbConstants.USB_CLASS_VENDOR_SPEC &&
                    device.getInterface(0).getInterfaceSubclass() == XB360_IFACE_SUBCLASS &&
                    device.getInterface(0).getInterfaceProtocol() == XB360_IFACE_PROTOCOL) {
                return true;
            }
        }

        return false;
    }

    public Xbox360Controller(UsbDevice device, UsbDeviceConnection connection, int deviceId, UsbDriverListener listener) {
        super(device, connection, deviceId, listener);
    }

    private int unsignByte(byte b) {
        if (b < 0) {
            return b + 256;
        }
        else {
            return b;
        }
    }

    @Override
    protected boolean handleRead(ByteBuffer buffer) {
        return true;
    }

    private boolean sendLedCommand(byte command) {
        byte[] commandBuffer = {0x01, 0x03, command};

        int res = connection.bulkTransfer(outEndpt, commandBuffer, commandBuffer.length, 3000);
        if (res != commandBuffer.length) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean doInit() {
        // Turn the LED on corresponding to our device ID
        sendLedCommand((byte)(2 + (getControllerId() % 4)));

        // No need to fail init if the LED command fails
        return true;
    }

    @Override
    public void rumble(short lowFreqMotor, short highFreqMotor) {
        byte[] data = {
                0x00, 0x08, 0x00,
                (byte)lowFreqMotor, (byte)highFreqMotor,
                0x00, 0x00, 0x00
        };
        int res = connection.bulkTransfer(outEndpt, data, data.length, 100);
    }
}