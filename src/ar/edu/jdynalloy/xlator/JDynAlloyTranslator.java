package ar.edu.jdynalloy.xlator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyPrinter;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;

public final class JDynAlloyTranslator {

	private Object inputToFix = null;
	
	private final JDynAlloyBinding binding;

	public static String headerComment(String fragmentId) {
		return "//-------------- " + fragmentId + "--------------//" + "\n";
	}

	public JDynAlloyTranslator(JDynAlloyBinding binding, Object inputToFix) {
		super();
		this.binding = binding;
		this.inputToFix = inputToFix;
	}

	public Vector<DynalloyModule> translateAll(JDynAlloyModule[] asts, boolean isJavaArith) {
		Vector<JDynAlloyModule> initial = new Vector<JDynAlloyModule>(Arrays.asList(asts));

		JDynAlloyContext context = createContext(initial);

		Vector<JDynAlloyModule> prepared = prepareAll(context, initial, isJavaArith);

		Vector<DynalloyModule> translated = translateAll(context, prepared, isJavaArith);

		return translated;
	}

	private JDynAlloyContext createContext(Vector<JDynAlloyModule> ms) {
		JDynAlloyContext context = JDynAlloyContext.buildNewContext();
		for (JDynAlloyModule m : ms)
			context.load(m);

		return context;
	}

	private Vector<JDynAlloyModule> prepareAll(JDynAlloyContext context, Vector<JDynAlloyModule> modules, boolean isJavaArith) {

		// create dynamic dispatch
		Vector<JDynAlloyModule> ms = new Vector<JDynAlloyModule>();
		VAlloyVisitor valloyVisitor = new VAlloyVisitor(binding, isJavaArith);
		for (JDynAlloyModule module : modules) {
			JDynAlloyModule resolved_module = (JDynAlloyModule) module.accept(valloyVisitor);
			ms.add(resolved_module);
		}
		
		// transform loop invariants into assert,havoc,assume
		Vector<JDynAlloyModule> transformedModules = new Vector<JDynAlloyModule>();
		JDynAlloyTransformationVisitor transformationVisitor = new JDynAlloyTransformationVisitor(isJavaArith);
		for (JDynAlloyModule module : ms) {
			JDynAlloyModule transformedModule = (JDynAlloyModule) module.accept(transformationVisitor);
			transformedModules.add(transformedModule);
		}
		ms = transformedModules;

		// create spec programs (if wanted)
		if (JDynAlloyConfig.getInstance().getModularReasoning() == true) {
			Vector<JDynAlloyModule> modular_modules = create_spec_programs(ms, isJavaArith);
			ms = modular_modules;

			// print spec programs (if wanted)
			if (JDynAlloyConfig.getInstance().getOutputModularJDynalloy() != null) {

				String filename = JDynAlloyConfig.getInstance().getOutputModularJDynalloy();

				writeDynJAlloyOutput(filename, ms.toArray(new JDynAlloyModule[] {}), isJavaArith);
			}

		}

		// create call graph
		CallGraphVisitor callGraphVisitor = new CallGraphVisitor(isJavaArith);
		for (JDynAlloyModule module : ms) {
			module.accept(callGraphVisitor);
		}
		Graph<String> callGraph = callGraphVisitor.getCallGraph();

		// prune unused methods
		String programToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		PruneVisitor pruneVisitor = new PruneVisitor(callGraph, programToCheck, isJavaArith);
		Vector<JDynAlloyModule> prunedModules = new Vector<JDynAlloyModule>();
		for (JDynAlloyModule module : ms) {
			JDynAlloyModule prunedModule = (JDynAlloyModule) module.accept(pruneVisitor);
			prunedModules.add(prunedModule);
		}

		// inline contracts (if needed)

		// Unfold recursion
		Vector<JDynAlloyModule> non_recursive_modules = new Vector<JDynAlloyModule>();
		int recursion_unfold = this.binding.unfoldScopeForRecursiveCode;
		RecursionUnfolderVisitor recursionUnfolderVisitor = new RecursionUnfolderVisitor(callGraph, recursion_unfold, isJavaArith);
		for (JDynAlloyModule module : prunedModules) {
			JDynAlloyModule non_recursive_module = (JDynAlloyModule) module.accept(recursionUnfolderVisitor);
			non_recursive_modules.add(non_recursive_module);
		}

		// create call graph
		CallGraphVisitor non_recursive_callGraphVisitor = new CallGraphVisitor(isJavaArith);
		for (JDynAlloyModule module : non_recursive_modules) {
			module.accept(non_recursive_callGraphVisitor);
		}
		Graph<String> non_recursive_callGraph = non_recursive_callGraphVisitor.getCallGraph();

		// fill modifies table
		ModifiesTableBuilder modifiesTableBuilder = new ModifiesTableBuilder();
		Map<String, Set<AlloyVariable>> modifiesTable = modifiesTableBuilder.buildTable(non_recursive_modules, non_recursive_callGraph, isJavaArith);

		context.setModifiesTable(modifiesTable);

		return non_recursive_modules;
	}

