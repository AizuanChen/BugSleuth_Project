package ramrakhiani.bugsleuth.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class CandidateFitness implements Fitness<CandidateList, Double> {

    /**
     *This method returns the sum of the Spearman footrule distance between a candidate rank list and all input rank lists
     */
    @Override
    public Double calculate(CandidateList chromosome , List<LinkedHashSet<String>> allRankLists) {

        List<String> genotype = new ArrayList<String>(Arrays.asList(chromosome.getCandidate()));

        double totalDistance = 0.0;

        for(LinkedHashSet<String> rankList:allRankLists)
        {

            LinkedHashSet<String> union = new LinkedHashSet<String>(genotype);
            union.addAll(rankList);
            double distance = 0.0;

            for (String statement : union) {

                int rankInGenotype = genotype.contains(statement) ? new ArrayList<String>(genotype).indexOf(statement) + 1 : rankList.size() + 1;
                int rankInList = rankList.contains(statement) ? new ArrayList<String>(rankList).indexOf(statement) + 1 : rankList.size() + 1;
                distance += Math.abs(rankInGenotype - rankInList);

            }
            totalDistance += distance;
            union.clear();
        }
        return totalDistance;

    }

    public double calculateDouble(CandidateList chromosome, List<LinkedHashSet<String>> allRankLists) {
        return calculate(chromosome, allRankLists);
    }

}