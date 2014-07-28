package ar.edu.jdynalloy.ast;

import java.util.Vector;

public class JDynAlloyVarDeclarationCollector extends JDynAlloyVisitor {
	
	public JDynAlloyVarDeclarationCollector() {
		// TODO Auto-generated constructor stub
	}

	
	public Object visit(JVariableDeclaration n) {
		Vector<Object> result = new Vector<Object>();
		result.add(n.getVariable());
		return result;
	}


}
