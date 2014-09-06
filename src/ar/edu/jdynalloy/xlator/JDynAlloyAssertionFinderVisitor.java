package ar.edu.jdynalloy.xlator;

import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssert;

public class JDynAlloyAssertionFinderVisitor extends JDynAlloyVisitor {

	public JDynAlloyAssertionFinderVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
	}

	private boolean assertFound = false;
	
	@Override
	public Object visit(JAssert n) {
		assertFound = true;
		return super.visit(n);
	}
	
	public boolean isAssertFound() {
		return assertFound;
	}
	
}
