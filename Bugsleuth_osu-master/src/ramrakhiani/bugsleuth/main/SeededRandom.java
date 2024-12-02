package ramrakhiani.bugsleuth.main;

import java.util.Random;

public class SeededRandom extends Random {
    private final long seed;

    public SeededRandom(long seed){
        super(seed);
        this.seed = seed;

    }
    public long getSeed()
    {
        return this.seed;
    }

}
