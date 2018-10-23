/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitCommitment;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * This class creates the template for objects representing
 * mathematical models for the Master Problem of the Bender's Decomposition
 * of the Unit Commitment Problem.
 * @author Luttner
 */
public class UnitCommitmentBendersMasterProblem {

    // Every class representing an 
    // optimization problem must have 
    // an IloCplex object. This is the
    // container of the mathematical 
    // programming elements.
    // Since we will not modify 
    // the variables holding the 
    // model and the problem 
    // we can make them final.

    private final IloCplex model;

    // Stores the problem, the variables and the constraints
    // in order to access them in the methods of the class
    // UnitCommitmentProblemModel.
    // We use 2 different arrays of variables, plus the Phi,
    // as detailed in the constructor below.

    private final UnitCommitmentProblem problem;
    private final IloIntVar u[][];
    private final IloNumVar c[][];
    private final IloNumVar phi;

    // We also define arrays for p and l in order to access the final
    // solution (in the print methods).
    private double p[][];
    private double l[];

    /**
     * Creates the Master Problem.
     * @param problem
     * @throws IloException 
     */
    
    public UnitCommitmentBendersMasterProblem(UnitCommitmentProblem problem) throws IloException {

        this.problem = problem;
        
        // 1. Every model needs an IloCplex object
        this.model = new IloCplex();
        
        // 2. Creates the decision variables
        // We will have two sets of decision variables in our first stage:
        // one for the startup costs incurred (c),
        // one for the on/off status of the generators (u),
        // thus we create two arrays of decision variables,
        // bidimensional on periods and generators, and one single for "phi".
        this.c = new IloNumVar[problem.getNGenerators()][problem.getNPeriods()];
        this.u = new IloIntVar[problem.getNGenerators()][problem.getNPeriods()];
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                c[i][j] = model.numVar(0, Double.POSITIVE_INFINITY, "c_"+i+"_"+j);
                u[i][j] = model.boolVar("u_"+i+"_"+j);
            }
        }

        this.phi = model.numVar(0, Double.POSITIVE_INFINITY,"phi");
        
        // 3. Creates the objective function
        // Create and add the objective function.
        // First we create an empty linear expression.
        IloLinearNumExpr objective = model.linearNumExpr();
        
        // Then we add the terms of the expression.
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                objective.addTerm(c[i][j], 1);
                objective.addTerm(u[i][j], problem.getCommitmentCosts()[i]);
            }
        }

        objective.addTerm(phi,1);

        // Finally we tell the model to minimize that 
        // linear expression.

        model.addMinimize(objective);
               
        // Now we add the Master Problem constraints, i.e, constraints
        // 1b, 1c and 1d, which refer to the variables of the first stage
        // problem, u and c.
    
        
        // Constraints 1b: Start-up costs
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(c[i][j], 1);
                lhs.addTerm(u[i][j], -problem.getStartupCosts()[i]);
                if (j>0){
                    lhs.addTerm(u[i][j-1], problem.getStartupCosts()[i]);
                }
                // Finally we add the constraint to the model 
                model.addGe(lhs, 0,"StartupCost_"+i+"_"+j);
            }
        }

        // Constraints 1c: Minimum ontime constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                for(int t=j; t< minimumOnTimeAtT(i,j); t++){
                    lhs.addTerm(u[i][t], 1);
                    lhs.addTerm(u[i][j], -1);
                    if (j>0){
                        lhs.addTerm(u[i][j-1], 1);
                    }
                }
                // Finally we add the constraint to the model 
                model.addGe(lhs, 0,"MinimumOntime_"+i+"_"+j);
            }
        }

        // Constraints 1d: Minimum offtime constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                for(int t=j; t< minimumOffTimeAtT(i,j); t++){
                    lhs.addTerm(u[i][t], -1);
                    lhs.addTerm(u[i][j], 1);
                    if (j>0){
                        lhs.addTerm(u[i][j-1], -1);
                    }
                }
                // Finally we add the constraint to the model 
                model.addGe(lhs, j-minimumOffTimeAtT(i,j)-1,"MinimumOffTime_"+i+"_"+j);
            }
        }
    }
    
    /**
     * Returns the minimum on-time for a generator at a given period
     * as the function stated in the assignment (2.1). The "-1" on the first
     * term is not present because the periods go from 0 to n-1, not from
     * 1 to n as in the assignment description.
     * @return T_gt^U
     */
    private int minimumOnTimeAtT(int generator, int period){
        return Math.min(period+problem.getMinimumOnTime()[generator], problem.getNPeriods());
    }
    
    /**
     * Returns the minimum off-time for a generator at a given period
     * as the function stated in the assignment (2.1). The "-1" on the first
     * term is not present because the periods go from 0 to n-1, not from
     * 1 to n as in the assignment description.
     * @return T_gt^D
     */
    private int minimumOffTimeAtT(int generator, int period){
        return Math.min(period+problem.getMinimumOffTime()[generator], problem.getNPeriods());
    }

    /**
    * Returns the value of phi at the current B&B integer node.
    * Outside the Callback class, model.getValue() is used.
    * @return the value of phi.
    * @throws IloException 
    */
    public double getPhi() throws IloException{
        return model.getValue(phi);
    }

    /**
    * Returns the value of U at the current B&B integer node.
    * Outside the Callback class, model.getValue() is used.
    * @return the value of U.
    * @throws IloException 
    */
    public int[][] getU() throws IloException {
        int[][] U = new int[problem.getNGenerators()][problem.getNPeriods()];
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                U[i][j]= (int) model.getValue(u[i][j]);
            }
        }    
        return U;
    }
    /**
     * Solves the Master Problem.
     * @throws IloException 
     */
    public void solve() throws IloException{
        
        // In this way we inform Cplex that
        // we want to use the callback we define below
        model.use(new Callback());
        
        // Solves the problem
        model.solve();
    }


    
    /**
     * The class Callback extends the LazyConstraintCallback. 
     * This means that objects of the class Callback must have a 
     * main() method. When we tell the model to use this callback
     * (model.use(newCallback()); this method called by the model 
     * every time the branch and bound algorithm reaches an integer node.
     * Therefore, within the main method we code all the actions
     * that must be performed upon reaching integer nodes,
     * that is all the actions to verify optimality and 
     * possibly add feasibility or optimality cuts. 
     * Note that "Callback" is an arbitrary name.
     * See more information about the LazyConstraintCallback
     * here https://www.ibm.com/support/knowledgecenter/SSSA5P_12.6.3/ilog.odms.cplex.help/refjavacplex/html/ilog/cplex/IloCplex.LazyConstraintCallback.html 
     */
    private class Callback extends IloCplex.LazyConstraintCallback{

        public Callback() {
        }
        /**
         * This is the main method of the Callback class.
         * Whatever we code in this method will be run every time
         * the B&B method reaches an integer node. Here we 
         * code the routines necessary to verify whether we need
         * cuts and, in case, generate and add cuts. 
         * @throws IloException
         */
        
        @Override
        protected void main() throws IloException {
            // 1. We start by obtaining the solution at the current node
            int[][] U = getU();
            double Phi = getPhi();
            
            // 2. We check feasibility of the subproblem 
            // 2.1 We create and solve a feasibility subproblem 
            UnitCommitmentBendersFeasibilityProblem fsp = new UnitCommitmentBendersFeasibilityProblem(problem,U);
            fsp.solve();
            double fspObjective = fsp.getObjective();
            
            // 2.2 We check if the suproblem is feasible. 
            // Remember, if the objective is zero the subproblem is feasible
            System.out.println("FSP "+fspObjective);
            if(fspObjective >= 0+1e-9){
                // 2.3 If the objective is positive 
                // the subproblem is not feasible. Thus we 
                // need a feasibility cut.
                System.out.println("Generating feasibility cut");
                // 2.4 We obtain the constant and the linear term of the cut
                // from the feasibility subproblem
                double constant = fsp.getCutConstant();
                IloLinearNumExpr linearTerm = fsp.getCutLinearTerm(u);
                
                // 2.5 Thus we generate and add a cut to the current model.
                // Remember that the cut is constant + linearTerm <= 0.
                // Notice that we use the method add() from the LazyConstraintCallback
                // class. This method adds the cut "lazily" to the model being
                // solved. Instead, the method model.le() does not add a cut!
                // It only creates and returns an IloRange object (which models 
                // a constraint. Notice the difference between model.le()
                // and model.addLe() which we used when creating the model. 
                add(model.le(linearTerm, -constant));
            }else{
                // 3. Since the subproblem is feasible, we check optimality
                // and verify whether we should add an optimality cut.
                
                // 3.1. First, we create and solve an optimality suproblem
                UnitCommitmentBendersOptimalityProblem osp = new UnitCommitmentBendersOptimalityProblem(problem,U);
                osp.solve();
                double ospObjective = osp.getObjective();
                
                // 3.2. Then we check if the optimality test is satisfied.
                System.out.println("Phi "+Phi+ " OSP "+ospObjective );
                if(Phi >= ospObjective - 1e-9){
                    // 3.3. In this case the problem at the current node
                    // is optimal.
                    System.out.println("The current node is optimal");
                    p = new double[problem.getNGenerators()][problem.getNPeriods()];
                    p = osp.getP();
                    l = new double[problem.getNPeriods()];
                    l = osp.getL();
                }else{
                    // 3.4. In this case we need an optimality cut. 
                    System.out.println("Generating optimality cut");
                    // We get the constant and the linear term from
                    // the optimality suproblem 
                    double cutConstant = osp.getCutConstant();
                    IloLinearNumExpr cutTerm = osp.getCutLinearTerm(u);
                    cutTerm.addTerm(-1, phi);
                    // and generate and add a cut. 
                    add(model.le(cutTerm, -cutConstant));
                }
            }
        }
        /**
        * Returns the value of phi at the current B&B integer node.
        * Notice the method getValue() is used, not model.getValue().
        * @return the value of phi.
        * @throws IloException 
        */
        public double getPhi() throws IloException{
           return getValue(phi);
        }
        /**
        * Returns the value of U at the current B&B integer node.
        * Notice the method getValue() is used, not model.getValue().
        * @return the value of U.
        * @throws IloException 
        */
        public int[][] getU() throws IloException {
            int[][] U = new int[problem.getNGenerators()][problem.getNPeriods()];
            for(int i = 0; i < problem.getNGenerators(); i++){
                for(int j = 0; j < problem.getNPeriods(); j++){
                    U[i][j]= (int) getValue(u[i][j]);
                }
            }    
            return U;
        }

    }
    
    
    /**
     * Returns the objective value
     * @return
     * @throws IloException 
     */
    public double getObjective() throws IloException{
        return model.getObjValue();
    }

     /**
     * Prints the solution found, i.e. the outputs for each generator and
     * the load sheds at each time period.
     * @throws IloException
     */
    public void printSolution() throws IloException{
        System.out.println("Solution: ");
        System.out.println("Load Shedding:");
        for (int j=0; j<problem.getNPeriods(); j++){
            System.out.print("T"+j+": "+l[j]+"   ");
            if (j % 5 == 4){
                System.out.println();
            }
        }
        System.out.println();
        System.out.println();
        for(int i = 0; i < problem.getNGenerators(); i++){
            System.out.println("Power outputs for generator "+problem.getGeneratorNames()[i]);
            for (int j=0; j<problem.getNPeriods(); j++){
                System.out.print("T"+j+": "+p[i][j]+"   ");
                if (j % 5 == 4){
                    System.out.println();
                }
            }
            System.out.println();
            System.out.println();
        }
        System.out.println("Optimal value: "+model.getObjValue());
    }

    /**
     * Prints the solution found, i.e. the outputs for each generator and
     * the load sheds at each time period, in CSV, which is more useful for
     * copying to a spreadsheet and analyzing the data.
     * @throws IloException
     */
    public void printCSVSolution() throws IloException{
        System.out.print("Load Shed; ");
        for (int j=0; j<problem.getNPeriods(); j++){
            System.out.print(l[j]+"; ");
        }
        System.out.println();
        for(int i = 0; i < problem.getNGenerators(); i++){
            System.out.print(problem.getGeneratorNames()[i]+"; ");
            for (int j=0; j<problem.getNPeriods(); j++){
                System.out.print(p[i][j]+"; ");
            }
            System.out.println();
        }
        System.out.println("Optimal value: "+model.getObjValue());
    }
}