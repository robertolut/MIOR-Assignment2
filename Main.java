/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitCommitment;

import ilog.concert.IloException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Luttner
 */
public class Main {
 
    public static void main(String[] args) throws IloException{
        // Populates the data of the problem
        int nGenerators = 31;
        int nPeriods = 24;
        
        double[] loadSheddingCosts = new double[nPeriods];
        
        double[] demand = new double[] {1344, 1379, 1425, 1468, 2010, 2145, 2334, 2545, 2230, 2134, 2200, 2221, 2345, 2268, 2100, 1980, 1992, 2245, 2345, 2432, 2564, 2080, 1800, 1325};

        double[] commitmentCosts = new double[nGenerators];
        double[] marginalCosts = new double[nGenerators];
        double[] startupCosts = new double[nGenerators];
        int[] minimumOnTime = new int[nGenerators];
        int[] minimumOffTime = new int[nGenerators];
        double[] minimumOutput = new double[nGenerators];
        double[] maximumOutput = new double[nGenerators];
        double[] rampUpLimit = new double[nGenerators];
        double[] rampDownLimit = new double[nGenerators];

        
        String[] genNames = new String[] {"U12", "U13", "U14", "U15", "U16", "U21", "U22", "U23", "U24", "U50", "U51", "U52", "U53", "U54", "U55", "U76", "U77", "U78", "U79", "U100", "U101", "U102", "U155", "U156", "U157", "U158", "U197", "U198", "U199", "U350", "U400"};
        double[][] dados = new double[][] {{	2.40	, 	12	, 	87.4	, 	0	, 	60	, 	4	, 	2	, 	23.41	}, 
{	2.40	, 	12	, 	87.4	, 	0	, 	60	, 	4	, 	2	, 	23.41	}, 
{	2.40	, 	12	, 	87.4	, 	0	, 	60	, 	4	, 	2	, 	23.41	}, 
{	2.40	, 	12	, 	87.4	, 	0	, 	60	, 	4	, 	2	, 	23.41	}, 
{	2.40	, 	12	, 	87.4	, 	0	, 	60	, 	4	, 	2	, 	23.41	}, 
{	15.80	, 	20	, 	15.0	, 	3000	, 	180	, 	1	, 	1	, 	29.58	}, 
{	15.80	, 	20	, 	15.0	, 	3000	, 	180	, 	1	, 	1	, 	29.58	}, 
{	15.80	, 	20	, 	15.0	, 	3000	, 	180	, 	1	, 	1	, 	29.58	}, 
{	15.80	, 	20	, 	15.0	, 	3000	, 	180	, 	1	, 	1	, 	29.58	}, 
{	0.00	, 	50	, 	0.0	, 	0	, 	50	, 	1	, 	1	, 	0.00	}, 
{	0.00	, 	50	, 	0.0	, 	0	, 	50	, 	1	, 	1	, 	0.00	}, 
{	0.00	, 	50	, 	0.0	, 	0	, 	50	, 	1	, 	1	, 	0.00	}, 
{	0.00	, 	50	, 	0.0	, 	0	, 	50	, 	1	, 	1	, 	0.00	}, 
{	0.00	, 	50	, 	0.0	, 	0	, 	50	, 	1	, 	1	, 	0.00	}, 
{	0.00	, 	50	, 	0.0	, 	0	, 	50	, 	1	, 	1	, 	0.00	}, 
{	15.20	, 	76	, 	715.2	, 	1200	, 	120	, 	8	, 	4	, 	11.46	}, 
{	15.20	, 	76	, 	715.2	, 	1200	, 	120	, 	8	, 	4	, 	11.46	}, 
{	15.20	, 	76	, 	715.2	, 	1200	, 	120	, 	8	, 	4	, 	11.46	}, 
{	15.20	, 	76	, 	715.2	, 	1200	, 	120	, 	8	, 	4	, 	11.46	}, 
{	25.00	, 	100	, 	575.0	, 	3000	, 	420	, 	8	, 	8	, 	18.60	}, 
{	25.00	, 	100	, 	575.0	, 	3000	, 	420	, 	8	, 	8	, 	18.60	}, 
{	25.00	, 	100	, 	575.0	, 	3000	, 	420	, 	8	, 	8	, 	18.60	}, 
{	54.25	, 	155	, 	312.0	, 	9000	, 	180	, 	8	, 	8	, 	9.92	}, 
{	54.25	, 	155	, 	312.0	, 	9000	, 	180	, 	8	, 	8	, 	9.92	}, 
{	54.25	, 	155	, 	312.0	, 	9000	, 	180	, 	8	, 	8	, 	9.92	}, 
{	54.25	, 	155	, 	312.0	, 	9000	, 	180	, 	8	, 	8	, 	9.92	}, 
{	68.95	, 	197	, 	1018.9	, 	420	, 	180	, 	12	, 	10	, 	19.20	}, 
{	68.95	, 	197	, 	1018.9	, 	420	, 	180	, 	12	, 	10	, 	19.20	}, 
{	68.95	, 	197	, 	1018.9	, 	420	, 	180	, 	12	, 	10	, 	19.20	}, 
{	140.00	, 	350	, 	2298.0	, 	9000	, 	240	, 	24	, 	24	, 	10.08	}, 
{	100.00	, 	400	, 	0.0	, 	65000	, 	1200	, 	1	, 	1	, 	5.31	}};
    
        for (int i=0; i<nGenerators; i++){
            commitmentCosts[i]=dados[i][3];
            marginalCosts[i]=dados[i][7];
            startupCosts[i]=dados[i][2];
            minimumOnTime[i]=(int) dados[i][5];
            minimumOffTime[i]=(int) dados[i][6];
            minimumOutput[i]=dados[i][0];
            maximumOutput[i]=dados[i][1];
            rampUpLimit[i]=dados[i][4];
            rampDownLimit[i]=dados[i][4];
        }
        for (int j=0; j<nPeriods; j++){
            loadSheddingCosts[j]=46;
        }

        UnitCommitmentProblem UCP = new UnitCommitmentProblem(nGenerators, nPeriods, commitmentCosts, marginalCosts, startupCosts, minimumOnTime, minimumOffTime, minimumOutput, maximumOutput, rampUpLimit, rampDownLimit, genNames, loadSheddingCosts, demand);

        UnitCommitmentProblemModel UCPModel = new UnitCommitmentProblemModel(UCP);
        
        UCPModel.solve();
    } 
}
