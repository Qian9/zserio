package zserio.ast4;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import zserio.antlr.Zserio4Parser;
import zserio.antlr.Zserio4ParserBaseVisitor;

/**
 * Implementation of ZserioParserBaseVisitor which builds Zserio AST.
 */
public class ZserioAstBuilder extends Zserio4ParserBaseVisitor<Object>
{
    /**
     * Gets built AST.
     *
     * @return Root AST node.
     */
    public Root getAst()
    {
        return new Root(packageNameMap);
    }

    /**
     * Custom visit overload which should be called on the parse tree of a single package (translation unit).
     *
     * @param tree          Parse tree for a single package.
     * @param tokenStream   Token stream for a single translation unit.
     * @return Object       Result of the main rule of ZserioParser grammar.
     *                      Should be a package if the method was called on a correct parse tree.
     */
    public Object visit(ParseTree tree, BufferedTokenStream tokenStream)
    {
        docCommentManager.setStream(tokenStream);
        final Object result = visit(tree);
        docCommentManager.printWarnings();
        docCommentManager.resetStream();

        return result;
    }

    @Override
    public Package visitPackageDeclaration(Zserio4Parser.PackageDeclarationContext ctx)
    {
        // package
        final PackageName packageName = visitPackageNameDefinition(ctx.packageNameDefinition());
        final DocComment docComment = ctx.packageNameDefinition() != null ?
                docCommentManager.findDocComment(ctx.packageNameDefinition()) : null;

        // imports
        final List<Import> imports = new ArrayList<Import>();
        for (Zserio4Parser.ImportDeclarationContext importCtx : ctx.importDeclaration())
            imports.add(visitImportDeclaration(importCtx));

        // package instance
        final ParserRuleContext packageLocationCtx = ctx.packageNameDefinition() != null
                ? ctx.packageNameDefinition().qualifiedName() : ctx;
        localTypes = new LinkedHashMap<String, ZserioType>();
        currentPackage = new Package(packageLocationCtx.getStart(), packageName, imports, localTypes,
                docComment);
        if (packageNameMap.put(currentPackage.getPackageName(), currentPackage) != null)
        {
            // translation unit package already exists, this could happen only for default packages
            throw new ParserException(currentPackage, "Multiple default packages are not allowed!");
        }

        // types declarations
        for (Zserio4Parser.TypeDeclarationContext typeCtx : ctx.typeDeclaration())
        {
            ZserioType type = (ZserioType)visitTypeDeclaration(typeCtx);
            final String typeName = type.getName();
            final ZserioType addedType = localTypes.put(typeName, type);
            if (addedType != null)
                throw new ParserException(type, "'" + typeName + "' is already defined in this package!");
        }

        localTypes = null;
        final Package unitPackage = currentPackage;
        currentPackage = null;

        return unitPackage;
    }

    @Override
    public PackageName visitPackageNameDefinition(Zserio4Parser.PackageNameDefinitionContext ctx)
    {
        if (ctx != null)
            return createPackageName(ctx.qualifiedName().id());
        else
            return PackageName.EMPTY; // default package
    }

    @Override
    public Import visitImportDeclaration(Zserio4Parser.ImportDeclarationContext ctx)
    {
        String importedTypeName = null;
        PackageName importedPackageName = null;

        if (ctx.MULTIPLY() == null)
        {
            importedPackageName = createPackageName(getPackageNameIds(ctx.id()));
            importedTypeName = getTypeNameId(ctx.id()).getText();
        }
        else
        {
            importedPackageName = createPackageName(ctx.id());
        }

        return new Import(ctx.id(0).getStart(), importedPackageName, importedTypeName);
    }

    @Override
    public ConstType visitConstDeclaration(Zserio4Parser.ConstDeclarationContext ctx)
    {
        final ZserioType type = visitTypeName(ctx.typeName());
        final String name = ctx.id().getText();
        final Expression valueExpression = (Expression)visit(ctx.expression());

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final ConstType constType = new ConstType(ctx.id().getStart(), currentPackage, type, name,
                valueExpression, docComment);

        return constType;
    }

    @Override
    public Subtype visitSubtypeDeclaration(Zserio4Parser.SubtypeDeclarationContext ctx)
    {
        final ZserioType targetType = visitTypeName(ctx.typeName());
        final String name = ctx.id().getText();

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final Subtype subtype = new Subtype(ctx.id().getStart(), currentPackage, targetType, name, docComment);

        return subtype;
    }

