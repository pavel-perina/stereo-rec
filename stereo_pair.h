#pragma once
#include <memory>
#include <string>

// forward declaration(s):
class StereoPairPriv;

class StereoPair final
{
public:
	
	//! \brief Constructor
	StereoPair();

	//! \brief Read anaglyph from red and green image channel
	bool readAnaglyph(const std::string &fileNameUtf8);

	bool readAnaglyph(const std::string &leftFileNameUtf8, const std::string &rightFileNameUtf8);

private:

	std::unique_ptr<StereoPairPriv> p;



};