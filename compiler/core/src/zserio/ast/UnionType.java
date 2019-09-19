package zserio.ast;

import java.util.ArrayList;
import java.util.List;


/**
 * AST node for Union types.
 *
 * Union types are Zserio types as well.
 */
public class UnionType extends TemplatableCompoundType
{
    /**
     * Constructor.
     *
     * @param location              AST node location.
     * @param pkg                   Package to which belongs the union type.
     * @param name                  Name of the union type.
     * @param templateParameters    List of template parameters.
     * @param typeParameters        List of parameters for the union type.
     * @param fields                List of all fields of the union type.
     * @param functions             List of all functions of the union type.
     * @param docComment            Documentation comment belonging to this node.
     */
    public UnionType(AstLocation location, Package pkg, String name, List<String> templateParameters,
            List<Parameter> typeParameters, List<Field> fields, List<FunctionType> functions,
            DocComment docComment)
    {
        super(location, pkg, name, templateParameters, typeParameters, fields, functions, docComment);
    }

    @Override
    public void accept(ZserioAstVisitor visitor)
    {
        visitor.visitUnionType(this);
    }

    @Override
    UnionType instantiateImpl(String name, List<ZserioType> templateArguments)
    {
        final List<Parameter> instantiatedTypeParameters = new ArrayList<Parameter>();
        for (Parameter typeParameter : getTypeParameters())
            instantiatedTypeParameters.add(typeParameter.instantiate(getTemplateParameters(), templateArguments));

        final List<Field> instantiatedFields = new ArrayList<Field>();
        for (Field field : getFields())
            instantiatedFields.add(field.instantiate(getTemplateParameters(), templateArguments));

        final List<FunctionType> instantiatedFunctions = new ArrayList<FunctionType>();
        for (FunctionType function : getFunctions())
            instantiatedFunctions.add(function.instantiate(getTemplateParameters(), templateArguments));

        return new UnionType(getLocation(), getPackage(), name, new ArrayList<String>(),
                instantiatedTypeParameters, instantiatedFields, instantiatedFunctions, getDocComment());
    }

    @Override
    void check()
    {
        super.check();
        checkTableFields();
    }
};
