/*** 
 * This file is part of Java API for AI planning
 * 
 * This file is based on code from project itSIMPLE.
 *
 * Java API for AI planning is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Other licenses might be available
 * upon written agreement.
 * 
 * Java API for AI planning is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Java API for AI planning.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors:	Tiago S. Vaquero, 
 *		 	Victor Romero, Martin Cerny.
 **/
package cz.cuni.amis.planning.external.impl.itsimple;

import cz.cuni.amis.planning.javaaiplanningapi.PlanningException;
import cz.cuni.amis.planning.javaaiplanningapi.ActionDescription;
import cz.cuni.amis.planning.external.impl.ExternalPlanningResult;
import cz.cuni.amis.planning.external.IExternalPlannerExecutor;
import cz.cuni.amis.planning.external.IExternalPlanningResult;
import cz.cuni.amis.planning.javaaiplanningapi.PlanningStatistics;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Element;

public class ItSimplePlannerExecutor implements IExternalPlannerExecutor {

    
    private static final Logger logger = Logger.getLogger(IExternalPlannerExecutor.LOGGER_NAME);

    private Element chosenPlanner;

    private File plannerBinariesDirectory;

    private File workingDirectory;

    public ItSimplePlannerExecutor(Element chosenPlanner) {
        this(chosenPlanner, new File("."));
    }

    public ItSimplePlannerExecutor(Element chosenPlanner, File plannerBinariesDirectory) {
        this(chosenPlanner, plannerBinariesDirectory, new File(System.getProperty("java.io.tmpdir")));
    }

    public ItSimplePlannerExecutor(Element chosenPlanner, File plannerBinariesDirectory, File workingDirectory) {
        this.chosenPlanner = chosenPlanner;
        this.plannerBinariesDirectory = plannerBinariesDirectory;
        this.workingDirectory = workingDirectory;
    }

    /**
     * Parses output and fills plan with information about actio and statistics with
     * statistical information
     * @param output
     * @param plan
     * @param statistics 
     */
    private void getPlanAndStatistics(List<String> output, List<String> plan, List<String> statistics) {
        //Separate statistics and plan (get plan)
        if (output != null) {
            for (String line : output) {
                if (!line.trim().equals("")) {
                    //get plan
                    if (line.trim().startsWith(";")) {
                        statistics.add(line.trim().substring(1).trim());
                    } else {
                        // if it is not a standard action then check if is still an action or a statistic
                        if (!(line.indexOf(":") > -1)) {
                            boolean isAnAction = false;

                            //check if the string can still be an action (e.g. 1 (action p1 p2 ... pn) )
                            if ((line.indexOf("(") > -1) && (line.indexOf(")") > -1)) {
                                //check if the first element on the string is the plan index
                                StringTokenizer st = new StringTokenizer(line.trim());
                                String firstItem = "index";
                                if (st.hasMoreTokens()) {
                                    firstItem = st.nextToken();
                                    try {
                                        double theIndex = Double.parseDouble(firstItem);
                                        isAnAction = true;
                                    } catch (Exception e) {
                                        isAnAction = false;
                                    }
                                }

                                //if it is an action the include the ":" for standarlization
                                if (isAnAction) {
                                    String actionBody = "";
                                    while (st.hasMoreTokens()) {
                                        // for each parameter, create a node
                                        actionBody += st.nextToken() + " ";
                                    }
                                    line = firstItem + ": " + actionBody;
                                }

                            }

                            if (isAnAction) {
                                plan.add(line.trim());
                            } else {
                                statistics.add(line.trim());
                            }


                        } else {//When it is really an action
                            plan.add(line.trim());
                        }

                    }
                }
            }
        }
    }

    public List<String> getPlan(List<String> Output) {
        //Separate statistics and plan (get plan)
        List<String> plan = new ArrayList<String>();
        if (Output != null) {
            for (Iterator<?> iter = Output.iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                //System.out.println(element);
                if (!element.trim().equals("")) {
                    //get plan
                    if (!element.trim().startsWith(";")) {
                        plan.add(element.trim());
                    }
                }
            }
        }
        return plan;
    }

