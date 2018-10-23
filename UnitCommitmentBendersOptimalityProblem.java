/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitCommitment;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;


/**
 * This class creates the template for objects representing
 * mathematical models for the Optimality Subproblem of the Bender's
 * Decomposition of the Unit Commitment Problem.
 * @author Luttner
 */
public class UnitCommitmentBendersOptimalityProblem {

    private final IloCplex model;
    // We define the variables of the second-stage problem, p and l.

    private final IloNumVar p[][];
    private final IloNumVar l[];

    private final UnitCommitmentProblem problem;

    // The sets of constraints 1e - 1i which will be used in the optimality
    // subproblem:
    private final IloRange[] constraints1e;
    private final IloRange[][] constraints1f;
    private final IloRange[][] constraints1g;
    private final IloRange[][] constraints1h;
    private final IloRange[][] constraints1i;

    /**
     * Creates the Optimalility Problem.
     * @param problem
     * @param U a solution to the first-stage problem
     * @throws IloException 
     */
  
    public UnitCommitmentBendersOptimalityProblem(UnitCommitmentProblem problem, int[][] U) throws IloException {
    
        this.problem=problem;
        this.model = new IloCplex();
        this.p = new IloNumVar[problem.getNGenerators()][problem.getNPeriods()];
        this.l = new IloNumVar[problem.getNPeriods()];

         // Now we need to populate the arrays with objects of type IloNumVar.
        for(int j = 0; j < problem.getNPeriods(); j++){
            l[j] = model.numVar(0, Double.POSITIVE_INFINITY,"l_"+j);
        }
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                p[i][j] = model.numVar(0, Double.POSITIVE_INFINITY, "p_"+i+"_"+j);
            }
        }
        
        // Create and add the objective function to minimize v+ and v-.
        // First we create an empty linear expression.
        IloLinearNumExpr objective = model.linearNumExpr();
        
        // Then we add the terms of the expression.
        for(int j = 0; j < problem.getNPeriods(); j++){
            objective.addTerm(l[j], problem.getLoadSheddingCosts()[j]);
        }
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                objective.addTerm(p[i][j], problem.getMarginalCosts()[i]);
            }
        }

        // Finally we tell the model to minimize that 
        // linear expression.
        model.addMinimize(objective);
        // Now for the constraints

        constraints1e = new IloRange[problem.getNPeriods()];
        constraints1f = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constraints1g = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constraints1h = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constraints1i = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        
        
        // Constraints 1e: Power balance constraints
        
        for(int j = 0; j < problem.getNPeriods(); j++){
            IloLinearNumExpr lhs = model.linearNumExpr();
            // We add the terms to the linear expression
            for(int i=0; i < problem.getNGenerators(); i++){
                lhs.addTerm(p[i][j], 1);
            }
            lhs.addTerm(l[j], 1);
            // Finally we add the constraint to the model 
            constraints1e[j] = model.addEq(lhs, problem.getPowerDemands()[j],"PowerBalance_"+j);
        }
        
        // Constraints 1f: Minimum output constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                constraints1f[i][j] = model.addGe(lhs, U[i][j]*problem.getMinimumOutput()[i],"MinimumOutput"+i+"_"+j);
            }
        }
        
        // Constraints 1g: Maximum output constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                constraints1g[i][j] = model.addLe(lhs, U[i][j]*problem.getMaximumOutput()[i],"MaximumOutput_"+i+"_"+j);
            }
        }

        // Constraints 1h: Maximum ramp-up constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                if (j>0){
                    lhs.addTerm(p[i][j-1], -1);
                }
                constraints1h[i][j] = model.addLe(lhs, problem.getRampUpLimit()[i],"MaximumRampUp_"+i+"_"+j);
            }
        }

        // Constraints 1i: Maximum ramp-down constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], -1);
                if (j>0){
                    lhs.addTerm(p[i][j-1], 1);
                }
                constraints1i[i][j] = model.addLe(lhs, problem.getRampDownLimit()[i],"StartupCost_"+i+"_"+j);
            }
        }
    }

    /**
     * Solves the problem.
     * @throws IloException 
     */
    public void solve() throws IloException{
        model.setOut(null);
        model.solve();
    }
    
    /**
     * Returns the objective value
     * @return the objective value
     * @throws IloException 
     */
    public double getObjective() throws IloException{
        return model.getObjValue();
    }
    
    /**
     * Returns the current P (needed in the end of the algorithm
     * for reporting the final solution).
     * @return P[][]
     * @throws IloException 
     */
    public double[][] getP() throws IloException {
        double P[][] = new double[problem.getNGenerators()][problem.getNPeriods()];
        for (int i = 0; i<problem.getNGenerators(); i++){
            for (int j = 0; j<problem.getNPeriods(); j++){
                P[i][j] = model.getValue(p[i][j]);
            }
        }
        return P;
    }
    /**
     * Returns the current L (needed in the end of the algorithm
     * for reporting the final solution).
     * @return L[]
     * @throws IloException 
     */
    public double[] getL() throws IloException {
        double L[] = new double[problem.getNPeriods()];
        for (int j = 0; j<problem.getNPeriods(); j++){
            L[j] = model.getValue(l[j]);
        }
        return L;
    }
    /**
     * Returns the constant part of the optimality cut.
     * That is, the part of the cut not dependent on u.
     * This is given by the constraints 1e, 1h, 1i
     * (demand, ramp-up and ramp down). 
     * @return the constant of the cut
     * @throws IloException 
     */
    public double getCutConstant() throws IloException{
    	double constant = 0;
    	for (int j = 0; j<problem.getNPeriods(); j++) {
    		constant = constant + problem.getPowerDemands()[j]*model.getDual(constraints1e[j]);
        }
        for (int i = 0; i<problem.getNGenerators(); i++){
            for (int j = 0; j<problem.getNPeriods(); j++){
    			constant = constant + problem.getRampUpLimit()[i]*model.getDual(constraints1h[i][j]);
    			constant = constant + problem.getRampDownLimit()[i]*model.getDual(constraints1i[i][j]);
            }
        }
        return constant;
    }
    /**
     * Returns the linear expression in u of the optimality cut.
     * The liner term is obtained from constraints 1f and 1g
     * (minimum and maximum capacity of generators)
     * since they involve u on the RHS. 
     * @param u the u variables of the master problem
     * @return the linear term of the cut
     * @throws IloException 
     */
    public IloLinearNumExpr getCutLinearTerm(IloNumVar u[][]) throws IloException{
        IloLinearNumExpr cutTerm = model.linearNumExpr();
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                cutTerm.addTerm(model.getDual(constraints1f[i][j])*problem.getMinimumOutput()[i], u[i][j]); 
                cutTerm.addTerm(model.getDual(constraints1g[i][j])*problem.getMaximumOutput()[i], u[i][j]); 
            }
        }
        return cutTerm;
    }
    
    /**
     * Releases all the objects retained by the IloCplex object.
     * In this particular application it makes no difference.
     * However, in bigger and resource-intensive applications
     * it is advised (if not necessary) to release the resources
     * used by the IloCplex object in order for the program to work well.
     * Note that once the method end() has been called, the IloCplex object
     * cannot be used (e.g., queried) anymore.
     */
    public void end(){
        model.end();
    }
    
}
