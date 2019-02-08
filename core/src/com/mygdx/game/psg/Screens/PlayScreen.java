package com.mygdx.game.psg.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.psg.Engine.Actions;
import com.mygdx.game.psg.Engine.Genetic;
import com.mygdx.game.psg.Engine.Bot;
import com.mygdx.game.psg.Engine.Detector;
import com.mygdx.game.psg.Engine.Gesture;
import com.mygdx.game.psg.Engine.History;
import com.mygdx.game.psg.MainGame;
import com.mygdx.game.psg.Sprites.Attack;
import com.mygdx.game.psg.Sprites.Unity;

import java.io.IOException;
import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.badlogic.gdx.math.MathUtils.randomBoolean;
import static com.mygdx.game.psg.Sprites.Unity.Clear;

public class PlayScreen implements Screen{

    //game elements
    public static Stage stage;

    public static Stage unity;
    public static Stage attack;
    public static Stage element;

    public static World world;

    private Box2DDebugRenderer box2DDebugRenderer;
    public static int player, bots, neutral;
    private Vector2 velocity = new Vector2();
    private static Actions actions;
    private MainGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    //load game
    public static Genetic attribute;
    public Bot botAction = new Bot();
    public History history = new History();

    //other variables
    public static boolean oneSelected, oneTarget, oneFire;
    public Actor targetBot = new Actor();
    private static Vector2 sizeViewport;
    public static Vector2 positionCamera;
    public static Unity selectedCell, targetCell;
    public static Unity[] selectedBots = new Unity[6];
    public static ArrayList<Body> contact = new ArrayList<Body>();
    public static float touchRadius, zoom, zoomInit, zoomFinal, attackDirection;
    public static Genetic.GenType typeAttack, botAttack;
    private float camX, camY;
    private static int explosionCount;
    public static int numberAttack, restartCount;
    public static boolean[] botActive = new boolean[5];

    //load textures
    private Texture textureCircle = new Texture("Textures//circle.png");
    private Texture textureSelect = new Texture("Textures//select.png");
    private Texture textureAttack = new Texture("Textures//attack.png");
    private Texture textureCell = new Texture("Textures//cell.png");
    private Texture attackEffect = new Texture("Textures//attackEffect.png");
    private Texture defense = new Texture("Textures//defense.png");
    private Texture speed = new Texture("Textures//speed.png");
    private Texture regen = new Texture("Textures//regen.png");
    private Texture regenWhite = new Texture("Textures//regenWhite.png");
    private Texture size = new Texture("Textures//size.png");
    private Texture sizeWhite = new Texture("Textures//sizeWhite.png");
    private TextureRegion regionAttack = new TextureRegion(attackEffect,512,512);
    private TextureRegion regionDefense = new TextureRegion(defense,512,512);
    private TextureRegion regionSpeed = new TextureRegion(speed,512,512);
    private TextureRegion regionRegen = new TextureRegion(regen,512,512);
    private TextureRegion regionRegenWhite = new TextureRegion(regenWhite,512,512);
    private TextureRegion regionSize = new TextureRegion(size,512,512);
    private TextureRegion regionSizeWhite = new TextureRegion(sizeWhite,512,512);

    public PlayScreen(MainGame game) throws IOException {

        //set zoom
        zoomInit = ((MainGame.W_Width/MainGame.V_Width)+(MainGame.W_Height/MainGame.V_Height))/2;
        zoom = MainGame.V_Width/MainGame.W_Width + MainGame.V_Height/MainGame.W_Height;
        zoomFinal = zoom;
        touchRadius = 100 * zoomInit;
        this.game = game;

        //create camera & create viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(MainGame.W_Width, MainGame.W_Height,camera);
        camera.position.set(0,0,0);
        camX = camera.position.x;
        camY = camera.position.y;
        Gesture gesture = new Gesture();
        Gdx.input.setInputProcessor(gesture);


        //update references
        positionCamera = new Vector2(camera.position.x, camera.position.y);
        sizeViewport = new Vector2();

        //box 2d
        world = new World(new Vector2(0,0), true);
        box2DDebugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new Detector());

