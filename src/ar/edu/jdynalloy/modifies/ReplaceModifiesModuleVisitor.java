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
package ar.edu.jdynalloy.modifies;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JCreateObject;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JHavoc;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JLoopInvariant;
import ar.edu.jdynalloy.ast.JModifies;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JPostcondition;
import ar.edu.jdynalloy.ast.JPrecondition;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;
import ar.edu.jdynalloy.ast.JSkip;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.ast.JWhile;
import ar.edu.jdynalloy.binding.symboltable.FieldDescriptor;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;

public class ReplaceModifiesModuleVisitor extends JDynAlloyVisitor {

	private JDynAlloyBinding dynJAlloyBinding;
	private JDynAlloyModule jDynAlloyModule;
	private SymbolTable symbolTable;
	
	private boolean isStaticProgram = false;

	public JDynAlloyBinding getDynJAlloyBinding() {
		return dynJAlloyBinding;
	}

	public ReplaceModifiesModuleVisitor(JDynAlloyBinding dynJAlloyBinding, SymbolTable symbolTable, boolean isJavaArith) {
		super(isJavaArith);
		this.dynJAlloyBinding = dynJAlloyBinding;
		this.jDynAlloyModule = null;
		this.symbolTable = symbolTable;
	}

	public JDynAlloyModule getJDynAlloyModule() {
		if (jDynAlloyModule == null) {
			throw new JDynAlloyException("jDynAlloyModule wasn't set");
		}
		return jDynAlloyModule;
	}

	@Override
	public Object visit(JSpecCase node) {
		boolean modifiesEverithing = false;
		for (JModifies jModifies : node.getModifies()) {
			if (jModifies.isModifiesEverything()) {
				modifiesEverithing = true;
			}
		}
		if (modifiesEverithing) {
			return node;
		}

		List<JPostcondition> ensuresVec = new ArrayList<JPostcondition>();
		ensuresVec.addAll(node.getEnsures());
		
		// fields set
		Set<FieldDescriptor> fieldsSet = this.getSymbolTable().getFieldSet(this.jDynAlloyModule.getModuleId());

		// unmodificables fields
		Set<String> modificablesFieldSet = new HashSet<String>();
		boolean modifiesNothing = false;
		if (node.getModifies().isEmpty()) {
			modifiesNothing = true;
		}
		
		// recolecta info
		AlloyFormula accumlatorFormula = null;

		// recolecta la info de todos los casos
		List<AbstractModifiesCase> modifiesCases = new ArrayList<AbstractModifiesCase>();
		modifiesCases.add(new FieldModifiesCase(this.getSymbolTable()));
		modifiesCases.add(new FieldArraysModifiesCase(this.getSymbolTable()));

		if (!modifiesNothing) {
			for (JModifies jModifies : node.getModifies()) {
				AlloyFormula thisIterationFormula = processJModifies(modifiesCases, jModifies, modificablesFieldSet);
				if (accumlatorFormula == null) {
					accumlatorFormula = thisIterationFormula;
				} else {
					accumlatorFormula = AndFormula.buildAndFormula(accumlatorFormula, thisIterationFormula);
				}
			}
		}
		
		if (!isStaticProgram) {
			for (FieldDescriptor fieldDescriptor : fieldsSet) {
				AlloyFormula thisIterationFormula = null;
				if (modifiesNothing || !modificablesFieldSet.contains(fieldDescriptor.getFieldName())) {
					thisIterationFormula = unmodifiableFieldSupport(fieldDescriptor);
				}
				
				if (thisIterationFormula != null) {
					if (accumlatorFormula == null) {
						accumlatorFormula = thisIterationFormula;
					} else {
						accumlatorFormula = AndFormula.buildAndFormula(accumlatorFormula, thisIterationFormula);
					}
				}
				
			}
		}
		
		if (accumlatorFormula != null) {
			ensuresVec.add(new JPostcondition(accumlatorFormula));
		}

		return new JSpecCase(node.getRequires(), ensuresVec, node.getModifies());
	}

	private AlloyFormula processJModifies(List<AbstractModifiesCase> modifiesCases, JModifies modifies, Set<String> modificablesFieldSet) {
		Iterator<AbstractModifiesCase> iterator = modifiesCases.iterator();

		AlloyFormula alloyFormula = null;
		do {
			AbstractModifiesCase abstractModifiesCase = iterator.next();
			if (abstractModifiesCase.accepts(modifies)) {
				alloyFormula = abstractModifiesCase.generateFormula(modifies);
				modificablesFieldSet.addAll(abstractModifiesCase.getModificableFieldSet());
			}
		} while (iterator.hasNext() && alloyFormula == null);

		if (alloyFormula == null) {
			throw new JDynAlloyException("JModifies case not implemented");
		}

		return alloyFormula;
		// for (AbstractModifiesCase abstractModifiesCase : modifiesCases) {
		// if (abstractModifiesCase.generateFormula(modifies)
		// }
	}