    public List<String> getPlannerOutput(Element chosenPlanner, File domain, File problem, List<String> consoleOutput) {
        List<String> output = null;

        //Used to know the current OS
        //System.out.println(System.getProperty("os.name"));

        //String domain = "resources/planners/domain.pddl";
        //String problem = "resources/planners/problem.pddl";    	
        String solutionFile = "solution.soln";


        //1.Get main planner's parameters and arguments
        Element settings = chosenPlanner.getChild("settings");
        List<String> commandArguments = new ArrayList<String>();

        //1.0 Get planner execution file
        commandArguments.add(settings.getChildText("filePath"));

        String plannerFile = settings.getChildText("filePath");
        //System.out.println(plannerFile);
        File plannerRunFile = new File(plannerBinariesDirectory, plannerFile);
        boolean plannerFileExists = true;
        if (!plannerRunFile.exists()) {
            plannerFileExists = false;
            String toolMessage = ">> Could not find selected planner '" + plannerFile + "' in directory '" + plannerBinariesDirectory.getAbsolutePath() + "'";

            throw new PlanningException(toolMessage);
        }

        if (plannerFileExists) { //proceed only if planner file exists

            //1.1 Get domain arguments
            Element domainElement = settings.getChild("arguments").getChild("domain");
            if (!domainElement.getAttributeValue("parameter").trim().equals("")) {
                commandArguments.add(domainElement.getAttributeValue("parameter"));
            }
            commandArguments.add(domain.getAbsolutePath()); //domain path

            //1.2 Get problem arguments
            Element problemElement = settings.getChild("arguments").getChild("problem");
            if (!problemElement.getAttributeValue("parameter").trim().equals("")) {
                commandArguments.add(problemElement.getAttributeValue("parameter"));
            }
            commandArguments.add(problem.getAbsolutePath()); //problem path

            //1.3 Get additional arguments
            List<?> additionalArgs = null;
            try {
                XPath path = new JDOMXPath("arguments/argument[enable='true']");
                additionalArgs = path.selectNodes(settings);
            } catch (JaxenException e1) {
                e1.printStackTrace();
            }
            if (additionalArgs != null) {
                if (additionalArgs.size() > 0) {
                    for (Iterator<?> iter = additionalArgs.iterator(); iter.hasNext();) {
                        Element argument = (Element) iter.next();
                        //System.out.println(argument.getChildText("name"));
                        if (!argument.getAttributeValue("parameter").trim().equals("")) {
                            commandArguments.add(argument.getAttributeValue("parameter"));
                        }
                        //if there is a value for the argument then add to the command
                        if (!argument.getChildText("value").trim().equals("")) {
                            commandArguments.add(argument.getChildText("value").trim());
                        }
                    }
                }
            }

            //1.4 Get output arguments
            boolean OutputFile;
            Element outputElement = settings.getChild("output");
            if (outputElement.getAttributeValue("hasOutputFile").equals("true")) {
                OutputFile = true;
                solutionFile = outputElement.getChild("outputFile").getChildText("fileName").trim();
                if (outputElement.getChild("outputFile").getChild("argument").getAttributeValue("needArgument").equals("true")) {
                    commandArguments.add(outputElement.getChild("outputFile").getChild("argument").getAttributeValue("parameter"));
                    commandArguments.add(solutionFile); //problem path
                }
            } else {
                OutputFile = false;
            }

            //System.out.println(commandArguments);



            logger.info("\n>> Calling planner " + chosenPlanner.getChildText("name") + "\n ");
            //Call the planner
            Process process;
            try {
                ProcessBuilder builder = new ProcessBuilder(commandArguments);
                builder.directory(workingDirectory);
                process = builder.start();
            } catch (Exception e) {
                String message = "Error while running the planner " + chosenPlanner.getChildText("name") + ". ";
                throw new PlanningException(message, e);
            }



            Scanner sc = new Scanner(process.getInputStream());
            //Get the planner answer exposed in the console
            //String ongoingConsole = "<html><body><font size=4 face=courier>";
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Planner console output:");
            }
            if (consoleOutput != null) {
                while (sc.hasNextLine()) {
                    //consoleOutput.add(sc.nextLine());
                    String line = sc.nextLine();
                    consoleOutput.add(line);
                    //System.out.println(line);


                    //ongoingConsole += line + "<br>";
                    //ItSIMPLE.getInstance().setPlanInfoPanelText(ongoingConsole);
                    //ItSIMPLE.getInstance().setOutputPanelText(ongoingConsole);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(line);
                    }
                }
            }
            sc.close();
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(ItSimplePlannerExecutor.class.getName()).log(Level.INFO, "Waiting for planner execution interrupted", ex);
                destroyProcess(process, plannerRunFile);
                return null;
            }
            
            
            process.destroy();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Planner console output end.");
            }

            if(process.exitValue() != 0){
                String errorOuput;
                try {
                    errorOuput = IOUtils.toString(process.getErrorStream());
                } catch (IOException ex) {
                    errorOuput = "Could not get error stream";
                }
                StringBuilder consoleOutputTogether = new StringBuilder();
                for(String s : consoleOutput){
                    consoleOutputTogether.append(s).append("\n");
                }
                throw new PlanningException("Planner terminated with an error. Planner output:\n " +consoleOutputTogether.toString() +"\nError output:\n" + errorOuput);
            }




            if (OutputFile) {  //The planner does provide a output file

                //Checks if the planner put some automatic string in the output file name (i.e., .SOL)
                if (!outputElement.getChild("outputFile").getChildText("fileNameAutomaticIncrement").trim().equals("")) {
                    solutionFile = solutionFile + outputElement.getChild("outputFile").getChildText("fileNameAutomaticIncrement").trim();
                }

                //Get the planner answer exposed in the solution Output File
                File outputFile = new File(workingDirectory, solutionFile);

                if (outputFile.exists()) {
                    //Get output
                    try {
                        output = FileUtils.readLines(outputFile);
                    } catch (IOException ex) {
                        throw new PlanningException("Could not read planner output", ex);
                    }

                    //remove output solution file (only if the plan create it)
                    outputFile.delete();
                    //TODO check permission
                } else {
                    throw new PlanningException("Could not find the planner output solution file! \n");
                    //System.out.println(toolMessage);
                }

                // delete additional generated files
                List<?> generatedFiles = chosenPlanner.getChild("settings").getChild("output").getChild("outputFile").getChild("additionalGeneratedFiles").getChildren("fileName");
                for (Iterator<?> iter = generatedFiles.iterator(); iter.hasNext();) {
                    Element generatedFile = (Element) iter.next();
                    File file = new File(generatedFile.getText());
                    if (file.exists()) {
                        // delete the file
                        file.delete();
                    }
                }


            } else {  //The planner does not provide a output file, just the console message

                String planStartIdentifier = outputElement.getChild("consoleOutput").getChildText("planStartIdentifier");
                int startsAfterNlines = Integer.parseInt(outputElement.getChild("consoleOutput").getChild("planStartIdentifier").getAttributeValue("startsAfterNlines"));
                String planEndIdentifier = outputElement.getChild("consoleOutput").getChildText("planEndIdentifier");
                // testing

                ArrayList<String> planList = new ArrayList<String>();
                ArrayList<String> statistics = new ArrayList<String>();

                Boolean isThePlan = false;

                //System.out.println(planStartIdentifier + ", " + startsAfterNlines + ", " + planEndIdentifier);
                for (Iterator<?> iter = consoleOutput.iterator(); iter.hasNext();) {
                    String line = (String) iter.next();

                    //Check if line contains start identifier (only if the plan was not found yet)
                    int indexPlanStart = -1;
                    if (!isThePlan) {
                        indexPlanStart = line.indexOf(planStartIdentifier);
                    }

                    if (!isThePlan && indexPlanStart > -1) {//The plan was found
                        isThePlan = true;
                        //Jump the necessary lines to reach the first line of the plan
                        if (startsAfterNlines == 0) {//First action is in the same line as the idetifier.
                            line = line.substring(indexPlanStart + planStartIdentifier.length(), line.length());
                            //System.out.println("First line for nlines 0: " +line);
                        } else if (startsAfterNlines > 0) {//Jump to the first line of the plan
                            for (int i = 0; i < startsAfterNlines; i++) {
                                line = (String) iter.next();
                            }
                        }
                        //System.out.println("The plan stats here!");
                    } //The plan ended
                    else if (isThePlan && ((!planEndIdentifier.trim().equals("") && line.trim().indexOf(planEndIdentifier) > -1) || line.trim().equals(""))) {
                        isThePlan = false;
                        //System.out.println("The plan ends here!");
                    }

                    //capturing the plan
                    if (isThePlan) {

                        if (line.trim().startsWith(";")) {
                            statistics.add(line.trim());
                        } else {
                            //System.out.println("Got it: " + line.trim());
                            String mline = line;
                            if (line.indexOf("(") == -1) {//checking if it is in a pddl format about Parentheses
                                //if it is not in pddl format just add "(" after ":" and ")" at the end of the line
                                int indexOfDoubleDot = line.indexOf(":");
                                mline = line.substring(0, indexOfDoubleDot + 2) + "("
                                        + line.substring(indexOfDoubleDot + 2, line.length()) + ")";
                            }
                            if (line.indexOf("[") == -1) {//checking if it is in a pddl format about "[" - action duration
                                //assume duration equals to 1
                                mline = mline + " [1]";
                            }
                            line = mline;
                            planList.add(line.trim());
                        }

                    } else if (line.trim().startsWith(";")) {
                        statistics.add(line.trim());
                    }

                }


                if (statistics.size() > 0 || planList.size() > 0) {
                    output = new ArrayList<String>();
                    if (statistics.size() > 0) {
                        output.addAll(statistics);
                        output.add("");
                    }
                    if (planList.size() > 0) {
                        output.addAll(planList);
                    }
                }


            }








        }


        return output;
    }

    /**
     * This method parses the lines of a plan in text format to a XML structure
     * @param plan
     * @return the plan XML structure
     */
    private List<ActionDescription> parsePlanToActionDescription(List<String> plan) {
        List<ActionDescription> result = new ArrayList<ActionDescription>();


        for (String line : plan) {
            ActionDescription action = new ActionDescription();

            //System.out.println(line);

            String actionInstance = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
            StringTokenizer st = new StringTokenizer(actionInstance);

            // the first token is the action name
            String actionName = st.nextToken();

            action.setName(actionName.toUpperCase());

            // the other tokens are the parameters
            List<String> parameterValues = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                String parameterStr = st.nextToken();
                parameterValues.add(parameterStr.toUpperCase());
            }
            action.setParameters(parameterValues);

            // set the startTime name
            String startTimeStr = line.substring(0, line.indexOf(':'));

            action.setStartTime(Double.parseDouble(startTimeStr));

            // set the action duration
            String durationStr = "1";
            if (line.indexOf('[') > - 1) {
                durationStr = line.substring(line.indexOf('[') + 1, line.lastIndexOf(']'));
            }
            action.setDuration(Double.parseDouble(durationStr));
            action.setNotes("");

            result.add(action);


        }

        return result;
    }

    private PlanningStatistics parseStatistics(List<String> statistic) {

        PlanningStatistics planningStatistics = new PlanningStatistics();

        for (String line : statistic) {

            String keyword;
            String value;
            if (line.indexOf(' ') > -1) {
                // there is a value
                keyword = line.substring(0, line.indexOf(' ')).trim();
                value = line.substring(line.indexOf(' '), line.length()).trim();
            } else {
                keyword = line;
                value = "";
            }

            if (value.isEmpty()) {
                continue;
            }

            if (keyword.equals("Time")) {
                planningStatistics.setTime(Double.parseDouble(value));
            } else if (keyword.equals("ParsingTime")) {
                planningStatistics.setParsingTime(Double.parseDouble(value));
            } else if (keyword.equals("NrActions")) {
                planningStatistics.setNumberOfActions(Integer.parseInt(value));
            } else if (keyword.equals("MakeSpan")) {
                planningStatistics.setMakeSpan(Double.parseDouble(value));
            } else if (keyword.equals("MetricValue")) {
                planningStatistics.setMetricValue(Double.parseDouble(value));
            } else if (keyword.equals("PlanningTechnique")) {
                planningStatistics.setPlanningTechnique(value);
            } else {
                planningStatistics.addAdditionalStat(keyword + " " + value);
            }
        }
        return planningStatistics;

    }

    /**
     * 
     * @param chosenPlanner
     * @param domainFile
     * @param problemFile
     * @return an xml representation of the plan
     */
    public IExternalPlanningResult executePlanner(File domainFile, File problemFile) {
        //set inicial time
        long start_time = System.currentTimeMillis();
        // create the xml plan format
        //Element xmlPlan = null;
        //Element xmlPlan = thePlan;

        //check if the planner file exists
        Element settings = chosenPlanner.getChild("settings");
        String plannerFile = settings.getChildText("filePath");
        //System.out.println(plannerFile);
        File f = new File(plannerBinariesDirectory, plannerFile);
        if (!f.exists()) {
            String toolMessage = "Could not find selected planner '" + plannerFile + "' in directory " + plannerBinariesDirectory.getAbsolutePath();
            throw new PlanningException(toolMessage);
        }


        //1. get chosen planner output
        List<String> output = new ArrayList<String>();
        List<String> consoleOutput = new ArrayList<String>();

        output = getPlannerOutput(chosenPlanner, domainFile, problemFile, consoleOutput);


        //2. separates the plan and the statistics
        List<String> plan = new ArrayList<String>();
        List<String> statistic = new ArrayList<String>();
        getPlanAndStatistics(output, plan, statistic);




        //4.3 set the planner console output
        //4.3.1 build up the text from the string array
        StringBuilder consoleOutputBuilder = new StringBuilder();
        for (Iterator<?> iter = consoleOutput.iterator(); iter.hasNext();) {
            String line = (String) iter.next();
            consoleOutputBuilder.append(line).append("\n");
        }


        PlanningStatistics stats = parseStatistics(statistic);

        //6. set the plan
        //Element planNode = xmlPlan.getChild("plan");
        List<ActionDescription> actionDescriptions = parsePlanToActionDescription(plan);

        boolean success = !actionDescriptions.isEmpty();


        long time = System.currentTimeMillis() - start_time;

        return new ExternalPlanningResult(success, actionDescriptions, consoleOutputBuilder.toString(), stats, time);
    }

    /**
     * This method creates a HTML version of the information contained in the xmlPlan
     * @param xmlPlan
     * @return a html string containing a simple plan report (basic info). In fact,itSIMPLE class also has 
     * such function (is is duplicated, use itSIMPLE's one) .
     */
    private String generateHTMLReport(Element xmlPlan) {


        /*
        // get the date
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = new Date();
        String dateTime = dateFormat.format(date);
         */

        String dateTime = xmlPlan.getChildText("datetime");
        // head
        String info = "<TABLE width='100%' BORDER='0' align='center'>"
                + "<TR><TD bgcolor='333399'><font size=4 face=arial color='FFFFFF'>"
                + "<b>REPORT</b> - " + dateTime + "</font></TD></TR>";




        // planner
        Element planner = xmlPlan.getChild("planner");
        Element settingsPlanner = null;
        try {
            XPath path = new JDOMXPath("planners/planner[@id='" + planner.getAttributeValue("id") + "']");
            settingsPlanner = (Element) path.selectSingleNode(null); // TODO: doplnit settings ItSIMPLE.getItPlanners());
        } catch (JaxenException e) {
            e.printStackTrace();
        }

        if (settingsPlanner != null) {
            info += "<TR><TD bgcolor='gray'><font size=4 face=arial color='FFFFFF'><b>Planner</b></TD></TR>"
                    + "<TR><TD><font size=3 face=arial><b>Name: </b>" + settingsPlanner.getChildText("name")
                    + "</font></TD></TR>"
                    + "<TR><TD><font size=3 face=arial><b>Version: </b>" + settingsPlanner.getChildText("version")
                    + "</font></TD></TR>"
                    + "<TR><TD><font size=3 face=arial><b>Author(s): </b>" + settingsPlanner.getChildText("author")
                    + "</font></TD></TR>"
                    + "<TR><TD><font size=3 face=arial><b>Institution(s): </b>" + settingsPlanner.getChildText("institution")
                    + "</font></TD></TR>"
                    + "<TR><TD><font size=3 face=arial><b>Link: </b>" + settingsPlanner.getChildText("link")
                    + "</font></TD></TR>"
                    + "<TR><TD><font size=3 face=arial><b>Description: </b>" + settingsPlanner.getChildText("description")
                    + "</font><p></TD></TR>";
        }

        // statistics
        Element statistics = xmlPlan.getChild("statistics");
        info += "<TR><TD bgcolor='gray'><font size=4 face=arial color='FFFFFF'><b>Statistics</b>"
                + "</TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Tool total time: </b>" + statistics.getChildText("toolTime")
                + "</font></TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Planner time: </b>" + statistics.getChildText("time")
                + "</font></TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Parsing time: </b>" + statistics.getChildText("parsingTime")
                + "</font></TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Number of actions: </b>" + statistics.getChildText("nrActions")
                + "</font></TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Make Span: </b>" + statistics.getChildText("makeSpan")
                + "</font></TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Metric value: </b>" + statistics.getChildText("metricValue")
                + "</font></TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Planning technique: </b>" + statistics.getChildText("planningTechnique")
                + "</font></TD></TR>"
                + "<TR><TD><font size=3 face=arial><b>Additional: </b>" + statistics.getChildText("additional").replaceAll("\n", "<br>")
                + "</font><p></TD></TR>";


        // plan
        info += "<TR><TD bgcolor='gray'><font size=4 face=arial color='FFFFFF'><b>Plan</b></TD></TR>";


        List<?> actions = xmlPlan.getChild("plan").getChildren("action");
        if (actions.size() > 0) {
            for (Iterator<?> iter = actions.iterator(); iter.hasNext();) {
                Element action = (Element) iter.next();
                // build up the action string
                // start time
                String actionStr = action.getChildText("startTime") + ": ";

                // action name
                actionStr += "(" + action.getAttributeValue("id") + " ";

                // action parameters
                List<?> parameters = action.getChild("parameters").getChildren("parameter");
                for (Iterator<?> iterator = parameters.iterator(); iterator.hasNext();) {
                    Element parameter = (Element) iterator.next();
                    actionStr += parameter.getAttributeValue("id");
                    if (iterator.hasNext()) {
                        actionStr += " ";
                    }
                }
                actionStr += ")";

                // action duration
                String duration = action.getChildText("duration");
                if (!duration.equals("")) {
                    actionStr += " [" + duration + "]";
                }

                if (iter.hasNext()) {
                    info += "<TR><TD><font size=3 face=arial>" + actionStr + "</font></TD></TR>";
                } else {
                    info += "<TR><TD><font size=3 face=arial>" + actionStr + "</font><p></TD></TR>";
                }
            }
        } else {
            info += "<TR><TD><font size=3 face=arial>No plan found.</font><p></TD></TR>";
        }


        // planner console output
        info += "<TR><TD bgcolor='gray'><font size=3 face=arial color='FFFFFF'>"
                + "<b>Planner Console Output</b></TD></TR>"
                + "<TR><TD><font size=4 face=courier>"
                + planner.getChildText("consoleOutput").replaceAll("\n", "<br>") + "</font><p></TD></TR>";

        info += "</TABLE>";


        return info;
    }

    public void destroyProcess(Process process, File plannerRunFile) {
        if (process != null) {
            process.destroy();

            String operatingSystem = System.getProperty("os.name").toLowerCase();
            if (operatingSystem.indexOf("linux") == 0) {
                //kill process in linux with comand 'killall -9 <process_name>'
                //System.out.println("Kill" );

                if (plannerRunFile != null && plannerRunFile.exists()) {

                    //System.out.println(plannerRunFile.getName());
                    String filename = plannerRunFile.getName();
                    if (!filename.trim().equals("")) {
                        String[] command = new String[3];
                        command[0] = "killall";
                        command[1] = "-9";
                        command[2] = filename;

                        try {
                            Runtime.getRuntime().exec(command);
                        } catch (IOException ex) {
                            Logger.getLogger(ItSimplePlannerExecutor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }


                }
            }



        }
    }
}
