package ramrakhiani.bugsleuth.main;
import ramrakhiani.bugsleuth.config.Configuration;

import java.util.*;

public class GeneticAlgorithm<C extends Chromosome<C>, T extends Comparable<T>> {

    private final ChromosomesComparator chromosomesComparator;
    private final Fitness<C, T> fitnessFunc;
    private Population<C> population;
    private final List<LinkedHashSet<String>> allRankLists;
    private final LinkedHashSet<String> allStatements;
    private final String defect;
    private final List<IterationListener<C, T>> iterationListeners = new LinkedList<IterationListener<C, T>>();
    private boolean terminate = false;

    private int iteration = 0;
    private class ChromosomesComparator implements Comparator<C> {

        private final Map<C, T> cache = new WeakHashMap<C, T>();

        @Override
        public int compare(C chr1, C chr2) {
            T fit1 = this.fit(chr1);
            T fit2 = this.fit(chr2);
            int ret = fit1.compareTo(fit2);
            return ret;
        }

        public T fit(C chr) {
            T fit = this.cache.get(chr);
            if (fit == null) {
                fit = GeneticAlgorithm.this.fitnessFunc.calculate(chr,GeneticAlgorithm.this.allRankLists);
                this.cache.put(chr, fit);
            }
            return fit;
        };
        public void clearCache() {
            this.cache.clear();
        }
    }

    public GeneticAlgorithm(Population<C> population, Fitness<C, T> fitnessFunc, List<LinkedHashSet<String>> allRankLists, LinkedHashSet<String> allStatements, String defect) {
        this.population = population;
        this.fitnessFunc = fitnessFunc;
        this.chromosomesComparator = new ChromosomesComparator();
        this.allRankLists = allRankLists;
        this.allStatements = allStatements;
        this.defect = defect;
        this.population.sortPopulationByFitness(this.chromosomesComparator);
    }

    public void evolve() {

        Population<C> newPopulation = new Population<C>();
        float selfraction = Configuration.random.nextFloat(0,1);
        int survivecount =  Math.round(selfraction * Configuration.popSize);

        //perform crossover and mutation
        while(newPopulation.getSize()<(Configuration.popSize- survivecount)){
           if(Configuration.random.nextFloat(0,1)<=Configuration.MP) {
                C chromosome = this.rouletteWheelSelection();
                C mutated = chromosome.mutate(this.allStatements);
                newPopulation.addChromosome(mutated);
            }

           if(Configuration.random.nextFloat(0,1)<=Configuration.CP)
                {

                C parent1 = this.rouletteWheelSelection();
                C parent2 = this.rouletteWheelSelection();
                while (parent2.equals(parent1)) {
                    parent2 = this.rouletteWheelSelection();
                }

                List<C> crossovered = parent1.partially_mapped_crossover(parent2);
                for (C c : crossovered) {
                    newPopulation.addChromosome(c);
                }
            }
        }

        //perform selection using tournament selection to pass on fit chromosomes to the next generation

        while(newPopulation.getSize()<Configuration.popSize) {

            C selectedChromosome = tournamentSelector(Configuration.tSize);
            newPopulation.addChromosome(selectedChromosome);
        }

        newPopulation.sortPopulationByFitness(this.chromosomesComparator);
        this.population = newPopulation;
    }

    public void evolve(int count) {
        this.terminate = false;

        for (int i = 0; i < count; i++) {
            if (this.terminate) {
                break;
            }
            this.evolve();
            this.iteration = i;
            for (IterationListener<C, T> l : this.iterationListeners) {
                l.update(this, this.defect);
            }
        }
    }

    public int getIteration() {
        return this.iteration;
    }

    public void terminate() {
        this.terminate = true;
    }

    public Population<C> getPopulation() {
        return this.population;
    }

    public C getBest() {

        this.population.sortPopulationByFitness(this.chromosomesComparator);
        return this.population.getChromosomeByIndex(0);
    }

    public C getWorst() {
        return this.population.getChromosomeByIndex(this.population.getSize() - 1);
    }

    public void addIterationListener(IterationListener<C, T> listener) {
        this.iterationListeners.add(listener);
    }

    public void removeIterationListener(IterationListener<C, T> listener) {
        this.iterationListeners.remove(listener);
    }

    public T fitness(C chromosome) {
        return this.chromosomesComparator.fit(chromosome);
    }

    public void clearCache() {
        this.chromosomesComparator.clearCache();
    }

    public double calculateTotalFitness() {
        double totalFitness = 0.0;
        for (int i = 0; i < this.population.getSize(); i++) {
            C chromosome = this.population.getChromosomeByIndex(i);
            totalFitness += this.fitnessFunc.calculateDouble(chromosome, this.allRankLists);
        }
        return totalFitness;
    }

    /**
     * Roulette wheel selector to select two parent chromosomes for crossover
     */
    public C rouletteWheelSelection() {

        double totalFitness = calculateTotalFitness();
        double pop_sector =Configuration.random.nextDouble() * totalFitness;
        double cumulativeFitness = 0.0;

        for (C chromosome : this.population) {
            cumulativeFitness += this.fitnessFunc.calculateDouble(chromosome, this.allRankLists);
            if (cumulativeFitness >= pop_sector) {
                return chromosome;
            }
        }
        throw new RuntimeException("Roulette wheel selection failed to select a chromosome");
    }

    /**
     * Tournament selector to select fit individual chromosomes for the next generation
     */
    public C tournamentSelector(int tournamentSize) {
        C bestChromosome = null;
        double bestFitness = Double.POSITIVE_INFINITY;
        for (int i = 0; i < tournamentSize; i++) {

          C randomChromosome = this.population.getRandomChromosome();

            double fitness = this.fitnessFunc.calculateDouble(randomChromosome, this.allRankLists);
            if (fitness < bestFitness) {
                bestChromosome = randomChromosome;
                bestFitness = fitness;
            }
        }
        if (bestChromosome == null) {
            throw new RuntimeException("Tournament selection failed to select a chromosome");
        }

        return bestChromosome;
    }
}
