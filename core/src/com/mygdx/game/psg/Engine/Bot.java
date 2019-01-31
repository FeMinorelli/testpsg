package com.mygdx.game.psg.Engine;

import com.mygdx.game.psg.MainGame;
import com.mygdx.game.psg.Sprites.Unity;

import static com.badlogic.gdx.math.MathUtils.random;

public class Bot {

    public int[] bot1, bot2, bot3, bot4, bot5, neutral;

    public Bot(){
        setBot();
    }

    public void setBot(int[] bot1, int[] bot2, int[] bot3, int[] bot4, int[] bot5, int[] neutral  ){
        this.bot1 = bot1;
        this.bot2 = bot2;
        this.bot3 = bot3;
        this.bot4 = bot4;
        this.bot5 = bot5;
        this.neutral = neutral;
    }

    public void setBot(){
        bot1 = new int[Genetic.GenType.values().length];
        bot2 = new int[Genetic.GenType.values().length];
        bot3 = new int[Genetic.GenType.values().length];
        bot4 = new int[Genetic.GenType.values().length];
        bot5 = new int[Genetic.GenType.values().length];
        neutral = new int[Genetic.GenType.values().length];

        for(int i = 0; i < Genetic.GenType.values().length; i++){
            bot1[i] = random(MainGame.min, MainGame.max);
            bot2[i] = random(MainGame.min, MainGame.max);
            bot3[i] = random(MainGame.min, MainGame.max);
            bot4[i] = random(MainGame.min, MainGame.max);
            bot5[i] = random(MainGame.min, MainGame.max);
            neutral[i] = random(MainGame.min, MainGame.max);
        }
    }

    public int[] getBot(Unity.Team team) {
        switch (team){
            case BOT1: return bot1;
            case BOT2: return bot2;
            case BOT3: return bot3;
            case BOT4: return bot4;
            case BOT5: return bot5;
            case NEUTRAL: return neutral;
        }
        return null;
    }

    public Genetic.GenType getAction(Unity selected){
        int index = random(0, Genetic.GenType.values().length - 1);

        switch (selected.team) {
            case BOT1: if(bot1[index] > random(0, MainGame.chance)){return Genetic.GenType.values()[index];} break;
            case BOT2: if(bot2[index] > random(0, MainGame.chance)){return Genetic.GenType.values()[index];} break;
            case BOT3: if(bot3[index] > random(0, MainGame.chance)){return Genetic.GenType.values()[index];} break;
            case BOT4: if(bot4[index] > random(0, MainGame.chance)){return Genetic.GenType.values()[index];} break;
            case BOT5: if(bot5[index] > random(0, MainGame.chance)){return Genetic.GenType.values()[index];} break;
            case NEUTRAL: if(neutral[index] > random(0, MainGame.chance)){return Genetic.GenType.values()[index];} break;
        }
        return null;
    }

    public void Adjust(){


    }

}
