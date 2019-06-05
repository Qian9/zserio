/**
 * Automatically generated by Zserio C++ extension version 1.2.0.
 */

#ifndef ENUMERATION_TYPES_BITFIELD_ENUM_COLOR_H
#define ENUMERATION_TYPES_BITFIELD_ENUM_COLOR_H

#include <type_traits>
#include <array>

namespace enumeration_types
{
namespace bitfield_enum
{

enum class Color : uint8_t
{
    NONE = UINT8_C(0),
    RED = UINT8_C(2),
    BLUE = UINT8_C(3),
    BLACK = UINT8_C(7)
};

} // namespace bitfield_enum
} // namespace enumeration_types

// This should be implemented in runtime library header.
namespace zserio
{

template<typename T>
struct EnumTraits
{
};

template<typename ENUM_TYPE>
size_t enumToOrdinal(ENUM_TYPE value);

template<typename ENUM_TYPE>
ENUM_TYPE valueToEnum(typename std::underlying_type<ENUM_TYPE>::type rawValue);

template<typename ENUM_TYPE>
typename std::underlying_type<ENUM_TYPE>::type enumToValue(ENUM_TYPE value)
{
    return static_cast<typename std::underlying_type<ENUM_TYPE>::type>(value);
}

template<typename ENUM_TYPE>
const char* enumToString(ENUM_TYPE value)
{
    return EnumTraits<ENUM_TYPE>::names[enumToOrdinal(value)];
}

} // namespace zserio

// This is full specialization for Color enumeration.
namespace zserio
{

template<>
size_t enumToOrdinal<enumeration_types::bitfield_enum::Color>(enumeration_types::bitfield_enum::Color value);

template<>
enumeration_types::bitfield_enum::Color valueToEnum<enumeration_types::bitfield_enum::Color>(
        typename std::underlying_type<enumeration_types::bitfield_enum::Color>::type rawValue);

template<>
struct EnumTraits<enumeration_types::bitfield_enum::Color>
{
    static constexpr std::array<const char*, 4> names =
    {
        "NONE",
        "RED",
        "BLUE",
        "BLACK"
    };

    static constexpr std::array<enumeration_types::bitfield_enum::Color, 4> values =
    {
        enumeration_types::bitfield_enum::Color::NONE,
        enumeration_types::bitfield_enum::Color::RED,
        enumeration_types::bitfield_enum::Color::BLUE,
        enumeration_types::bitfield_enum::Color::BLACK
    };
};

} // namespace zserio

#endif // ENUMERATION_TYPES_BITFIELD_ENUM_COLOR_H
