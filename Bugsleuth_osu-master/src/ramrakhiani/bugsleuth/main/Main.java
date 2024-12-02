package ramrakhiani.bugsleuth.main;

import ramrakhiani.bugsleuth.config.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.io.BufferedWriter;

public class Main {
    public static void main(String[] args) {

        LinkedHashSet<String> allStatements = new LinkedHashSet<>();
        List<LinkedHashSet<String>> allRankLists = new ArrayList<>();
        Boolean all_defects = false;
        LinkedHashSet<String> defects_all = new LinkedHashSet<String>();
        try
        {
            setParametersFromSettingsFile();
        }
        catch(IOException e){};

        String defect = args[0].trim().toLowerCase();
        int numberOfFiles = Integer.parseInt(args[1]);

        readDefects(defect, defects_all,all_defects);
        System.out.println("Read defects");
        long startTime = System.nanoTime();

        for(String defects: defects_all ) {

            defect = defects;
            allRankLists.clear();
            allStatements.clear();
            System.out.println("Processing defect "+defect);

            for (int i = 2; i <= numberOfFiles + 1; i++) {
                allRankLists.add(new LinkedHashSet<>());
                String filePath = args[i];
                File flPath = new File(filePath);
                if (!flPath.exists()) {
                    System.out.println(filePath + " file path does not exist");
                    System.exit(1);
                }

                readStatementsFromFile(defect, filePath, i - 2, allStatements, allRankLists);
            }
            if(allRankLists.size()!= numberOfFiles)
            {
                System.out.println("Not enough input results found for "+defect);
                continue;
            }


            Population<CandidateList> population = createInitialPopulation(Configuration.popSize, allStatements);
            Fitness<CandidateList, Double> fitness = new CandidateFitness();

            GeneticAlgorithm<CandidateList, Double> ga = new GeneticAlgorithm<CandidateList, Double>(population, fitness, allRankLists, allStatements, defect);
            addListener(ga, defect);
            ga.evolve(Configuration.maxIter);


        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000000;
        System.out.println("Total execution time for " + defect + " took " + duration + " seconds");
        System.out.println("Total number of defects: "+defects_all.size());
        long avg_time =(long) (duration/ defects_all.size());
        System.out.println("Average Time for " + defect + " took " + avg_time + " seconds");

    }

    /**
     * Configuring the value for root directory
     */

    private static void setParametersFromSettingsFile() throws IOException{
        File settings_file = new File("bugsleuth.settings");
        BufferedReader br = new BufferedReader(new FileReader(settings_file));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("root"))
                Configuration.rootDirectory = line.split("=")[1].trim();
        }
        br.close();
        System.out.println("Setting root directory as: " + Configuration.rootDirectory);
        Configuration.setParameters(Configuration.rootDirectory);
    }

    /**
     * reading defects from defect file
     */
    private static void readDefects(String defect, LinkedHashSet<String> defects_all, Boolean all_defects)
    {
        if(defect.equals("all"))
        {
            String line,d;
            all_defects = true;
            File fl_result = new File(Configuration.defectsFilePath);
            if (!fl_result.exists()) {
                System.out.println("Error, results not found in the file path: "+Configuration.defectsFilePath);
                System.exit(1);
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(Configuration.defectsFilePath))){
                while ((line = reader.readLine()) != null)
                {
                    d = line.trim().toLowerCase();
                    if(!(d.isEmpty()))
                    {
                        defects_all.add( line.trim().toLowerCase());
                    }
                }

            }catch(IOException e) {e.printStackTrace();}
        }
        else {
            defects_all.add(defect);
        }
    }

    /**
     * reading statements from input files to create input rank lists
     */

    private static void readStatementsFromFile(String defect,String filePath, int rankListIndex, LinkedHashSet<String> allStatements,List<LinkedHashSet<String>> allRankLists ) {

        String result_file = filePath + "/" + defect.split("_")[0].toLowerCase() + "/" + defect.split("_")[1] + "/stmt-susps.txt";
        File fl_result = new File(result_file);
        if (!fl_result.exists()) {
            System.out.println("Error, results not found in the file path: "+result_file);
        }
        else
        {
            HashMap<String, Double> FL = new HashMap<String, Double>();
            HashMap<String, Double> sortedFL = new HashMap<String, Double>();

            try (BufferedReader reader = new BufferedReader(new FileReader(result_file))) {
                String line;
                LinkedHashSet<String> uniqueStatements = new LinkedHashSet<>();
                uniqueStatements.clear();
                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        double score = Double.parseDouble(parts[1]);

                        if(score > 0.0)
                        {
                            String statement = parts[0].trim();

                            if(!FL.containsKey(statement))
                            {
                                FL.put(statement, score);
                            }
                            else {
                                if(FL.get(statement) < score) {
                                    FL.put(statement, score);

                                }

                            }

                        }
                    }
                }

                if(!FL.isEmpty())
                {
                    sortedFL = sortByValues(FL);

                    int counter = 0;
                    for(String stmt: sortedFL.keySet())
                    {
                        uniqueStatements.add(stmt);
                        counter ++;
                        if(counter == Configuration.k)
                            break;
                    }
                    allStatements.addAll(uniqueStatements);
                    allRankLists.get(rankListIndex).addAll(uniqueStatements);

                    uniqueStatements.clear();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *Sort a Hash Map of Statements and their suspicion scores in ascending order of their score
     */
    private static HashMap<String, Double> sortByValues(HashMap<String, Double> map) {
        List<Object> list = new LinkedList<Object>(map.entrySet());

        Collections.sort(list, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        Collections.reverse(list);

        double maxscore = Double.parseDouble(list.get(0).toString().split("=")[1].trim());
        double minscore = Double.parseDouble(list.get(list.size() - 1).toString().split("=")[1].trim());

        HashMap<String, Double> sortedHashMap = new LinkedHashMap<String, Double>();
        for (Iterator<Object> it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Double score = (Double) entry.getValue();
            Double normalizedScore = (score - minscore) / (maxscore - minscore);
            sortedHashMap.put((String) entry.getKey(), normalizedScore);
        }
        return sortedHashMap;
    }

    /**
     * create initial population by randomly generating candidate ranklists
     */
    private static Population<CandidateList> createInitialPopulation(int populationSize, LinkedHashSet<String> allStatements) {
        Population<CandidateList> population = new Population<CandidateList>();
        CandidateList base = new CandidateList();
        for (int i = 0; i < populationSize; i++) {
            CandidateList chr = base.getRandomRanklist(allStatements);
            population.addChromosome(chr);
        }
        return population;
    }

    /**
     * Adding a Iteration listener for Genetic Algorithm
     */

    private static void addListener(GeneticAlgorithm<CandidateList, Double> ga, String defect) {

        System.out.println(String.format("%s\t%s\t%s\t%s", "iter", "fit", "chromosome", "global best chromosome"));

        ga.addIterationListener(new IterationListener<CandidateList, Double>() {

            private final double threshold = 1e-5;
            int noImprovement = 0;
            CandidateList globalbest = null;
            @Override
            public void update(GeneticAlgorithm<CandidateList, Double> ga, String defect) {


                CandidateList best = ga.getBest();

                if(ga.getIteration() == 0) {
                    globalbest = best.clone();
                    noImprovement++;

                }
                else {
                    if(ga.fitness(best) < ga.fitness(globalbest))
                    {
                        noImprovement = 0;
                        globalbest = best.clone();
                    }
                    else
                    {
                        noImprovement++;
                    }
                }
                double bestFit = ga.fitness(best);
                int iteration = ga.getIteration();
                System.out.println(String.format("%s\t%s\t%s\t%s", iteration, bestFit, Arrays.toString(best.getCandidate()),Arrays.toString(globalbest.getCandidate())));
                if(iteration == Configuration.maxIter -1 || noImprovement>=Configuration.convIn)
                {
                    saveResultsToFile(defect,best.getCandidate());
                    ga.terminate();
                }
            }
        });
    }

    /**
     * saving the results in a .txt file in the destination directory
     */

    private static void saveResultsToFile(String defect,String[] bestPhenotype) {
        String result_file = Configuration.resultDirectory+"/BugSleuth_results_seed"+Configuration.seed + "/" + defect.split("_")[0].toLowerCase() + "/"
                + defect.split("_")[1] + "/stmt-susps.txt";
        File rfile = new File(result_file);

        rfile.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rfile))) {
            for (String gene : bestPhenotype) {
                writer.write(gene);
                writer.newLine();
            }
            System.out.println("Results saved to "+ result_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}