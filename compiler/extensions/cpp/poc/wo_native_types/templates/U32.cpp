/**
 * Automatically generated by Zserio C++ extension version 1.2.0.
 */

#include <zserio/StringConvertUtil.h>
#include <zserio/CppRuntimeException.h>
#include <zserio/HashCodeUtil.h>
#include <zserio/BitPositionUtil.h>
#include <zserio/BitSizeOfCalculator.h>
#include <zserio/BitFieldUtil.h>

#include <templates/U32.h>

namespace templates
{

U32::U32() noexcept :
        m_value_(uint32_t())
{
}

U32::U32(::zserio::BitStreamReader& in) :
        m_value_(readValue(in))
{
}

uint32_t U32::getValue() const
{
    return m_value_;
}

void U32::setValue(uint32_t value_)
{
    m_value_ = value_;
}

size_t U32::bitSizeOf(size_t bitPosition) const
{
    size_t endBitPosition = bitPosition;

    endBitPosition += UINT8_C(32);

    return endBitPosition - bitPosition;
}

size_t U32::initializeOffsets(size_t bitPosition)
{
    size_t endBitPosition = bitPosition;

    endBitPosition += UINT8_C(32);

    return endBitPosition;
}

bool U32::operator==(const U32& other) const
{
    if (this != &other)
    {
        return
                (m_value_ == other.m_value_);
    }

    return true;
}

int U32::hashCode() const
{
    int result = ::zserio::HASH_SEED;

    result = ::zserio::calcHashCode(result, m_value_);

    return result;
}

void U32::read(::zserio::BitStreamReader& in)
{
    m_value_ = readValue(in);
}

void U32::write(::zserio::BitStreamWriter& out, ::zserio::PreWriteAction)
{
    out.writeBits(m_value_, UINT8_C(32));
}

uint32_t U32::readValue(::zserio::BitStreamReader& in)
{
    return static_cast<uint32_t>(in.readBits(UINT8_C(32)));
}

} // namespace templates