    @Override
    public StructureType visitStructureDeclaration(Zserio4Parser.StructureDeclarationContext ctx)
    {
        final String name = ctx.id().getText();

        final List<Parameter> parameters = visitParameterList(ctx.parameterList());

        final List<Field> fields = new ArrayList<Field>();
        for (Zserio4Parser.StructureFieldDefinitionContext fieldCtx : ctx.structureFieldDefinition())
            fields.add(visitStructureFieldDefinition(fieldCtx));

        final List<FunctionType> functions = new ArrayList<FunctionType>();
        for (Zserio4Parser.FunctionDefinitionContext functionDefinitionCtx : ctx.functionDefinition())
            functions.add(visitFunctionDefinition(functionDefinitionCtx));

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final StructureType structureType = new StructureType(ctx.id().getStart(), currentPackage, name,
                parameters, fields, functions, docComment);

        return structureType;
    }

    @Override
    public Field visitStructureFieldDefinition(Zserio4Parser.StructureFieldDefinitionContext ctx)
    {
        final ZserioType type = getFieldType(ctx.fieldTypeId());
        final String name = ctx.fieldTypeId().id().getText();
        final boolean isAutoOptional = ctx.OPTIONAL() != null;

        final Expression alignmentExpr = visitFieldAlignment(ctx.fieldAlignment());
        final Expression offsetExpr = visitFieldOffset(ctx.fieldOffset());
        final Expression initializerExpr = visitFieldInitializer(ctx.fieldInitializer());
        final Expression optionalClauseExpr = visitFieldOptionalClause(ctx.fieldOptionalClause());
        final Expression constraintExpr = visitFieldConstraint(ctx.fieldConstraint());

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        return new Field(ctx.fieldTypeId().id().getStart(), type, name, isAutoOptional, alignmentExpr,
                offsetExpr, initializerExpr, optionalClauseExpr, constraintExpr, docComment);
    }

    @Override
    public Expression visitFieldAlignment(Zserio4Parser.FieldAlignmentContext ctx)
    {
        if (ctx == null)
            return null;

        return new Expression(ctx.DECIMAL_LITERAL().getSymbol(), currentPackage);
    }

    @Override
    public Expression visitFieldOffset(Zserio4Parser.FieldOffsetContext ctx)
    {
        if (ctx == null)
            return null;

        return (Expression)visit(ctx.expression());
    }

    @Override
    public Expression visitFieldInitializer(Zserio4Parser.FieldInitializerContext ctx)
    {
        if (ctx == null)
            return null;

        return (Expression)visit(ctx.expression());
    }

    @Override
    public Expression visitFieldOptionalClause(Zserio4Parser.FieldOptionalClauseContext ctx)
    {
        if (ctx == null)
            return null;

        return (Expression)visit(ctx.expression());
    }

    @Override
    public Expression visitFieldConstraint(Zserio4Parser.FieldConstraintContext ctx)
    {
        if (ctx == null)
            return null;

        return (Expression)visit(ctx.expression());
    }

    @Override
    public ChoiceType visitChoiceDeclaration(Zserio4Parser.ChoiceDeclarationContext ctx)
    {
        final String name = ctx.id().getText();

        final List<Parameter> parameters = visitParameterList(ctx.parameterList());

        final Expression selectorExpression = (Expression)visit(ctx.expression());

        final List<ChoiceCase> choiceCases = new ArrayList<ChoiceCase>();
        for (Zserio4Parser.ChoiceCasesContext choiceCasesCtx : ctx.choiceCases())
            choiceCases.add(visitChoiceCases(choiceCasesCtx));

        final ChoiceDefault choiceDefault = visitChoiceDefault(ctx.choiceDefault());

        final List<FunctionType> functions = new ArrayList<FunctionType>();
        for (Zserio4Parser.FunctionDefinitionContext functionDefinitionCtx : ctx.functionDefinition())
            functions.add(visitFunctionDefinition(functionDefinitionCtx));

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final ChoiceType choiceType = new ChoiceType(ctx.id().getStart(), currentPackage, name, parameters,
                selectorExpression, choiceCases, choiceDefault, functions, docComment);

        return choiceType;
    }

    @Override
    public ChoiceCase visitChoiceCases(Zserio4Parser.ChoiceCasesContext ctx)
    {
        List<ChoiceCaseExpression> caseExpressions = new ArrayList<ChoiceCaseExpression>();
        for (Zserio4Parser.ChoiceCaseContext choiceCaseCtx : ctx.choiceCase())
            caseExpressions.add(visitChoiceCase(choiceCaseCtx));

        final Field caseField = visitChoiceFieldDefinition(ctx.choiceFieldDefinition());

        return new ChoiceCase(ctx.getStart(), caseExpressions, caseField);
    }

