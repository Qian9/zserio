package zserio.ast;

/**
 * Implementation of ZserioAstVisitor which manages resolving phase.
 */
public class ZserioAstResolver extends ZserioAstWalker
{
    @Override
    public void visitPackage(Package pkg)
    {
        currentPackage = pkg;

        pkg.visitChildren(this);

        currentPackage = null;
    }

    @Override
    public void visitSubtype(Subtype subtype)
    {
        subtype.resolve();
        subtype.visitChildren(this);
    }

    @Override
    public void visitEnumType(EnumType enumType)
    {
        visitType(enumType);
    }

    @Override
    public void visitChoiceType(ChoiceType choiceType)
    {
        visitType(choiceType);
    }

    @Override
    public void visitSqlDatabaseType(SqlDatabaseType sqlDatabaseType)
    {
        visitType(sqlDatabaseType);
    }

    @Override
    public void visitSqlTableType(SqlTableType sqlTableType)
    {
        visitType(sqlTableType);
    }

    @Override
    public void visitStructureType(StructureType structureType)
    {
        visitType(structureType);
    }

    @Override
    public void visitUnionType(UnionType unionType)
    {
        visitType(unionType);
    }

    @Override
    public void visitServiceType(ServiceType serviceType)
    {
        visitType(serviceType);
    }

    @Override
    public void visitSqlConstraint(SqlConstraint sqlConstraint)
    {
        sqlConstraint.resolve(currentCompoundType);
        sqlConstraint.visitChildren(this);
    }

    @Override
    public void visitTypeReference(TypeReference typeReference)
    {
        typeReference.resolve();
        typeReference.visitChildren(this);
    }

    @Override
    public void visitDocTagSee(DocTagSee docTagSee)
    {
        docTagSee.resolve(currentPackage, currentScopedType);
        docTagSee.visitChildren(this);
    }

    private void visitType(TemplatableCompoundType templatableCompoundType)
    {
        if (templatableCompoundType.getTemplateParameters().isEmpty())
            visitType((CompoundType)templatableCompoundType);
        else
            visitInstantiations(templatableCompoundType);
    }

    private void visitType(CompoundType compoundType)
    {
        currentScopedType = compoundType;
        currentCompoundType = compoundType;

        compoundType.visitChildren(this);

        currentScopedType = null;
        currentCompoundType = null;
    }

    private void visitType(ZserioScopedType scopedType)
    {
        currentScopedType = scopedType;

        scopedType.visitChildren(this);

        currentScopedType = null;
    }

    private void visitInstantiations(ZserioTemplatableType template)
    {
        for (ZserioTemplatableType instantiation : template.getInstantiations())
            instantiation.accept(this);
    }

    private Package currentPackage = null;
    private CompoundType currentCompoundType = null;
    private ZserioScopedType currentScopedType = null;
};
