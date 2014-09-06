package ar.edu.jdynalloy.xlator;

import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;

class CallGraphVisitor extends JDynAlloyVisitor {

	public CallGraphVisitor(boolean isJavaArith) {
		super(isJavaArith);
	}
	
	public Graph<String> getCallGraph() {
		return callGraph;
	}

	private String currentProgramDeclaration;

	private final Graph<String> callGraph = new Graph<String>(true);

	@Override
	public Object visit(JProgramCall n) {
		String caller = currentProgramDeclaration;
		String callee = n.getProgramId();

		if (!callGraph.containsNode(caller))
			callGraph.addNode(caller);
		
		if (!callGraph.containsNode(callee))
			callGraph.addNode(callee);
		
		callGraph.addEdge(caller, callee);

		return super.visit(n);
	}

	@Override
	public Object visit(JProgramDeclaration node) {
		currentProgramDeclaration = node.getProgramId();

		if (!callGraph.containsNode(currentProgramDeclaration))
			callGraph.addNode(currentProgramDeclaration);
		
		return super.visit(node);
	}

}
