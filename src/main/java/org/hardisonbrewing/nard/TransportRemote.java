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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TransportRemote implements Transport {

    private Socket Y;
    private Socket V;
    private InputStream e;
    private OutputStream c;
    private OutputStream Z;
    private InputStream X;
    private int a;

    public boolean connect() throws IOException {

        String ip = "192.168.1.121";

        System.out.println( "first" );
        Y = new Socket( ip, 3563 );
        Y.setTcpNoDelay( true );
        e = new BufferedInputStream( Y.getInputStream() );
        c = new BufferedOutputStream( Y.getOutputStream() );

        System.out.println( "second" );
        V = new Socket( ip, 3563 );
        V.setTcpNoDelay( true );
        X = V.getInputStream();
        Z = V.getOutputStream();

        return true;
    }

    public void close() {

        if ( Y != null ) {
            try {
                Y.close();
            }
            catch (Exception e) {
                // do nothing
            }
            Y = null;
        }

        if ( V != null ) {
            try {
                V.close();
            }
            catch (Exception e) {
                // do nothing
            }
            V = null;
        }

        if ( e != null ) {
            try {
                e.close();
            }
            catch (Exception e) {
                // do nothing
            }
            e = null;
        }

        if ( c != null ) {
            try {
                c.close();
            }
            catch (Exception e) {
                // do nothing
            }
            c = null;
        }

        if ( X != null ) {
            try {
                X.close();
            }
            catch (Exception e) {
                // do nothing
            }
            X = null;
        }

        if ( Z != null ) {
            try {
                Z.close();
            }
            catch (Exception e) {
                // do nothing
            }
            Z = null;
        }
    }

    public void writeByte( int i ) throws IOException {

        c.write( i );
        a++;
    }

    public void flush() throws IOException {

        c.flush();
        Z.write( 2 );
        Z.write( a >> 8 & 0xff );
        Z.write( a & 0xff );
        a = 0;
    }

    public int readByte() throws IOException {

        Z.write( 3 );
        return e.read();
    }
}
