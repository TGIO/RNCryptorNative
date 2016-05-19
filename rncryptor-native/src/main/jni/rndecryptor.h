#ifndef RNDECRYPTOR_H
#define RNDECRYPTOR_H

#include "rncryptor.h"

#include <iostream>
using std::string;

class RNDecryptor : public RNCryptor {

	RNCryptorPayloadComponents unpackEncryptedBase64Data(string encryptedBase64);
	bool hmacIsValid(RNCryptorPayloadComponents components, string password);

	public:
		string decrypt(string encryptedBase64, string password);
};

#endif
