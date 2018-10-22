/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitCommitment;

import ilog.concert.IloException;
import java.io.FileNotFoundException;

/**
 *
 * @author Luttner
 */
public class Main {
 
    public static void main(String[] args) throws IloException, FileNotFoundException{

        // Populates the data of the problem
        int nGenerators = 31;
        int nPeriods = 24;
        String generatorFile = "generators.txt";
        String demandsFile = "loads.txt";
        double loadSheddingCost = 46;

        UnitCommitmentProblem UCP = new UnitCommitmentProblem(nGenerators, nPeriods, generatorFile, demandsFile, loadSheddingCost);

        System.out.println("Solving the problem directly:");
        
        UnitCommitmentProblemModel UCPModel = new UnitCommitmentProblemModel(UCP);
        
        UCPModel.solve();
        UCPModel.printSolution();

        System.out.println();
        System.out.println("Solving the problem using Bender's Algorithm:");
        System.out.println();
        
        UnitCommitmentBendersMasterProblem UCPMaster = new UnitCommitmentBendersMasterProblem(UCP);
        
                
        UCPMaster.solve();
        System.out.println(UCPMaster.getObjective());
        
        
    }
}
