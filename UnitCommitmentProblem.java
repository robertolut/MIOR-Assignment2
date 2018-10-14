package UnitCommitment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *
 * @author Luttner
 */
public class UnitCommitmentProblem {
    // For an instance of the Unit Commitment Problem, we will need the
    // number of generators and the number of time periods.
    // Then, for each generator, we need the commitment, startup and 
    // marginal costs, minimum on and off times, minimum and maximum
    // outputs, ramp up and ramp down limits. For the time periods, we need
    // the load shedding cost and power demand. Finally, we will have the
    // minimum on and off times of each generator at each time period, which
    // will not be informed as a parameter, but rather calculated by the
    // constructor method.

    private final int nGenerators;
    private final int nPeriods;

    private final double commitmentCosts[];
    private final double marginalCosts[];
    private final double startupCosts[];
    private final int minimumOnTime[];
    private final int minimumOffTime[];
    private final double minimumOutput[];
    private final double maximumOutput[];
    private final double rampUpLimit[];
    private final double rampDownLimit[];
    private final String generatorNames[];

    private final double loadSheddingCosts[];
    private final double powerDemands[];

    // Constructors

    /** Constructor of UnitCommitmentProblem from files
     *
     * @param nGenerators
     * @param nPeriods
     * @param generatorFileName
     * @param demandsFileName
     * @param loadSheddingCosts
     * @throws FileNotFoundException
     */
    public UnitCommitmentProblem(int nGenerators, int nPeriods, String generatorFileName, String demandsFileName, double loadSheddingCosts) throws FileNotFoundException {
        this.nGenerators=nGenerators;
        this.nPeriods=nPeriods;
        this.commitmentCosts= new double[nGenerators];
        this.marginalCosts=new double[nGenerators];
        this.startupCosts=new double[nGenerators];
        this.minimumOnTime=new int[nGenerators];
        this.minimumOffTime=new int[nGenerators];
        this.minimumOutput=new double[nGenerators];
        this.maximumOutput=new double[nGenerators];
        this.rampUpLimit=new double[nGenerators];
        this.rampDownLimit=new double[nGenerators];
        this.generatorNames=new String[nGenerators];
        this.loadSheddingCosts=new double[nGenerators];
        this.powerDemands=new double[nPeriods];
        
        File generatorFile = new File(generatorFileName);
        Scanner generatorScanner = new Scanner(generatorFile);

        generatorScanner.useLocale(Locale.US);

        generatorScanner.nextLine();
        generatorScanner.nextLine();
        for (int i =0; i<nGenerators; i++){
            this.generatorNames[i]=generatorScanner.next();
            this.minimumOutput[i]=generatorScanner.nextDouble();
            this.maximumOutput[i]=generatorScanner.nextDouble();
            this.startupCosts[i]=generatorScanner.nextDouble();
            this.commitmentCosts[i]=generatorScanner.nextDouble();
            this.rampUpLimit[i]=generatorScanner.nextDouble();
            this.rampDownLimit[i]=rampUpLimit[i];
            this.minimumOnTime[i]=generatorScanner.nextInt();
            this.minimumOffTime[i]=generatorScanner.nextInt();
            this.marginalCosts[i]=generatorScanner.nextDouble();
            this.loadSheddingCosts[i]=loadSheddingCosts;
        }
        generatorScanner.close();
        
        File demandsFile = new File(demandsFileName);
        Scanner demandsScanner = new Scanner(demandsFile);
        demandsScanner.nextLine();
        for (int j=0; j<nPeriods; j++){
            this.powerDemands[j] = demandsScanner.nextDouble();
        }
        demandsScanner.close();

    }


