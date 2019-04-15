package zserio.ast4;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;


/**
 * AST node for choice types.
 *
 * Choice types are Zserio types as well.
 */
public class ChoiceType extends CompoundType
{
    public ChoiceType(Token token, Package pkg, String name, List<Parameter> parameters,
            Expression selectorExpression, List<ChoiceCase> choiceCases, ChoiceDefault choiceDefault,
            List<FunctionType> functions)
    {
        super(token, pkg, name, parameters, getChoiceFields(choiceCases, choiceDefault), functions);

        this.selectorExpression = selectorExpression;
        this.choiceCases = choiceCases;
        this.choiceDefault = choiceDefault;
    }

    @Override
    public void accept(ZserioVisitor visitor)
    {
        visitor.visitChoiceType(this);
    }

    @Override
    public void visitChildren(ZserioVisitor visitor)
    {
        for (Parameter parameter : getParameters())
            parameter.accept(visitor);

        selectorExpression.accept(visitor);

        for (ChoiceCase choiceCase : choiceCases)
            choiceCase.accept(visitor);

        if (choiceDefault != null)
            choiceDefault.accept(visitor);

        for (FunctionType function : getFunctions())
            function.accept(visitor);
    }

    /*@Override
    public <T extends ZserioType> Set<T> getReferencedTypes(Class<? extends T> clazz)
    {
        final Set<T> referencedTypes = super.getReferencedTypes(clazz);

        // add choice-specific expressions: selector expression
        referencedTypes.addAll(selectorExpression.getReferencedSymbolObjects(clazz));

        // add choice-specific expressions: case expressions
        for (ChoiceCase choiceCase : choiceCases)
        {
            final Iterable<ChoiceCase.CaseExpression> caseExpressions = choiceCase.getExpressions();
            for (ChoiceCase.CaseExpression caseExpression : caseExpressions)
                referencedTypes.addAll(caseExpression.getExpression().getReferencedSymbolObjects(clazz));
        }

        return referencedTypes;
    }*/ // TODO:

    /**
     * Extends scope for case expressions to support enumeration values.
     *
     * This method is called from expression evaluator generated by ANTLR.
     *
     * @param scope Scope which shall be added to case expression scopes.
     */
    /*public void addScopeForCaseExpressions(Scope scope)
    {
        for (ChoiceCase choiceCase : choiceCases)
        {
            final List<ChoiceCase.CaseExpression> caseExpressions = choiceCase.getExpressions();
            for (ChoiceCase.CaseExpression caseExpression : caseExpressions)
            {
                final Expression expression = caseExpression.getExpression();
                expression.addScope(scope);
            }
        }
    }*/ // TODO:

    /**
     * Gets selector expression.
     *
     * Selector expression is compulsory for choice types, therefore this method cannot return null.
     *
     * @return Returns expressions which is given as choice selector.
     */
    public Expression getSelectorExpression()
    {
        return selectorExpression;
    }

    /**
     * Gets list of choice cases defined by the choice.
     *
     * @return List of choice cases.
     */
    public Iterable<ChoiceCase> getChoiceCases()
    {
        return choiceCases;
    }

    /**
     * Gets default case defined by the choice.
     *
     * @return Default case or null if default case is not defined.
     */
    public ChoiceDefault getChoiceDefault()
    {
        return choiceDefault;
    }

    /**
     * Checks if default case in choice can happen.
     *
     * Actually, only boolean choices can have default case unreachable. This can happen only if the bool
     * choice has defined both cases (true and false).
     *
     * @return Returns true if default case is unreachable.
     */
    public boolean isChoiceDefaultUnreachable()
    {
        return isChoiceDefaultUnreachable;
    }

    private static List<Field> getChoiceFields(List<ChoiceCase> choiceCases, ChoiceDefault choiceDefault)
    {
        List<Field> fields = new ArrayList<Field>();
        for (ChoiceCase choiceCase : choiceCases)
        {
            if (choiceCase.getField() != null)
                fields.add(choiceCase.getField());
        }
        if (choiceDefault != null && choiceDefault.getField() != null)
            fields.add(choiceDefault.getField());
        return fields;
    }

    /*@Override
    protected void check() throws ParserException
    {
        super.check();
        checkTableFields();

        checkSelectorType();
        checkCaseTypes();
        checkDuplicatedCases();
        checkEnumerationCases();
        isChoiceDefaultUnreachable = checkUnreachableDefault();
    }*/ // TODO:

    /*private void checkSelectorType() throws ParserException
    {
        final Expression.ExpressionType selectorExpressionType = selectorExpression.getExprType();
        if (selectorExpressionType != Expression.ExpressionType.INTEGER &&
            selectorExpressionType != Expression.ExpressionType.BOOLEAN &&
            selectorExpressionType != Expression.ExpressionType.ENUM)
            throw new ParserException(this, "Choice '" + getName() + "' uses forbidden " +
                    selectorExpressionType.name() + " selector!");
    }*/

    /*private void checkCaseTypes() throws ParserException
    {
        final Expression.ExpressionType selectorExpressionType = selectorExpression.getExprType();
        for (ChoiceCase choiceCase : choiceCases)
        {
            final List<ChoiceCase.CaseExpression> caseExpressions = choiceCase.getExpressions();
            for (ChoiceCase.CaseExpression caseExpression : caseExpressions)
            {
                final Expression expression = caseExpression.getExpression();
                if (expression.getExprType() != selectorExpressionType)
                    throw new ParserException(expression, "Choice '" + getName() +
                            "' has incompatible case type!");

                if (!expression.getReferencedSymbolObjects(Parameter.class).isEmpty())
                    throw new ParserException(expression, "Choice '" + getName() +
                            "' has non-constant case expression!");
            }
        }
    }

    private void checkDuplicatedCases() throws ParserException
    {
        final List<Expression> allExpressions = new ArrayList<Expression>();
        for (ChoiceCase choiceCase : choiceCases)
        {
            final List<ChoiceCase.CaseExpression> newCaseExpressions = choiceCase.getExpressions();
            for (ChoiceCase.CaseExpression newCaseExpression : newCaseExpressions)
            {
                final Expression newExpression = newCaseExpression.getExpression();
                for (Expression caseExpression : allExpressions)
                {
                    if (newExpression.equals(caseExpression))
                        throw new ParserException(newExpression, "Choice '" + getName() +
                                "' has duplicated case!");
                }
                allExpressions.add(newExpression);
            }
        }
    }

    private void checkEnumerationCases() throws ParserException
    {
        final ZserioType selectorExpressionType = selectorExpression.getExprZserioType();
        if (selectorExpressionType instanceof EnumType)
        {
            final EnumType resolvedEnumType = (EnumType)TypeReference.resolveType(selectorExpressionType);
            final List<EnumItem> availableEnumItems = new ArrayList<EnumItem>(resolvedEnumType.getItems());

            for (ChoiceCase choiceCase : choiceCases)
            {
                final List<ChoiceCase.CaseExpression> caseExpressions = choiceCase.getExpressions();
                for (ChoiceCase.CaseExpression caseExpression : caseExpressions)
                {
                    final Expression expression = caseExpression.getExpression();
                    final Set<EnumItem> referencedEnumItems =
                            expression.getReferencedSymbolObjects(EnumItem.class);
                    for (EnumItem referencedEnumItem : referencedEnumItems)
                        if (!availableEnumItems.remove(referencedEnumItem))
                            throw new ParserException(expression, "Choice '" + getName() +
                                    "' has case with different enumeration type than selector!");
                }
            }

            if (choiceDefault == null)
            {
                for (EnumItem availableEnumItem : availableEnumItems)
                {
                    ZserioToolPrinter.printWarning(this, "Enumeration value '" +
                            availableEnumItem.getName() + "' is not handled in choice '" + getName() +
                            "'.");
                }
            }
        }
    }

    private boolean checkUnreachableDefault() throws ParserException
    {
        boolean isDefaulUnreachable = false;
        if (selectorExpression.getExprType() == Expression.ExpressionType.BOOLEAN && numCases() > 1)
        {
            if (choiceDefault != null)
                throw new ParserException(choiceDefault, "Choice '" + getName() +
                        "' has unreachable default case!");

            isDefaulUnreachable = true;
        }

        return isDefaulUnreachable;
    }

    private int numCases()
    {
        int numCases = 0;
        for (ChoiceCase choiceCase : choiceCases)
            numCases += choiceCase.getExpressions().size();
        return numCases;
    }*/

    private final Expression selectorExpression;
    private final List<ChoiceCase> choiceCases;
    private final ChoiceDefault choiceDefault;

    private boolean isChoiceDefaultUnreachable;
}
