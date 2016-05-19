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
#include <jni.h>
#include <stddef.h>

#ifndef rncryptor_RNCRYPTO_NATIVE_H
#define rncryptor_RNCRYPTO_NATIVE_H


#ifdef __cplusplus
extern "C" {
#endif
    JNIEXPORT jstring JNICALL Java_tgio_rncryptor_RNCryptorNative_getABI(JNIEnv* env, jobject thiz);
    JNIEXPORT jbyteArray JNICALL Java_tgio_rncryptor_RNCryptorNative_encrypt(JNIEnv *env, jobject instance, jstring raw_, jstring password_);
    JNIEXPORT jstring JNICALL Java_tgio_rncryptor_RNCryptorNative_decrypt(JNIEnv *env, jobject instance, jstring encrypted_, jstring password_);
#ifdef __cplusplus
}
#endif
#endif