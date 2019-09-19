package zserio.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AST node for cases defined by choice types.
 */
public class ChoiceCase extends AstNodeBase
{
    /**
     * Constructor.
     *
     * @param location        AST node location.
     * @param caseExpressions List of all case expressions associated to this choice case.
     * @param caseField       Case field associated to this choice case or null if it's not defined.
     */
    public ChoiceCase(AstLocation location, List<ChoiceCaseExpression> caseExpressions, Field caseField)
    {
        super(location);

        this.caseExpressions = caseExpressions;
        this.caseField = caseField;
    }

    @Override
    public void accept(ZserioAstVisitor visitor)
    {
        visitor.visitChoiceCase(this);
    }

    @Override
    public void visitChildren(ZserioAstVisitor visitor)
    {
        for (ChoiceCaseExpression caseExpression : caseExpressions)
            caseExpression.accept(visitor);
        if (caseField != null)
            caseField.accept(visitor);
    }

    /**
     * Gets expressions defined by the choice case.
     *
     * @return Choice case expression list.
     */
    public List<ChoiceCaseExpression> getExpressions()
    {
        return Collections.unmodifiableList(caseExpressions);
    }

    /**
     * Gets field defined by the choice case.
     *
     * @return Field defined by the choice case or null if this case is empty.
     */
    public Field getField()
    {
        return caseField;
    }

    ChoiceCase instantiate(List<String> templateParameters, List<ZserioType> templateArguments)
    {
        final Field instantiatedCaseField = caseField == null ? null :
                caseField.instantiate(templateParameters, templateArguments);

        final List<ChoiceCaseExpression> instantiatedCaseExpressions = new ArrayList<ChoiceCaseExpression>();
        for (ChoiceCaseExpression choiceCaseExpression : caseExpressions)
        {
            instantiatedCaseExpressions.add(
                    choiceCaseExpression.instantiate(templateParameters, templateArguments));
        }

        return new ChoiceCase(getLocation(), instantiatedCaseExpressions, instantiatedCaseField);
    }

    private final List<ChoiceCaseExpression> caseExpressions;
    private final Field caseField;
};
