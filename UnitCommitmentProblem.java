package UnitCommitment;

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

    public double getCommitmentCost(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return commitmentCosts[generator];
    }
    public double getMarginalCost(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return marginalCosts[generator];
    }
    public double getStartupCost(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return startupCosts[generator];
    }
    public double getMinimumOutput(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return minimumOutput[generator];
    }
    public double getMaximumOutput(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return maximumOutput[generator];
    }
    public double getRampUpLimit(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return rampUpLimit[generator];
    }
    public double getRampDownLimit(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return rampDownLimit[generator];
    }
    public String getGeneratorName(int generator){
        if(generator < 0 || generator > nGenerators-1){
            throw new IllegalArgumentException("The generator number must be in [ 0,"+(nGenerators-1)+" ]");
        }
        return generatorNames[generator];
    }

    public double getPowerDemand(int period){
        if(period < 0 || period > nPeriods-1){
            throw new IllegalArgumentException("The period must be in [ 0,"+(nPeriods-1)+" ]");
        }
        return powerDemands[period];
    }
    public double getLoadSheddingCost(int period){
        if(period < 0 || period > nPeriods-1){
            throw new IllegalArgumentException("The period must be in [ 0,"+(nPeriods-1)+" ]");
        }
        return loadSheddingCosts[period];
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
