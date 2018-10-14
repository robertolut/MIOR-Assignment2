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

        String generatorFile = "generators.txt";
        String demandsFile = "loads.txt";
        int nGenerators = 31;
        int nPeriods = 24;
        double loadSheddingCost = 46;

        UnitCommitmentProblem UCP = new UnitCommitmentProblem(nGenerators, nPeriods, generatorFile, demandsFile, loadSheddingCost);

        UnitCommitmentProblemModel UCPModel = new UnitCommitmentProblemModel(UCP);
        
        UCPModel.solve();
        UCPModel.printSolution();
    }
}
