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

import cz.cuni.amis.planning4j.IAsyncPlanner;
import cz.cuni.amis.planning4j.IDomainProvider;
import cz.cuni.amis.planning4j.IPlanFuture;
import cz.cuni.amis.planning4j.IPlanningResult;
import cz.cuni.amis.planning4j.IProblemProvider;
import cz.cuni.amis.planning4j.PlanningException;

/**
 * Helper class for easily implementing asynchronuous planners - provides implementation
 * for the synchronuous planning method.
 * 
 * @author Martin Cerny
 */
public abstract class AbstractAsyncPlanner implements IAsyncPlanner{

    @Override
    public IPlanningResult plan(IDomainProvider domainProvider, IProblemProvider problemProvider) {
        IPlanFuture future = planAsync(domainProvider, problemProvider);
        try {
            return future.get();
        } catch(PlanningException ex){
            throw ex;
        }
        catch (Exception ex) {
            throw new PlanningException("Asynchronuous plan computation failed: " + ex.getMessage(), ex);
        } 
        
    }
    
}
