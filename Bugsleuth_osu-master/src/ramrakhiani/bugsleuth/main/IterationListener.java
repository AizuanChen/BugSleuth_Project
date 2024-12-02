package ramrakhiani.bugsleuth.main;

public interface IterationListener<C extends Chromosome<C>, T extends Comparable<T>> {

    void update( GeneticAlgorithm<C, T> environment ,String D );

}
