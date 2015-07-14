/**
 * Copyright (c) 2014, 2015 Nhan Nguyen.
 *
 * This file is part of Cfinder.
 *
 * Cfinder is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Cfinder is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Cfinder. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package edu.uconn.cse.ccg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.uconn.cse.ccg.source.model.LineInfo;
import edu.uconn.cse.ccg.source.model.NextNode;

/**
 * 
 * @author nhannguyen
 *
 */
public class Utils {

  public static void printResult5(String output) {
    // System.out.println(output);
  }

  public static void printResult4(String output) {
    // System.out.println(output);
  }

  public static void printResult2(String output) {
    // System.out.println(output);
  }

  public static void printResult(String output) {
    // System.out.println(output);
  }

  public static void printResult3(String output) {
    // System.out.println(output);
  }

  public static void printAdditionalInformation(String info) {
    // System.out.println(info);
  }

  public static void printAdditionalInformation2(String info) {
    // System.out.println(info);
  }

  public static String readInputStreamToString(InputStream stream, String encoding)
      throws IOException {

    Reader r = new BufferedReader(new InputStreamReader(stream, encoding), 16384);
    StringBuilder result = new StringBuilder(16384);
    char[] buffer = new char[16384];

    int len;
    while ((len = r.read(buffer, 0, buffer.length)) >= 0) {
      result.append(buffer, 0, len);
    }

    return result.toString();
  }

