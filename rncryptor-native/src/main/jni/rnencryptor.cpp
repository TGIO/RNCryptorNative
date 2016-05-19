#include "rnencryptor.h"

#include <sstream>
#include <jni.h>

using std::stringstream;

#include "cryptopp/osrng.h"
using CryptoPP::AutoSeededRandomPool;

#include "cryptopp/aes.h"
using CryptoPP::AES;

#include "cryptopp/modes.h"
using CryptoPP::CBC_Mode;
using CryptoPP::CTR_Mode;

#include "cryptopp/filters.h"
using CryptoPP::StreamTransformationFilter;
using CryptoPP::StringSink;
using CryptoPP::StringSource;

string RNEncryptor::encrypt(string plaintext, string password, RNCryptorSchema schemaVersion)
{
	this->configureSettings(schemaVersion);

	RNCryptorPayloadComponents components;
	components.schema = (char)schemaVersion;
	components.options = (char)this->options;
	components.salt = this->generateSalt();
	components.hmacSalt = this->generateSalt();
	components.iv = this->generateIv(this->ivLength);

	SecByteBlock key = this->generateKey(components.salt, password);

	switch (this->aesMode) {
		case MODE_CTR: {

			CTR_Mode<AES>::Encryption encryptor;
			encryptor.SetKeyWithIV((const byte *)key.data(), key.size(), (const byte *)components.iv.data());

			StringSource(plaintext, true,
				// StreamTransformationFilter adds padding as required.
				new StreamTransformationFilter(encryptor,
					new StringSink(components.ciphertext)
				)
			);

			break;
		}
		case MODE_CBC: {

			CBC_Mode<AES>::Encryption encryptor;
			encryptor.SetKeyWithIV(key.BytePtr(), key.size(), (const byte *)components.iv.data());

			StringSource(plaintext, true,
				// StreamTransformationFilter adds padding as required.
				new StreamTransformationFilter(encryptor,
					new StringSink(components.ciphertext)
				)
			);

			break;
		}
	}

	stringstream binaryData;
	binaryData << components.schema;
	binaryData << components.options;
	binaryData << components.salt;
	binaryData << components.hmacSalt;
	binaryData << components.iv;
	binaryData << components.ciphertext;

	std::cout << "Hex encoded: " << this->hex_encode(binaryData.str()) << std::endl;

	binaryData << this->generateHmac(components, password);

	return this->base64_encode(binaryData.str());
}

string RNEncryptor::generateSalt()
{
	return this->generateIv(this->saltLength);
}

string RNEncryptor::generateIv(int length)
{
	AutoSeededRandomPool prng;

	byte iv[length];
	prng.GenerateBlock(iv, length);

	return string((char *)iv, length);
}