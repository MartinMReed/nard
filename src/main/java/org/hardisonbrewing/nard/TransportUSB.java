/**
 * Copyright (c) 2011 Martin M Reed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.hardisonbrewing.nard;

import java.io.IOException;
import java.util.Vector;

import net.rim.usb.USBConnection;

public class TransportUSB implements Transport {

    private final byte[] CACHE = new byte[Short.MAX_VALUE];
    private Vector<Byte> byteBuffer = new Vector<Byte>();

    private USBConnection usbConnection;

    public boolean connect() throws IOException {

        int[] attachedDevices = USBConnection.getAttachedDevices();
        if ( attachedDevices.length == 0 ) {
            return false;
        }

        for (int i = 0; i < attachedDevices.length; i++) {
            String pin = Integer.toHexString( attachedDevices[0] ).toUpperCase();
            System.out.println( "Device[" + pin + "] Simulator[" + USBConnection.isSimulator( attachedDevices[i] ) + "]" );
        }

        USBConnection usbConnection = new USBConnection();
        boolean opened = usbConnection.openChannel( "RIM_JVMDebug", attachedDevices[0], null );

        if ( !opened ) {
            return false;
        }

        this.usbConnection = usbConnection;
        return true;
    }

    public void close() {

        if ( usbConnection != null ) {
            try {
                usbConnection.closeChannel();
            }
            catch (Exception e) {
                // do nothing
            }
            usbConnection = null;
        }
    }

    public void writeByte( int i ) throws IOException {

        byte[] b = new byte[] { (byte) ( i & 0xff ) };
        usbConnection.sendPacket( b, 0, b.length );
    }

    public void flush() throws IOException {

    }

    public int readByte() throws IOException {

        if ( byteBuffer.isEmpty() ) {
            int size = usbConnection.receivePacket( CACHE );
            if ( size <= 0 ) {
                return -1;
            }
            for (int i = 0; i < size; i++) {
                byteBuffer.add( CACHE[i] );
            }
        }
        return byteBuffer.remove( 0 );
    }
}
