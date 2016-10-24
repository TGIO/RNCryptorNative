package tgio.rncryptor;

import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * Copyright (C) 2016 Giorgi TGIO Tabatadze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RNCryptorNative {

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("cryptopp_shared");
        System.loadLibrary("rncrypto");
    }

    /**
     * @return device architecture type
     */
    public native String getABI();

    /**
     * Encrypts raw string.
     * @param raw just a raw string
     * @param password strong generated password
     * @return base64 byte array of encryteped string
     */
    public native byte[] encrypt(String raw, String password);

    /**
     * Decrypts base64 string.
     * @param encrypted base64 string
     * @param password strong generated password
     * @return decrypted raw string
     */
    public native String decrypt(String encrypted, String password);


    /**
     * Decrypts encrypted base64 string and returns via callback
     * @param encrypted base64 string
     * @param password strong generated password
     * @param RNCryptorNativeCallback just a callback
     */
    public static void decryptAsync(String encrypted, String password, RNCryptorNativeCallback RNCryptorNativeCallback) {
        String decrypted;
        try {
            decrypted = new RNCryptorNative().decrypt(encrypted, password);
            RNCryptorNativeCallback.done(decrypted, null);
        } catch (Exception e) {
            RNCryptorNativeCallback.done(null, e);
        }
    }

    /**
     * Encrypts raw string and returns result via callback
     * @param raw just a raw string
     * @param password strong generated password
     * @param RNCryptorNativeCallback just a callback
     */
    public static void encryptAsync(String raw, String password, RNCryptorNativeCallback RNCryptorNativeCallback) {
        byte[] encrypted;
        try {
            encrypted = new RNCryptorNative().encrypt(raw, password);
            RNCryptorNativeCallback.done(new String(encrypted, "UTF-8"), null);
        } catch (Exception e) {
            RNCryptorNativeCallback.done(null, e);
        }

    }

    /**
     * Encrypts file using password
     * @param raw the original file to be encrypted
     * @param encryptedFile the result file
     * @param password strong generated password
     * @throws IOException
     */
    public static void encryptFile(File raw, File encryptedFile, String password) throws IOException {
        byte[] b = readBytes(raw);
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        byte[] encryptedBytes = new RNCryptorNative().encrypt(encodedImage, password);
        writeBytes(encryptedFile, encryptedBytes);
    }

    /**
     * Decrypts file using password
     * @param encryptedFile file which needs to be decrypted
     * @param result destination file for decrypted file
     * @param password strong generated password
     * @throws IOException
     */
    public static void decryptFile(File encryptedFile, File result, String password) throws IOException {
        byte[] b = readBytes(encryptedFile);
        String decryptedImageString = new RNCryptorNative().decrypt(new String(b), password);
        byte[] decodedBytes = Base64.decode(decryptedImageString, 0);
        writeBytes(result, decodedBytes);
    }


    private static byte[] readBytes(File file){
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    private static void writeBytes(File file, byte[] bytes) throws IOException{
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bos.write(bytes);
        bos.flush();
        bos.close();
    }

    public interface RNCryptorNativeCallback {
        void done(String result, Exception e);
    }
}
