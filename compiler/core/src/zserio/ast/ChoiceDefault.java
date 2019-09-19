package zserio.ast;

import java.util.List;


/**
 * AST node for default case defined by choice types.
 */
public class ChoiceDefault extends AstNodeWithDoc
{
    /**
     * Constructor.
     *
     * @param location     AST node location.
     * @param defaultField Default field associated to this default case or null if it's not defined.
     * @param docComment   Documentation comment belonging to this node.
     */
    public ChoiceDefault(AstLocation location, Field defaultField, DocComment docComment)
    {
        super(location, docComment);

        this.defaultField = defaultField;
    }

    @Override
    public void accept(ZserioAstVisitor visitor)
    {
        visitor.visitChoiceDefault(this);
    }

    @Override
    public void visitChildren(ZserioAstVisitor visitor)
    {
        if (defaultField != null)
            defaultField.accept(visitor);

        super.visitChildren(visitor);
    }

    /**
     * Gets field defined by the default choice case.
     *
     * @return Field defined by the default choice case or null if the default case is not defined.
     */
    public Field getField()
    {
        return defaultField;
    }

    ChoiceDefault instantiate(List<String> templateParameters, List<ZserioType> templateArguments)
    {
        final Field instantiatedDefaultField = defaultField == null ? null :
                defaultField.instantiate(templateParameters, templateArguments);

        return new ChoiceDefault(getLocation(), instantiatedDefaultField, getDocComment());
    }

    private final Field defaultField;
}
