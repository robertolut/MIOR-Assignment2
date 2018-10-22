package UnitCommitment;

// Necessary imports of classes 
// which are not in our package
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * This class creates the template for objects representing
 * mathematical models for the Unit Commitment Problem.
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
    private final IloIntVar[][] u;
    private final IloNumVar[][] p;
    
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
        u = new IloIntVar[problem.getNGenerators()][problem.getNPeriods()];
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
            objective.addTerm(l[j], problem.getLoadSheddingCosts()[j]);
        }
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                objective.addTerm(c[i][j], 1);
                objective.addTerm(u[i][j], problem.getCommitmentCosts()[i]);
                objective.addTerm(p[i][j], problem.getMarginalCosts()[i]);
            }
        }
        // Finally we tell the model to minimize that 
        // linear expression.
        model.addMinimize(objective);

        // Now for the constraints, which are divided into sets according
        // to the problem formulation, named with 1b, 1c, ... 1i.
        // Constraints 1j ... 1m are domain constraints, and are enforced
        // in the setvariable defintion.
        
    
        // We build each of the constraint sets, adding the terms to the
        // linear expression and then add the constraint sets to the model.

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

        // Constraints 1e: Power balance constraints
        
        for(int j = 0; j < problem.getNPeriods(); j++){
            IloLinearNumExpr lhs = model.linearNumExpr();
            // We add the terms to the linear expression
            for(int i=0; i < problem.getNGenerators(); i++){
                lhs.addTerm(p[i][j], 1);
            }
            lhs.addTerm(l[j], 1);
            // Finally we add the constraint to the model 
            model.addEq(lhs, problem.getPowerDemands()[j],"PowerBalance_"+j);
        }
        
        // Constraints 1f: Minimum output constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                lhs.addTerm(u[i][j], -problem.getMinimumOutput()[i]);
                model.addGe(lhs, 0,"MinimumOutput"+i+"_"+j);
            }
        }
        
        // Constraints 1g: Maximum output constraints
        
        for(int i = 0; i < problem.getNGenerators(); i++){
            for(int j = 0; j < problem.getNPeriods(); j++){
                IloLinearNumExpr lhs = model.linearNumExpr();
                // We add the terms to the linear expression
                lhs.addTerm(p[i][j], 1);
                lhs.addTerm(u[i][j], -problem.getMaximumOutput()[i]);
                model.addLe(lhs, 0,"MaximumOutput_"+i+"_"+j);
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
                model.addLe(lhs, problem.getRampUpLimit()[i],"MaximumRampUp_"+i+"_"+j);
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
                model.addLe(lhs, problem.getRampDownLimit()[i],"StartupCost_"+i+"_"+j);
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
    private int minimumOnTimeAtT(int generator, int period){
        return Math.min(period+problem.getMinimumOnTime()[generator]-1, problem.getNPeriods());
    }
    private int minimumOffTimeAtT(int generator, int period){
        return Math.min(period+problem.getMinimumOffTime()[generator]-1, problem.getNPeriods());
    }
    public void printSolution() throws IloException{
        System.out.println("Solution: ");
        for(int i = 0; i < problem.getNGenerators(); i++){
            System.out.println("Power outputs for generator "+problem.getGeneratorNames()[i]);
            for (int j=0; j<problem.getNPeriods(); j++){
                System.out.print("T"+j+": "+model.getValue(p[i][j])+"   ");
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
     * Prints the model.
     */
    public void print(){
        System.out.println(model.toString());
    }
    
}