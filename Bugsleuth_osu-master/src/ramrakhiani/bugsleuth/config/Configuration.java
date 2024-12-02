package ramrakhiani.bugsleuth.config;

import ramrakhiani.bugsleuth.main.SeededRandom;
public class Configuration {

    public static String rootDirectory = null;
    public static String resultDirectory = null;
    public static String defectsFilePath = null;
    public static int k = 5;
    public static long seed = 956L;
    public static int maxIter = 1000;
    public static int convIn = 60;
    public static int popSize = 200;
    public static Double CP = 0.9;
    public static Double MP = 0.6;
    public static int tSize = 20;
    public static SeededRandom random = new SeededRandom(seed);

    public static void setParameters(String rootDirectory){
        defectsFilePath = rootDirectory + "/data/59Defects.txt";
        resultDirectory = rootDirectory +"/data/BugSleuth_results/OTM";
    }
}