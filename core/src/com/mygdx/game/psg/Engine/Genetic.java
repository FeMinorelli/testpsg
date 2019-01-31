package com.mygdx.game.psg.Engine;

import com.mygdx.game.psg.MainGame;

import static com.badlogic.gdx.math.MathUtils.random;

public class Genetic {

    public enum GenType {
        SIZE,
        OFFENSIVE,
        DEFENSIVE,
        SPEED,
        REGEN
    }

    private GenType[] DNA = new GenType[MainGame.DNANumber];
    private int[] resume = new int[GenType.values().length];

    public Genetic(){
        setDNA();
        setResume();
    }

    public int AttributeCount(GenType type){
        int count = 0;
        for (int i = 0; i < MainGame.DNANumber; i ++) {
            if (DNA[i] == type) {
                count++;
            }
        }
        return count;
    }

    public void setDNA(){

        for(int i = 0; i < 25; i ++) {
            DNA[i] = GenType.values()[random(0,4)];
        }
    }

    public void setDNA(GenType[] DNA){

        this.DNA =  DNA;

    }

    public void setResume(int[] resume) {
        this.resume = resume;
    }

    public int[] getResume() {
        return resume;
    }

    public GenType[] getDNA(){
        return DNA;
    }

    public void setResume() {

        for(int i = 0; i < 5; i ++) {

            resume[i] = this.AttributeCount(GenType.values()[i]);

        }
    }
}
