/**
 * Automatically generated by Zserio C++ extension version 1.2.0.
 */

#ifndef TEMPLATES_U8_H
#define TEMPLATES_U8_H

#include <type_traits>
#include <zserio/BitStreamReader.h>
#include <zserio/BitStreamWriter.h>
#include <zserio/PreWriteAction.h>
#include <zserio/Types.h>

namespace templates
{

class U8
{
public:
    U8() noexcept;

    explicit U8(
            uint8_t value_) :
            m_value_(value_)
    {
    }

    explicit U8(::zserio::BitStreamReader& in);

    ~U8() = default;

    U8(const U8&) = default;
    U8& operator=(const U8&) = default;

    U8(U8&&) = default;
    U8& operator=(U8&&) = default;

    uint8_t getValue() const;
    void setValue(uint8_t value_);

    size_t bitSizeOf(size_t bitPosition = 0) const;
    size_t initializeOffsets(size_t bitPosition);

    bool operator==(const U8& other) const;
    int hashCode() const;

    void read(::zserio::BitStreamReader& in);
    void write(::zserio::BitStreamWriter& out,
            ::zserio::PreWriteAction preWriteAction = ::zserio::ALL_PRE_WRITE_ACTIONS);

private:
    uint8_t readValue(::zserio::BitStreamReader& in);

    uint8_t m_value_;
};

} // namespace templates

#endif // TEMPLATES_U8_H
