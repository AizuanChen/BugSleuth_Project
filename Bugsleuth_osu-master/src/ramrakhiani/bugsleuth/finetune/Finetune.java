package ramrakhiani.bugsleuth.finetune;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import java.util.Arrays;
import java.util.Collection;
import ramrakhiani.bugsleuth.config.Configuration;
import ramrakhiani.bugsleuth.main.Main;

@RunWith(Parameterized.class)
public class Finetune {

    private double CP;
    private double MP;
    //private int convIn;
    private int popSize;

    public Finetune(double CP, double MP, int popSize) {
        this.CP = CP;
        this.MP = MP;
        this.popSize = popSize;
        //this.convIn = convIn;
    }

    @Parameters
    public static Collection<Object[]> data() {
        int numCombinations = (int) 1000;
        Object[][] data = new Object[numCombinations][3];
        int index = 0;

        for (double CP = 0.1; CP <= 1.0; CP += 0.1) {
            for (double MP = 0.1; MP <= 1.0; MP += 0.1) {
                for (int popSize = 50; popSize <= 500; popSize += 50) {
                    data[index] = new Object[]{CP, MP, popSize};
                    index++;

                }
            }
        }
        return Arrays.asList(data);
    }
    @Test
    public void configFinetune() {
        Configuration.popSize = popSize;
        Configuration.CP = CP;
        Configuration.MP = MP;
        Configuration.resultDirectory = "/home/ashish/bugsleuth_osu/BugSleuth_finetune_results/JacksonDatabind/CP=" + String.format("%.2f", CP) + "_MP=" + String.format("%.2f", MP) + "_popSize=" + popSize;
        //System.out.println("Valuesssss are"+ Configuration.popSize);
        System.out.println("Valuesssss are"+ Configuration.resultDirectory);
        try {
            Main.main(new String[]{"all", "2", "/home/ashish/SBIR-ReplicationPackage/FaultLocalization/data/SBFL_results_jacksondatabind", "/home/ashish/SBIR-ReplicationPackage/FaultLocalization/data/blues_results_jacksondatabind"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
