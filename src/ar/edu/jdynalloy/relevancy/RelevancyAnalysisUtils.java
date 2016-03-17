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
package ar.edu.jdynalloy.relevancy;

import java.util.HashSet;
import java.util.List;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.binding.callbinding.PredicateCallAlloyFormulaDescriptor;
import ar.edu.jdynalloy.binding.callbinding.FunctionCallAlloyFormulaDescriptor;
import ar.edu.jdynalloy.binding.callbinding.PredicateAndFunctionCallRelevancyVisitor;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;

public class RelevancyAnalysisUtils {

	
	private static int bitWidth;

	
	private RelevancyAnalysisUtils() {

	}

	public static void setBitWidth(int bw){
		bitWidth = bw;
	}
	
	/**
	 * 
	 * @param moduleId
	 * @return "null" if moduleId is a primitive type or module called
	 *         "moduleId"
	 */
	static JDynAlloyModule findModuleByName(String moduleId,
			List<JDynAlloyModule> modules) {
		if (moduleId.equals("boolean") || moduleId.equals("Int")
				|| moduleId.equals("univ")) {
			return null;
		}
		for (JDynAlloyModule module : modules) {
			if (module.getModuleId().equals(moduleId)) {
				return module;
			} else if (module.getLiteralSingleton() != null
					&& module.getLiteralSingleton().getSignatureId().equals(
							moduleId)) {
				return module;
			}
		}
		throw new JDynAlloyException("Module '" + moduleId + "' not found.");
	}

	static void findModuleAndToScene(Scene scene, JType type,
			List<JDynAlloyModule> modules) {
		String moduleName = type.dpdTypeNameExtract();
		JDynAlloyModule module = RelevancyAnalysisUtils.findModuleByName(
				moduleName, modules);
		// if module isn't primitive type (ie: Int)
		if (module != null) {
			scene.addModule(module);
		}
	}

	public static JProgramDeclaration findMethodToCheckDeclaration(
			List<JDynAlloyModule> modules) {
		JProgramDeclaration methodToCheckDeclaration = null;

		String classToCheck = JDynAlloyConfig.getInstance().getClassToCheck();
		
		/* Keyword "Instrumented" as part of class/method names seems to be obsolete.
		String[] splitClassToCheck = classToCheck.split("_");
		classToCheck = "";
		for (int idx = 0; idx < splitClassToCheck.length - 2; idx++){
			classToCheck += splitClassToCheck[idx] + "_";
		}
		if (splitClassToCheck.length > 1){
			classToCheck += splitClassToCheck[splitClassToCheck.length - 2] + "Instrumented_";
		}
		classToCheck += splitClassToCheck[splitClassToCheck.length - 1];
		*/
		
		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		
		/* Keyword "Instrumented" as part of class/method names seems to be obsolete.
		String[] splitMethodToCheck = methodToCheck.split("_");
		methodToCheck = "";
		for (int idx = 0; idx < splitMethodToCheck.length - 4; idx++){
			methodToCheck += splitMethodToCheck[idx] + "_";
		}
		if (splitMethodToCheck.length >= 4){
			methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 4] + "Instrumented_";
		}
		methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 3] + "_";
		methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 2] + "_";
		methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 1];
		*/
		

		for (JDynAlloyModule dynJAlloyModule : modules) {
			if (dynJAlloyModule.getModuleId().equals(classToCheck)) {

				for (JProgramDeclaration programDeclaration : dynJAlloyModule
						.getPrograms()) {
					String qualifiedMethodName = programDeclaration
							.getSignatureId()
							+ "_" + programDeclaration.getProgramId() + "_";
					if (methodToCheck.startsWith(qualifiedMethodName)) {
						methodToCheckDeclaration = programDeclaration;
					}
				}
			}
		}

		if (methodToCheckDeclaration == null) {
			String moreInfo = "classToCheck: "
					+ JDynAlloyConfig.getInstance().getClassToCheck()
					+ ". methodToCheck: "
					+ JDynAlloyConfig.getInstance().getMethodToCheck();
			throw new JDynAlloyException(
					"The method to check was not found. Please source files and check the configurations keys 'classToCheck' and 'methodToCheck'. "
							+ moreInfo);
		}
		return methodToCheckDeclaration;
	}

