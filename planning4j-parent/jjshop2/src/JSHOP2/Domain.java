package JSHOP2;

import java.util.Comparator;
import java.util.Map;

/** Each domain at run time is represented as a class derived from this
 *  abstract class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public abstract class Domain
{
  /** The axioms in this domain. The array is indexed by first the
   *  predicate each axiom can prove, and second the order the axioms that
   *  prove the same predicate appear in the domain description.
  */
  protected Axiom[][] axioms;

  /** The <code>String</code> names of compound tasks that appear in the
   *  domain description. These <code>String</code>s are only used to print the
   *  task lists, since the compound tasks are mapped to integers at compile
   *  time. The same integers are used to index this array.
  */
  protected String[] compoundTasks;

  /** The <code>String</code> names of constant symbols that appear in the
   *  domain description. These <code>String</code>s are only used to print the
   *  constant symbols, since the constant symbols are mapped to integers at
   *  compile time. The same integers are used to index this array.
  */
  protected String[] constants;

  /** The methods in this domain. The array is indexed by first the compound
   *  task each method can decompose, and second the order the methods that
   *  decompose the same compound task appear in the domain description.
  */
  protected Method[][] methods;

  /** The operators in this domain. The array is indexed by first the primitive
   *  task each operator can achieve, and second the order the operators that
   *  achieve the same primitive task appear in the domain description.
  */
  protected Operator[][] ops;

  /** The <code>String</code> names of primitive tasks that appear in the
   *  domain description. These <code>String</code>s are only used to print the
   *  task lists, since the primitive tasks are mapped to integers at compile
   *  time. The same integers are used to index this array.
  */
  protected String[] primitiveTasks;

  /** The <code>String</code> names of constant symbols that appear in the
   *  problem description. These <code>String</code>s are only used to print
   *  the constant symbols, since the constant symbols are mapped to integers
   *  at compile time. The same integers are used to index this array.
  */
  protected String[] problemConstants;

  /** This function returns the axioms in this domain.
   *
   *  @return
   *          the axioms in this domain.
  */
  public Axiom[][] getAxioms()
  {
    return axioms;
  }

  /** This function returns the <code>String</code> representation of a given
   *  constant symbol that appears in the domain description, the problem
   *  description, or both.
   *
   *  @param idx
   *          the integer equivalent of the constant symbol.
   *  @return
   *          the <code>String</code> representation of the constant symbol.
  */
  public String getConstant(int idx)
  {
    //-- This is when the constant symbol appeared in the domain description.
    if (idx < constants.length)
      return constants[idx];

    //-- This is when the constant symbol did not appear in the domain
    //-- description. The reason it exists is because it appeared in the
    //-- problem description.
    return problemConstants[idx - constants.length];
  }

  public int getDomainConstantCount(){
      return constants.length;
  }
  
  public String getDomainConstant(int idx){
      return constants[idx];
  }
  
  /** This function returns an array of the <code>String</code> representations
   *  of all the primitive tasks in the domain description. This list is used
   *  to print the plans after they are found.
   *
   *  @return
   *          the array of <code>String</code> representations of the primitive
   *          tasks in the domain.
  */
  public String[] getPrimitiveTasks()
  {
    return primitiveTasks;
  }

    public String[] getCompoundTasks() {
        return compoundTasks;
    }

  
  
  /** This function sets the array of <code>String</code> representations of
   *  constant symbols that do not appear in the domain description but in the
   *  planning problem that is being solved.
   *
   *  @param inp
   *          the array of <code>String</code> representations of constant
   *          symbols.
  */
  public void setProblemConstants(String[] inp)
  {
    problemConstants = inp;
  }
  
  protected Calculate getFunctionImplementation(String functionName, Map<String, Calculate> userFunctionImplementations){
      if(userFunctionImplementations.containsKey(functionName)){
          return userFunctionImplementations.get(functionName);
      } else {
          try {
              //The old implementation just instantiated a class of given name, so we need to do so as well.
              Class calculateClass = Class.forName(functionName);
              Calculate implementation = (Calculate)calculateClass.newInstance();
              return implementation;
          } catch (Exception ex) {
              throw new JSHOPException("No implementation supplied for function " + functionName + " and class of this name cannot be instantiated.", ex);
          }
      }
  }
  
  protected Comparator createComparatorImplementation(String comparatorName, int varIndex, Map<String, Comparator<Term>> userComparatorImplementations){
      if(userComparatorImplementations.containsKey(comparatorName)){
          return new TermIndexComparator(userComparatorImplementations.get(comparatorName), varIndex);
      } else {
          Class comparatorClass;
          try {
              //The old implementation just instantiated a class of given name, so we need to do so as well.
              comparatorClass = Class.forName(comparatorName);
              return (Comparator) comparatorClass.getConstructor(Integer.class).newInstance(varIndex);
          } catch (Exception ex) {
                throw new JSHOPException("Could not instantiate class for compartor "+ comparatorName + ". Make sure the class has a single int-arg constructor.", ex);
          }
      }            
  }
}
