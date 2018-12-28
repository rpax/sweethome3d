package com.eteks.codec;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.table.TableModel;

import com.eteks.functionsheet.FunctionSheetDefinition;
import com.eteks.functionsheet.FunctionSheetTableModel;
import com.eteks.jeks.JeksCell;
import com.eteks.jeks.JeksExpressionSyntax;
import com.eteks.jeks.JeksFunctionSyntax;
import com.eteks.jeks.SharedState;
import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledExpression;
import com.eteks.parser.CompiledFunction;
import com.eteks.parser.ExpressionParameter;
import com.eteks.parser.ExpressionParser;

public class TranslationParser extends ExpressionParser
{
    private TableModel model;
    private SharedState state;
    
    public TranslationParser(final JeksFunctionSyntax syntax,
            final ExpressionParameter expressionParameter,
            final TableModel model,
            final SharedState state)
    {
        super(syntax, expressionParameter);
        this.model = model;
    }

    public String translateExpressionToSyntax(
            final CompiledExpression expression,
            final JeksExpressionSyntax translationSyntax)
    {
        return translateExpressionDefinitionToSyntax(
                expression.getDefinition(), expression.getParameters(),
                translationSyntax);
    }

    public String translateFunctionToSyntax(final CompiledFunction function,
            final JeksFunctionSyntax translationSyntax)
    {
        String functionDefinition = function.getDefinition();
        JeksFunctionSyntax fromSyntax = (JeksFunctionSyntax) getSyntax();

        // Translate brackets and parameters separator
        String translatedFunctionStart = functionDefinition.substring(0,
                functionDefinition.indexOf(fromSyntax.getAssignmentOperator()));

        translatedFunctionStart = translatedFunctionStart.replace(
                fromSyntax.getOpeningBracket(),
                translationSyntax.getOpeningBracket());

        translatedFunctionStart = translatedFunctionStart.replace(
                fromSyntax.getClosingBracket(),
                translationSyntax.getClosingBracket());

        translatedFunctionStart = translatedFunctionStart.replace(
                fromSyntax.getParameterSeparator(),
                translationSyntax.getParameterSeparator());

        // Translate delimiters unknown in the translation syntax
        String whiteSpaces = fromSyntax.getWhiteSpaceCharacters();

        for (int i = 0; i < whiteSpaces.length(); i++)
            if (translationSyntax.getWhiteSpaceCharacters().indexOf(
                    whiteSpaces.charAt(i)) == -1)
                translatedFunctionStart = translatedFunctionStart.replace(
                        whiteSpaces.charAt(i), translationSyntax
                                .getWhiteSpaceCharacters().charAt(0));

        String expressionDefinition = functionDefinition
                .substring(functionDefinition.indexOf(fromSyntax
                        .getAssignmentOperator()));

        String translatedExpression = translateExpressionDefinitionToSyntax(
                expressionDefinition, function, translationSyntax);

        return translatedFunctionStart + translatedExpression;
    }

