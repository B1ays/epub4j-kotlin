package io.documentnode.epub4j.util.commons.io

import java.io.FilterInputStream
import java.io.IOException /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.InputStream

/**
 * A Proxy stream which acts as expected, that is it passes the method
 * calls on to the proxied stream and doesn't change which methods are
 * being called.
 *
 *
 * It is an alternative base class to FilterInputStream
 * to increase reusability, because FilterInputStream changes the
 * methods being called, such as read(byte[]) to read(byte[], int, int).
 *
 *
 * See the protected methods for ways in which a subclass can easily decorate
 * a stream with custom pre-, post- or error processing functionality.
 *
 * @author Stephen Colebourne
 * @version $Id: ProxyInputStream.java 934041 2010-04-14 17:37:24Z jukka $
 */
abstract class ProxyInputStream(proxy: InputStream) : FilterInputStream(proxy) {
    /**
     * Invokes the delegate's `read()` method.
     * @return the byte read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(): Int {
        try {
            beforeRead(1)
            val byte = `in`.read()
            afterRead(if (byte != -1) 1 else -1)
            return byte
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
    }

    /**
     * Invokes the delegate's `read(byte[])` method.
     * @param bytes the buffer to read the bytes into
     * @return the number of bytes read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(bytes: ByteArray): Int {
        try {
            beforeRead(bytes.size)
            val n = `in`.read(bytes)
            afterRead(n)
            return n
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
    }

    /**
     * Invokes the delegate's `read(byte[], int, int)` method.
     * @param bts the buffer to read the bytes into
     * @param off The start offset
     * @param len The number of bytes to read
     * @return the number of bytes read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(bts: ByteArray, off: Int, len: Int): Int {
        try {
            beforeRead(len)
            val n = `in`.read(bts, off, len)
            afterRead(n)
            return n
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
    }

    /**
     * Invokes the delegate's `skip(long)` method.
     * @param ln the number of bytes to skip
     * @return the actual number of bytes skipped
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun skip(ln: Long): Long {
        try {
            return `in`.skip(ln)
        } catch (e: IOException) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * Invokes the delegate's `available()` method.
     * @return the number of available bytes
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun available(): Int {
        try {
            return super.available()
        } catch (e: IOException) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * Invokes the delegate's `close()` method.
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    override fun close() {
        try {
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Invokes the delegate's `mark(int)` method.
     * @param readlimit read ahead limit
     */
    @Synchronized
    override fun mark(readlimit: Int) {
        `in`.mark(readlimit)
    }

    /**
     * Invokes the delegate's `reset()` method.
     * @throws IOException if an I/O error occurs
     */
    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        try {
            `in`.reset()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Invokes the delegate's `markSupported()` method.
     * @return true if mark is supported, otherwise false
     */
    override fun markSupported(): Boolean {
        return `in`.markSupported()
    }

    /**
     * Invoked by the read methods before the call is proxied. The number
     * of bytes that the caller wanted to read (1 for the [.read]
     * method, buffer length for [.read], etc.) is given as
     * an argument.
     *
     *
     * Subclasses can override this method to add common pre-processing
     * functionality without having to override all the read methods.
     * The default implementation does nothing.
     *
     *
     * Note this method is *not* called from [.skip] or
     * [.reset]. You need to explicitly override those methods if
     * you want to add pre-processing steps also to them.
     *
     * @since Commons IO 2.0
     * @param n number of bytes that the caller asked to be read
     * @throws IOException if the pre-processing fails
     */
    @Throws(IOException::class)
    protected fun beforeRead(n: Int) {
    }

    /**
     * Invoked by the read methods after the proxied call has returned
     * successfully. The number of bytes returned to the caller (or -1 if
     * the end of stream was reached) is given as an argument.
     *
     *
     * Subclasses can override this method to add common post-processing
     * functionality without having to override all the read methods.
     * The default implementation does nothing.
     *
     *
     * Note this method is *not* called from [.skip] or
     * [.reset]. You need to explicitly override those methods if
     * you want to add post-processing steps also to them.
     *
     * @since Commons IO 2.0
     * @param n number of bytes read, or -1 if the end of stream was reached
     * @throws IOException if the post-processing fails
     */
    @Throws(IOException::class)
    protected fun afterRead(n: Int) = Unit
}
