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
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.MethodToCheckNotFoundException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyPrinter;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.binding.fieldcollector.FieldCollectorVisitor;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;

public class ModifiesSolverManager {
	private static Logger log = Logger.getLogger(ModifiesSolverManager.class);

	/**
	 * This method replace predicate calls with the modifies clausule in 
	 * 
	 */
	public List<JDynAlloyModule> process(List<JDynAlloyModule> modules, JDynAlloyBinding dynJAlloyBinding, boolean isJavaArith) {
		boolean checkedMethodFound = false;
		
		String classToCheck = JDynAlloyConfig.getInstance().getClassToCheck();
		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		
		log.debug("Resolving JDynAlloy modifies: ");
		List<JDynAlloyModule> modulesWithoutModifies = new ArrayList<JDynAlloyModule>();
		for (JDynAlloyModule dynJAlloyModule : modules) {
			if (dynJAlloyModule.getModuleId().equals(classToCheck)) {
				

				SymbolTable symbolTable = new SymbolTable();				
				FieldCollectorVisitor fieldCollectorVisitor = new FieldCollectorVisitor(symbolTable, isJavaArith);
				
				for (JDynAlloyModule aModule : modules) {
					aModule.accept(fieldCollectorVisitor);
				}
				
				for (JProgramDeclaration programDeclaration : dynJAlloyModule.getPrograms()) {
					String qualifiedMethodName = programDeclaration.getSignatureId() + "_" + programDeclaration.getProgramId() + "_"; 
					if (methodToCheck.startsWith(qualifiedMethodName)) {
						checkedMethodFound = true;
					}
				}
				
				log.debug("Module: " + dynJAlloyModule.getModuleId());
				
				ReplaceModifiesModuleVisitor replaceModifiesModuleVisitor = new ReplaceModifiesModuleVisitor(dynJAlloyBinding, symbolTable, isJavaArith);
				JDynAlloyModule dynJAlloyModuleWithOutModifies = (JDynAlloyModule) dynJAlloyModule.accept(replaceModifiesModuleVisitor);
	
				JDynAlloyPrinter printer = new JDynAlloyPrinter(isJavaArith);
				log.debug("New Module WITHOUT Modifies: ");
				log.debug(dynJAlloyModuleWithOutModifies.accept(printer));
				
				modulesWithoutModifies.add(dynJAlloyModuleWithOutModifies);
			} else {
				modulesWithoutModifies.add(dynJAlloyModule);
			}
		}
		
		if (!checkedMethodFound) {
			String moreInfo = "classToCheck: " + JDynAlloyConfig.getInstance().getClassToCheck() + ". methodToCheck: " + JDynAlloyConfig.getInstance().getMethodToCheck();  
			throw new MethodToCheckNotFoundException("The method to check was not found. Please check the configurations keys 'classToCheck' and 'methodToCheck'. " + moreInfo );
		}
		
		return modulesWithoutModifies;
	}
}
