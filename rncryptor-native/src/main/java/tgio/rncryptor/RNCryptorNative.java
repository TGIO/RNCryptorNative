package tgio.rncryptor;

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

    public interface RNCryptorNativeCallback {
        void done(String result, Exception e);
    }
}
