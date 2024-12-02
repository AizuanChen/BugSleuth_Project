package ramrakhiani.bugsleuth.main;
import ramrakhiani.bugsleuth.config.Configuration;

import java.sql.CallableStatement;
import java.util.*;

public class CandidateList implements Chromosome<CandidateList>, Cloneable {

    private String[] candidate = new String[Configuration.k];



    public CandidateList mutate(LinkedHashSet<String> allStatements){
        int mut_op = Configuration.random.nextInt(4);
        CandidateList result = this.clone();
        switch(mut_op) {
            case 0: {
                SwapMutation(result);
                break;
            }
            case 1:
            {
                BoundarySwapMutation(result);
                break;

            }
            case 2:
            {
                ReplacementMutation(result, allStatements);
                break;
            }
            case 3:
            {
                Inc_Dec_Mutation(result,allStatements);
                break;
            }

        }
        return result;
    }

    /**
     * returns a random rank list of statements utilizing the unique statement pool
     */
    public CandidateList getRandomRanklist(LinkedHashSet<String> allStatements)
    {
        CandidateList result = this.clone();
        String[] stmt_pool = allStatements.toArray(new String[0]);
        for(int i=0;i<Configuration.k;i++)
        {
            int index = Configuration.random.nextInt(stmt_pool.length);
            while(Arrays.asList(result.candidate).contains(stmt_pool[index])) {
                index = Configuration.random.nextInt(stmt_pool.length);
            }
            result.candidate[i] = stmt_pool[index];
        }
        return result;

    }

    public String[] getCandidate() {
        return this.candidate;
    }

    /**
     * Clone the CandidateList(chromosome) object
     */
    @Override
    protected CandidateList clone() {
        CandidateList clone = new CandidateList();
        System.arraycopy(this.candidate, 0, clone.candidate, 0, this.candidate.length);
        return clone;
    }

    /**partially mapped crossover for generating offsprings
     */
    public List<CandidateList> partially_mapped_crossover(CandidateList other){
        CandidateList thisClone = this.clone();
        CandidateList otherClone = other.clone();
        int index1 = Configuration.random.nextInt(this.candidate.length - 1);
        int index2 = Configuration.random.nextInt(index1,this.candidate.length-1);

        for (int i = index1; i <=index2; i++) {
            String tmp = thisClone.candidate[i];
            thisClone.candidate[i] = otherClone.candidate[i];
            otherClone.candidate[i] = tmp;
        }
        repair(thisClone,otherClone,index1,index2);
        repair(otherClone,thisClone,index1,index2);

        return Arrays.asList(thisClone, otherClone);
    }

    /**
     * repair process to avoid duplicate genes after crossover (used for partially mapped crossover
     */
    private static void repair(CandidateList thisClone, CandidateList otherClone,int index1, int index2)
    {
        for(int i=0;i<index1;i++)
        {
            int index = indexExists(thisClone,thisClone.candidate[i],index1,index2);
            while(index!=-1)
            {
                thisClone.candidate[i] = otherClone.candidate[index];
                index = indexExists(thisClone,thisClone.candidate[i],index1,index2);
            }

        }
        for(int i=index2+1,n=thisClone.candidate.length;i<n;i++)
        {

            int index = indexExists(thisClone,thisClone.candidate[i],index1,index2);
            while(index!=-1)
            {
                thisClone.candidate[i] = otherClone.candidate[index];
                index = indexExists(thisClone,thisClone.candidate[i],index1,index2);
            }

        }

    }

    /**
     * check if such an index exists where statement is present inside a subset of a String array
     */

    public static int indexExists(CandidateList thisClone,String statement, int begin, int end)
    {
        for(int i= begin;i<=end;i++)
        {
            if(thisClone.candidate[i].equals(statement))
            {
                return i;
            }
        }
        return -1;
    }

    private void SwapMutation(CandidateList result)
    {
        int index1 = Configuration.random.nextInt(this.candidate.length);
        int index2 = Configuration.random.nextInt(this.candidate.length);
        while(index2==index1)
        {
            index2 = Configuration.random.nextInt(this.candidate.length);
        }
        String temp = result.candidate[index1];
        result.candidate[index1] = result.candidate[index2];
        result.candidate[index2] = temp;

    }
    private void BoundarySwapMutation(CandidateList result)
    {
        String temp = result.candidate[0];
        result.candidate[0] = result.candidate[result.candidate.length-1];
        result.candidate[result.candidate.length-1] = temp;
    }
    private void ReplacementMutation(CandidateList result, LinkedHashSet<String> allStatements)
    {
        String[] array2 = allStatements.toArray(new String[0]);
        Arrays.sort(array2);
        String[] array1 = result.candidate;
        Arrays.sort(array1);
        if(!Arrays.equals(array1,array2)) {
            List<String> allStatementsList = new ArrayList<>(allStatements);
            int index1 = Configuration.random.nextInt(0, allStatements.size());
            while (Arrays.asList(result.candidate).contains(allStatementsList.get(index1))) {
                index1 = Configuration.random.nextInt(0, allStatements.size());

            }
            int index2 = Configuration.random.nextInt(this.candidate.length);
            result.candidate[index2] = allStatements.toArray(new String[0])[index1];
        }
    }

    private void Inc_Dec_Mutation(CandidateList result, LinkedHashSet<String> allStatements)
    {
        List<String> allStatementsList = new ArrayList<>(allStatements);
        int index = Configuration.random.nextInt(this.candidate.length);
        if(result.candidate[index].contains("#")) {
            String path = result.candidate[index].split("#")[0];
            int line_no = Integer.parseInt(result.candidate[index].split("#")[1]);

            String[] statementList = new String[6];
            for (int i = 1, j = 0; i <= 3; i++, j++) {
                statementList[j] = path + "#" + (line_no + i);
                j++;
                statementList[j] = path + "#" + (line_no - i);

            }
            for (int k = 0; k < statementList.length; k++) {
                if (allStatementsList.contains(statementList[k]) && !(Arrays.asList(result.candidate).contains(statementList[k]))) {
                    result.candidate[index] = statementList[k];
                    break;
                }
            }
        }
    }

}

