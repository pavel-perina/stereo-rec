#pragma once

#include <vector>
#include <limits>

/////////////////////////////////////////////////////////////////////////////

//! \brief Pattern search result
class PsResult final
{
public:
	int x;       //!< \brief vector x component
	int y;		 //!< \brief vector y component
	double cost; //!< \brief cost
	double quality; //! < \brief Difference between minimal and maximal cost in last search (quality criteria) 

	//! \brief Constructor
	PsResult()
		: x(0)
		, y(0)
		, cost(std::numeric_limits<double>::max())
		, quality(0.0)
	{
	}
	
};

/////////////////////////////////////////////////////////////////////////////

//! \brief Pattern search result matrix / 2D array
template<typename T>
class PsArray2x2 final
{
public:
	//! \brief Constructor
	PsArray2x2()
		: m_rows(0)
		, m_cols(0)
		, m_data()
	{
	}

	//! \brief Constructor
	explicit PsArray2x2(int rows, int cols)
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
	__inline T &operator() (int row, int col)
	{
		return m_data[offset(row, col)];
	}

	//! \brief Getter
	__inline const T &operator() (int row, int col) const
	{
		return m_data[offset(row, col)];
	}

private:

	int m_rows;
	int m_cols;
	std::vector<T> m_data;
};