	private Vector<JDynAlloyModule> create_spec_programs(Vector<JDynAlloyModule> ms, boolean isJavaArith) {
		Vector<JDynAlloyModule> modular_modules;
		modular_modules = new Vector<JDynAlloyModule>();
		ModularMutator modularMutator = new ModularMutator(isJavaArith);
		for (JDynAlloyModule module : ms) {
			JDynAlloyModule spec_program_module = (JDynAlloyModule) module.accept(modularMutator);
			modular_modules.add(spec_program_module);
		}
		return modular_modules;
	}

	private Vector<DynalloyModule> translateAll(JDynAlloyContext context, Vector<JDynAlloyModule> modules, boolean isJavaArith) {

		Vector<DynalloyModule> ms = new Vector<DynalloyModule>();
		
		// start by translating relevant modules
		for (JDynAlloyModule m : modules) {
			String signatureId = m.getSignature().getSignatureId();

			// Collect vars per module. These vars come from arithmetic constraints and 
			// should be constrained outside the program. That requires to prefix them 
			// with a "QF.".
			HashSet<AlloyVariable> sav = new HashSet<AlloyVariable>();
			if (m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants() != null)
					sav.addAll(m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping());
			
			for (JProgramDeclaration jpd : m.getPrograms()){
				if (jpd.getVarsResultOfArithmeticOperationsInContracts() != null)
					sav.addAll(jpd.getVarsResultOfArithmeticOperationsInContracts().getVarsInTyping());
			}
			

			if (containsModule(context.getRelevantModules(), signatureId)) {
				JDynAlloyXlatorVisitor visitor = new JDynAlloyXlatorVisitor(context, sav, inputToFix, isJavaArith);
				DynalloyModule dynalloyModule = (DynalloyModule) m.accept(visitor);
				ms.add(dynalloyModule);
			}
		}

		// continue with the rest of the modules
		for (JDynAlloyModule m : modules) {
			String signatureId = m.getSignature().getSignatureId();
			HashSet<AlloyVariable> sav = new HashSet<AlloyVariable>();
			if (m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants() != null)
					sav.addAll(m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping());
			
			for (JProgramDeclaration jpd : m.getPrograms()){
				if (jpd.getVarsResultOfArithmeticOperationsInContracts() != null)
					sav.addAll(jpd.getVarsResultOfArithmeticOperationsInContracts().getVarsInTyping());
			}

			if (!containsModule(context.getRelevantModules(), signatureId)) {
				JDynAlloyXlatorVisitor visitor = new JDynAlloyXlatorVisitor(context, sav, inputToFix, isJavaArith);
				DynalloyModule dynalloyModule = (DynalloyModule) m.accept(visitor);
				ms.add(dynalloyModule);
			}
		}
		return ms;
	}

	private boolean containsModule(List<JDynAlloyModule> modules, String signatureId) {
		for (JDynAlloyModule module : modules) {
			if (module.getSignature().getSignatureId().equals(signatureId))
				return true;
		}
		return false;
	}

	public static void writeDynJAlloyOutput(String outputDynJAlloy, JDynAlloyModule[] modules, boolean isJavaArith) {
		StringBuffer sb = new StringBuffer();
		for (JDynAlloyModule m : modules) {
			String modHeader = headerComment(m.getSignature().getSignatureId());
			String modBody = (String) m.accept(new JDynAlloyPrinter(isJavaArith));
			sb.append(modHeader);
			sb.append(modBody);
		}
		try {
			FileWriter writer = new FileWriter(outputDynJAlloy);
			writer.write(sb.toString());
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