    @Override
    public ChoiceCaseExpression visitChoiceCase(Zserio4Parser.ChoiceCaseContext ctx)
    {
        final DocComment docComment = docCommentManager.findDocComment(ctx);
        return new ChoiceCaseExpression(ctx.getStart(), (Expression)visit(ctx.expression()), docComment);
    }

    @Override
    public ChoiceDefault visitChoiceDefault(Zserio4Parser.ChoiceDefaultContext ctx)
    {
        if (ctx == null)
            return null;

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final Field defaultField = visitChoiceFieldDefinition(ctx.choiceFieldDefinition());
        return new ChoiceDefault(ctx.getStart(), defaultField, docComment);
    }

    @Override
    public Field visitChoiceFieldDefinition(Zserio4Parser.ChoiceFieldDefinitionContext ctx)
    {
        if (ctx == null)
            return null;

        final ZserioType type = getFieldType(ctx.fieldTypeId());
        final ParserRuleContext nameCtx = ctx.fieldTypeId().id();
        final String name = nameCtx.getText();
        final boolean isAutoOptional = false;

        final Expression alignmentExpr = null;
        final Expression offsetExpr = null;
        final Expression initializerExpr = null;
        final Expression optionalClauseExpr = null;
        final Expression constraintExpr = visitFieldConstraint(ctx.fieldConstraint());

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        return new Field(nameCtx.getStart(), type, name, isAutoOptional, alignmentExpr, offsetExpr,
                initializerExpr, optionalClauseExpr, constraintExpr, docComment);
    }

    @Override
    public UnionType visitUnionDeclaration(Zserio4Parser.UnionDeclarationContext ctx)
    {
        final String name = ctx.id().getText();

        final List<Parameter> parameters = visitParameterList(ctx.parameterList());

        final List<Field> fields = new ArrayList<Field>();
        for (Zserio4Parser.UnionFieldDefinitionContext fieldCtx : ctx.unionFieldDefinition())
            fields.add(visitChoiceFieldDefinition(fieldCtx.choiceFieldDefinition()));

        final List<FunctionType> functions = new ArrayList<FunctionType>();
        for (Zserio4Parser.FunctionDefinitionContext functionDefinitionCtx : ctx.functionDefinition())
            functions.add(visitFunctionDefinition(functionDefinitionCtx));

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final UnionType unionType = new UnionType(ctx.id().getStart(), currentPackage, name, parameters, fields,
                functions, docComment);

        return unionType;
    }

    @Override
    public EnumType visitEnumDeclaration(Zserio4Parser.EnumDeclarationContext ctx)
    {
        final ZserioType zserioEnumType = visitTypeName(ctx.typeName());
        final String name = ctx.id().getText();
        final List<EnumItem> enumItems = new ArrayList<EnumItem>();
        for (Zserio4Parser.EnumItemContext enumItemCtx : ctx.enumItem())
            enumItems.add(visitEnumItem(enumItemCtx));

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final EnumType enumType = new EnumType(ctx.id().getStart(), currentPackage, zserioEnumType, name,
                enumItems, docComment);

        return enumType;
    }

    @Override
    public EnumItem visitEnumItem(Zserio4Parser.EnumItemContext ctx)
    {
        final String name = ctx.id().getText();
        final Expression valueExpression = (Expression)visit(ctx.expression());

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        return new EnumItem(ctx.getStart(), name, valueExpression, docComment);
    }

    @Override
    public SqlTableType visitSqlTableDeclaration(Zserio4Parser.SqlTableDeclarationContext ctx)
    {
        final String name = ctx.id(0).getText();
        final String sqlUsingId = ctx.id(1) != null ? ctx.id(1).getText() : null;
        final List<Field> fields = new ArrayList<Field>();
        for (Zserio4Parser.SqlTableFieldDefinitionContext fieldCtx : ctx.sqlTableFieldDefinition())
            fields.add(visitSqlTableFieldDefinition(fieldCtx));
        // TODO: should constraint have comments?
        final SqlConstraint sqlConstraint = visitSqlConstraintDefinition(ctx.sqlConstraintDefinition());
        final boolean sqlWithoutRowId = ctx.sqlWithoutRowId() != null;

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final SqlTableType sqlTableType = new SqlTableType(ctx.id(0).getStart(), currentPackage, name,
                sqlUsingId, fields, sqlConstraint, sqlWithoutRowId, docComment);

        return sqlTableType;
    }

