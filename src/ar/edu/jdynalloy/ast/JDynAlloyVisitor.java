package ar.edu.jdynalloy.ast;

import java.util.HashSet;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.alloy.util.QFtransformer;

public class JDynAlloyVisitor implements IJDynAlloyVisitor {

	protected static class JDynAlloyModuleVisitResult {

		public Object signature_result;

		public Object class_singleton_result;

		public Object literal_singleton_result;

		public Object fields_result;

		public Object class_invariants_result;

		public Object class_constraints_result;

		public Object object_invariants_result;

		public Object object_constraints_result;

		public Object represents_result;

		public Object programs_result;

		public Object predsEncodingArithmeticConstraintsFromObjectInvariant;
		
		public Object varsUsedInArithmeticConstraintsFromObjectInvariant;
		
	}

	
	public JDynAlloyVisitor() {
	}

	public Object visit(JAssert n) {
		return null;
	}

	public Object visit(JCreateObject n) {
		return null;
	}

	public Object visit(JAssignment n) {
		return null;
	}

	public Object visit(JBlock n) {
		Vector<Object> result = new Vector<Object>();
		for (JStatement s : n.getBlock()) {
			result.add(s.accept(this));
		}
		return result;
	}

	public Object visit(JIfThenElse n) {
		Vector<Object> result = new Vector<Object>();
		result.add(n.getThen().accept(this));
		result.add(n.getElse().accept(this));
		return result;
	}

	public Object visit(JSkip n) {
		return null;
	}

	public Object visit(JWhile n) {
		Vector<Object> result = new Vector<Object>();

		if (n.getLoopInvariant() == null)
			result.add(null);
		else
			result.add(n.getLoopInvariant().accept(this));

		result.add(n.getBody().accept(this));

		return result;
	}

	public Object visit(JVariableDeclaration n) {
		return null;
	}

	public Object visit(JProgramCall n) {
		return null;
	}

	public Object visit(ar.edu.jdynalloy.ast.JAssertionDeclaration assertion) {
		throw new IllegalArgumentException(
				"class JAlloyAssertion not supported");
	}

	public Object visit(JAlloyCondition condition) {
		throw new IllegalArgumentException(
				"class JAlloyCondition not supported");
	}

	public Object visit(JProgramDeclaration node) {
		Vector<Object> result = new Vector<Object>();

		Vector<Object> varResults = new Vector<Object>();
		for (JVariableDeclaration child : node.getParameters()) {
			varResults.add(child.accept(this));
		}
		result.add(varResults);

		Vector<Object> specCasesResult = new Vector<Object>();
		for (JSpecCase child : node.getSpecCases()) {
			specCasesResult.add(child.accept(this));
		}
		result.add(specCasesResult);

		Object bodyResult = node.getBody().accept(this);
		result.add(bodyResult);
		
		Object predsFromContracts = node.getPredsEncodingValueOfArithmeticOperationsInContracts();
		result.add(predsFromContracts);
		
		Object varsFromContracts = node.getVarsResultOfArithmeticOperationsInContracts();
		result.add(varsFromContracts);

		return result;
	}