	private AlloyFormula unmodifiableFieldSupport(FieldDescriptor fieldDescriptor) {
		AlloyFormula thisIterationFormula;
		thisIterationFormula = ModifiesUtils.createFormulaFieldValueEqualsToOldFieldValue("thiz", fieldDescriptor.getFieldName());
		return thisIterationFormula;
	}

	@Override
	public Object visit(JHavoc n) {
		return n;
	}

	@Override
	public Object visit(JAssert n) {
		return n;
	}

	@Override
	public Object visit(JAssignment n) {
		return n;
	}

	@Override
	public Object visit(JBlock n) {
		return n;
	}

	@Override
	public Object visit(JIfThenElse n) {
		return n;
	}

	@Override
	public Object visit(JSkip n) {
		return n;
	}

	@Override
	public Object visit(JWhile n) {
		return n;
	}

	@Override
	public Object visit(JCreateObject n) {
		return n;
	}

	@Override
	public Object visit(JVariableDeclaration n) {
		return n;
	}

	@Override
	public Object visit(JDynAlloyModule node) {

		symbolTable.beginScope();
		JType jtype = JTypeFactory.buildReference(node.getModuleId());
		this.symbolTable.insertLocal(JExpressionFactory.THIS_VARIABLE.getVariableId(), jtype);

		jDynAlloyModule = node;
		Set<JProgramDeclaration> programs = new HashSet<JProgramDeclaration>();
		for (JProgramDeclaration programDeclaration : node.getPrograms()) {
			programs.add((JProgramDeclaration) programDeclaration.accept(this));
		}

		JDynAlloyModule module = new JDynAlloyModule(node.getModuleId(), node.getSignature(), node.getClassSingleton(), node.getLiteralSingleton(), 
													node.getFields(), node.getClassInvariants(), node.getClassConstraints(), 
													node.getObjectInvariants(), node.getObjectConstraints(), node.getRepresents(), programs, 
													node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants(), node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants(), node.pinnedForNonRelevancyAnalysisForStryker);

		if (node.getClassSingleton() != null) {
			module.setLiteralSingleton(node.getClassSingleton());
		}

		if (node.getLiteralSingleton() != null) {
			module.setLiteralSingleton(node.getLiteralSingleton());
		}

		symbolTable.endScope();
		
		return module;
	}

	@Override
	public Object visit(JProgramCall n) {
		return n;
	}

	@Override
	public Object visit(JProgramDeclaration node) {

		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		String qualifiedMethodName = node.getSignatureId() + "_" + node.getProgramId() + "_";
		if (methodToCheck.startsWith(qualifiedMethodName)) {

			symbolTable.beginScope();

			boolean firstParameter = true;
			for (JVariableDeclaration variableDeclaration : node.getParameters()) {
				// "thiz" variable was added to SymbolTable when the method was
				// started to be analized
				// We need to skip "thiz" analysis to avoid redeclare variable
				// (only
				// for non-static methods)
				if (!node.isStatic() && firstParameter) {
					assert (variableDeclaration.getVariable().equals(JExpressionFactory.THIS_EXPRESSION.getVariable()));
					firstParameter = false;
					// skip "thiz" variable
				} else {
					this.symbolTable.insertLocal(variableDeclaration.getVariable().getVariableId(), variableDeclaration.getType());
				}
			}
			
			this.isStaticProgram = node.isStatic();

			Vector<JSpecCase> specCasesResult = new Vector<JSpecCase>();
			for (JSpecCase child : node.getSpecCases()) {
				JSpecCase specCase = (JSpecCase) child.accept(this);
				specCasesResult.add(specCase);
			}

			symbolTable.endScope();

			// dynJAlloyBinding.
			return new JProgramDeclaration(node.isVirtual(), node.getSignatureId(), node.getProgramId(), node.getParameters(), specCasesResult, 
					node.getBody(), node.getVarsResultOfArithmeticOperationsInContracts(), 
					node.getPredsEncodingValueOfArithmeticOperationsInContracts());
		} else {
			return node;
		}

	}

	@Override
	public Object visit(JSignature node) {
		return node;
	}

	@Override
	public Object visit(JField node) {
		return node;
	}

	@Override
	public Object visit(JAssume n) {
		return n;
	}

	@Override
	public Object visit(JLoopInvariant n) {
		return n;
	}

	@Override
	public Object visit(JObjectInvariant n) {
		return n;
	}

	@Override
	public Object visit(JObjectConstraint n) {
		return n;
	}

	@Override
	public Object visit(JRepresents n) {
		return n;
	}

	@Override
	public Object visit(JPostcondition n) {
		return n;
	}

	@Override
	public Object visit(JPrecondition n) {
		return n;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

}