    @Override
    public Field visitSqlTableFieldDefinition(Zserio4Parser.SqlTableFieldDefinitionContext ctx)
    {
        final boolean isVirtual = ctx.SQL_VIRTUAL() != null;
        final ZserioType type = visitTypeReference(ctx.typeReference());
        final String name = ctx.id().getText();
        final SqlConstraint sqlConstraint = visitSqlConstraint(ctx.sqlConstraint());

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        return new Field(ctx.id().getStart(), type, name, isVirtual, (sqlConstraint == null) ?
                SqlConstraint.createDefaultFieldConstraint(currentPackage) : sqlConstraint, docComment);
    }

    @Override
    public SqlConstraint visitSqlConstraintDefinition(Zserio4Parser.SqlConstraintDefinitionContext ctx)
    {
        if (ctx == null)
            return null;

        return visitSqlConstraint(ctx.sqlConstraint());
    }

    @Override
    public SqlConstraint visitSqlConstraint(Zserio4Parser.SqlConstraintContext ctx)
    {
        if (ctx == null)
            return null;

        return new SqlConstraint(ctx.getStart(), new Expression(ctx.STRING_LITERAL().getSymbol(),
                currentPackage));
    }

    @Override
    public SqlDatabaseType visitSqlDatabaseDefinition(Zserio4Parser.SqlDatabaseDefinitionContext ctx)
    {
        final String name = ctx.id().getText();
        final List<Field> fields = new ArrayList<Field>();
        for (Zserio4Parser.SqlDatabaseFieldDefinitionContext fieldCtx : ctx.sqlDatabaseFieldDefinition())
            fields.add(visitSqlDatabaseFieldDefinition(fieldCtx));

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final SqlDatabaseType sqlDatabaseType = new SqlDatabaseType(ctx.id().getStart(), currentPackage, name,
                fields, docComment);

        return sqlDatabaseType;
    }

    @Override
    public Field visitSqlDatabaseFieldDefinition(Zserio4Parser.SqlDatabaseFieldDefinitionContext ctx)
    {
        final ZserioType type = visitQualifiedName(ctx.sqlTableReference().qualifiedName());
        final String name = ctx.id().getText();

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        return new Field(ctx.getStart(), type, name, docComment);
    }

    @Override
    public ServiceType visitServiceDefinition(Zserio4Parser.ServiceDefinitionContext ctx)
    {
        final String name = ctx.id().getText();

        List<Rpc> rpcs = new ArrayList<Rpc>();
        for (Zserio4Parser.RpcDeclarationContext rpcDeclarationCtx : ctx.rpcDeclaration())
            rpcs.add(visitRpcDeclaration(rpcDeclarationCtx));

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        final ServiceType serviceType = new ServiceType(ctx.id().getStart(), currentPackage, name, rpcs,
                docComment);

        return serviceType;
    }

    @Override
    public Rpc visitRpcDeclaration(Zserio4Parser.RpcDeclarationContext ctx)
    {
        final boolean responseStreaming = ctx.rpcTypeName(0).STREAM() != null;
        final ZserioType responseType = visitQualifiedName(ctx.rpcTypeName(0).qualifiedName());

        final String name = ctx.id().getText();

        final boolean requestStreaming = ctx.rpcTypeName(1).STREAM() != null;
        final ZserioType requestType = visitQualifiedName(ctx.rpcTypeName(1).qualifiedName());

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        return new Rpc(ctx.id().getStart(), name, responseType, responseStreaming, requestType,
                requestStreaming, docComment);
    }

    @Override
    public FunctionType visitFunctionDefinition(Zserio4Parser.FunctionDefinitionContext ctx)
    {
        final ZserioType returnType = visitTypeName(ctx.functionType().typeName());
        final String name = ctx.functionName().getText();
        final Expression resultExpression = (Expression)visit(ctx.functionBody().expression());

        final DocComment docComment = docCommentManager.findDocComment(ctx);

        return new FunctionType(ctx.functionName().getStart(), currentPackage, returnType, name,
                resultExpression, docComment);
    }

    @Override
    public List<Parameter> visitParameterList(Zserio4Parser.ParameterListContext ctx)
    {
        List<Parameter> parameters = new ArrayList<Parameter>();
        if (ctx != null)
        {
            for (Zserio4Parser.ParameterDefinitionContext parameterDefinitionCtx : ctx.parameterDefinition())
                parameters.add((Parameter)visitParameterDefinition(parameterDefinitionCtx));
        }
        return parameters;
    }