    public String translateExpressionDefinitionToSyntax(
            final String expressionDefinition, final Object parserData,
            final JeksFunctionSyntax toSyntax)
    {
        JeksFunctionSyntax fromSyntax = (JeksFunctionSyntax) getSyntax();
        // Reuse getLexical () to parse expression and find which cells to
        // change
        StringBuffer translatedExpression = new StringBuffer(
                toSyntax.getAssignmentOperator());
        Lexical lexical = null;
        for (int parserIndex = fromSyntax.getAssignmentOperator().length(); parserIndex < expressionDefinition
                .length(); parserIndex += lexical.getExtractedString().length())
        {
            Object key;
            String newLexical = null;
            try
            {
                lexical = getLexical(expressionDefinition, parserIndex,
                        parserData);
                String extractedString = lexical.getExtractedString();
                switch (lexical.getCode())
                {
                    case 16: // DEFUALT
                        newLexical = "DEFAULT";
                        break;
                    case LEXICAL_WHITE_SPACE:
                        // Translate white spaces
                        newLexical = "";
                        for (int i = 0; i < extractedString.length(); i++)
                            if (toSyntax.getWhiteSpaceCharacters().indexOf(
                                    extractedString.charAt(i)) == -1)
                                newLexical += toSyntax
                                        .getWhiteSpaceCharacters().charAt(0);
                            else
                                newLexical += extractedString.charAt(i);
                        break;
                    case LEXICAL_LITERAL:
                        if (extractedString.charAt(0) == fromSyntax
                                .getQuoteCharacter())
                        {
                            // Litteral string values
                            newLexical = extractedString;
                            // Replace quote char and escape characters if
                            // different
                            if (fromSyntax.getQuoteCharacter() != toSyntax
                                    .getQuoteCharacter())
                                newLexical = newLexical.replace(
                                        fromSyntax.getQuoteCharacter(),
                                        toSyntax.getQuoteCharacter());
                        } else
                        {
                            // Literal numeric values
                            Number number = (Number) fromSyntax.getLiteral(
                                    extractedString, new StringBuffer());
                            newLexical = toSyntax.getNumberFormat().format(
                                    number);
                        }
                        break;
                    case LEXICAL_CONSTANT:
                        newLexical = toSyntax.getConstant(fromSyntax
                                .getConstantKey(extractedString));
                        break;
                    case LEXICAL_PARAMETER:
                        // Change parameter's constant char and cell set
                        // separator
                        if (parserData instanceof Hashtable)
                        {
                            Hashtable parameters = (Hashtable) parserData;
                            for (Enumeration enumer = parameters.keys(); enumer
                                    .hasMoreElements();)
                                if (compare((String) enumer.nextElement(),
                                        extractedString,
                                        fromSyntax.isCaseSensitive()))
                                {
                                    newLexical = extractedString;
                                    if (fromSyntax instanceof JeksExpressionSyntax)
                                    {
                                        JeksExpressionSyntax fromExpressionSyntax = (JeksExpressionSyntax) fromSyntax;
                                        JeksExpressionSyntax toExpressionSyntax = (JeksExpressionSyntax) toSyntax;
                                        if (compare(
                                                fromExpressionSyntax
                                                        .getCellError(JeksExpressionSyntax.ERROR_ILLEGAL_CELL),
                                                extractedString, fromSyntax
                                                        .isCaseSensitive()))
                                            newLexical = toExpressionSyntax
                                                    .getCellError(JeksExpressionSyntax.ERROR_ILLEGAL_CELL);
                                        else
                                        {
                                            int separatorIndex = newLexical
                                                    .indexOf(fromExpressionSyntax
                                                            .getCellSetSeparator());

                                            if (separatorIndex == -1)
                                            {
                                                try
                                                {
                                                    newLexical = translateCellToSyntax(
                                                            newLexical,
                                                            toExpressionSyntax);
                                                } catch (StringIndexOutOfBoundsException e)
                                                {

                                                }
                                            } else
                                            {
                                                newLexical = translateCellToSyntax(
                                                        newLexical.substring(0,
                                                                separatorIndex),
                                                        toExpressionSyntax)
                                                        + toExpressionSyntax
                                                                .getCellSetSeparator()
                                                        + translateCellToSyntax(
                                                                newLexical
                                                                        .substring(separatorIndex + 1),
                                                                toExpressionSyntax);
                                            }
                                        }
                                    }
                                    break;
                                }
                        } else if (parserData instanceof CompiledFunction)
                        {
                            String[] parameters = ((CompiledFunction) parserData)
                                    .getParameters();
                            for (int i = 0; i < parameters.length; i++)
                                if (compare(parameters[i], extractedString,
                                        fromSyntax.isCaseSensitive()))
                                {
                                    newLexical = extractedString;
                                    break;
                                }
                        } else
                            newLexical = extractedString;
                        break;
                    case LEXICAL_SYNONYMOUS_OPERATOR:
                        // Consider that synonymous operators are each time the
                        // same in
                        // each
                        // syntax
                    case LEXICAL_UNARY_OPERATOR:
                        newLexical = toSyntax.getUnaryOperator(fromSyntax
                                .getUnaryOperatorKey(extractedString));
                        break;
                    case LEXICAL_BINARY_OPERATOR:
                        newLexical = toSyntax.getBinaryOperator(fromSyntax
                                .getBinaryOperatorKey(extractedString));
                        break;
                    case LEXICAL_COMMON_FUNCTION:
                        newLexical = toSyntax.getCommonFunction(fromSyntax
                                .getCommonFunctionKey(extractedString));
                        break;
                    case LEXICAL_OPENING_BRACKET:
                        newLexical = extractedString;
                        if (fromSyntax.getOpeningBracket() != toSyntax
                                .getOpeningBracket())
                            newLexical = String.valueOf(toSyntax
                                    .getOpeningBracket());
                        break;
                    case LEXICAL_CLOSING_BRACKET:
                        newLexical = extractedString;
                        if (fromSyntax.getClosingBracket() != toSyntax
                                .getClosingBracket())
                            newLexical = String.valueOf(toSyntax
                                    .getClosingBracket());
                        break;
                    case LEXICAL_FUNCTION:
                        key = fromSyntax.getJeksFunctionKey(extractedString);
                        // If the key exist it's a Jeks function otherwise it's
                        // a user
                        // defined function
                        if (key != null)
                            newLexical = toSyntax.getJeksFunction(key);
                        else
                            newLexical = extractedString;
                        break;
                    case LEXICAL_PARAMETER_SEPARATOR:
                        newLexical = extractedString;
                        if (fromSyntax.getParameterSeparator() != toSyntax
                                .getParameterSeparator())
                            newLexical = String.valueOf(toSyntax
                                    .getParameterSeparator());
                        break;
                    case LEXICAL_IF:
                    case LEXICAL_THEN:
                    case LEXICAL_ELSE:
                        newLexical = toSyntax.getConditionPart(fromSyntax
                                .getConditionPartKey(extractedString));
                        break;
                }
            } catch (CompilationException e)
            {
            } // Can't happen : expression already correctly parsed

            if (newLexical != null)
                translatedExpression.append(newLexical);
            else
                // Otherwise append the read substring (Shouldn't happen if
                // toSyntax
                // has defined the same keys)
                translatedExpression.append(expressionDefinition.substring(
                        parserIndex, parserIndex));
        }

        return translatedExpression.toString();
    }

    /**
     * Translates the cell <code>cellIdentifier</code>, to the syntax
     * <code>toSyntax</code>.
     */
    private String translateCellToSyntax(final String cellIdentifier,
            final JeksExpressionSyntax toSyntax)
    {
        JeksExpressionSyntax fromSyntax = (JeksExpressionSyntax) getSyntax();
        if(model instanceof FunctionSheetTableModel)
        {
            for (FunctionSheetDefinition def : ((FunctionSheetTableModel) model)
                    .getDefinitions())
            {
                if (def.isParameter(cellIdentifier))
                {
                    return cellIdentifier;
                }
            }
        }
        JeksCell cell = fromSyntax.getCellAt(cellIdentifier);
        return toSyntax.toString(cell.getSheet(), cell.getRow(),
                cellIdentifier.indexOf(fromSyntax.getConstantChar(), 1) != -1,
                cell.getColumn(),
                cellIdentifier.charAt(0) == fromSyntax.getConstantChar());
    }
}