  public static String readFileToString(File file, String encoding) throws IOException {
    FileInputStream stream = new FileInputStream(file);
    String result = null;
    try {
      result = readInputStreamToString(stream, encoding);
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
        // ignore
      }
    }
    return result;
  }

  public static String getType(Type type) {
    String result = "";
    if (type.isPrimitiveType()) {
      PrimitiveType type2 = (PrimitiveType) type;
      return type2.getPrimitiveTypeCode().toString();
      // System.out.println("------Declaration of  " +
      // type2.getPrimitiveTypeCode() +
      // "' at line "+cu.getLineNumber(node.getStartPosition()));
    } else if (type.isSimpleType()) {
      SimpleType type2 = (SimpleType) type;
      return type2.getName().getFullyQualifiedName();
      // System.out.println("------Declaration of  " + type2.getName()
      // tPosition()));
    } else if (type.isArrayType()) {
      ArrayType type2 = (ArrayType) type;
      Type componenType = type2.getComponentType();
      String componenTypeStr = getType(componenType);
      StringBuffer kinds = new StringBuffer("Array:");
      return kinds.append(componenTypeStr).toString();
      // System.out.println("------Declaration of array " +
      // "' at line "+cu.getLineNumber(node.getStartPosition()));
    } else if (type.isQualifiedType()) {
      QualifiedType type2 = (QualifiedType) type;
      return "QualifiedType:" + type2.getName().getFullyQualifiedName();
    }
    return result;
  }

  public static void parseStatement(Statement stmt) {
    if (stmt instanceof Block) {
      System.out.println("Block");
    } else if (stmt instanceof BreakStatement) {
      System.out.println("BreakStatement");
    } else if (stmt instanceof ConstructorInvocation) {
      System.out.println("ConstructorInvocation");
    } else if (stmt instanceof ContinueStatement) {
      System.out.println("ContinueStatement");
    } else if (stmt instanceof DoStatement) {
      System.out.println("DoStatement");
    } else if (stmt instanceof ExpressionStatement) {
      System.out.println("ExpressionStatement");
    } else if (stmt instanceof ForStatement) {
      System.out.println("ForStatement");
    } else if (stmt instanceof IfStatement) {
      System.out.println("IfStatement");
    } else if (stmt instanceof ReturnStatement) {
      System.out.println("ReturnStatement");
      ReturnStatement returnStatement = (ReturnStatement) stmt;
    } else if (stmt instanceof SuperConstructorInvocation) {
      System.out.println("SuperConstructorInvocation");
    } else if (stmt instanceof SwitchCase) {
      System.out.println("SwitchCase");
    } else if (stmt instanceof SwitchStatement) {
      System.out.println("SwitchStatement");
    } else if (stmt instanceof SynchronizedStatement) {
      System.out.println("SynchronizedStatement");
    } else if (stmt instanceof TypeDeclarationStatement) {
      System.out.println("TypeDeclarationStatement");
    } else if (stmt instanceof VariableDeclarationStatement) {
      System.out.println("VariableDeclarationStatement");
    } else if (stmt instanceof WhileStatement) {
      System.out.println("WhileStatement");
    } else if (stmt instanceof EnhancedForStatement) {

    }
  }

  public static void parseExpression(Map<String, String> usedClass, int type, CompilationUnit cu,
      Expression expr) {
    // System.out.println("Expression " + expr);
    if (expr instanceof ArrayAccess) {
      ArrayAccess arrayAccess = (ArrayAccess) expr;
    } else if (expr instanceof Name) {
      Name name = (Name) expr;
    } else if (expr instanceof FieldAccess) {
      FieldAccess fileAccess = (FieldAccess) expr;
      // System.out.println("FileAccess " + fileAccess.getName().getFullyQualifiedName());
    } else if (expr instanceof StringLiteral) {
      StringLiteral stringLiteral = (StringLiteral) expr;
      // System.out.println("StringLiteral " + stringLiteral.getLiteralValue());
    } else if (expr instanceof BooleanLiteral) {
      BooleanLiteral booleanLiteral = (BooleanLiteral) expr;
      String value = booleanLiteral.booleanValue() ? "true" : "false";
    } else if (expr instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression variableDeclarationExpression =
          (VariableDeclarationExpression) expr;
      // System.out.println("VariableDeclarationExpression " +
      // variableDeclarationExpression.toString());
    } else if (expr instanceof TypeLiteral) {
      // System.out.println("VariableDeclarationExpression ");
    } else if (expr instanceof ArrayCreation) {
      // System.out.println("ArrayCreation ");
    } else if (expr instanceof ArrayInitializer) {
      // System.out.println("ArrayInitializer ");
    } else if (expr instanceof Assignment) {
      // System.out.println("Assignment ");
      // System.out.println("left " + ((Assignment) expr).getLeftHandSide());
      // System.out.println("left " + ((Assignment) expr).getRightHandSide());
      parseExpression(usedClass, LineInfo.LEFT_HANDSIDE, cu, ((Assignment) expr).getLeftHandSide());
      parseExpression(usedClass, LineInfo.RIGHT_HANDSIDE, cu,
          ((Assignment) expr).getRightHandSide());
      // parseExpression(usedClass, LineInfo.LEFT_HANDSIDE, cu, ((Assignment)
      // expr).getLeftHandSide());
      // parseExpression(usedClass, LineInfo.RIGHT_HANDSIDE, cu, ((Assignment)
      // expr).getRightHandSide());
    } else if (expr instanceof CastExpression) {
      // System.out.println("CastExpression ");
      CastExpression castExpression = (CastExpression) expr;
      parseExpression(usedClass, LineInfo.CASTEXPRESSION, cu, castExpression.getExpression());
    } else if (expr instanceof CharacterLiteral) {
      // System.out.println("CharacterLiteral ");
    } else if (expr instanceof ClassInstanceCreation) {
      // System.out.println("ClassInstanceCreation ");
      ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expr;
      parseExpression(usedClass, LineInfo.CLASSINSTANCECREATION, cu,
          classInstanceCreation.getExpression());
      List<Expression> exprs = classInstanceCreation.arguments();
      for (Expression exp : exprs) {
        // System.out.println("Expr " + exp);
        parseExpression(usedClass, LineInfo.CLASS_ARGUMENT, cu, exp);
      }
      if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
        // System.out.println("Need to analyze this");
      }

    } else if (expr instanceof ConditionalExpression) {
      // System.out.println("ConditionalExpression ");
      ConditionalExpression conditionalExpression = (ConditionalExpression) expr;
      // //System.out.println(conditionalExpression.getExpression());
      // //System.out.println(conditionalExpression.getThenExpression());
      // //System.out.println(conditionalExpression.getElseExpression());
      parseExpression(usedClass, LineInfo.IF_EXPRESSION, cu, conditionalExpression.getExpression());
      parseExpression(usedClass, LineInfo.THEN_EXPRESSION, cu,
          conditionalExpression.getThenExpression());
      parseExpression(usedClass, LineInfo.ELSE_EXPRESSION, cu,
          conditionalExpression.getElseExpression());
    } else if (expr instanceof InfixExpression) {
      // System.out.println("InfixExpression ");
      InfixExpression infixExpression = (InfixExpression) expr;
      // //System.out.println("left InfixExpression" + infixExpression.getLeftOperand());
      // //System.out.println("right InfixExpression" + infixExpression.getRightOperand());
      parseExpression(usedClass, LineInfo.LEFT_INFIXEXPRESSION, cu,
          infixExpression.getLeftOperand());
      parseExpression(usedClass, LineInfo.RIGHT_INFIXEXPRESSION, cu,
          infixExpression.getRightOperand());
      if (infixExpression.hasExtendedOperands()) {
        List<Expression> exprs = infixExpression.extendedOperands();
        for (Expression exp : exprs) {
          // System.out.println("Expr " + exp);
          parseExpression(usedClass, LineInfo.EXTEND_INFIXEXPRESSION, cu, exp);
        }
      }
    } else if (expr instanceof InstanceofExpression) {
      // System.out.println("InstanceofExpression ");
    } else if (expr instanceof NullLiteral) {
      // System.out.println("NullLiteral ");
    } else if (expr instanceof NumberLiteral) {
      // System.out.println("NumberLiteral ");
    } else if (expr instanceof ParenthesizedExpression) {
      // System.out.println("ParenthesizedExpression ");
      ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expr;
      parseExpression(usedClass, LineInfo.PARENTHESIZEDEXPRESSION, cu,
          parenthesizedExpression.getExpression());
    } else if (expr instanceof PostfixExpression) {
      // System.out.println("PostfixExpression ");
    } else if (expr instanceof PrefixExpression) {
      // System.out.println("PrefixExpression ");
    } else if (expr instanceof SuperFieldAccess) {
      // System.out.println("SuperFieldAccess ");
    } else if (expr instanceof ThisExpression) {
      // System.out.println("ThisExpression ");
    } else if (expr instanceof SuperMethodInvocation) {
      // System.out.println("SuperMethodInvocation ");
    } else if (expr instanceof Annotation) {
      // Annotation annotation = (Annotation)expr;
      // System.out.println("Annotation ");

    } else if (expr instanceof MethodInvocation) {
      // System.out.println("MethodInvocation ");
      MethodInvocation methodInvocation = (MethodInvocation) expr;
      // //System.out.println("MethodInvocation " +
      // methodInvocation.resolveMethodBinding().getName() + " : " +
      // methodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName());

      if (methodInvocation != null && methodInvocation.resolveMethodBinding() != null
          && methodInvocation.resolveMethodBinding().getDeclaringClass() != null) {
        usedClass.put(
            methodInvocation.resolveMethodBinding().getName()
                + ":"
                + cu.getLineNumber(methodInvocation.getStartPosition())
                + ":"
                + cu.getColumnNumber(methodInvocation.getStartPosition())
                + ":"
                + cu.getLineNumber(methodInvocation.getStartPosition()
                    + methodInvocation.getLength())
                + ":"
                + cu.getColumnNumber(methodInvocation.getStartPosition()
                    + methodInvocation.getLength()) + ":" + methodInvocation.getStartPosition(),
            methodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName());


        // //System.out.println("-------not null------");
        // else if (methodInvocation.resolveMethodBinding()
        // .getMethodDeclaration() != null)
        // //System.out.println("MethodInvocation as Argument  at "
        // + methodInvocation.hashCode()
        // + " : "
        // + cu.getLineNumber(methodInvocation
        // .getStartPosition())
        // + " length "
        // + methodInvocation.getLength()
        // + " in "
        // + methodInvocation.resolveMethodBinding()
        // .getMethodDeclaration().getName()
        // + " with parent "
        // + methodInvocation.getParent().hashCode()
        // + " at "
        // + cu.getLineNumber(methodInvocation.getParent()
        // .getStartPosition())
        // + " exp "
        // + methodInvocation.getExpression()
        // + " : "
        // + methodInvocation.resolveMethodBinding()
        // .getDeclaringClass().getQualifiedName());
      }
      parseMethodInvocation(usedClass, LineInfo.METHODINVOCATION, cu, methodInvocation);
      // //System.out.println("" +
      // methodInvocation.getName().getFullyQualifiedName());
    } else {
      // System.out.println("Can't detect :(");
    }
    // System.out.println("Done");
  }

  public static void parseDetailExpression(int type, CompilationUnit cu, Expression expr) {
    // System.out.println("Expression " + expr);
    if (expr instanceof ArrayAccess) {
      ArrayAccess arrayAccess = (ArrayAccess) expr;
      // System.out.println("ArrayAccess " + arrayAccess.getArray());
    } else if (expr instanceof Name) {
      // Name name = (Name) expr;
      // if (name.isQualifiedName())
      // // System.out.println("Qualified Name " + name.getFullyQualifiedName() + " of " +
      // ((QualifiedName)name).getName().getFullyQualifiedName());
      // else
      // // System.out.println("Simple Name " + name.getFullyQualifiedName());
    } else if (expr instanceof FieldAccess) {
      FieldAccess fileAccess = (FieldAccess) expr;
      // System.out.println("FileAccess " + fileAccess.getName().getFullyQualifiedName());
    } else if (expr instanceof StringLiteral) {
      StringLiteral stringLiteral = (StringLiteral) expr;
      // System.out.println("StringLiteral " + stringLiteral.getLiteralValue());
    } else if (expr instanceof BooleanLiteral) {
      BooleanLiteral booleanLiteral = (BooleanLiteral) expr;
      String value = booleanLiteral.booleanValue() ? "true" : "false";
      // System.out.println("BooleanLiteral " + value);
    } else if (expr instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression variableDeclarationExpression =
          (VariableDeclarationExpression) expr;
      // System.out.println("VariableDeclarationExpression " +
      // variableDeclarationExpression.toString());
    } else if (expr instanceof TypeLiteral) {
      // System.out.println("VariableDeclarationExpression ");
    } else if (expr instanceof ArrayCreation) {
      // System.out.println("ArrayCreation ");
    } else if (expr instanceof ArrayInitializer) {
      // System.out.println("ArrayInitializer ");
    } else if (expr instanceof Assignment) {
      // System.out.println("Assignment ");
      // System.out.println("left " + ((Assignment) expr).getLeftHandSide());
      // System.out.println("left " + ((Assignment) expr).getRightHandSide());
      parseDetailExpression(LineInfo.LEFT_HANDSIDE, cu, ((Assignment) expr).getLeftHandSide());
      parseDetailExpression(LineInfo.RIGHT_HANDSIDE, cu, ((Assignment) expr).getRightHandSide());
      // parseExpression(usedClass, LineInfo.LEFT_HANDSIDE, cu, ((Assignment)
      // expr).getLeftHandSide());
      // parseExpression(usedClass, LineInfo.RIGHT_HANDSIDE, cu, ((Assignment)
      // expr).getRightHandSide());
    } else if (expr instanceof CastExpression) {
      // System.out.println("CastExpression ");
      CastExpression castExpression = (CastExpression) expr;
      parseDetailExpression(LineInfo.CASTEXPRESSION, cu, castExpression.getExpression());
    } else if (expr instanceof CharacterLiteral) {
      // System.out.println("CharacterLiteral ");
    } else if (expr instanceof ClassInstanceCreation) {
      // System.out.println("ClassInstanceCreation ");
      ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expr;
      parseDetailExpression(LineInfo.CLASSINSTANCECREATION, cu,
          classInstanceCreation.getExpression());
      List<Expression> exprs = classInstanceCreation.arguments();
      for (Expression exp : exprs) {
        // System.out.println("Expr " + exp);
        parseDetailExpression(LineInfo.CLASS_ARGUMENT, cu, exp);
      }
      if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
        // System.out.println("Need to analyze this");
      }

    } else if (expr instanceof ConditionalExpression) {
      // System.out.println("ConditionalExpression ");
      ConditionalExpression conditionalExpression = (ConditionalExpression) expr;
      // //System.out.println(conditionalExpression.getExpression());
      // //System.out.println(conditionalExpression.getThenExpression());
      // //System.out.println(conditionalExpression.getElseExpression());
      parseDetailExpression(LineInfo.IF_EXPRESSION, cu, conditionalExpression.getExpression());
      parseDetailExpression(LineInfo.THEN_EXPRESSION, cu, conditionalExpression.getThenExpression());
      parseDetailExpression(LineInfo.ELSE_EXPRESSION, cu, conditionalExpression.getElseExpression());
    } else if (expr instanceof InfixExpression) {
      // System.out.println("InfixExpression ");
      InfixExpression infixExpression = (InfixExpression) expr;
      // System.out.println(infixExpression.getOperator().toString());
      // //System.out.println("left InfixExpression" + infixExpression.getLeftOperand());
      // //System.out.println("right InfixExpression" + infixExpression.getRightOperand());
      parseDetailExpression(LineInfo.LEFT_INFIXEXPRESSION, cu, infixExpression.getLeftOperand());
      parseDetailExpression(LineInfo.RIGHT_INFIXEXPRESSION, cu, infixExpression.getRightOperand());
      if (infixExpression.hasExtendedOperands()) {
        List<Expression> exprs = infixExpression.extendedOperands();
        for (Expression exp : exprs) {
          // System.out.println("Expr " + exp);
          parseDetailExpression(LineInfo.EXTEND_INFIXEXPRESSION, cu, exp);
        }
      }
    } else if (expr instanceof InstanceofExpression) {
      // System.out.println("InstanceofExpression ");
    } else if (expr instanceof NullLiteral) {
      // System.out.println("NullLiteral ");
    } else if (expr instanceof NumberLiteral) {
      // System.out.println("NumberLiteral ");
    } else if (expr instanceof ParenthesizedExpression) {
      // System.out.println("ParenthesizedExpression ");
      ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expr;
      parseDetailExpression(LineInfo.PARENTHESIZEDEXPRESSION, cu,
          parenthesizedExpression.getExpression());
    } else if (expr instanceof PostfixExpression) {
      // System.out.println("PostfixExpression ");
    } else if (expr instanceof PrefixExpression) {
      // System.out.println("PrefixExpression ");
    } else if (expr instanceof SuperFieldAccess) {
      // System.out.println("SuperFieldAccess ");
    } else if (expr instanceof ThisExpression) {
      // System.out.println("ThisExpression ");
    } else if (expr instanceof SuperMethodInvocation) {
      // System.out.println("SuperMethodInvocation ");
    } else if (expr instanceof Annotation) {
      // Annotation annotation = (Annotation)expr;
      // System.out.println("Annotation ");

    } else if (expr instanceof MethodInvocation) {
      // System.out.println("MethodInvocation ");
      MethodInvocation methodInvocation = (MethodInvocation) expr;
      // //System.out.println("MethodInvocation " +
      // methodInvocation.resolveMethodBinding().getName() + " : " +
      // methodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName());

      if (methodInvocation.resolveMethodBinding() != null) {
        // usedClass.put(methodInvocation.resolveMethodBinding().getName() + ":" +
        // cu.getLineNumber(methodInvocation.getStartPosition()) + ":" +
        // methodInvocation.getStartPosition(),
        // methodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName());


        // //System.out.println("-------not null------");
        // else if (methodInvocation.resolveMethodBinding()
        // .getMethodDeclaration() != null)
        // //System.out.println("MethodInvocation as Argument  at "
        // + methodInvocation.hashCode()
        // + " : "
        // + cu.getLineNumber(methodInvocation
        // .getStartPosition())
        // + " length "
        // + methodInvocation.getLength()
        // + " in "
        // + methodInvocation.resolveMethodBinding()
        // .getMethodDeclaration().getName()
        // + " with parent "
        // + methodInvocation.getParent().hashCode()
        // + " at "
        // + cu.getLineNumber(methodInvocation.getParent()
        // .getStartPosition())
        // + " exp "
        // + methodInvocation.getExpression()
        // + " : "
        // + methodInvocation.resolveMethodBinding()
        // .getDeclaringClass().getQualifiedName());
      }
      parseDetailMethodInvocation(LineInfo.METHODINVOCATION, cu, methodInvocation);
      // //System.out.println("" +
      // methodInvocation.getName().getFullyQualifiedName());
    } else {
      // System.out.println("Can't detect :(");
    }
    // System.out.println("Done");
  }

  public static void getExpressionType(Expression expr) {
    System.out.println("Expression " + expr);
    if (expr instanceof ArrayAccess) {
      ArrayAccess arrayAccess = (ArrayAccess) expr;
      System.out.println("ArrayAccess " + arrayAccess.getArray());
    } else if (expr instanceof Name) {
      Name name = (Name) expr;

      if (name.isQualifiedName()) {
        System.out.println("Qualified Name " + name.getFullyQualifiedName());
        QualifiedName qName = (QualifiedName) name;

        // qName.getQualifier().resolveTypeBinding().getDeclaringClass().getName()
        if (qName.getQualifier() != null && qName.getQualifier().resolveTypeBinding() != null)
          System.out.println(" : " + qName.getQualifier().getFullyQualifiedName() + " : "
              + qName.getName().getFullyQualifiedName() + " : " + qName.getFullyQualifiedName());
      } else {
        System.out.println("Simple Name " + name.getFullyQualifiedName());
      }
    } else if (expr instanceof FieldAccess) {
      FieldAccess fileAccess = (FieldAccess) expr;
      System.out.println("FileAccess " + fileAccess.getName().getFullyQualifiedName());
    } else if (expr instanceof StringLiteral) {
      StringLiteral stringLiteral = (StringLiteral) expr;
      System.out.println("StringLiteral " + stringLiteral.getLiteralValue());
    } else if (expr instanceof BooleanLiteral) {
      BooleanLiteral booleanLiteral = (BooleanLiteral) expr;
      String value = booleanLiteral.booleanValue() ? "true" : "false";
      System.out.println("BooleanLiteral " + value);
    } else if (expr instanceof VariableDeclarationExpression) {
      VariableDeclarationExpression variableDeclarationExpression =
          (VariableDeclarationExpression) expr;
      System.out.println("VariableDeclarationExpression "
          + variableDeclarationExpression.toString());
    } else if (expr instanceof TypeLiteral) {
      System.out.println("VariableDeclarationExpression ");
    } else if (expr instanceof ArrayCreation) {
      System.out.println("ArrayCreation ");
    } else if (expr instanceof ArrayInitializer) {
      System.out.println("ArrayInitializer ");
    } else if (expr instanceof Assignment) {
      System.out.println("Assignment ");
      System.out.println("left " + ((Assignment) expr).getLeftHandSide());
      System.out.println("left " + ((Assignment) expr).getRightHandSide());

      // parseExpression(usedClass, LineInfo.LEFT_HANDSIDE, cu, ((Assignment)
      // expr).getLeftHandSide());
      // parseExpression(usedClass, LineInfo.RIGHT_HANDSIDE, cu, ((Assignment)
      // expr).getRightHandSide());
    } else if (expr instanceof CastExpression) {
      System.out.println("CastExpression ");
      CastExpression castExpression = (CastExpression) expr;

    } else if (expr instanceof CharacterLiteral) {
      System.out.println("CharacterLiteral ");
    } else if (expr instanceof ClassInstanceCreation) {
      System.out.println("ClassInstanceCreation ");
      ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expr;

      List<Expression> exprs = classInstanceCreation.arguments();
      for (Expression exp : exprs) {
        System.out.println("Expr " + exp);

      }
      if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
        System.out.println("Need to analyze this");
      }

    } else if (expr instanceof ConditionalExpression) {
      System.out.println("ConditionalExpression ");
      ConditionalExpression conditionalExpression = (ConditionalExpression) expr;
      // System.out.println(conditionalExpression.getExpression());
      // System.out.println(conditionalExpression.getThenExpression());
      // System.out.println(conditionalExpression.getElseExpression());

    } else if (expr instanceof InfixExpression) {
      System.out.println("InfixExpression ");
      InfixExpression infixExpression = (InfixExpression) expr;
      System.out.println(infixExpression.getLeftOperand().toString());
      System.out.println(infixExpression.getRightOperand().toString());
      System.out.println(infixExpression.getOperator().toString());
      // System.out.println("left InfixExpression" + infixExpression.getLeftOperand());
      // System.out.println("right InfixExpression" + infixExpression.getRightOperand());

      if (infixExpression.hasExtendedOperands()) {
        List<Expression> exprs = infixExpression.extendedOperands();
        for (Expression exp : exprs) {
          System.out.println("Expr " + exp);

        }
      }
    } else if (expr instanceof InstanceofExpression) {
      System.out.println("InstanceofExpression ");
    } else if (expr instanceof NullLiteral) {
      System.out.println("NullLiteral ");
    } else if (expr instanceof NumberLiteral) {
      System.out.println("NumberLiteral ");
    } else if (expr instanceof ParenthesizedExpression) {
      System.out.println("ParenthesizedExpression ");
      ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expr;

    } else if (expr instanceof PostfixExpression) {
      System.out.println("PostfixExpression ");
    } else if (expr instanceof PrefixExpression) {
      System.out.println("PrefixExpression ");
    } else if (expr instanceof SuperFieldAccess) {
      System.out.println("SuperFieldAccess ");
    } else if (expr instanceof ThisExpression) {
      System.out.println("ThisExpression ");
    } else if (expr instanceof SuperMethodInvocation) {
      System.out.println("SuperMethodInvocation ");
    } else if (expr instanceof Annotation) {
      // Annotation annotation = (Annotation)expr;
      System.out.println("Annotation ");

    } else if (expr instanceof MethodInvocation) {
      System.out.println("MethodInvocation ");
      MethodInvocation methodInvocation = (MethodInvocation) expr;
      // System.out.println("MethodInvocation " + methodInvocation.resolveMethodBinding().getName()
      // + " : " + methodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName());

      if (methodInvocation.resolveMethodBinding() != null) {
        // usedClass.put(methodInvocation.resolveMethodBinding().getName() + ":" +
        // cu.getLineNumber(methodInvocation.getStartPosition()) + ":" +
        // methodInvocation.getStartPosition(),
        // methodInvocation.resolveMethodBinding().getDeclaringClass().getQualifiedName());


        // System.out.println("-------not null------");
        // else if (methodInvocation.resolveMethodBinding()
        // .getMethodDeclaration() != null)
        // System.out.println("MethodInvocation as Argument  at "
        // + methodInvocation.hashCode()
        // + " : "
        // + cu.getLineNumber(methodInvocation
        // .getStartPosition())
        // + " length "
        // + methodInvocation.getLength()
        // + " in "
        // + methodInvocation.resolveMethodBinding()
        // .getMethodDeclaration().getName()
        // + " with parent "
        // + methodInvocation.getParent().hashCode()
        // + " at "
        // + cu.getLineNumber(methodInvocation.getParent()
        // .getStartPosition())
        // + " exp "
        // + methodInvocation.getExpression()
        // + " : "
        // + methodInvocation.resolveMethodBinding()
        // .getDeclaringClass().getQualifiedName());
      }

      // System.out.println("" +
      // methodInvocation.getName().getFullyQualifiedName());
    } else {
      System.out.println("Can't detect :(");
    }
    System.out.println("Done");
  }

  public static String getExpressionRawType(Expression expr) {
    String result = "";
    if (expr instanceof ArrayAccess) {
      result = "ArrayAccess";
    } else if (expr instanceof Name) {
      Name name = (Name) expr;
      if (name.isQualifiedName()) {
        result = "QualifiedName";
      } else {
        result = "SimpleName";
      }
    } else if (expr instanceof FieldAccess) {
      result = FIELDACCESS;
    } else if (expr instanceof StringLiteral) {
      result = "StringLiteral";
    } else if (expr instanceof BooleanLiteral) {
      result = "BooleanLiteral";
    } else if (expr instanceof VariableDeclarationExpression) {
      result = "VariableDeclarationExpression";
    } else if (expr instanceof TypeLiteral) {
      result = "VariableDeclarationExpression";
    } else if (expr instanceof ArrayCreation) {
      result = "ArrayCreation";
    } else if (expr instanceof ArrayInitializer) {
      result = "ArrayInitializer";
    } else if (expr instanceof Assignment) {
      result = "Assignment";
    } else if (expr instanceof CastExpression) {
      result = "CastExpression";
    } else if (expr instanceof CharacterLiteral) {
      result = "CharacterLiteral";
    } else if (expr instanceof ClassInstanceCreation) {
      result = "ClassInstanceCreation";
    } else if (expr instanceof ConditionalExpression) {
      result = "ConditionalExpression";
    } else if (expr instanceof InfixExpression) {
      result = "InfixExpression";
    } else if (expr instanceof InstanceofExpression) {
      result = "InstanceofExpression";
    } else if (expr instanceof NullLiteral) {
      result = "NullLiteral";
    } else if (expr instanceof NumberLiteral) {
      result = "NumberLiteral";
    } else if (expr instanceof ParenthesizedExpression) {
      result = "ParenthesizedExpression";
    } else if (expr instanceof PostfixExpression) {
      result = "PostfixExpression";
    } else if (expr instanceof PrefixExpression) {
      result = "PrefixExpression";
    } else if (expr instanceof SuperFieldAccess) {
      result = "SuperFieldAccess";
    } else if (expr instanceof ThisExpression) {
      result = "ThisExpression";
    } else if (expr instanceof SuperMethodInvocation) {
      result = "SuperMethodInvocation";
    } else if (expr instanceof Annotation) {
      result = "Annotation";
    } else if (expr instanceof MethodInvocation) {
      result = "MethodInvocation";
    } else {
      result = "nil";
    }
    return result;
  }

  public static String parseBinding(ASTNode node, IBinding fBinding) {

    // System.out.println("-------not null------");
    switch (fBinding.getKind()) {
      case IBinding.VARIABLE:
        IVariableBinding variableBinding = (IVariableBinding) fBinding;
        // System.out.println("SimpleName " + node.getFullyQualifiedName());
        if (variableBinding.isField()) {
          // else if (confSettings.contains(variableBinding.getDeclaringClass()
          // .getQualifiedName())) {
          // System.out.println("Usage of field super '"
          // + " in class "
          // + variableBinding.getDeclaringClass()
          // .getQualifiedName()
          // + "' at line "
          // + cu.getLineNumber(node.getStartPosition())
          // + " with parent "
          // + node.getParent().hashCode()
          // + " at "
          // + cu.getLineNumber(node.getParent()
          // .getStartPosition()) + " id "
          // + variableBinding.getVariableId());
          // }
        }
        return (variableBinding.getDeclaringClass() != null) ? variableBinding.getDeclaringClass()
            .getQualifiedName() : "";
        // variableBinding.
        // break;

      case IBinding.TYPE:
        ITypeBinding typeBinding = (ITypeBinding) fBinding;
        // System.out.println("Type " + typeBinding.getName());
        return (typeBinding != null) ? typeBinding.getName() : "";
        // break;
      default:
        return "";

    }
  }

  public static void parseMethodInvocation(Map<String, String> usedClass, int type,
      CompilationUnit cu, MethodInvocation node) {

    // System.out.println("Arguments ");
    parseExpression(usedClass, type, cu, node.getExpression());
    List<Expression> arguments = node.arguments();
    for (Expression expr : arguments) {
      parseExpression(usedClass, LineInfo.METHOD_ARGUMENT, cu, expr);
    }
  }

  public static void parseDetailMethodInvocation(int type, CompilationUnit cu, MethodInvocation node) {

    // System.out.println("Arguments ");
    parseDetailExpression(type, cu, node.getExpression());
    List<Expression> arguments = node.arguments();
    for (Expression expr : arguments) {
      parseDetailExpression(LineInfo.METHOD_ARGUMENT, cu, expr);
    }
  }

  public static LineInfo getInfoFromLine(int lineNumber, Map<Integer, LineInfo> lineToNode) {
    LineInfo result = new LineInfo(-1, -1, -1, "");
    int[] keys = new int[lineToNode.keySet().size()];
    int i = 0;
    for (Integer key : lineToNode.keySet()) {
      keys[i++] = key;
    }
    Arrays.sort(keys);
    for (i = 0; i < keys.length; i++) {
      LineInfo node = lineToNode.get(keys[i]);
      if (node != null)
        if (node.startLine <= lineNumber && node.endLine >= lineNumber) {
          result = node;
        }
    }
    return result;
  }

  public static LineInfo getClosestAssignmentStatement(int lineNumber,
      Map<Integer, LineInfo> lineToNode) {
    LineInfo result = null;
    int[] keys = new int[lineToNode.keySet().size()];
    int i = 0;
    for (Integer key : lineToNode.keySet()) {
      keys[i++] = key;
    }
    Arrays.sort(keys);
    for (i = 0; i < keys.length; i++) {
      LineInfo node = lineToNode.get(keys[i]);

      if (node != null && node.startLine <= lineNumber && node.endLine >= lineNumber)
        if (node.type == LineInfo.ASSIGNMENT) {
          result = node;
        }
    }
    return result;
  }

  // unchecked
  public static List<LineInfo> getInfoFromMethodInvocation(String methodName, int nbParameters,
      Map<Integer, LineInfo> lineToNode) {
    // LineInfo result = new LineInfo(-1, -1, -1, "");
    ArrayList<LineInfo> result = new ArrayList<LineInfo>();
    int[] keys = new int[lineToNode.keySet().size()];
    int i = 0;
    for (Integer key : lineToNode.keySet()) {
      keys[i++] = key;
    }
    Arrays.sort(keys);
    for (i = 0; i < keys.length; i++) {
      LineInfo node = lineToNode.get(keys[i]);
      if (node.type == LineInfo.METHODINVOCATION)
        if (methodName.equals(node.name)) {
          // System.out.println("Found method " + node.name + " has " + node.parameters.size() +
          // " need to have " + nbParameters);
          // if (node.parameters.size() == nbParameters) //need to check later to get the correct
          // method of a class. A class might have many method with the same name
          result.add(node);
          // return node;
        }
    }
    return result;
  }

  // unchecked
  public static LineInfo getInfoFromMethodDeclaration(String methodName, int nbParameters,
      Map<Integer, LineInfo> lineToNode) {
    // LineInfo result = new LineInfo(-1, -1, -1, "");
    LineInfo result = null;
    int[] keys = new int[lineToNode.keySet().size()];
    int i = 0;
    for (Integer key : lineToNode.keySet()) {
      keys[i++] = key;
    }
    Arrays.sort(keys);
    for (i = 0; i < keys.length; i++) {
      LineInfo node = lineToNode.get(keys[i]);
      if (node != null && node.type == LineInfo.METHOD_DECLARATION)
        if (methodName.equals(node.name)) {
          // System.out.println("Found method " + node.name + " has " + node.parameters.size() +
          // " need to have " + nbParameters);
          // if (node.parameters.size() == nbParameters) //need to check later to get the correct
          // method of a class. A class might have many method with the same name
          return node;
        }
    }
    return result;
  }

  public static String getStatementType(LineInfo node) {
    String stmtType = "";
    int nbInstance = -1;
    if (node.node != null)
      nbInstance = node.node.getStartPosition();
    // for (int ii = 0; ii < node.nbInstancePerType.length; ii++) {
    // for (int statementType : node.statementType) {
    if (node.statementType.size() > 0) {
      for (int i = 0; i < 1; i++) {
        int statementType = node.statementType.get(i);
        if (i < node.nameVsStatement.size())
          // stmtType += node.nameVsStatement.get(i) + ":";
          // System.out.println(node.nameVsStatement.get(i) );
          // int nbInstance = node.nbInstancePerType[ii];
          // if (nbInstance > 0) {
          // int statementType = ii;
          switch (statementType) {
            case LineInfo.ASSERTSTATEMENT:
              stmtType += "AssertStatement";
              break;
            case LineInfo.BLOCK:
              stmtType += "Block";
              break;
            case LineInfo.BREAKSTATEMENT:
              stmtType += "BreakStatement";
              break;
            case LineInfo.CONSTRUCTORINVOCATION:
              stmtType += "ConstructorInvocation";
              break;
            case LineInfo.DOSTATEMENT:
              stmtType += "DoStatement";
              break;
            case LineInfo.EXPRESSIONSTATEMENT:
              stmtType += "ExpressionStatement";
              break;
            case LineInfo.FORSTATEMENT:
              stmtType += "ForStatement";
              break;
            case LineInfo.ASSIGNMENT:
              stmtType += "Assignment";
              break;
            case LineInfo.CONDITIONALEXPRESSION:
              stmtType += "ConditionalExpression";
              break;
            case LineInfo.ENHANCEDFORSTATEMENT:
              stmtType += "EnhancedForStatement";
              break;
            case LineInfo.FIELDACCESS:
              stmtType += "FieldAccess";
              break;
            case LineInfo.IFSTATEMENT:
              stmtType += "IfStatement";
              break;
            case LineInfo.METHODINVOCATION:
              stmtType += "MethodInvocation";
              break;
            case LineInfo.NUMBERLITERAL:
              stmtType += "NumberLiteral";
              break;
            case LineInfo.STRINGLITERAL:
              stmtType += "StringLiteral";
              break;
            case LineInfo.RETURNSTATEMENT:
              stmtType += "ReturnStatement";
              break;
            case LineInfo.SWITCHCASE:
              stmtType += "SwitchCase";
              break;
            case LineInfo.SWITCHSTATEMENT:
              stmtType += "SwitchStatement";
              break;
            case LineInfo.SYNCHRONIZEDSTATEMENT:
              stmtType += "SynchronizedStatement";
              break;
            case LineInfo.WHILESTATEMENT:
              stmtType += "WhileStatement";
              break;
            case LineInfo.SIMPLENAME:
              stmtType += "SimpleName";
              break;
            case LineInfo.CLASSINSTANCECREATION:
              stmtType += "ClassInstanceCreation";
              break;
            case LineInfo.VARIABLEDECLARATIONFRAGMENT:
              stmtType += "VariableDeclarationFragment";
              break;
            case LineInfo.QUALIFIEDNAME:
              stmtType += "QualifiedName";
              break;
          }
        // }
      }
    }
    return stmtType;
  }

  // public static String getStatementType(LineInfo node) {
  // String stmtType = "";
  // int nbInstance = -1;
  // if (node.node!=null)
  // nbInstance = node.node.getStartPosition();
  // // for (int ii = 0; ii < node.nbInstancePerType.length; ii++) {
  // // for (int statementType : node.statementType) {
  // for (int i = 0; i < 1; i++) {
  // int statementType = node.statementType.get(i);
  // if (i < node.nameVsStatement.size())
  // // stmtType += node.nameVsStatement.get(i) + ":";
  // // System.out.println(node.nameVsStatement.get(i) );
  // // int nbInstance = node.nbInstancePerType[ii];
  // // if (nbInstance > 0) {
  // // int statementType = ii;
  // switch (statementType) {
  // case LineInfo.ASSERTSTATEMENT:
  // stmtType += "AssertStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.BLOCK:
  // stmtType += "Block(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.BREAKSTATEMENT:
  // stmtType += "BreakStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.CONSTRUCTORINVOCATION:
  // stmtType += "ConstructorInvocation(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.DOSTATEMENT:
  // stmtType += "DoStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.EXPRESSIONSTATEMENT:
  // stmtType += "ExpressionStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.FORSTATEMENT:
  // stmtType += "ForStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.ASSIGNMENT:
  // stmtType += "Assignment(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.CONDITIONALEXPRESSION:
  // stmtType += "ConditionalExpression(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.ENHANCEDFORSTATEMENT:
  // stmtType += "EnhancedForStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.FIELDACCESS:
  // stmtType += "FieldAccess(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.IFSTATEMENT:
  // stmtType += "IfStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.METHODINVOCATION:
  // stmtType += "MethodInvocation(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.NUMBERLITERAL:
  // stmtType += "NumberLiteral(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.STRINGLITERAL:
  // stmtType += "StringLiteral(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.RETURNSTATEMENT:
  // stmtType += "ReturnStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.SWITCHCASE:
  // stmtType += "SwitchCase(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.SWITCHSTATEMENT:
  // stmtType += "SwitchStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.SYNCHRONIZEDSTATEMENT:
  // stmtType += "SynchronizedStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.WHILESTATEMENT:
  // stmtType += "WhileStatement(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.SIMPLENAME:
  // stmtType += "SimpleName(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.CLASSINSTANCECREATION:
  // stmtType += "ClassInstanceCreation(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.VARIABLEDECLARATIONFRAGMENT:
  // stmtType += "VariableDeclarationFragment(" + nbInstance + ")-" ;
  // break;
  // case LineInfo.QUALIFIEDNAME:
  // stmtType += "QualifiedName(" + nbInstance + ")-" ;
  // break;
  // }
  // // }
  // }
  // return stmtType;
  // }


  public static String parseLineInfo(LineInfo node) {
    String stmtType = "";
    int nbInstance = -1;
    if (node.node != null)
      nbInstance = node.node.getStartPosition();
    // for (int ii = 0; ii < node.nbInstancePerType.length; ii++) {
    // for (int statementType : node.statementType) {
    for (int i = 0; i < node.statementType.size(); i++) {
      int statementType = node.statementType.get(i);
      if (i < node.nameVsStatement.size())
        stmtType += node.nameVsStatement.get(i) + ":";
      // System.out.println(node.nameVsStatement.get(i) );
      // int nbInstance = node.nbInstancePerType[ii];
      // if (nbInstance > 0) {
      // int statementType = ii;
      switch (statementType) {
        case LineInfo.ASSERTSTATEMENT:
          stmtType += "AssertStatement(" + nbInstance + ")-";
          break;
        case LineInfo.BLOCK:
          stmtType += "Block(" + nbInstance + ")-";
          break;
        case LineInfo.BREAKSTATEMENT:
          stmtType += "BreakStatement(" + nbInstance + ")-";
          break;
        case LineInfo.CONSTRUCTORINVOCATION:
          stmtType += "ConstructorInvocation(" + nbInstance + ")-";
          break;
        case LineInfo.DOSTATEMENT:
          stmtType += "DoStatement(" + nbInstance + ")-";
          break;
        case LineInfo.EXPRESSIONSTATEMENT:
          stmtType += "ExpressionStatement(" + nbInstance + ")-";
          break;
        case LineInfo.FORSTATEMENT:
          stmtType += "ForStatement(" + nbInstance + ")-";
          break;
        case LineInfo.ASSIGNMENT:
          stmtType += "Assignment(" + nbInstance + ")-";
          break;
        case LineInfo.CONDITIONALEXPRESSION:
          stmtType += "ConditionalExpression(" + nbInstance + ")-";
          break;
        case LineInfo.ENHANCEDFORSTATEMENT:
          stmtType += "EnhancedForStatement(" + nbInstance + ")-";
          break;
        case LineInfo.FIELDACCESS:
          stmtType += "FieldAccess(" + nbInstance + ")-";
          break;
        case LineInfo.IFSTATEMENT:
          stmtType += "IfStatement(" + nbInstance + ")-";
          break;
        case LineInfo.METHODINVOCATION:
          stmtType += "MethodInvocation(" + nbInstance + ")-";
          break;
        case LineInfo.NUMBERLITERAL:
          stmtType += "NumberLiteral(" + nbInstance + ")-";
          break;
        case LineInfo.STRINGLITERAL:
          stmtType += "StringLiteral(" + nbInstance + ")-";
          break;
        case LineInfo.RETURNSTATEMENT:
          stmtType += "ReturnStatement(" + nbInstance + ")-";
          break;
        case LineInfo.SWITCHCASE:
          stmtType += "SwitchCase(" + nbInstance + ")-";
          break;
        case LineInfo.SWITCHSTATEMENT:
          stmtType += "SwitchStatement(" + nbInstance + ")-";
          break;
        case LineInfo.SYNCHRONIZEDSTATEMENT:
          stmtType += "SynchronizedStatement(" + nbInstance + ")-";
          break;
        case LineInfo.WHILESTATEMENT:
          stmtType += "WhileStatement(" + nbInstance + ")-";
          break;
        case LineInfo.SIMPLENAME:
          stmtType += "SimpleName(" + nbInstance + ")-";
          break;
        case LineInfo.CLASSINSTANCECREATION:
          stmtType += "ClassInstanceCreation(" + nbInstance + ")-";
          break;
        case LineInfo.VARIABLEDECLARATIONFRAGMENT:
          stmtType += "VariableDeclarationFragment(" + nbInstance + ")-";
          break;
        case LineInfo.QUALIFIEDNAME:
          stmtType += "QualifiedName(" + nbInstance + ")-";
          break;
      }
      // }
    }
    return stmtType;
  }

  public static String getNextNodeType(int type) {
    switch (type) {
      case NextNode.VARIABLE:
        return "LocalVariable";
      case NextNode.FIELD:
        return "Field";
      case NextNode.METHODINVOCATION:
        return "MethodDeclaration";
      case NextNode.RETURN_METHOD:
        return "MethodInvocation";
      case NextNode.CLASS:
        return "Class";
      case NextNode.METHOD:
        return "Method";
      default:
        return "Parameter";
    }
  }

  public static String METHOD_INVOCATION = "MethodInvocation";
  public static String FIELDACCESS = "FieldAccess";
  public static String VARIABLE_DECLARATION_FRAGMENT = "VariableDeclarationFragment";

  /*
   * http://ustfyp-hunk3-0910.googlecode.com/svn-history/r42/myAST/src/myProgramHere/myAst.java
   */
  public static String getASTNodeType(ASTNode node) {
    if (node == null) {
      return "";
    }
    switch (node.getNodeType()) {
      case ASTNode.ASSIGNMENT:
        return "Assignment";
      case ASTNode.METHOD_INVOCATION:
        return "MethodInvocation";
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
        return "VariableDeclarationFragment";
      case ASTNode.IF_STATEMENT:
        return "IfStatement";
      case ASTNode.CONDITIONAL_EXPRESSION: // expr ? value1 : value2 might relate to an assignment
        return "ConditionalExpression";
      case ASTNode.CLASS_INSTANCE_CREATION: // new class name
        return "ClassInstanceCreation";
      case ASTNode.INFIX_EXPRESSION: // ==, ||, !=, ... => might relate to If Statement
        return "InfixExpression";
      case ASTNode.PARENTHESIZED_EXPRESSION: // methodinvocation(( ...))
        return "ParenthesizedExpression";
      case ASTNode.PREFIX_EXPRESSION: // !variable_or_expression
        PrefixExpression expr = (PrefixExpression) node;
        return "PrefixExpression " + expr.getOperator();
      case ASTNode.RETURN_STATEMENT:
        return "ReturnStatement";
      case ASTNode.CAST_EXPRESSION:
        return "CastExpression";
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: // super(...)
        return "SuperConstructorInvocation";
      case ASTNode.CONSTRUCTOR_INVOCATION:
        return "ConstructorInvocation"; // this(parameter ...)
        // all node types above this line are used by analyzeParentStatement
        // all node types below this line are used by other analyses which are called within
        // analyzeParentStatement
      case ASTNode.FIELD_ACCESS:
        return "FieldAccess";
      case ASTNode.ARRAY_CREATION:
        return "ArrayCreation";
      case ASTNode.EXPRESSION_STATEMENT:
        return "ExpressionStatement";
      case ASTNode.INSTANCEOF_EXPRESSION:
        return "InstanceOfExpression";
      case ASTNode.MEMBER_REF:
        return "MemberRef";
      case ASTNode.PARAMETERIZED_TYPE:
        return "ParameterizedType";
      case ASTNode.POSTFIX_EXPRESSION:
        return "PostFix";
      case ASTNode.QUALIFIED_TYPE:
        return "QualifiedType";
      case ASTNode.MEMBER_VALUE_PAIR:
        return "MemberValuePair";
      case ASTNode.METHOD_REF:
        return "MethodRef";
      case ASTNode.METHOD_REF_PARAMETER:
        return "MethodRefParam";
      case ASTNode.QUALIFIED_NAME:
        return "QualifiedName";
      case ASTNode.TYPE_PARAMETER:
        return "TypeParam";
      case ASTNode.BLOCK:
        return "Block";
      case ASTNode.FOR_STATEMENT:
        return "ForStatement";
      case ASTNode.SWITCH_CASE:
        return "SwitchCase";
      case ASTNode.SWITCH_STATEMENT:
        return "SwitchStatement";
      default:
        return "";
    }
  }

  /*
   * backup
   * http://ustfyp-hunk3-0910.googlecode.com/svn-history/r42/myAST/src/myProgramHere/myAst.java
   * 
   * public static String getASTNodeType(ASTNode node) { switch (node.getNodeType()) { case
   * ASTNode.ASSIGNMENT: return "Assignment"; case ASTNode.METHOD_INVOCATION: return
   * "MethodInvocation"; case ASTNode.VARIABLE_DECLARATION_FRAGMENT: return
   * "VariableDeclarationFragment"; case ASTNode.IF_STATEMENT: return "IfStatement"; case
   * ASTNode.CONDITIONAL_EXPRESSION: // expr ? value1 : value2 might relate to an assignment return
   * "ConditionalExpression"; // case ASTNode.EXPRESSION_STATEMENT: // return "ExpressionStatement";
   * // case ASTNode.BOOLEAN_LITERAL: // return "BooleanLiteral"; case
   * ASTNode.CLASS_INSTANCE_CREATION: //new class name return "ClassInstanceCreation"; // case
   * ASTNode.FIELD_ACCESS: // return "FieldAccess"; case ASTNode.INFIX_EXPRESSION: // ==, ||, !=,
   * ... => might relate to If Statement return "InfixExpression"; // case
   * ASTNode.INSTANCEOF_EXPRESSION: // return "InstanceOfExpression"; case
   * ASTNode.PARENTHESIZED_EXPRESSION: //methodinvocation(( ...)) return "ParenthesizedExpression";
   * case ASTNode.PREFIX_EXPRESSION: // !variable_or_expression PrefixExpression expr =
   * (PrefixExpression)node; return "PrefixExpression " + expr.getOperator(); // case
   * ASTNode.WILDCARD_TYPE: // return "WildcardType"; case ASTNode.RETURN_STATEMENT: return
   * "ReturnStatement"; case ASTNode.CAST_EXPRESSION: return "CastExpression"; // case
   * ASTNode.SUPER_METHOD_INVOCATION: // return "SuperMethodInvocation"; case
   * ASTNode.SUPER_CONSTRUCTOR_INVOCATION: //super(...) return "SuperConstructorInvocation"; // case
   * ASTNode.SUPER_FIELD_ACCESS: // return "SuperFieldAccess"; // case ASTNode.THIS_EXPRESSION: //
   * return "ThisExpression"; // case ASTNode.INITIALIZER: // return "Initializer"; case
   * ASTNode.CONSTRUCTOR_INVOCATION: return "ConstructorInvocation"; //this(parameter ...) default:
   * return ""; } }
   */
}
