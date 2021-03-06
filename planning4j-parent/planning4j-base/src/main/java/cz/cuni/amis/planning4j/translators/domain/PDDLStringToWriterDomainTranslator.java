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

package cz.cuni.amis.planning4j.translators.domain;

import cz.cuni.amis.planning4j.IPDDLStringDomainProvider;
import cz.cuni.amis.planning4j.IPDDLWriterDomainProvider;
import cz.cuni.amis.planning4j.impl.AbstractDomainTranslator;
import java.io.IOException;
import java.io.Writer;

/**
 * A simple translator that writes a string in PDDL to a writer.
 * @author Martin Cerny
 */
public class PDDLStringToWriterDomainTranslator extends AbstractDomainTranslator<IPDDLStringDomainProvider, IPDDLWriterDomainProvider> {

    public PDDLStringToWriterDomainTranslator() {
        super(IPDDLStringDomainProvider.class, IPDDLWriterDomainProvider.class);
    }

    @Override
    public IPDDLWriterDomainProvider translateDomain(IPDDLStringDomainProvider domain) {
        return new TranslatedDomainProvider(domain);
    }
    
    

    private class TranslatedDomainProvider implements IPDDLWriterDomainProvider {
        private IPDDLStringDomainProvider domainProvider;

        public TranslatedDomainProvider(IPDDLStringDomainProvider domainProvider) {
            this.domainProvider = domainProvider;
        }

        
        
        @Override
        public void writeDomain(Writer writer) throws IOException {
            writer.write(domainProvider.getDomainAsPDDLString());            
        }
        
        
    }
}
