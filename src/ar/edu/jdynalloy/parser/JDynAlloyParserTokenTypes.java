// $ANTLR 2.7.6 (2005-12-22): "jdynalloy.g" -> "JDynAlloyLexer.java"$

 /*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
	
package ar.edu.jdynalloy.parser;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.*;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula.Operator;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntersection;
import ar.uba.dc.rfm.alloy.parser.ExpressionParser;
import ar.uba.dc.rfm.alloy.parser.FormalParametersDeclaration;
import ar.uba.dc.rfm.alloy.parser.IAlloyExpressionParseContext;	
import ar.edu.jdynalloy.parser.JDynAlloyParsingException;
import ar.edu.jdynalloy.parser.LabelUtils;

import ar.edu.jdynalloy.ast.*;

import ar.edu.jdynalloy.xlator.JType;

import ar.edu.jdynalloy.buffer.Represents;
import ar.edu.jdynalloy.buffer.JDynAlloyParserModuleBuffer;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.util.Collections;


public interface JDynAlloyParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LITERAL_module = 4;
	int IDENT = 5;
	int LITERAL_abstract = 6;
	int LITERAL_sig = 7;
	int LITERAL_extends = 8;
	int LITERAL_in = 9;
	int LBRACE = 10;
	int RBRACE = 11;
	int LITERAL_field = 12;
	int COLON = 13;
	int OBJECT_INVARIANT = 14;
	int CLASS_INVARIANT = 15;
	int OBJECT_CONSTRAINT = 16;
	int CLASS_CONSTRAINT = 17;
	int REPRESENTS = 18;
	int SUCH_THAT = 19;
	int VIRTUAL = 20;
	int PROGRAM = 21;
	int LBRACKET = 22;
	int COMMA = 23;
	int RBRACKET = 24;
	int SPECIFICATION = 25;
	int IMPLEMENTATION = 26;
	int SPECCASE = 27;
	int SPECCASE_ID = 28;
	int REQUIRES = 29;
	int ENSURES = 30;
	int MODIFIES = 31;
	int ASSERT = 32;
	int SEMICOLON = 33;
	int IF = 34;
	int ELSE = 35;
	int ASSUME = 36;
	int VAR = 37;
	int SKIP = 38;
	int CREATE_OBJECT = 39;
	int LT = 40;
	int GT = 41;
	int ASSIGNMENT = 42;
	int SUPER = 43;
	int CALL = 44;
	int WHILE = 45;
	int LOOP_INVARIANT = 46;
	int HAVOC = 47;
	int IMPLIES = 48;
	int OR = 49;
	int AND = 50;
	int LPAREN = 51;
	int RPAREN = 52;
	int NOT = 53;
	int LITERAL_some = 54;
	int LITERAL_all = 55;
	int LITERAL_lone = 56;
	int LITERAL_no = 57;
	int LITERAL_one = 58;
	int PIPE = 59;
	int STATICCALLSPEC = 60;
	int CALLSPEC = 61;
	int EQUALS = 62;
	int RARROW = 63;
	int PLUS = 64;
	int LITERAL_seq = 65;
	int LITERAL_set = 66;
	int SLASH = 67;
	int DOT = 68;
	int PLUSPLUS = 69;
	int AMPERSTAND = 70;
	int NUMBER = 71;
	int TRUE = 72;
	int FALSE = 73;
	int STAR = 74;
	int QUESTION = 75;
	int COMMENT = 76;
	int COMMENT_SLASH_SLASH = 77;
	int COMMENT_ML = 78;
	int WS = 79;
}