        //Other
        botAction.setBot();
        history.setHistory(MainGame.historiesLoad);

        actions = new Actions();
        actions.setActions();

        MainGame.lose = false;
        MainGame.win = false;
    }

    @Override
    public void show() {
        //add units on stage
        stage = new Stage();

        unity = new Stage();
        attack = new Stage();
        element = new Stage();

        newGame();
    }

    @Override
    public void render(float delta) {

        if (Math.abs(zoom - zoomFinal) > 0.005f) {
            zoom = zoom + (zoomFinal - zoom) * delta*2.5f;
        } else {
            zoom = zoomFinal;
        }


        //physics time execute
        world.step(1/60f, 6,2);

        //clear board
        Gdx.gl.glClearColor(0.0f,0.0f,0.0f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Draw body
        Matrix4 matrix4 = game.batch.getProjectionMatrix().cpy().scale(MainGame.PPM, MainGame.PPM, 0);
        //box2DDebugRenderer.render(world, matrix4);
        game.batch.setProjectionMatrix(camera.combined);

        //update players
        stage.act();
        createAttack();
        explosionCount++;

        //count players
        player = 0;
        neutral = 0;
        bots = 0;

        //other updates
      //  if(restartCount == 0) {
            updateCollision();
      //  }
        updateCamera(delta);
        Draw();
        try {
            WinOrLose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height);
        sizeViewport.set(viewport.getScreenWidth(),viewport.getScreenHeight());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        textureCircle.dispose();
        textureSelect.dispose();
        textureAttack.dispose();
        textureCell.dispose();
        world.dispose();
        stage.dispose();
        box2DDebugRenderer.dispose();
    }

    private void updateCamera(float delta){
        //update camera
        if(!Gesture.zoom) {

            if (Gdx.input.isTouched()) {
                camX -= Gdx.input.getDeltaX() * zoom;
                camY += Gdx.input.getDeltaY() * zoom;
            }

            if(Math.abs(camera.position.x - camX) > 0.0005f){
                camera.position.x = camera.position.x  + (camX - camera.position.x)*delta*10f;
            }else{
                camera.position.x = camX;
            }

            if(Math.abs(camera.position.y - camY) > 0.0005f){
                camera.position.y = camera.position.y  + (camY - camera.position.y)*delta*10f;
            }else{
                camera.position.y = camY;
            }


        }else{if(oneSelected){
            Unity.Stop(selectedCell);}}


        if (camera.position.x < MainGame.W_Width * zoom - MainGame.W_Width * zoom / 2 - MainGame.V_Width) {
            camera.position.x = MainGame.W_Width * zoom - MainGame.W_Width * zoom / 2 - MainGame.V_Width;
            camX = camera.position.x;
        }

        if (camera.position.x > -MainGame.W_Width * zoom + MainGame.W_Width * zoom / 2 + MainGame.V_Width) {
            camera.position.x = -MainGame.W_Width * zoom + MainGame.W_Width * zoom / 2 + MainGame.V_Width;
            camX = camera.position.x;
        }

        if (camera.position.y < MainGame.W_Height * zoom - MainGame.W_Height * zoom / 2 - MainGame.V_Height) {
            camera.position.y = MainGame.W_Height * zoom - MainGame.W_Height * zoom / 2 - MainGame.V_Height;
            camY = camera.position.y;
        }

        if (camera.position.y > -MainGame.W_Height * zoom + MainGame.W_Height * zoom / 2 + MainGame.V_Height) {
            camera.position.y = -MainGame.W_Height * zoom + MainGame.W_Height * zoom / 2 + MainGame.V_Height;
            camY = camera.position.y;
        }

        positionCamera.set(camera.position.x, camera.position.y);
        sizeViewport.set(viewport.getScreenWidth(),viewport.getScreenHeight());
        positionCamera.set(camera.position.x, camera.position.y);
        camera.zoom = zoom;
        camera.update();
    }

    private void updateCollision(){

        while(contact.size() > 0){

            Body bodyA = contact.get(0);
            contact.remove(0);
            contact.trimToSize();

            Body bodyB = contact.get(0);
            contact.remove(0);
            contact.trimToSize();

            for(int a = 0; a < stage.getActors().size; a++){

                if (stage.getActors().get(a).getClass() == Unity.class){

                    if (((Unity)stage.getActors().get(a)).body == bodyA || ((Unity)stage.getActors().get(a)).body == bodyB){

                        for (int b = 0; b < stage.getActors().size; b++) {

                            if (stage.getActors().get(b).getClass() == Attack.class) {

                                 if(((Attack)stage.getActors().get(b)).body == bodyA || ((Attack)stage.getActors().get(b)).body == bodyB){

                                        if((stage.getActors().get(a)).getClass() == Unity.class)
                                            Contact(stage.getActors().get(a), stage.getActors().get(b));
                                        else
                                        Contact(stage.getActors().get(b), stage.getActors().get(a));
                                }
                            }
                        }
                    }
                 }
            }
        }
    }

    private static void Contact(Actor actorA, Actor actorB){

        if(actorA.getClass() == Unity.class){

            if(((Unity) actorA).team == ((Attack) actorB).team ){
                if( ((Attack) actorB).type != Genetic.GenType.SIZE){
                ((Unity)actorA).actualEnergy = ((Unity)actorA).actualEnergy + ((Attack)actorB).actualEnergy*0.75f;
                }else{
                ((Unity)actorA).actualEnergy = ((Unity)actorA).actualEnergy + ((Attack)actorB).actualEnergy*0.3f;
                }
            }else {
                ((Unity) actorA).actualEnergy = ((Unity) actorA).actualEnergy - ((Attack) actorB).actualEnergy;
            }

            if(((Unity)actorA).actualEnergy < 0){
                ((Unity)actorA).actualEnergy = (-1)*((Unity)actorA).actualEnergy;
                ((Unity)actorA).team = ((Attack)actorB).team;
            }

            ((Attack)actorB).actualEnergy = ((Attack)actorB).actualEnergy - ((Attack)actorB).actualEnergy*0.3f;
            ((Attack)actorB).modifyEnergy = true;
            ((Attack)actorB).inactivity = 0;

            if(((Attack)actorB).actualEnergy <= ((Attack)actorB).baseAttack){
                ((Attack)actorB).remove = true;
            }

        }else{

            if(((Unity)actorB).team == ((Attack)actorA).team) {
                if(((Attack)actorA).type != Genetic.GenType.SIZE){
                ((Unity)actorB).actualEnergy = ((Unity)actorB).actualEnergy + ((Attack)actorA).actualEnergy * 0.75f;
                }else{
                ((Unity)actorB).actualEnergy = ((Unity)actorB).actualEnergy + ((Attack)actorA).actualEnergy * 0.3f;
                }
            }else {
                ((Unity) actorB).actualEnergy = ((Unity) actorB).actualEnergy - ((Attack) actorA).actualEnergy;
            }

            if(((Unity)actorB).actualEnergy < 0){
                ((Unity)actorB).actualEnergy = (-1)*((Unity)actorB).actualEnergy;
                ((Unity)actorB).team = ((Attack)actorA).team;
            }

            ((Attack)actorA).actualEnergy = ((Attack)actorA).actualEnergy - ((Attack)actorA).actualEnergy*0.3f;
            ((Attack)actorA).modifyEnergy = true;
            ((Attack)actorA).inactivity = 0;

            if(((Attack)actorA).actualEnergy < ((Attack)actorA).baseAttack){
                ((Attack)actorA).remove = true;
            }
        }
    }

    private void WinOrLose() throws IOException {
        //condition win and lose
        if(player == 0 || bots == 0) {
            PlayScreen.zoomFinal = 2f / PlayScreen.zoomInit;
            restartCount++;

            if(restartCount == 1){
                if (bots == 0) {
                    MainGame.win = true;
                }
                if (player == 0) {
                    MainGame.lose = true;
                }
            }


            if(restartCount == 300) {
                oneSelected = false;
                oneTarget = false;
                oneFire = false;
                MainGame.altered = true;
                MainGame.controler = MainGame.Controler.RESTART;
            }

        }else{restartCount = 0;}
    }

    private void newGame() {
        int total = (int) (MainGame.V_Width*MainGame.V_Height/((1080*1920)/32));
        int max = (int) (1 + MainGame.V_Width*MainGame.V_Height/((1080*1920)/4));
        int min = (int) (1 + MainGame.V_Width*MainGame.V_Height/((1080*1920)/2));

        player = 0;
        bots = 0;


            //Player
            int teamCells = 0;
            while (teamCells == 0) {
                teamCells = random(min, max);
            }

            for (int i = 0; i < teamCells; i++) {
                if (i == 0) {
                    attribute = MainGame.playerUnits[random(0,24)];
                    stage.addActor(new Unity(0, 0, Unity.Team.PLAYER));
                    selectedCell = (Unity) stage.getActors().get(0);
                    oneSelected = true;
                } else {
                    attribute = MainGame.playerUnits[random(0,24)];
                    stage.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                            random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.PLAYER));
                }
                player++;
            }

            while (bots == 0 || player == 0) {
                //Bot1
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot1Units[random(0,MainGame.unityNumber - 1)];
                        stage.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT1));
                        bots++;
                    }
                    botActive[0] = true;
                }
                //Bot2
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot2Units[random(0,MainGame.unityNumber - 1)];
                        stage.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT2));
                        bots++;
                    }
                    botActive[1] = true;
                }
                //Bot3
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot3Units[random(0,MainGame.unityNumber - 1)];
                        stage.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT3));
                        bots++;
                    }
                    botActive[2] = true;
                }

                //Bot4
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot4Units[random(0,MainGame.unityNumber - 1)];
                        stage.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT4));
                        bots++;
                    }
                    botActive[3] = true;
                }
                //Bot5
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot5Units[random(0,MainGame.unityNumber - 1)];
                        stage.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT5));
                        bots++;
                    }
                    botActive[4] = true;
                }
            }

            //Neutral
            int teamNeutral = random(total / 4, total / 2);
            for (int i = 0; i < teamNeutral; i++) {
                attribute = MainGame.neutralUnits[random(0,MainGame.unityNumber - 1)];
                stage.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                        random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.NEUTRAL));
            }

    }

    private void createAttack(){


        if(oneSelected && oneTarget && selectedCell.team == Unity.Team.PLAYER || oneFire){

            if(typeAttack == Genetic.GenType.DEFENSIVE){
                actions.AddAction(Unity.Team.PLAYER, typeAttack);
                float angle1 = attackDirection;
                float angle2 = attackDirection;
                stage.addActor(new Attack(selectedCell, targetCell, Genetic.GenType.DEFENSIVE, attackDirection));
                for(int i = 0; i < 2; i++){
                    angle1 += 30;
                    stage.addActor(new Attack(selectedCell, targetCell, Genetic.GenType.DEFENSIVE, angle1));
                    angle2 -= 30;
                    stage.addActor(new Attack(selectedCell, targetCell, Genetic.GenType.DEFENSIVE, angle2));
                }
                selectedCell.actualEnergy = selectedCell.actualEnergy * 0.8f;
                oneFire = false;
                Clear(selectedCell);
            }else{
                stage.addActor(new Attack(selectedCell, targetCell, typeAttack, attackDirection));
                actions.AddAction(Unity.Team.PLAYER, typeAttack);
                Clear(selectedCell);
            }
        }
    }

    private void Explosion(Unity cell){
        int angle = 0;
        explosionCount = 0;

        for(int i = 0; i < 20; i++){

            stage.addActor(new Attack(cell, targetCell, Genetic.GenType.SIZE, angle));
            angle += 18;

        }
        actions.AddAction(cell.team, Genetic.GenType.SIZE);
        cell.team = Unity.Team.NEUTRAL;
        cell.actualEnergy = cell.maxEnergy * 0.75f;
    }

    private void Draw(){
        game.batch.begin();

        //draw selected and target
        DrawInfo();

        //draw light
        for(Actor actor : stage.getActors())
        {
            if(actor.getClass()== Unity.class) {

                DrawLightCell((Unity) actor);
            }
        }

        for(Actor actor : stage.getActors())
        {
            if(actor.getClass()== Attack.class) {
                DrawLightAttack((Attack)actor);
            }

        }

        //draw cell
        for(Actor actor : stage.getActors())
        {
            if(actor.getClass()== Unity.class) {
                if (((Unity)actor).actualEnergy == ((Unity)actor).maxEnergy && explosionCount > 60 ){
                    Explosion((Unity) actor);
                }
                if(((Unity)actor).team == Unity.Team.PLAYER){
                    player++;
                }
                if(((Unity)actor).team == Unity.Team.NEUTRAL){
                    neutral++;
                }
                if(((Unity)actor).team != Unity.Team.PLAYER && ((Unity)actor).team != Unity.Team.NEUTRAL){
                    bots++;
                }

                DrawCell((Unity) actor);
            }
        }

        //draw energy
        for(Actor actor : stage.getActors())
        {
            if(actor.getClass()== Unity.class) {
                if(((Unity)actor).team != Unity.Team.PLAYER && ((Unity)actor).team != Unity.Team.NEUTRAL){
                    BotAction((Unity) actor);
                }

                DrawEnergy((Unity) actor);
            }
        }

        numberAttack = 0;
        for(Actor actor : stage.getActors())
        {
            if(actor.getClass()== Attack.class) {
                if(((Attack)actor).remove) {
                    ((Attack)actor).body.destroyFixture(((Attack)actor).body.getFixtureList().pop());
                    world.destroyBody(((Attack)actor).body);
                    actor.remove();
                }else{
                    numberAttack++;
                    DrawAttack((Attack) actor);
                }
            }
        }

        //draw selected and target
        DrawInfo();

        game.batch.end();
    }

    private void DrawLightAttack(Attack attack){
/*
        game.batch.setColor(
                attack.getColor().r,
                attack.getColor().g,
                attack.getColor().b,
                attack.getColor().a*0.8f);

        game.batch.draw(textureAttack,
                attack.body.getPosition().x* MainGame.PPM - attack.energyRadius*2f,
                attack.body.getPosition().y* MainGame.PPM - attack.energyRadius*2f,
                attack.energyRadius *4,attack.energyRadius *4);
*/
        game.batch.setColor(attack.getColor());

        switch (attack.type){
            case OFFENSIVE:
                game.batch.draw(regionAttack,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 2f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 4f,
                        attack.energyRadius* 4f,
                        1,
                        1,
                        attack.body.getAngle()*180/3.14f);
                break;
            case DEFENSIVE:
                game.batch.draw(regionDefense,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 1.25f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 1.25f,
                        attack.energyRadius * 1.25f,
                        attack.energyRadius * 1.25f,
                        attack.energyRadius * 2.5f,
                        attack.energyRadius* 2.5f,
                        1,
                        1,
                        attack.body.getAngle()*180/3.14f);
                break;
            case SPEED:
                game.batch.draw(regionSpeed,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 2f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 4f,
                        attack.energyRadius * 4f,
                        1,
                        1,
                        attack.body.getAngle()*180/3.14f);
                break;

        }
    }

    private void DrawLightCell(Unity cell){
/*
        game.batch.setColor(
                cell.getColor().r,
                cell.getColor().g,
                cell.getColor().b,
                cell.getColor().a*0.5f);

        //draw energy
        game.batch.draw(textureAttack,
                cell.body.getPosition().x * MainGame.PPM  - (cell.radiusEnergy * 3f),
                cell.body.getPosition().y * MainGame.PPM -  (cell.radiusEnergy * 3f),
                cell.radiusEnergy * 6f,
                cell.radiusEnergy * 6f);
*/
        //draw effects
        game.batch.setColor(
                cell.getColor().r,
                cell.getColor().g,
                cell.getColor().b,
                cell.getColor().a * cell.resume[1]/25);

        game.batch.draw(regionAttack,
                cell.body.getPosition().x * MainGame.PPM - cell.baseRadius * 2f,
                cell.body.getPosition().y * MainGame.PPM - cell.baseRadius * 2f,
                cell.baseRadius * 2f,
                cell.baseRadius * 2f,
                cell.baseRadius * 4f,
                cell.baseRadius * 4f,
                1,
                1,
                cell.body.getAngle()*180/3.14f);

        game.batch.setColor(
                cell.getColor().r,
                cell.getColor().g,
                cell.getColor().b,
                cell.getColor().a * cell.resume[2]/25);

        game.batch.draw(regionDefense,
                cell.body.getPosition().x * MainGame.PPM - cell.baseRadius * 1.25f,
                cell.body.getPosition().y * MainGame.PPM - cell.baseRadius * 1.25f,
                cell.baseRadius * 1.25f,
                cell.baseRadius * 1.25f,
                cell.baseRadius * 2.5f,
                cell.baseRadius * 2.5f,
                1,
                1,
                cell.body.getAngle()*180/3.14f);

        game.batch.setColor(
                cell.getColor().r,
                cell.getColor().g,
                cell.getColor().b,
                cell.getColor().a * cell.resume[3]/25);

        game.batch.draw(regionSpeed,
                cell.body.getPosition().x * MainGame.PPM - cell.baseRadius * 1.75f,
                cell.body.getPosition().y * MainGame.PPM - cell.baseRadius * 1.75f,
                cell.baseRadius * 1.75f,
                cell.baseRadius * 1.75f,
                cell.baseRadius * 3.5f,
                cell.baseRadius * 3.5f,
                1,
                1,
                cell.body.getAngle()*180/3.14f);

    }

    private  void DrawInfo(){

        game.batch.setColor(
                MainGame.colors.get(0).r,
                MainGame.colors.get(0).g,
                MainGame.colors.get(0).b,
                MainGame.colors.get(0).a * 0.8f);

        if(oneSelected){

            //draw select
            /*
            game.batch.setColor(
                    selectedCell.getColor().r,
                    selectedCell.getColor().g,
                    selectedCell.getColor().b,
                    selectedCell.getColor().a*0.3f);

            game.batch.draw(
                    textureAttack,

                    selectedCell.body.getPosition().x* MainGame.PPM - (selectedCell.baseRadius + touchRadius*zoom) * 2f,
                    selectedCell.body.getPosition().y* MainGame.PPM - (selectedCell.baseRadius + touchRadius*zoom) * 2f,
                    (selectedCell.baseRadius + touchRadius*zoom) * 4f,
                    (selectedCell.baseRadius + touchRadius*zoom) * 4f);

            game.batch.setColor(
                    selectedCell.getColor().r,
                    selectedCell.getColor().g,
                    selectedCell.getColor().b,
                    selectedCell.getColor().a*0.8f);
*/
            game.batch.draw(
                    textureSelect,
                    selectedCell.body.getPosition().x* MainGame.PPM - selectedCell.baseRadius - touchRadius,
                    selectedCell.body.getPosition().y* MainGame.PPM - selectedCell.baseRadius - touchRadius,
                    (selectedCell.baseRadius + touchRadius) * 2,
                    (selectedCell.baseRadius + touchRadius) * 2);

        }

        if(oneTarget){
/*
            game.batch.setColor(
                    targetCell.getColor().r,
                    targetCell.getColor().g,
                    targetCell.getColor().b,
                    targetCell.getColor().a*0.3f);

            game.batch.draw(
                    textureAttack,
                    targetCell.body.getPosition().x* MainGame.PPM - (targetCell.baseRadius + touchRadius*zoom) * 2f,
                    targetCell.body.getPosition().y* MainGame.PPM - (targetCell.baseRadius + touchRadius*zoom) * 2f,
                    (targetCell.baseRadius + touchRadius*zoom) * 4,
                    (targetCell.baseRadius + touchRadius*zoom) * 4);
*/

/*
            if(targetCell.team == Unity.Team.NEUTRAL) {
                //draw select
                game.batch.draw(
                        speed,
                        targetCell.body.getPosition().x * MainGame.PPM - (targetCell.baseRadius + touchRadius * zoom),
                        targetCell.body.getPosition().y * MainGame.PPM - (targetCell.baseRadius + touchRadius * zoom),
                        (targetCell.baseRadius + touchRadius * zoom) * 2f,
                        (targetCell.baseRadius + touchRadius * zoom) * 2f);
                        */
      //      }else{
                game.batch.draw(
                        textureSelect,
                        targetCell.body.getPosition().x * MainGame.PPM - (targetCell.baseRadius + touchRadius * zoom),
                        targetCell.body.getPosition().y * MainGame.PPM - (targetCell.baseRadius + touchRadius * zoom),
                        (targetCell.baseRadius + touchRadius * zoom) * 2f,
                        (targetCell.baseRadius + touchRadius * zoom) * 2f);

        //    }

        }
    }

    private void DrawCell(Unity cell){


        game.batch.setColor(cell.getColor());

        //draw cell
        game.batch.draw(
                textureCell,
                cell.body.getPosition().x* MainGame.PPM - cell.baseRadius,
                cell.body.getPosition().y* MainGame.PPM - cell.baseRadius,
                cell.baseRadius *2,
                cell.baseRadius *2);


    }

    private void DrawEnergy(Unity cell){

        game.batch.setColor(cell.getColor());

        game.batch.draw(textureCircle,
                cell.body.getPosition().x * MainGame.PPM - cell.radiusEnergy,
                cell.body.getPosition().y * MainGame.PPM - cell.radiusEnergy,
                cell.radiusEnergy * 2,
                cell.radiusEnergy * 2);

        game.batch.setColor(
                cell.getColor().r,
                cell.getColor().g,
                cell.getColor().b,
                cell.getColor().a * cell.resume[0]/25);

        game.batch.draw(regionSize,
                cell.body.getPosition().x * MainGame.PPM - cell.baseRadius,
                cell.body.getPosition().y * MainGame.PPM - cell.baseRadius,
                cell.baseRadius,
                cell.baseRadius,
                cell.baseRadius * 2f,
                cell.baseRadius * 2f,
                1,
                1,
                cell.body.getAngle()*180/3.14f);

        game.batch.setColor(
                cell.getColor().r,
                cell.getColor().g,
                cell.getColor().b,
                cell.getColor().a * cell.resume[4]/25);

        game.batch.draw(regionRegen,
                cell.body.getPosition().x * MainGame.PPM - cell.radiusEnergy * 0.75f,
                cell.body.getPosition().y * MainGame.PPM - cell.radiusEnergy * 0.75f,
                cell.radiusEnergy * 0.75f,
                cell.radiusEnergy * 0.75f,
                cell.radiusEnergy * 1.5f,
                cell.radiusEnergy * 1.5f,
                1,
                1,
                cell.body.getAngle()*180/3.14f);



    }

    private void DrawAttack(Attack attack){

        game.batch.setColor(attack.getColor());

        game.batch.draw(textureCell,
                attack.body.getPosition().x* MainGame.PPM - attack.energyRadius,
                attack.body.getPosition().y* MainGame.PPM - attack.energyRadius,
                attack.energyRadius *2,attack.energyRadius *2);

        switch (attack.type){
            case SIZE:
/*
                game.batch.draw(regionSizeWhite,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 0.75f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 0.75f,
                        attack.energyRadius * 0.75f,
                        attack.energyRadius * 0.75f,
                        attack.energyRadius * 1.5f,
                        attack.energyRadius * 1.5f,
                        1,
                        1,
                        attack.body.getAngle()*180/3.14f);
/*
                game.batch.draw(regionSize,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 1f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 1f,
                        attack.energyRadius * 1f,
                        attack.energyRadius * 1f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        1,
                        1,
                        attack.body.getAngle()*180/3.14f + 45);

*/
                break;

            case REGEN:

                game.batch.draw(regionRegenWhite,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 0.75f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 0.75f,
                        attack.energyRadius * 0.75f,
                        attack.energyRadius * 0.75f,
                        attack.energyRadius * 1.5f,
                        attack.energyRadius * 1.5f,
                        1,
                        1,
                        attack.body.getAngle()*180/3.14f + 45);
/*
                game.batch.draw(regionRegen,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 1f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 1f,
                        attack.energyRadius * 1f,
                        attack.energyRadius * 1f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        1,
                        1,
                        attack.body.getAngle()*180/3.14f);
*/

                break;
        }


    }

    private void BotAction(Unity bot) {

        targetBot = (stage.getActors().items)[random(0, stage.getActors().size - 1)];

        while(targetBot.getClass() != Unity.class){

            targetBot = (stage.getActors().items)[random(0, stage.getActors().size - 1)];

        }

        botAttack = botAction.getAction(bot);

        if (botAttack != null) {

            switch (botAttack) {

                case OFFENSIVE:
                    if (bot.team != Unity.Team.NEUTRAL) {
                        stage.addActor(new Attack(bot, (Unity) targetBot, botAttack, (((Unity) targetBot).body.getPosition()).sub(bot.body.getPosition()).angle()));
                        actions.AddAction(bot.team, botAttack);
                    }
                    break;
                case SIZE:
                    selectedBots[botIndex(bot.team)] = bot;
                    break;
                case REGEN:
                    if (bot.team != Unity.Team.NEUTRAL) {
                        stage.addActor(new Attack(bot, (Unity) targetBot, botAttack, (((Unity) targetBot).body.getPosition()).sub(bot.body.getPosition()).angle()));
                        actions.AddAction(bot.team, botAttack);
                    }
                    break;
                case SPEED:
                    if (bot.team != Unity.Team.NEUTRAL) {
                        velocity.set(bot.baseMove, bot.baseMove).setAngle(random(0, 360));
                        bot.body.setLinearVelocity(velocity);
                        actions.AddAction(bot.team, botAttack);
                    }
                    break;
                case DEFENSIVE:
                    if (bot.team != Unity.Team.NEUTRAL) {
                        float angle1 = (((Unity) targetBot).body.getPosition()).sub(bot.body.getPosition()).angle();
                        float angle2 = angle1;
                        stage.addActor(new Attack(bot, (Unity) targetBot, Genetic.GenType.DEFENSIVE, angle1));
                        for (int i = 0; i < 2; i++) {

                            angle1 += 30;
                            stage.addActor(new Attack(bot, (Unity) targetBot, Genetic.GenType.DEFENSIVE, angle1));
                            angle2 -= 30;
                            stage.addActor(new Attack(bot, (Unity) targetBot, Genetic.GenType.DEFENSIVE, angle2));

                        }
                        actions.AddAction(bot.team, botAttack);
                        bot.actualEnergy = bot.actualEnergy * 0.8f;
                    }
                    break;


            }
        }
    }

    private int botIndex(Unity.Team team){
        switch (team){
            case BOT1: return 0;
            case BOT2: return 1;
            case BOT3: return 2;
            case BOT4: return 3;
            case BOT5: return 4;
            case NEUTRAL: return 5;
        }

        return 0;
    }
}