	public static void analyzeFormula(Scene scene, AlloyFormula formula,
			RelevancyAnalysisSymbolTable symbolTable,
			JDynAlloyBinding dynJAlloyBinding, List<JDynAlloyModule> modules) {

		PredicateAndFunctionCallRelevancyVisitor visitor = new PredicateAndFunctionCallRelevancyVisitor(
				symbolTable);

		RelevantAnalysisExpressionTypeResolver expressionTypeResolver = new RelevantAnalysisExpressionTypeResolver(
				visitor, symbolTable);
		visitor.setExpressionVisitor(expressionTypeResolver);

		symbolTable.setEnableRelevantAnalysis(true);
		symbolTable.setRelevantTypes(new HashSet<JType>());

		formula.accept(visitor);

//		visitor.getCalledFunctions().addAll(((RelevantAnalysisExpressionTypeResolver)visitor.getDfsExprVisitor()).getCalledFunctionsNames());
//		for (String funName : visitor.getCalledFunctionsNames()){
//			if ()
//			JProgramCall fakePC = new JProgramCall(false, funName, )
//			if (dynJAlloyBinding.resolve(node))
//				
//			JProgramDeclaration programDeclaration = this.dynJAlloyBinding.resolve(node);
//			scene.addProgram(programDeclaration);
//			//JPG::workaround to use AnalyzeFormula
//			PredicateFormula predFormula = new PredicateFormula(null,node.getProgramId(),node.getArguments());
//			RelevancyAnalysisUtils.analyzeFormula(scene, predFormula, symbolTable, dynJAlloyBinding, modules);
//
//		}
		processCollectedFunctionNames(scene, visitor, modules);
		
		
		symbolTable.setEnableRelevantAnalysis(false);
		// For each c in MethodCall in P (*)
		// 		scene.Add( c.Method )
		// End For each
		retriveBindingsTypeSupport(scene, visitor, dynJAlloyBinding);

		// For each f in FieldExpression in P (*)
		// 		Scene.Add( f.Prefix.Type )
		// 		scene.Add( f.Field )
		// End For each
		// For each v in Quantified Variable in P (*)
		// 		scene.Add( v.Type )
		// End For each

		for (JType type : symbolTable.getRelevantTypes()) {
			RelevancyAnalysisUtils.findModuleAndToScene(scene, type, modules);
		}

		// For each constant C in P (*)
		//   scene.Add (C.Type)
		// End For
		for (JType type : expressionTypeResolver.getRelevantTypes()) {
			String module_id = type.dpdTypeNameExtract();

			JDynAlloyModule module = RelevancyAnalysisUtils.findModuleByName(
					module_id, modules);

			if (module != null) {
				RelevancyAnalysisUtils.findModuleAndToScene(scene, type,
						modules);
			}
		}
		
		
		for (FunctionCallAlloyFormulaDescriptor fc : visitor.getCalledFunctions()) {
			RelevancyAnalysisUtils.findCalledProgramAndToScene(scene, 
					dynJAlloyBinding, fc);
		}
		
		
	}
	
	
	
	
	private static void findCalledProgramAndToScene(Scene scene, JDynAlloyBinding dynJAlloyBinding,
			FunctionCallAlloyFormulaDescriptor fc) {
		JProgramCall programCall = new JProgramCall(false, 
				fc.getfunctionCallInAlloyFormulaInfo().getFunctionId(), 
				fc.getfunctionCallInAlloyFormulaInfo().getParameters());
		JProgramDeclaration program = dynJAlloyBinding.resolve(programCall);
		if (program == null)
			throw new JDynAlloyException("Program " + fc.getfunctionCallInAlloyFormulaInfo().getFunctionId() + " called in specification cannot be found.");
		scene.addProgram(program);
	}