    /**
     * Constructor of a Unit Commitment Problem from its data.
     * @param nGenerators
     * @param nPeriods
     * @param commitmentCosts
     * @param marginalCosts
     * @param startupCosts
     * @param minimumOnTime
     * @param minimumOffTime
     * @param minimumOutput
     * @param maximumOutput
     * @param rampUpLimit
     * @param rampDownLimit
     * @param generatorNames
     * @param loadSheddingCosts
     * @param powerDemands
     */
    public UnitCommitmentProblem(int nGenerators, int nPeriods, double commitmentCosts[], double marginalCosts[], double startupCosts[], int minimumOnTime[], int minimumOffTime[], double minimumOutput[], double maximumOutput[], double rampUpLimit[], double rampDownLimit[], String generatorNames[], double loadSheddingCosts[], double powerDemands[]) {
        this.nGenerators=nGenerators;
        this.nPeriods=nPeriods;
        this.commitmentCosts=commitmentCosts;
        this.marginalCosts=marginalCosts;
        this.startupCosts=startupCosts;
        this.minimumOnTime=minimumOnTime;
        this.minimumOffTime=minimumOffTime;
        this.minimumOutput=minimumOutput;
        this.maximumOutput=maximumOutput;
        this.rampUpLimit=rampUpLimit;
        this.rampDownLimit=rampDownLimit;
        this.generatorNames=generatorNames;
        this.loadSheddingCosts=loadSheddingCosts;
        this.powerDemands=powerDemands;
    }
    // Methods 
    /**
     * Returns the number of generators.
     * @return 
     */
    public int getNGenerators() {
        return nGenerators;
    }
    /**
     * Returns the number of time periods.
     * @return 
     */
    public int getNPeriods() {
        return nPeriods;
    }
    /**
     * Returns the vector of commitment costs.
     * @return 
     */
    public double[] getCommitmentCosts() {
        return commitmentCosts;
    }
    /**
     * Returns the vector of marginal costs.
     * @return 
     */
    public double[] getMarginalCosts() {
        return marginalCosts;
    }
    /**
     * Returns the vector of startup costs.
     * @return 
     */
    public double[] getStartupCosts() {
        return startupCosts;
    }
    /**
     * Returns the vector of minimum on-times.
     * @return 
     */
    public int[] getMinimumOnTime() {
        return minimumOnTime;
    }
    /**
     * Returns the vector of minimum off-times.
     * @return 
     */
    public int[] getMinimumOffTime() {
        return minimumOffTime;
    }
    /**
     * Returns the vector of minimum outputs.
     * @return 
     */
    public double[] getMinimumOutput() {
        return minimumOutput;
    }
    /**
     * Returns the vector of maximum outputs.
     * @return 
     */
    public double[] getMaximumOutput() {
        return maximumOutput;
    }
    /**
     * Returns the vector of ramp up limits.
     * @return 
     */
    public double[] getRampUpLimit() {
        return rampUpLimit;
    }
    /**
     * Returns the vector of ramp down limits.
     * @return 
     */
    public double[] getRampDownLimit() {
        return rampDownLimit;
    }
    public String[] getGeneratorNames() {
        return generatorNames;
    }
    
    /**
     * Returns the vector of load shedding costs.
     * @return 
     */
    public double[] getLoadSheddingCosts() {
        return loadSheddingCosts;
    }
    /**
     * Returns the vector of power demands.
     * @return 
     */
    public double[] getPowerDemands() {
        return powerDemands;
    }

    public void print(){
        System.out.println("************************");
        System.out.println("Unit Commitment Problem ");
        System.out.println("************************");
        System.out.println("# Generators : "+nGenerators);
        System.out.println("# Time Periods : "+ nPeriods);
        System.out.println("Comitmment costs :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+commitmentCosts[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Marginal production costs :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+marginalCosts[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Start-up Costs :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+startupCosts[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Minimum on times :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+minimumOnTime[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Minimum off times :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+minimumOffTime[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Minimum outputs :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+minimumOutput[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Maximum outputs :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+maximumOutput[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Ramp-up limits :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+rampUpLimit[i-1]+" ");
        }
        System.out.println(" ");
        System.out.println("Ramp-down limits :");
        for(int i = 1 ; i <= nGenerators; i++){
            System.out.print("Gen"+i+" "+rampDownLimit[i-1]+" ");
        }
        System.out.println(" ");
    }


}
