package com.mygdx.game.psg.Engine;

import com.mygdx.game.psg.MainGame;
import com.mygdx.game.psg.Sprites.Unity;

import static com.badlogic.gdx.math.MathUtils.random;

public class Actions {

    public Genetic.GenType[] player, bot1, bot2, bot3, bot4, bot5, neutral;


    public int fitness[];

    public void setActions(){
        player = new Genetic.GenType[MainGame.unityNumber];
        bot1 = new Genetic.GenType[MainGame.unityNumber];
        bot2 = new Genetic.GenType[MainGame.unityNumber];
        bot3 = new Genetic.GenType[MainGame.unityNumber];
        bot4 = new Genetic.GenType[MainGame.unityNumber];
        bot5 = new Genetic.GenType[MainGame.unityNumber];
        neutral = new Genetic.GenType[MainGame.unityNumber];

        for(int i =0; i < MainGame.unityNumber; i++){
            player[random(0, MainGame.unityNumber - 1)] = Genetic.GenType.values()[random(0, Genetic.GenType.values().length - 1)];
            bot1[random(0, MainGame.unityNumber - 1)] = Genetic.GenType.values()[random(0, Genetic.GenType.values().length - 1)];
            bot2[random(0, MainGame.unityNumber - 1)] = Genetic.GenType.values()[random(0, Genetic.GenType.values().length - 1)];
            bot3[random(0, MainGame.unityNumber - 1)] = Genetic.GenType.values()[random(0, Genetic.GenType.values().length - 1)];
            bot4[random(0, MainGame.unityNumber - 1)] = Genetic.GenType.values()[random(0, Genetic.GenType.values().length - 1)];
            bot5[random(0, MainGame.unityNumber - 1)] = Genetic.GenType.values()[random(0, Genetic.GenType.values().length - 1)];
            neutral[random(0, MainGame.unityNumber - 1)] = Genetic.GenType.values()[random(0, Genetic.GenType.values().length - 1)];
        }
    }

    public void AddAction(Unity.Team team, Genetic.GenType attributeType){
        switch (team){
            case PLAYER: player[random(0, MainGame.unityNumber - 1)] = attributeType;
            case BOT1: bot1[random(0, MainGame.unityNumber - 1)] = attributeType;
            case BOT2: bot2[random(0, MainGame.unityNumber - 1)] = attributeType;
            case BOT3: bot3[random(0, MainGame.unityNumber - 1)] = attributeType;
            case BOT4: bot4[random(0, MainGame.unityNumber - 1)] = attributeType;
            case BOT5: bot5[random(0, MainGame.unityNumber - 1)] = attributeType;
            case NEUTRAL: neutral[random(0, MainGame.unityNumber - 1)] = attributeType;
        }
    }

    public int[] getFitness(Unity.Team team){
        switch (team){
            case PLAYER: return generateFitness(player);
            case BOT1: return generateFitness(bot1);
            case BOT2: return generateFitness(bot2);
            case BOT3: return generateFitness(bot3);
            case BOT4: return generateFitness(bot4);
            case BOT5: return generateFitness(bot5);
            case NEUTRAL: return generateFitness(neutral);
        }

        return fitness;
    }

    private int AttributeCount(Genetic.GenType[] attributes, Genetic.GenType type){
        int count = 0;

        for (Genetic.GenType attribute : attributes) {
            if (type == attribute) {
                count++;
            }
        }
        return count;
    }

    private int[] generateFitness(Genetic.GenType[] attribute){

        fitness = new int[Genetic.GenType.values().length];

        for (int i = 0; i < Genetic.GenType.values().length; i++) {

            fitness[i] = AttributeCount(attribute, Genetic.GenType.values()[i]);

        }

        return fitness;
    }
}
