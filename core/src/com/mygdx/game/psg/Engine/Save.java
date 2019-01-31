package com.mygdx.game.psg.Engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;

import java.io.IOException;

public class Save {

    Json myjson;
    JsonReader jsonReader;


    public Save() throws IOException {

        myjson = new Json();
        jsonReader = new JsonReader();

    }

    public Population GetPopulation(){

        return myjson.readValue(Population.class, jsonReader.parse(Gdx.files.local("Save/population.json").readString()));

    }

    public boolean SavePopulation(Population population){

        Gdx.files.local("Save/population.json").writeString(myjson.prettyPrint(population), false);

        return true;
    }

    public Bot GetBot(){

        return myjson.readValue(Bot.class, jsonReader.parse(Gdx.files.local("Save/bot.json").readString()));

    }

    public boolean SaveBot(Bot bot){

        Gdx.files.local("Save/bot.json").writeString(myjson.prettyPrint(bot), false);

        return true;
    }

    public History GetHistory(){

        return myjson.readValue(History.class, jsonReader.parse(Gdx.files.local("Save/history.json").readString()));

    }

    public boolean SaveHistory(History history){

        Gdx.files.local("Save/history.json").writeString(myjson.prettyPrint(history), false);

        return true;
    }

    public Actions GetActions(){

        return myjson.readValue(Actions.class, jsonReader.parse(Gdx.files.local("Save/actions.json").readString()));

    }

    public boolean SaveActions(Actions actions){

        Gdx.files.local("Save/actions.json").writeString(myjson.prettyPrint(actions), false);

        return true;
    }
}