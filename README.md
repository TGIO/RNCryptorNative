[![Codacy Badge](https://api.codacy.com/project/badge/Grade/99ac4e30d4234c55bc7501a5a6f1da34)](https://www.codacy.com/app/gio-caporegime/RNCryptorNative?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TGIO/RNCryptorNative&amp;utm_campaign=Badge_Grade)

## RNCryptorNative

Android JNI model implementation of Rob Napier's RNCryptor.


## Import guide

```
dependencies {
    compile 'com.github.tgio:rncryptor-native:0.0.7'
}
```

## Usage

  ```java
  
RNCryptorNative rncryptor = new RNCryptorNative();

String encrypted = new String(rncryptor.encrypt(raw, password));

String decrypted = rncryptor.decrypt(encrypted, password)

  ```
  
  or

  ```java
  
String password = "StrongGeneratedPasswordxD";
  
RNCryptorNative.encryptAsync("test", password, new RNCryptorNative.Callback() {
    @Override
    public void done(String encrypted, Exception e) {
        System.out.println("encrypted async: " + encrypted);
    }
});

//Decrypt
RNCryptorNative.decryptAsync(encrypted, password, new RNCryptorNative.Callback() {
    @Override
    public void done(String decrypted, Exception e) {
        System.out.println("decrypted async: " + decrypted);
    }
});
  ```
#### Benchmark:

You might know that JNCryptor (Java port for RNCryptor) already exists. But there is a huge problem - perfomance. This is why RNCryptorNative is better solution. Screenshots of benchmarks: 

| <img src="screenshots/1.png" width="250"/> | <img src="screenshots/25.png" width="250"/> |
  
## Credits

[Rob Napier](https://github.com/rnapier)

[RNCryptor Team](https://github.com/RNCryptor)

[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)