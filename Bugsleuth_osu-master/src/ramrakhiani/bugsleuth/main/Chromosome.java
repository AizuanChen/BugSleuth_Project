package ramrakhiani.bugsleuth.main;
import java.util.LinkedHashSet;
import java.util.List;

public interface Chromosome <C extends Chromosome<C>> {
    List <C> partially_mapped_crossover (C anotherChromosome);
    C mutate(LinkedHashSet<String> S);
    String[] getCandidate();
}
