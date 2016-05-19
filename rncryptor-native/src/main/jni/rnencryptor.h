#ifndef RNENCRYPTOR_H
#define RNENCRYPTOR_H

#include "rncryptor.h"

#include <iostream>
using std::string;

class RNEncryptor : public RNCryptor {
	string generateIv(int length);
	string generateSalt();

	public:
		string encrypt(string plaintext, string password, RNCryptorSchema schemaVersion = SCHEMA_2);
};

#endif
