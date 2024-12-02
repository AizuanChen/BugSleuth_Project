package ramrakhiani.bugsleuth.main;

import ramrakhiani.bugsleuth.config.Configuration;

import java.util.*;

public class Population<C extends Chromosome<C>> implements Iterable<C> {

    private List<C> chromosomes = new ArrayList<C>(Configuration.popSize);


    public void addChromosome(C chromosome) {
        this.chromosomes.add(chromosome);
    }

    public int getSize() {
        return this.chromosomes.size();
    }

    public C getRandomChromosome() {
        int numOfChromosomes = this.chromosomes.size();
        int indx = Configuration.random.nextInt(numOfChromosomes);
        return this.chromosomes.get(indx);
    }

    public C getChromosomeByIndex(int indx) {
        return this.chromosomes.get(indx);
    }

    public void sortPopulationByFitness(Comparator<C> chromosomesComparator) {
       // Collections.shuffle(this.chromosomes);
        Collections.sort(this.chromosomes, chromosomesComparator);
    }

    public void trim(int len) {
        this.chromosomes = this.chromosomes.subList(0, len);
    }

    @Override
    public Iterator<C> iterator() {
        return this.chromosomes.iterator();
    }

}