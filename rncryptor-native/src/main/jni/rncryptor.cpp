#include "rncryptor.h"
#include <sstream>
using std::stringstream;

#include <iostream>
using std::cout;
using std::endl;

#include <string>
using std::string;

#include "cryptopp/pwdbased.h"
using CryptoPP::PKCS5_PBKDF2_HMAC;

#include "cryptopp/sha.h"
using CryptoPP::SHA1;
using CryptoPP::SHA256;

#include "cryptopp/filters.h"
using CryptoPP::StringSink;
using CryptoPP::StringSource;
using CryptoPP::HashFilter;

#include "cryptopp/hmac.h"
using CryptoPP::HMAC;

#include "cryptopp/hex.h"
using CryptoPP::HexEncoder;

#include "cryptopp/secblock.h"
using CryptoPP::SecByteBlock;

#include "cryptopp/base64.h"
using CryptoPP::Base64Encoder;
using CryptoPP::Base64Decoder;

RNCryptor::RNCryptor() {
	configureSettings(SCHEMA_2);
}

void RNCryptor::configureSettings(RNCryptorSchema schemaVersion)
{
	switch (schemaVersion) {

		case SCHEMA_0:
			aesMode = MODE_CTR;
			options = OPTIONS_0;
			hmac_includesHeader = false;
			hmac_includesPadding = true;
			hmac_algorithm = HMAC_SHA1;
			break;

		case SCHEMA_1:
			aesMode = MODE_CBC;
			options = OPTIONS_1;
			hmac_includesHeader = false;
			hmac_includesPadding = false;
			hmac_algorithm = HMAC_SHA256;
			break;

		case SCHEMA_2:
			aesMode = MODE_CBC;
			options = OPTIONS_1;
			hmac_includesHeader = true;
			hmac_includesPadding = false;
			hmac_algorithm = HMAC_SHA256;
			break;
	}
}

string RNCryptor::generateHmac(RNCryptorPayloadComponents components, string password)
{
	stringstream hmacMessage;
	if (hmac_includesHeader) {
		hmacMessage << components.schema;
		hmacMessage << components.options;
		hmacMessage << components.salt;
		hmacMessage << components.hmacSalt;
		hmacMessage << components.iv;
	}
	hmacMessage << components.ciphertext;

	SecByteBlock key = this->generateKey(components.hmacSalt, password);

	string hmac;

	//cout << endl << "--- Generate HMAC ---" << endl;

	switch (RNCryptor::hmac_algorithm) {

		case HMAC_SHA1: {
			//cout << "Algo: SHA1" << endl;

			HMAC<SHA1> hmac_sha1(key, key.size());

			StringSource(hmacMessage.str(), true,
				new HashFilter(hmac_sha1,
					new StringSink(hmac)
				)
			);
			break;
		}
		case HMAC_SHA256: {
			//cout << "Algo: SHA256" << endl;

			HMAC<SHA256> hmac_sha256(key, key.size());

			StringSource(hmacMessage.str(), true,
				new HashFilter(hmac_sha256,
					new StringSink(hmac)
				)
			);
			break;
		}
	}

	if (this->hmac_includesPadding && (int)hmac.length() < this->hmac_length) {
		stringstream padding;
		for (int i = hmac.length(); i < this->hmac_length; i++) {
			padding << 0x00;
		}
		hmac.append(padding.str());
	}

	/*
	cout << "HMAC Message: " << RNCryptor::base64_encode(hmacMessage.str()) << endl;
	cout << "HMAC: " << RNCryptor::base64_encode(hmac) << endl;
	cout << endl;
	*/

	return hmac;
}

SecByteBlock RNCryptor::generateKey(const string salt, const string password)
{
	SecByteBlock key(RNCryptor::pbkdf2_keyLength);

	switch (RNCryptor::pbkdf2_prf) {
		case PRF_SHA1: {
			PKCS5_PBKDF2_HMAC<SHA1> pbkdf;
			pbkdf.DeriveKey(
				// buffer that holds the derived key
				key, key.size(),
				// purpose byte. unused by this PBKDF implementation.
				0x00,
				// password bytes. careful to be consistent with encoding...
				(const byte *)password.data(), password.size(),
				// salt bytes
				(const byte *)salt.data(), salt.size(),
				// iteration count. See SP 800-132 for details. You want this as large as you can tolerate.
				// make sure to use the same iteration count on both sides...
				RNCryptor::pbkdf2_iterations
			);

			break;
		}
	}

	/*
	cout << endl << "--- Generate Key ---" << endl;
	cout << "Password: " << password << endl;
	cout << "Salt: " << RNCryptor::base64_encode(salt) << endl;
	cout << "PBKDF2 hashed key: " << RNCryptor::base64_encode((char *)key.BytePtr()) << endl;
	cout << endl;
	*/

	return key;
}

string RNCryptor::base64_decode(string encoded) {
	string plaintext;
	plaintext.clear();
	StringSource(encoded, true,
		new Base64Decoder(
			new StringSink(plaintext)
		)
	);
	return plaintext;
}

string RNCryptor::base64_encode(string plaintext) {

	string encoded;
	encoded.clear();
	StringSource(plaintext, true,
		new Base64Encoder(
			new StringSink(encoded),
			false
		)
	);
	return encoded;
}

string RNCryptor::hex_encode(string plaintext) {

	string encoded;
	encoded.clear();
	StringSource(plaintext, true,
		new HexEncoder(
			new StringSink(encoded),
			false
		)
	);
	return encoded;
}
