/*
 * Copyright (C) 2012 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.cuni.amis.planning4j.impl;

import cz.cuni.amis.planning4j.*;
import java.util.List;

/**
 *
 * @author Martin Cerny
 */
public class TranslatingValidator 
    <SOURCE_DOMAIN extends IDomainProvider, DESTINATION_DOMAIN extends IDomainProvider,
     SOURCE_PROBLEM extends IProblemProvider, DESTINATION_PROBLEM extends IProblemProvider>
        implements IValidator<SOURCE_DOMAIN, SOURCE_PROBLEM>
        {
    
    IValidator<DESTINATION_DOMAIN, DESTINATION_PROBLEM> originalValidator;
    IDomainTranslator<SOURCE_DOMAIN, DESTINATION_DOMAIN> domainTranslator;
    IProblemTranslator<SOURCE_PROBLEM, DESTINATION_PROBLEM> problemTranslator;

    @Override
    public Class<SOURCE_DOMAIN> getDomainClass() {
        return domainTranslator.getSourceDomainClass();
    }

    @Override
    public Class<SOURCE_PROBLEM> getProblemClass() {
        return problemTranslator.getSourceProblemClass();
    }

    
    
    public TranslatingValidator(IValidator<DESTINATION_DOMAIN, DESTINATION_PROBLEM> originalValidator, IDomainTranslator<SOURCE_DOMAIN, DESTINATION_DOMAIN> domainTranslator, IProblemTranslator<SOURCE_PROBLEM, DESTINATION_PROBLEM> problemTranslator) {
        this.originalValidator = originalValidator;
        this.domainTranslator = domainTranslator;
        this.problemTranslator = problemTranslator;
    }

        
    @Override
    public IValidationResult validate(SOURCE_DOMAIN domainProvider, SOURCE_PROBLEM problemProvider, List<ActionDescription> plan) {
        return originalValidator.validate(domainTranslator.translateDomain(domainProvider), problemTranslator.translateProblem(problemProvider), plan);        
    }
    
}
