package templates.expression_full_template_argument;

import templates.expression_full_template_argument.color.Color;

enum uint8 Color
{
    BLACK,
    WHITE
};

struct FullTemplateArgument<E>
{
    bool    boolField;
    int32   expressionField if valueof(E.BLACK) == 0;
};

struct FullTemplateArgumentHolder
{
    FullTemplateArgument<Color> TemplateArgumentInternal;
    FullTemplateArgument<templates.expression_full_template_argument.color.Color> TemplateArgumentExternal;
};
