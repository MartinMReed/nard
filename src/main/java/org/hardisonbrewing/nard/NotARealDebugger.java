/**
 * Copyright (c) 2011 Martin M Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hardisonbrewing.nard;

import java.io.IOException;

public class NotARealDebugger {

    public NardListener nardListener;

    private Transport transport;
    private TransportInputStream inputStream;
    private TransportOutputStream outputStream;

    private int versionMajor;
    private int versionMinor;

    public NotARealDebugger(Transport transport) {

        this.transport = transport;
    }

    public boolean connect() throws Exception {

        transport.connect();
        inputStream = new TransportInputStream( transport );
        outputStream = new TransportOutputStream( transport );

        init();

        //        for (int i = 7; i < 64; i++) {
        //            logCmd( i );
        //        }

        return true;
    }

    public void close() {

        if ( transport != null ) {
            try {
                transport.close();
            }
            catch (Exception e) {
                // do nothing
            }
            transport = null;
        }
    }

    public void read() {

        switch (byteReply()) {

            case 141: // print log statement
                int stringCmd = versionCheck( 1, 51 ) ? 172 : 64;
                do {
                    cmd( stringCmd );
                    String str = stringReply();
                    if ( stringCmd == 172 ) {
                        long time = getLong();
                    }
                    if ( str.length() == 0 ) {
                        break;
                    }
                    nardListener.out( str );
                }
                while (true);
                cmd( 6 );
                noReply();
                break;

            default: // ignore and continue
                cmd( 6 );
                noReply();
                break;
        }
    }

    //    private void logCmd( int cmd ) {
    //
    //        cmd( cmd );
    //
    //        short size = turnaround();
    //        byte[] data = new byte[size];
    //
    //        if ( size == 0 ) {
    //            noReply();
    //        }
    //        else {
    //            for (int i = 0; i < size; i++) {
    //                data[i] = (byte) getByte();
    //            }
    //        }
    //
    //        String d;
    //        switch (size) {
    //            case 0: {
    //                d = "no reply";
    //                break;
    //            }
    //            case 1: {
    //                d = Integer.toHexString( data[0] & 0xff );
    //                break;
    //            }
    //            case 2: {
    //                short s = (short) ( data[0] << 8 | data[1] & 0xff );
    //                d = Short.toString( s );
    //                break;
    //            }
    //            case 4: {
    //                int i = (int) ( data[0] << 32 );
    //                i += (int) ( data[1] << 16 );
    //                i += (int) ( data[2] << 8 );
    //                i += (int) ( data[3] & 0xff );
    //                d = Integer.toString( i );
    //                break;
    //            }
    //            case 8: {
    //                long i = (long) ( data[0] << 512 );
    //                i += (long) ( data[1] << 256 );
    //                i += (long) ( data[2] << 128 );
    //                i += (long) ( data[3] << 64 );
    //                i += (long) ( data[4] << 32 );
    //                i += (long) ( data[5] << 16 );
    //                i += (long) ( data[6] << 8 );
    //                i += (long) ( data[7] & 0xff );
    //                d = Long.toString( i );
    //                break;
    //            }
    //            default:
    //                d = "String";
    //                //                try {
    //                //                    d = new String( data, "UTF-8" );
    //                //                }
    //                //                catch (Exception e) {
    //                //                    throw new IllegalStateException();
    //                //                }
    //        }
    //
    //        nardListener.outln( "cmd[" + cmd + "], size[" + size + "], d[" + d + "]" );
    //    }

    private void init() {

        login();

        cmd( 1 );

        versionMajor = intReply();
        nardListener.outln( "versionMajor: " + versionMajor );

        versionMinor = getInt();
        nardListener.outln( "versionMinor: " + versionMinor );

        int devCode = getInt();
        nardListener.outln( "devCode: " + devCode );

        switch (devCode) {
            case 3: // isReleaseSimulator
                nardListener.outln( "isReleaseSimulator = true" );
                break;
            case 2: // isSecureDevice
                nardListener.outln( "isSecureDevice = true" );
                // fall through
            case 1: // isDevice
                nardListener.outln( "isDevice = true" );
                break;
        }

        if ( versionMajor != 1 ) {
            throw new IllegalStateException( "ErrorVersionIncompatible" );
        }

        if ( versionCheck( 1, 1 ) ) {
            cmd( 111 );
            String simulatorSessionName = stringReply();
            nardListener.outln( "simulatorSessionName: " + simulatorSessionName );
        }

        if ( versionCheck( 1, 29 ) ) {
            cmd( 138 );
            int hwid = intReply();
            String hwidHex = Integer.toHexString( hwid ).toUpperCase();
            nardListener.outln( "hwid = " + hwidHex );
        }

        if ( versionCheck( 1, 36 ) ) {
            cmd( 144 );
            noReply();
        }

        cmd( 2 );
        noReply();
    }

    private boolean versionCheck( int major, int minor ) {

        if ( versionMajor == major ) {
            return versionMinor >= minor;
        }
        else {
            return versionMajor > major;
        }
    }

    private void login() {

        int maxAttempts = 10;
        do {
            cmd( 83 );
            int failureCount = intReply();
            nardListener.outln( "failureCount: " + failureCount );
            int loginType = getInt();
            nardListener.outln( "loginType: " + loginType );
            switch (loginType) {
                case 3: // '\003'
                    maxAttempts = getInt();
                    nardListener.outln( "maxAttempts: " + maxAttempts );
                    if ( getInt() == 0 ) {
                        return;
                    }
                    break;

                case 2: // '\002'
                    switch (getInt()) {
                        case 0: // '\0'
                            return;
                        case 2: // '\002'
                            cmd( 142 );
                            maxAttempts = intReply();
                            nardListener.outln( "maxAttempts: " + maxAttempts );
                            break;
                    }
                    break;

                case 0: // '\0'
                case 1: // '\001'
                default:
                    throw new IllegalStateException( "ErrorLoginNotAllowed" );
            }
            throw new UnsupportedOperationException( "Password input not supported, but required" );
            //            byte password[] = S.promptForPassword( maxAttempts, maxAttempts - failureCount );
            //            cmd( 82, password );
        }
        while (intReply() == 0);
    }

    private void cmd( int req ) {

        try {
            outputStream.writeShort( 1 );
            outputStream.writeByte( req );
        }
        catch (Throwable e) {
            //            nardListener.errln( e.getMessage() );
            throw new IllegalStateException( e.getMessage() );
        }
    }

    private void cmd( int req, int i ) {

        try {
            outputStream.writeShort( 5 );
            outputStream.writeByte( req );
            outputStream.writeInt( i );
        }
        catch (Throwable e) {
            //            nardListener.errln( e.getMessage() );
            throw new IllegalStateException( e.getMessage() );
        }
    }

    private void cmd( int req, String str ) {

        try {
            int len = str.length();
            if ( len > 1019 ) {
                len = 1019;
            }
            outputStream.writeShort( 5 + len );
            outputStream.writeByte( req );
            outputStream.writeInt( len );
            for (int i = 0; i < len; i++) {
                outputStream.writeByte( (byte) str.charAt( i ) );
            }
        }
        catch (Throwable e) {
            //            nardListener.errln( e.getMessage() );
            throw new IllegalStateException( e.getMessage() );
        }
    }

    private short turnaround() {

        try {
            outputStream.flush();
            return inputStream.readShort();
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return 0;
    }

    private void noReply() {

        turnaround();
    }

    private int getByte() {

        try {
            return inputStream.readByte() & 0xff;
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return 0;
    }

    private int getChar() {

        try {
            return inputStream.readChar() & 0xffff;
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return 0;
    }

    private int getShort() {

        try {
            return inputStream.readShort();
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return 0;
    }

    private int getUnsignedShort() {

        try {
            return inputStream.readShort() & 0xffff;
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return 0;
    }

    private int getInt() {

        try {
            return inputStream.readInt();
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return 0;
    }

    private String getString() {

        try {
            return inputStream.readUTF();
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return null;
    }

    private long getLong() {

        try {
            return inputStream.readLong();
        }
        catch (Throwable e) {
            nardListener.errln( e.getMessage() );
        }
        return 0L;
    }

    private int unsignedShortReply() {

        short size = turnaround();
        return getUnsignedShort();
    }

    private int byteReply() {

        short size = turnaround();
        return getByte();
    }

    private int charReply() {

        short size = turnaround();
        return getChar();
    }

    private int shortReply() {

        short size = turnaround();
        return getShort();
    }

    private int intReply() {

        short size = turnaround();
        return getInt();
    }

    private long longReply() {

        short size = turnaround();
        return getLong();
    }

    private String stringReply() {

        short size = turnaround();
        return getString();
    }

    private final class TransportInputStream {

        private final Transport transport;

        public TransportInputStream(Transport transport) {

            this.transport = transport;
        }

        public int readByte() throws IOException {

            return transport.readByte();
        }

        public int readChar() throws IOException {

            int i = readByte();
            i <<= 8;
            i += readByte();
            return i & 0xffff;
        }

        public int readInt() throws IOException {

            int i = readChar();
            i <<= 16;
            i += readChar();
            return i;
        }

        public long readLong() throws IOException {

            long l = readInt();
            l <<= 32;
            l += (long) readInt() & 0xffffffffL;
            return l;
        }

        public short readShort() throws IOException {

            return (short) readChar();
        }

        public String readUTF() throws IOException {

            int len = readChar();
            StringBuffer s = new StringBuffer( len );
            while (--len >= 0) {
                s.append( (char) readByte() );
            }
            return s.toString();
        }
    }

    private final class TransportOutputStream {

        private final Transport transport;

        public TransportOutputStream(Transport transport) {

            this.transport = transport;
        }

        public void flush() throws IOException {

            transport.flush();
        }

        public void writeByte( int i ) throws IOException {

            transport.writeByte( i );
        }

        public void writeInt( int i ) throws IOException {

            writeShort( i >> 16 );
            writeShort( i );
        }

        public void writeLong( long l ) throws IOException {

            writeInt( (int) ( l >> 32 ) );
            writeInt( (int) l );
        }

        public void writeShort( int i ) throws IOException {

            writeByte( i >> 8 );
            writeByte( i );
        }
    }
}
