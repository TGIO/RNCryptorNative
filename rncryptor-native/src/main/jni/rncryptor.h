#ifndef RNCRYPTOR_H
#define RNCRYPTOR_H

#include <iostream>

#include <string>
using std::string;

#include "cryptopp/secblock.h"
using CryptoPP::SecByteBlock;


enum RNCryptorAesMode {
	MODE_CTR, MODE_CBC
};

enum RNCryptorPbkdf2Prf {
	PRF_SHA1
};

enum RNCryptorHmacAlgorithm {
	HMAC_SHA1, HMAC_SHA256
};

enum RNCryptorAlgorithm {
	ALGO_RIJNDAEL_128
};

enum RNCryptorSchema {
	SCHEMA_0, SCHEMA_1, SCHEMA_2
};

enum RNCryptorOptions {
	OPTIONS_0, OPTIONS_1
};

struct RNCryptorPayloadComponents {
	string schema;
	string options;
	string salt;
	string hmacSalt;
	string iv;
	int header_length;
	string hmac;
	string ciphertext;
};

class RNCryptor {
	//void deriveKey(CryptoPP::PKCS5_PBKDF2_HMAC<CryptoPP::HashTransformation> pbkdf, CryptoPP::SecByteBlock key, const char * salt, const string password);

	protected:

		RNCryptorAesMode aesMode;
		RNCryptorOptions options;
		bool hmac_includesHeader;
		bool hmac_includesPadding;
		RNCryptorHmacAlgorithm hmac_algorithm;

		string generateHmac(RNCryptorPayloadComponents components, string password);
		SecByteBlock generateKey(const string salt, const string password);

		static string base64_encode(string plaintext);
		static string base64_decode(string encoded);

		static string hex_encode(string plaintext);

	public:
		static const RNCryptorAlgorithm algorithm = ALGO_RIJNDAEL_128;
		static const short saltLength = 8;
		static const short ivLength = 16;
		static const RNCryptorPbkdf2Prf pbkdf2_prf = PRF_SHA1;
		static const int pbkdf2_iterations = 10000;
		static const short pbkdf2_keyLength = 32;
		static const short hmac_length = 32;

		RNCryptor();
		void configureSettings(RNCryptorSchema schemaVersion);
};

#endif