	public static void analyzeObjectInvariant(Scene scene, AlloyFormula objectInvariant,
			RelevancyAnalysisSymbolTable symbolTable,
			JDynAlloyBinding dynJAlloyBinding, List<JDynAlloyModule> modules) {

		PredicateAndFunctionCallRelevancyVisitor visitor = new PredicateAndFunctionCallRelevancyVisitor(symbolTable);

		RelevantAnalysisExpressionTypeResolver expressionTypeResolver = new RelevantAnalysisExpressionTypeResolver(
				visitor, symbolTable);
		visitor.setExpressionVisitor(expressionTypeResolver);

		symbolTable.setEnableRelevantAnalysis(true);
		symbolTable.setRelevantTypes(new HashSet<JType>());

		objectInvariant.accept(visitor);

//		visitor.getCalledFunctions().addAll(((RelevantAnalysisExpressionTypeResolver)visitor.getDfsExprVisitor()).getCalledFunctionsNames());
		processCollectedFunctionNames(scene, visitor, modules);

		symbolTable.setEnableRelevantAnalysis(false);
		// For each c in MethodCall in P (*)
		// 		scene.Add( c.Method )
		// End For each
		retriveBindingsTypeSupport(scene, visitor, dynJAlloyBinding);

		// For each f in FieldExpression in P (*)
		// 		Scene.Add( f.Prefix.Type )
		// 		scene.Add( f.Field )
		// End For each
		// For each v in Quantified Variable in P (*)
		// 		scene.Add( v.Type )
		// End For each

		for (JType type : symbolTable.getRelevantTypes()) {
			RelevancyAnalysisUtils.findModuleAndToScene(scene, type, modules);
		}

		// For each constant C in P (*)
		//   scene.Add (C.Type)
		// End For
		for (JType type : expressionTypeResolver.getRelevantTypes()) {
			String module_id = type.dpdTypeNameExtract();

			JDynAlloyModule module = RelevancyAnalysisUtils.findModuleByName(
					module_id, modules);

			if (module != null) {
				RelevancyAnalysisUtils.findModuleAndToScene(scene, type,
						modules);
			}
		}
		
		
	}
	

	private static void retriveBindingsTypeSupport(Scene scene,
			PredicateAndFunctionCallRelevancyVisitor visitor,
			JDynAlloyBinding dynJAlloyBinding) {
		for (PredicateCallAlloyFormulaDescriptor element : visitor
				.getPredicatesCollected()) {
			JProgramDeclaration programDeclaration = dynJAlloyBinding
					.resolve(element.getPredicateCallAlloyFormula());
			if (programDeclaration == null) {
				throw new JDynAlloyException("Binding not found for: "
						+ programDeclaration);
			}
			scene.addProgram(programDeclaration);
		}
	}
	
	
	private static void processCollectedFunctionNames(Scene scene, PredicateAndFunctionCallRelevancyVisitor visitor, List<JDynAlloyModule> modules){
		if (visitor.getCalledFunctions().contains("fun_java_primitive_integer_value_size_of")){
			for (JDynAlloyModule module : modules){
				if (module.getModuleId().contains("JavaPrimitiveIntegerLiteral")){
					String strNum = module.getModuleId().substring(module.getModuleId().lastIndexOf("l") + 1);
					int actualLiteral;
					if (strNum.startsWith("Minus")){
						strNum = module.getModuleId().substring(module.getModuleId().lastIndexOf("l") + 6);
						actualLiteral = -Integer.valueOf(strNum);
					} else {
						actualLiteral = Integer.valueOf(strNum);
					}
					if (0 <= actualLiteral && actualLiteral < Math.pow(2, bitWidth - 1)){
						scene.addModule(module);
					}
				}
			}
			
		}
	}
}
