package com.mygdx.game.psg.Engine;

import com.mygdx.game.psg.MainGame;
import com.mygdx.game.psg.Sprites.Unity;

import java.io.IOException;
import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.random;

public class Evolutive {

    private Genetic.GenType[] X = new Genetic.GenType[MainGame.DNANumber];
    private Genetic.GenType[] Y = new Genetic.GenType[MainGame.DNANumber];

    private ArrayList<Integer> integers = new ArrayList<Integer>();

    Genetic.GenType x;
    Genetic.GenType y;

    Actions actions;
    Save save = new Save();

    public Evolutive() throws IOException {

        this.actions = save.GetActions();

    }

    public Genetic[] NewGeneration(Unity.Team team){




        return null;
    }

    public void generateWheel(Unity.Team team){


    }

    public void Crossover(){

    }

    private Genetic.GenType[] Mutation(Genetic DNA){


        return null;
    }
}