	public Object visit(JDynAlloyModule node) {

		JDynAlloyModuleVisitResult result = new JDynAlloyModuleVisitResult();

		/*
		 * signature
		 */
		Object signature_result = node.getSignature().accept(this);
		result.signature_result = signature_result;

		/*
		 * class singleton
		 */

		if (node.getClassSingleton() != null) {
			Object class_singleton_result = node.getClassSingleton().accept(
					this);
			result.class_singleton_result = class_singleton_result;
		}

		/*
		 * literal singleton
		 */
		if (node.getLiteralSingleton() != null) {
			Object literal_singleton_result = node.getLiteralSingleton()
					.accept(this);
			result.literal_singleton_result = literal_singleton_result;
		}

		/*
		 * fields 
		 */
		Vector<Object> field_results = new Vector<Object>();
		for (JField child : node.getFields()) {
			field_results.add(child.accept(this));
		}
		result.fields_result = field_results;

		/*
		 * class invariants
		 */
		Vector<Object> class_invariants_result = new Vector<Object>();
		for (JClassInvariant child : node.getClassInvariants()) {
			class_invariants_result.add(child.accept(this));
		}
		result.class_invariants_result = class_invariants_result;

		/*
		 * class constraints
		 */
		Vector<Object> class_constraints_result = new Vector<Object>();
		for (JClassConstraint child : node.getClassConstraints()) {
			class_constraints_result.add(child.accept(this));
		}
		result.class_constraints_result = class_constraints_result;

		/*
		 * object invariants
		 */
		Vector<Object> object_invariant_results = new Vector<Object>();
		for (JObjectInvariant child : node.getObjectInvariants()) {
			object_invariant_results.add(child.accept(this));
		}
		result.object_invariants_result = object_invariant_results;
		result.predsEncodingArithmeticConstraintsFromObjectInvariant = node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants();
		result.varsUsedInArithmeticConstraintsFromObjectInvariant = node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants();
		
		
		/*
		 * object constraints
		 */
		Vector<Object> object_constraints_result = new Vector<Object>();
		for (JObjectConstraint child : node.getObjectConstraints()) {
			object_constraints_result.add(child.accept(this));
		}
		result.object_constraints_result = object_constraints_result;

		/*
		 * represents
		 */
		Vector<Object> represents_results = new Vector<Object>();
		for (JRepresents child : node.getRepresents()) {
			represents_results.add(child.accept(this));
		}
		result.represents_result = represents_results;

		/*
		 * programs
		 */
		Vector<Object> program_results = new Vector<Object>();
		for (JProgramDeclaration child : node.getPrograms()) {
			program_results.add(child.accept(this));
		}
		result.programs_result = program_results;
		

		return result;
	}

	public Object visit(JSignature node) {
		return null;
	}

	@Override
	public Object visit(JField node) {
		Vector<Object> result = new Vector <Object>();
		result.add(node.getFieldVariable());
		result.add(node.getFieldType());
		return result;
	}

	@Override
	public Object visit(JAssume n) {
		return null;
	}

	@Override
	public Object visit(JLoopInvariant n) {
		return null;
	}

	@Override
	public Object visit(JObjectInvariant node) {
		return null;
	}

	@Override
	public Object visit(JObjectConstraint node) {
		return null;
	}

	@Override
	public Object visit(JRepresents node) {
		return null;
	}

	@Override
	public Object visit(JPrecondition node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JPostcondition node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JHavoc n) {
		return null;
	}

	@Override
	public Object visit(JModifies node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JSpecCase node) {
		Vector<Object> requiresResults = new Vector<Object>();
		for (JPrecondition requires : node.getRequires()) {
			Object result = requires.accept(this);
//			QFtransformer qfPrefixer = new QFtransformer(this.varsToPrefix);
//			FormulaMutator fm = new FormulaMutator(qfPrefixer);
//			Object result =
			requiresResults.add(result);
		}

		Vector<Object> ensuresResults = new Vector<Object>();
		for (JPostcondition ensures : node.getEnsures()) {
			Object result = ensures.accept(this);
			ensures.accept(this);
			ensuresResults.add(result);
		}

		Vector<Object> modifiesResults = new Vector<Object>();
		for (JModifies modifies : node.getModifies()) {
			Object result = modifies.accept(this);
			modifiesResults.add(result);
		}

		Vector<Object> result = new Vector<Object>();
		result.add(requiresResults);
		result.add(ensuresResults);
		result.add(modifiesResults);

		return result;
	}

	@Override
	public Object visit(JClassInvariant n) {
		return null;
	}

	@Override
	public Object visit(JClassConstraint n) {
		return null;
	}

}
