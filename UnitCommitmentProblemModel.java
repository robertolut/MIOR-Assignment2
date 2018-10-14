package UnitCommitment;

// Necessary imports of classes 
// which are not in our package
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

/**
 * This class creates the template for objects representing
 * mathematical models for the Unit Commitment Problem.
 * An instance of this class represents the mathematical
 * model for a specific instance of the Diet Problem.
 * Remember: different instance - > different data.
 * @author Luttner
 */
public class UnitCommitmentProblemModel {
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
    // We use 4 different arrays of variables, and 8 sets of constraints,
    // as detailed in the constructor below.
    
    private final UnitCommitmentProblem problem;
    private final IloNumVar[][] c;
    private final IloNumVar[] l;
    private final IloNumVar[][] u;
    private final IloNumVar[][] p;
    private final IloRange[][] constr1b;
    private final IloRange[][] constr1c;
    private final IloRange[][] constr1d;
    private final IloRange[] constr1e;
    private final IloRange[][] constr1f;
    private final IloRange[][] constr1g;
    private final IloRange[][] constr1h;
    private final IloRange[][] constr1i;
    
    public UnitCommitmentProblemModel(UnitCommitmentProblem problem) throws IloException{
        // Creates the IloCplex object
        // and stores the problem in the
        // field variable. 
        model = new IloCplex();
        this.problem = problem;
        
        // Create the decision variables.

        // We will have four sets of decision variables:
        // one for the startup costs incurred (c),
        // one for the load shed (l),
        // one for the on/off status of the generators (u),
        // and one for the production levels (p),
        // thus we create three arrays of decision variables,
        // l being unidimensional on periods and the other two being
        // bidimensional on periods and generators.
        
        c = new IloNumVar[problem.getNGenerators()][problem.getNPeriods()];
        l = new IloNumVar[problem.getNPeriods()];
        u = new IloNumVar[problem.getNGenerators()][problem.getNPeriods()];
        p = new IloNumVar[problem.getNGenerators()][problem.getNPeriods()];
        
        // Now we need to populate the arrays with objects of type IloNumVar.
        for(int j = 0; j < problem.getNPeriods(); j++){
            l[j] = model.numVar(0, Double.POSITIVE_INFINITY,"l_"+j);
        }
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                c[i][j] = model.numVar(0, Double.POSITIVE_INFINITY, "c_"+i+"_"+j);
                u[i][j] = model.boolVar("u_"+i+"_"+j);
                p[i][j] = model.numVar(0, Double.POSITIVE_INFINITY, "p_"+i+"_"+j);
            }
        }
        
        // Create and add the objective function.
        // First we create an empty linear expression.
        IloLinearNumExpr objective = model.linearNumExpr();
        
        // Then we add the terms of the expression.
        for(int j = 0; j < problem.getNPeriods(); j++){
            objective.addTerm(l[j], problem.getLoadSheddingCost(j));
        }
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                objective.addTerm(c[i][j], 1);
                objective.addTerm(u[i][j], problem.getCommitmentCost(i));
                objective.addTerm(p[i][j], problem.getMarginalCost(i));
            }
        }
        // Finally we tell the model to minimize that 
        // linear expression.
        model.addMinimize(objective);

        // Now for the constraints, which are divided into sets according
        // to the problem formulation, named with 1b, 1c, ... 1i.
        // Constraints 1j ... 1m are domain constraints, and are enforced
        // in the setvariable defintion.
        
        constr1b = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constr1c = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constr1d = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constr1e = new IloRange[problem.getNPeriods()];
        constr1f = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constr1g = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constr1h = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        constr1i = new IloRange[problem.getNGenerators()][problem.getNPeriods()];
        
        // Now we build each of the constraint sets, adding the terms to the
        // linear expression and then add the constraint sets to the model.

        // constr1b: Start-up costs
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(c[i][j], 1);
                lhs.addTerm(u[i][j], -problem.getStartupCost(i));
                if (j>0){
                    lhs.addTerm(u[i][j-1], problem.getStartupCost(i));
                }
                // Finally we add the constraint to the model 
                constr1b[i][j] = model.addGe(lhs, 0,"StartupCost_"+i+"_"+j);
            }
        }

        // constr1c: Minimum ontime constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                for(int t=j; t<= problem.getMinimumOnTimeAtT(i,j); t++){
                    lhs.addTerm(u[i][t], 1);
                    lhs.addTerm(u[i][j], -1);
                    if (j>0){
                        lhs.addTerm(u[i][j-1], 1);
                    }
                }
                // Finally we add the constraint to the model 
                constr1c[i][j] = model.addGe(lhs, 0,"MinimumOntime_"+i+"_"+j);
            }
        }

        // constr1d: Minimum offtime constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                for(int t=j; t<= problem.getMinimumOffTimeAtT(i,j); t++){
                    lhs.addTerm(u[i][t], -1);
                    lhs.addTerm(u[i][j], 1);
                    if (j>0){
                        lhs.addTerm(u[i][j-1], -1);
                    }
                }
                // Finally we add the constraint to the model 
                constr1d[i][j] = model.addGe(lhs, j-problem.getMinimumOffTimeAtT(i,j),"MinimumOffTime_"+i+"_"+j);
            }
        }

        // constr1e: Power balance constraints
        
        for(int j = 0; j < problem.getNPeriods(); j++){
            IloLinearNumExpr lhs = model.linearNumExpr();
            // We add the terms to the linear expression
            for(int i=0; i < problem.getNGenerators(); i++){
                lhs.addTerm(p[i][j], 1);
            }
            lhs.addTerm(l[j], 1);
            // Finally we add the constraint to the model 
            constr1e[j] = model.addEq(lhs, problem.getPowerDemand(j),"PowerBalance_"+j);
        }
        
        // constr1f: Minimum output constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                lhs.addTerm(u[i][j], -problem.getMinimumOutput(i));
                constr1f[i][j] = model.addGe(lhs, 0,"MinimumOutput"+i+"_"+j);
            }
        }
        
        // constr1g: Maximum output constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                lhs.addTerm(u[i][j], -problem.getMaximumOutput(i));
                constr1g[i][j] = model.addLe(lhs, 0,"MaximumOutput_"+i+"_"+j);
            }
        }

        // constr1h: Maximum ramp-up constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                if (j>0){
                    lhs.addTerm(p[i][j-1], -1);
                }
                constr1h[i][j] = model.addLe(lhs, problem.getRampUpLimit(i),"MaximumRampUp_"+i+"_"+j);
            }
        }

        // constr1g: Maximum ramp-down constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], -1);
                if (j>0){
                    lhs.addTerm(p[i][j-1], 1);
                }
                constr1i[i][j] = model.addLe(lhs, problem.getRampDownLimit(i),"StartupCost_"+i+"_"+j);
            }
        }
    }
    public void solve() throws IloException{
        boolean has_feasible_solution = model.solve();
        if(has_feasible_solution){
            System.out.println("Optimal value "+model.getObjValue());
        }else{
            System.out.println("No feasible solution has been found");
        }
    }
    public void printSolution() throws IloException{
        System.out.println("Solution: ");
        for(int i = 0; i < problem.getNGenerators(); i++){
            System.out.println("Power outputs for generator"+problem.getGeneratorName(i));
            for (int j=0; j<problem.getNPeriods(); j++){
                System.out.print("T"+j+1+": "+model.getValue(p[i][j]));
                if (j % 5 == 0){
                    System.out.println();
                }
            }
        }
    }
    /**
     * Prints the model.
     */
    public void print(){
        // The method toString returns a human-readable 
        // summary of the model.
        System.out.println(model.toString());
    }
    /**
     * Prints the optimal dual variables associated with the constraints. 
     * @throws IloException 
     */
//    public void printDuals() throws IloException{
//        for(int j = 1; j <= problem.getNNutrients(); j++){
//            System.out.println("Dual const "+j+" = "+model.getDual(constr[j-1]));
//        }
//    }

}
