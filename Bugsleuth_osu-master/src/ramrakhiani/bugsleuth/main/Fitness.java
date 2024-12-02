package ramrakhiani.bugsleuth.main;

import java.util.LinkedHashSet;
import java.util.List;

public interface Fitness<C extends Chromosome<C>, T extends Comparable<T>> {

    T calculate(C chromosome, List<LinkedHashSet<String>> R);
    double calculateDouble(C chromosome, List<LinkedHashSet<String>> R);
}
