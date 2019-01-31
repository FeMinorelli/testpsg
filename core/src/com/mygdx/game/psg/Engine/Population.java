package com.mygdx.game.psg.Engine;

import com.mygdx.game.psg.MainGame;
import com.mygdx.game.psg.Sprites.Unity;

public class Population {

    public Genetic[] player = new Genetic[MainGame.unityNumber];
    public Genetic[] bot1 = new Genetic[MainGame.unityNumber];
    public Genetic[] bot2 = new Genetic[MainGame.unityNumber];
    public Genetic[] bot3 = new Genetic[MainGame.unityNumber];
    public Genetic[] bot4 = new Genetic[MainGame.unityNumber];
    public Genetic[] bot5 = new Genetic[MainGame.unityNumber];
    public Genetic[] neutral = new Genetic[MainGame.unityNumber];

    public Population(){

        setPopulation();
    }

    public void setPopulation(){

        for(int i = 0; i < MainGame.unityNumber; i++){
            player[i] = new Genetic();
            bot1[i] = new Genetic();
            bot2[i] = new Genetic();
            bot3[i] = new Genetic();
            bot4[i] = new Genetic();
            bot5[i] = new Genetic();
            neutral[i] = new Genetic();
        }
    }

    public void setPopulation(Genetic[] player, Genetic[] bot1, Genetic[] bot2, Genetic[] bot3, Genetic[] bot4, Genetic[] bot5, Genetic[] neutral){
        this.player = player;
        this.bot1 = bot1;
        this.bot2 = bot2;
        this.bot3 = bot3;
        this.bot4 = bot4;
        this.bot5 = bot5;
        this.neutral = neutral;
    }

    public Genetic[] getPopulation(Unity.Team team){

        switch (team){
            case PLAYER: return player;
            case BOT1: return bot1;
            case BOT2: return bot2;
            case BOT3: return bot3;
            case BOT4: return bot4;
            case BOT5: return bot5;
            case NEUTRAL: return neutral;
        }
        return null;
    }
}
