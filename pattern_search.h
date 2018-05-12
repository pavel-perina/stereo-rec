#pragma once
#include <memory>
#include <string>
#include <vector>

// forward declaration(s):
class PatternSearchPriv;

/////////////////////////////////////////////////////////////

//! \brief Pattern search result
class PsResult final
{
public:
	int x;       //!< \brief vector x component
	int y;		 //!< \brief vector y component
	double cost; //!< \brief cost
	double quality; //! < \brief Difference between minimal and maximal cost in last search (quality criteria) 

	//! \brief Constructor
	PsResult();
};

//////////////////////////////////////////////////////////

class PsResultMatrix final
{
public:
	//! \brief Constructor
	PsResultMatrix()
		: m_rows(0)
		, m_cols(0)
	{
	}

	//! \brief Constructor
	explicit PsResultMatrix(int rows, int cols)
		: m_rows(rows)
		, m_cols(cols)
	{
		m_data.resize(rows*cols);
	}

	//! \brief Number of rows (height)
	__inline int rows() const
	{
		return m_rows;
	}

	//! \brief Number of columns (width)
	__inline int cols() const
	{
		return m_cols;
	}

	//! \brief Get offset of row, col
	__inline size_t offset(int row, int col) const
	{
		return (size_t)row * m_cols + col;
	}

	//! \brief Setter 
	__inline PsResult &operator() (int row, int col)
	{
		return m_data[offset(row, col)];
	}

	//! \brief Getter
	__inline const PsResult &operator() (int row, int col) const
	{
		return m_data[offset(row, col)];
	}
private:

	int m_rows;
	int m_cols;
	std::vector<PsResult> m_data;
};

//////////////////////////////////////////////////////////


class PatternSearch final
{
public:
	
	//! \brief Constructor
	PatternSearch();

	//! \brief Read anaglyph from red and green image channel
	bool readAnaglyph(const std::string &fileNameUtf8);

	bool readAnaglyph(const std::string &leftFileNameUtf8, const std::string &rightFileNameUtf8);

private:

	std::unique_ptr<PatternSearchPriv> p;



};