    @Override
    public Object visitParameterDefinition(Zserio4Parser.ParameterDefinitionContext ctx)
    {
        return new Parameter(ctx.id().getStart(), visitTypeName(ctx.typeName()), ctx.id().getText());
    }

    @Override
    public Object visitParenthesizedExpression(Zserio4Parser.ParenthesizedExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1);
    }

    @Override
    public Object visitFunctionCallExpression(Zserio4Parser.FunctionCallExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1);
    }

    @Override
    public Object visitArrayExpression(Zserio4Parser.ArrayExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitDotExpression(Zserio4Parser.DotExpressionContext ctx)
    {
        final Expression.ExpressionFlag expressionFlag = (isInDotExpression) ? Expression.ExpressionFlag.NONE :
            Expression.ExpressionFlag.IS_TOP_LEVEL_DOT;
        isInDotExpression = true;
        final Expression operand1 = (Expression)visit(ctx.expression());
        final Expression operand2 = new Expression(ctx.id().ID().getSymbol(), currentPackage,
                Expression.ExpressionFlag.IS_DOT_RIGHT_OPERAND);

        return new Expression(ctx.getStart(), currentPackage, ctx.operator, expressionFlag, operand1, operand2);
    }

    @Override
    public Object visitLengthofExpression(Zserio4Parser.LengthofExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1);
    }

    @Override
    public Object visitSumExpression(Zserio4Parser.SumExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1);
    }

    @Override
    public Object visitValueofExpression(Zserio4Parser.ValueofExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1);
    }

    @Override
    public Object visitNumbitsExpression(Zserio4Parser.NumbitsExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1);
    }

    @Override
    public Object visitUnaryExpression(Zserio4Parser.UnaryExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression());
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1);
    }

    @Override
    public Object visitMultiplicativeExpression(Zserio4Parser.MultiplicativeExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitAdditiveExpression(Zserio4Parser.AdditiveExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitShiftExpression(Zserio4Parser.ShiftExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitRelationalExpression(Zserio4Parser.RelationalExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitEqualityExpression(Zserio4Parser.EqualityExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitBitwiseAndExpression(Zserio4Parser.BitwiseAndExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitBitwiseXorExpression(Zserio4Parser.BitwiseXorExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitBitwiseOrExpression(Zserio4Parser.BitwiseOrExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitLogicalAndExpression(Zserio4Parser.LogicalAndExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitLogicalOrExpression(Zserio4Parser.LogicalOrExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2);
    }

    @Override
    public Object visitTernaryExpression(Zserio4Parser.TernaryExpressionContext ctx)
    {
        final Expression operand1 = (Expression)visit(ctx.expression(0));
        final Expression operand2 = (Expression)visit(ctx.expression(1));
        final Expression operand3 = (Expression)visit(ctx.expression(2));
        return new Expression(ctx.getStart(), currentPackage, ctx.operator, operand1, operand2, operand3);
    }

    @Override
    public Object visitLiteralExpression(Zserio4Parser.LiteralExpressionContext ctx)
    {
        return new Expression(ctx.literal().getStart(), currentPackage);
    }

    @Override
    public Object visitIndexExpression(Zserio4Parser.IndexExpressionContext ctx)
    {
        return new Expression(ctx.INDEX().getSymbol(), currentPackage);
    }

    @Override
    public Object visitIdentifierExpression(Zserio4Parser.IdentifierExpressionContext ctx)
    {
        return new Expression(ctx.id().ID().getSymbol(), currentPackage);
    }

    @Override
    public ZserioType visitTypeName(Zserio4Parser.TypeNameContext ctx)
    {
        if (ctx.builtinType() != null)
            return (ZserioType)visitBuiltinType(ctx.builtinType());

        return visitQualifiedName(ctx.qualifiedName());
    }

    @Override
    public ZserioType visitTypeReference(Zserio4Parser.TypeReferenceContext ctx)
    {
        if (ctx.builtinType() != null)
            return (ZserioType)visitBuiltinType(ctx.builtinType());

        final boolean isParameterized = ctx.typeArgumentList() != null;
        final TypeReference typeReference = visitQualifiedName(ctx.qualifiedName(), isParameterized);

        if (isParameterized)
        {
            final List<Expression> arguments = new ArrayList<Expression>();
            for (Zserio4Parser.TypeArgumentContext typeArgumentCtx : ctx.typeArgumentList().typeArgument())
                arguments.add(visitTypeArgument(typeArgumentCtx));
            return new TypeInstantiation(ctx.getStart(), typeReference, arguments);
        }
        else
        {
            return typeReference;
        }
    }

    @Override
    public TypeReference visitQualifiedName(Zserio4Parser.QualifiedNameContext ctx)
    {
        return visitQualifiedName(ctx, false);
    }

    public TypeReference visitQualifiedName(Zserio4Parser.QualifiedNameContext ctx, boolean isParameterized)
    {
        final PackageName referencedPackageName = createPackageName(
                getPackageNameIds(ctx.id()));
        final String referencedTypeName = getTypeNameId(ctx.id()).getText();

        final TypeReference typeReference =
                new TypeReference(ctx.getStart(), currentPackage, referencedPackageName, referencedTypeName,
                        isParameterized);

        return typeReference;
    }

    @Override
    public Expression visitTypeArgument(Zserio4Parser.TypeArgumentContext ctx)
    {
        if (ctx.EXPLICIT() != null)
            return new Expression(ctx.getStart(), currentPackage, ctx.id().ID().getSymbol(),
                    Expression.ExpressionFlag.IS_EXPLICIT);
        else
            return (Expression)visit(ctx.expression());
    }

    @Override
    public StdIntegerType visitIntType(Zserio4Parser.IntTypeContext ctx)
    {
        return new StdIntegerType(ctx.getStart());
    }

    @Override
    public VarIntegerType visitVarintType(Zserio4Parser.VarintTypeContext ctx)
    {
        return new VarIntegerType(ctx.getStart());
    }

    @Override
    public UnsignedBitFieldType visitUnsignedBitFieldType(Zserio4Parser.UnsignedBitFieldTypeContext ctx)
    {
        final Expression lengthExpression = visitBitFieldLength(ctx.bitFieldLength());
        return new UnsignedBitFieldType(ctx.getStart(), lengthExpression);
    }

    @Override
    public SignedBitFieldType visitSignedBitFieldType(Zserio4Parser.SignedBitFieldTypeContext ctx)
    {
        final Expression lengthExpression = visitBitFieldLength(ctx.bitFieldLength());
        return new SignedBitFieldType(ctx.getStart(), lengthExpression);
    }

    @Override
    public Expression visitBitFieldLength(Zserio4Parser.BitFieldLengthContext ctx)
    {
        if (ctx.DECIMAL_LITERAL() != null)
            return new Expression(ctx.DECIMAL_LITERAL().getSymbol(), currentPackage);

        return (Expression)visit(ctx.expression());
    }

    @Override
    public BooleanType visitBoolType(Zserio4Parser.BoolTypeContext ctx)
    {
        return new BooleanType(ctx.getStart());
    }

    @Override
    public StringType visitStringType(Zserio4Parser.StringTypeContext ctx)
    {
        return new StringType(ctx.getStart());
    }

    @Override
    public FloatType visitFloatType(Zserio4Parser.FloatTypeContext ctx)
    {
        return new FloatType(ctx.getStart());
    }

    private PackageName createPackageName(List<Zserio4Parser.IdContext> ids)
    {
        final PackageName.Builder packageNameBuilder = new PackageName.Builder();
        for (Zserio4Parser.IdContext id : ids)
            packageNameBuilder.addId(id.getText());
        return packageNameBuilder.get();
    }

    private List<Zserio4Parser.IdContext> getPackageNameIds(List<Zserio4Parser.IdContext> qualifiedName)
    {
        return qualifiedName.subList(0, qualifiedName.size() - 1);
    }

    private Zserio4Parser.IdContext getTypeNameId(List<Zserio4Parser.IdContext> qualifiedName)
    {
        return qualifiedName.get(qualifiedName.size() - 1);
    }

    private ZserioType getFieldType(Zserio4Parser.FieldTypeIdContext ctx)
    {
        final ZserioType type = visitTypeReference(ctx.typeReference());
        if (ctx.fieldArrayRange() == null)
            return type;

        final Expression lengthExpression = (Expression)visit(ctx.fieldArrayRange().expression());
        return new ArrayType(ctx.getStart(), type, lengthExpression, ctx.IMPLICIT() != null);
    }

    private final DocCommentManager docCommentManager = new DocCommentManager();
    private final LinkedHashMap<PackageName, Package> packageNameMap =
            new LinkedHashMap<PackageName, Package>();

    private Package currentPackage = null;
    private LinkedHashMap<String, ZserioType> localTypes = null;
    private boolean isInDotExpression = false;
}
