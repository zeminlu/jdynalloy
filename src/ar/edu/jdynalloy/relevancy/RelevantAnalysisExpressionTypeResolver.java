package ar.edu.jdynalloy.relevancy;

import java.util.HashSet;
import java.util.Set;

import ar.edu.jdynalloy.binding.callbinding.ExpressionTypeResolver;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;

public class RelevantAnalysisExpressionTypeResolver extends
		ExpressionTypeResolver {

	private final Set<JType> relevantTypes;
	
	private Set<String> calledFunctionsNames = new HashSet<String>();

	public RelevantAnalysisExpressionTypeResolver(
			FormulaVisitor formulaVisitor, SymbolTable symbolTable) {
		super(formulaVisitor, symbolTable);
		relevantTypes = new HashSet<JType>();
	}

	
	public Set<String> getCalledFunctionsNames(){
		return this.calledFunctionsNames;
	}
	
	public Object visit(ExprConstant exprConstant) {
		JType type = (JType) super.visit(exprConstant);
		if (!type.equals(new JType("null")) && !type.equals(new JType("univ"))
				&& !type.equals(new JType("Int")) && !type.equals(new JType("boolean"))
				&& !type.equals(new JType("none"))) {
			
			relevantTypes.add(type);
		}
		return type;
	}
	
	
	public Object visit(ExprFunction exprFunction){
		this.calledFunctionsNames.add(exprFunction.getFunctionId());
		return super.visit(exprFunction);
	}
	
	public Object visit(ExprVariable exprVariable) {
		return super.visit(exprVariable);
	}

	public Set<JType> getRelevantTypes() {
		return relevantTypes;
	}

}
