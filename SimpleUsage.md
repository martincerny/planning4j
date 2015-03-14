# Simple usage #

To run this examples you need to add Planning4J jars to your project. See [Installation](Installation.md) for a description of this.

# ItSimple external planners #

This is the most valuable planning connector so far. It executes an external binary planner that complies to [IPC](http://ipc.icaps-conference.org/) standards. You can specfiy your own external planners with ItSimple XML format or PlannerInfo class and pass it to [PlannerListManager](http://diana.ms.mff.cuni.cz:8080/view/Planning/job/Planning4J/javadoc/cz/cuni/amis/planning4j/external/impl/itsimple/PlannerListManager.html) class. Or you can use the provided planner pack. The latter case will be discussed further.

## Unpacking the planners ##

The planners are contained in the external-planners-pack artifact. You can either extract it's contents manually to your project working directory, or you can unpack them at runtime as in the following example. You can also extract the planners with Maven see [ExtractingPlannersWithMaven](ExtractingPlannersWithMaven.md). Note that under Linux systems, you need to set executable permissions as well (if extracting at runtime, this is done for you).

## Calling a planner ##

This example can be also found in the Planning4JSimpleExample class in the test packages of external-planners-pack artifact.

```java
import cz.cuni.amis.planning4j.*;
import cz.cuni.amis.planning4j.external.plannerspack.PlannersPackUtils;
import cz.cuni.amis.planning4j.external.ExternalPlanner;
import cz.cuni.amis.planning4j.external.impl.itsimple.ItSimplePlannerExecutor;
import cz.cuni.amis.planning4j.external.impl.itsimple.ItSimplePlannerInformation;
import cz.cuni.amis.planning4j.external.impl.itsimple.PlannerListManager;
import cz.cuni.amis.planning4j.impl.PDDLObjectDomainProvider;
import cz.cuni.amis.planning4j.impl.PDDLObjectProblemProvider;
import cz.cuni.amis.planning4j.pddl.PDDLDomain;
import cz.cuni.amis.planning4j.pddl.PDDLPredicate;
import cz.cuni.amis.planning4j.pddl.PDDLProblem;
import cz.cuni.amis.planning4j.pddl.PDDLRequirement;
import cz.cuni.amis.planning4j.pddl.PDDLSimpleAction;
import cz.cuni.amis.planning4j.utils.Planning4JUtils;
import java.io.File;
import java.util.EnumSet;
import java.util.List;

/**
 *
 * @author Martin Cerny
 */
public class Planning4JSimpleExample {
  public static void main(String args[]) {
   

        /**
         * Create a simple domain - two locations and an action to move between them
         */
        PDDLDomain domain = new PDDLDomain("test", EnumSet.of(PDDLRequirement.STRIPS));
        domain.addPredicate(new PDDLPredicate("at_loc1"));
        domain.addPredicate(new PDDLPredicate("at_loc2"));

        PDDLSimpleAction moveAction = new PDDLSimpleAction("move");
        moveAction.setPreconditionList("at_loc1");
        moveAction.setPositiveEffects("at_loc2");
        moveAction.setNegativeEffects("at_loc1");
        domain.addAction(moveAction);

        PDDLProblem problem = new PDDLProblem("problem_1", "test");
        problem.setInitialLiterals("at_loc1");
        problem.setGoalCondition("at_loc2");

        IPDDLObjectDomainProvider domainProvider = new PDDLObjectDomainProvider(domain);
        IPDDLObjectProblemProvider problemProvider = new PDDLObjectProblemProvider(problem);

        /**
         * Get a planner:
         */
        PlannerListManager plannerManager = PlannersPackUtils.getPlannerListManager();

        //Let the engine suggest as a planner that supports strips and runs on current platform
        List<ItSimplePlannerInformation> suggestedPlanners = plannerManager.suggestPlanners(PDDLRequirement.STRIPS);

        if (suggestedPlanners.isEmpty()) {
            System.out.println("No planner found for current platform.");
            return;
        }

        //let's use the first suggested planner
        ItSimplePlannerInformation plannerInfo = suggestedPlanners.get(0);

        //To use a planner by name either call getPlannerByName
        //   ItSimplePlannerInformation plannerInfo = plannerManager.getPlannerByName("Metric-FF");
        //Or use one of the PlannerPackUtils.getXXX predefined methods
        //   ItSimplePlannerInformation plannerInfo = PlannersPackUtils.getBlackBox();

       //This is the place to extract the planner
        File plannersDirectory = new File("target");
        //The planner is extracted (only if it does not exist yet) and exec permissions are set under Linux
        plannerManager.extractAndPreparePlanner(plannersDirectory, plannerInfo);
        
        try {
            IPlanner planner = new ExternalPlanner(new ItSimplePlannerExecutor(plannerInfo,plannersDirectory));            
            //Call to Planning4JUtils.plan gathers domain and problem translators (if needed) and performs the planning
            IPlanningResult result =  Planning4JUtils.plan(planner, domainProvider, problemProvider);
            if (!result.isSuccess()) {
                System.out.println("No solution found.");
                return;
            } else {
                System.out.println("Found solution. The plan is:");
                for (ActionDescription action : result.getPlan()) {
                    System.out.println(action.getName());
                }
            }
        } catch (PlanningException ex) {
            System.out.println("Exception during planning.");
            ex.printStackTrace();
        }



    }    
}


```


If you have your domain in a string, just create different domain and problem providers:

```
        IDomainProvider domainProvider = new PDDLStringDomainProvider(domainString);
        IProblemProvider problemProvider = new PDDLStringProblemProvider(problemString);

```

## Asynchronuous execution ##

See [AdvancedUsage](AdvancedUsage.md).