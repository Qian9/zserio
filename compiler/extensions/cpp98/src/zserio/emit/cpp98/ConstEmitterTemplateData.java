package zserio.emit.cpp98;

import zserio.ast.Constant;
import zserio.emit.common.ExpressionFormatter;
import zserio.emit.common.ZserioEmitException;
import zserio.emit.cpp98.symbols.CppNativeSymbol;
import zserio.emit.cpp98.types.CppNativeType;

public class ConstEmitterTemplateData extends CppTemplateData
{
    public ConstEmitterTemplateData(TemplateDataContext context, Constant constant) throws ZserioEmitException
    {
        super(context);

        final CppNativeMapper cppNativeMapper = context.getCppNativeMapper();
        final ExpressionFormatter cppExpressionFormatter = context.getExpressionFormatter(
                new HeaderIncludeCollectorAdapter(this));

        final CppNativeSymbol constantNativeSymbol = cppNativeMapper.getCppSymbol(constant);
        packageData = new PackageTemplateData(constantNativeSymbol.getPackageName());
        name = constantNativeSymbol.getName();

        final CppNativeType nativeTargetType = cppNativeMapper.getCppType(constant.getTypeReference());
        addHeaderIncludesForType(nativeTargetType);

        cppTypeName = nativeTargetType.getFullName();
        value = cppExpressionFormatter.formatGetter(constant.getValueExpression());
    }

    public PackageTemplateData getPackage()
    {
        return packageData;
    }

    public String getName()
    {
        return name;
    }

    public String getCppTypeName()
    {
        return cppTypeName;
    }

    public String getValue()
    {
        return value;
    }

    private final PackageTemplateData packageData;
    private final String name;
    private final String cppTypeName;
    private final String value;
}