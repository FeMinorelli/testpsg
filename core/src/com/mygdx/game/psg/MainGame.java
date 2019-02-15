package com.mygdx.game.psg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.psg.Engine.Actions;
import com.mygdx.game.psg.Engine.Genetic;
import com.mygdx.game.psg.Engine.Bot;
import com.mygdx.game.psg.Engine.History;
import com.mygdx.game.psg.Engine.Population;
import com.mygdx.game.psg.Engine.Save;
import com.mygdx.game.psg.Screens.MenuScreen;
import com.mygdx.game.psg.Screens.PlayScreen;
import com.mygdx.game.psg.Sprites.Unity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



public class MainGame extends Game{


    public static final float M_Width = 3240, M_Height = 1920;
	public static float V_Width = 1080, V_Height = 1920, PPM = 100, W_Width, W_Height;

    public static final int DNANumber = 25;

	public static final int unityNumber = 25;
    public static Genetic[] playerUnits, bot1Units, bot2Units, bot3Units, bot4Units, bot5Units, neutralUnits;

    public static final int min = 25, max = 75, chance = 50000;
    public static final int cooldownAttack = 60 , coolDownUnity = 60;
    public static final float touch = 2.5f;

    public SpriteBatch batch;
    private PlayScreen playScreen;
    private MenuScreen menuScreen;

	//save and load
	public static boolean load, exists;
    public Save saveGame = new Save();

    private Population loadPopulation;
    private History loadHistory;
    private Bot loadBot;
    private Actions loadActions;

    public static boolean[] historiesLoad;
    public static int wins, loses;

	public static ArrayList<Color> colors = new ArrayList<Color>();
	public static boolean altered = true;

    public MainGame() throws IOException {

    }

    public enum Controler{
        MENU,
        COLOR,
        START,
        RESTART
    }
    public static Controler controler = Controler.MENU;

    private File population = new File("Save/population.json");
    private File bot = new File("Save/bot.json");
    private File history = new File("Save/history.json");

	public static boolean win, lose, color;

    @Override
	public void create() {

        colors.add(0, Color.WHITE);
        colors.add(0, Color.GREEN);
        colors.add(0, Color.ROYAL);
        colors.add(0, Color.PURPLE);
        colors.add(0, Color.RED);
        colors.add(0, Color.ORANGE);
        colors.add(0, Color.YELLOW);

        win = false;
        lose = false;
        color = false;
        load = true;

        W_Width = Gdx.graphics.getWidth();
        W_Height = Gdx.graphics.getHeight();

        batch = new SpriteBatch();

        if(population.exists() && bot.exists() && history.exists()){
            exists = true;
        }

        if (exists) {
            loadPopulation = saveGame.GetPopulation();
            loadBot = saveGame.GetBot();
            loadHistory = saveGame.GetHistory();
        } else {
            loadPopulation = new Population();
            loadPopulation.setPopulation();
            saveGame.SavePopulation(loadPopulation);

            loadBot = new Bot();
            loadBot.setBot();
            saveGame.SaveBot(loadBot);

            loadHistory = new History();
            loadHistory.setHistory();
            saveGame.SaveHistory(loadHistory);
        }

        setTeams();
    }

	@Override
	public void render() {

        if(altered) {

            if (win) {
                V_Width += 1080 * 0.001f * wins;
                V_Height += 1920 * 0.001f * wins;
            }

            if (lose) {
                V_Width -= 1080 * 0.001f * loses;
                V_Height -= 1920 * 0.001f * loses;
            }

            if (V_Width < 1080 / 4 || V_Height < 1920 / 4) {
                V_Width = 1080 / 4;
                V_Height = 1920 / 4;
            }

            if (V_Width > 1080 * 4 || V_Height > 1920 * 4) {
                V_Width = 1080 * 4;
                V_Height = 1920 * 4;
            }
        }


        if(altered) {

            if (controler != Controler.START) {
                try {
                    menuScreen = new MenuScreen(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                setScreen(menuScreen);
                altered = false;

            } else {

                playScreen = new PlayScreen(this);
                setScreen(playScreen);
                altered = false;
            }
        }
		super.render();
	}

	private void setTeams(){
        playerUnits = loadPopulation.getPopulation(Unity.Team.PLAYER);
        bot1Units = loadPopulation.getPopulation(Unity.Team.BOT1);
        bot2Units = loadPopulation.getPopulation(Unity.Team.BOT2);
        bot3Units = loadPopulation.getPopulation(Unity.Team.BOT3);
        bot4Units = loadPopulation.getPopulation(Unity.Team.BOT4);
        bot5Units = loadPopulation.getPopulation(Unity.Team.BOT5);
        neutralUnits = loadPopulation.getPopulation(Unity.Team.NEUTRAL);
    }


}
