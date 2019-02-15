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
import static com.mygdx.game.psg.Sprites.Unity.Stop;

public class PlayScreen implements Screen{

    //game elements
    //public static Stage stage;

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
    public static boolean oneSelected, oneTarget, oneFire, oneMove;
    public Unity targetBot;
    public Attack attackBot;

    private static Vector2 sizeViewport;
    public static Vector2 positionCamera;
    public static Unity selectedCell, targetCell;
    public static ArrayList<Body> contact = new ArrayList<Body>();
    public static float zoom, zoomInit, zoomFinal, attackDirection;
    public static Genetic.GenType typeAttack, botAttack, botAvoid;
    private float camX, camY;
    private static int explosionCount;
    public static int numberAttack, restartCount, cooldown = 0;
    public static boolean[] botActive = new boolean[5];

    //load textures
    private Texture textureCircle = new Texture("Textures//circle.png");
    private Texture textureSelect = new Texture("Textures//select.png");
    private Texture textureAttack = new Texture("Textures//attack.png");
    private Texture textureCell = new Texture("Textures//cell.png");

    private TextureRegion regionAttack = new TextureRegion(new Texture("Textures//attackEffect.png"),512,512);
    private TextureRegion regionDefense = new TextureRegion(new Texture("Textures//defense.png"),512,512);
    private TextureRegion regionSpeed = new TextureRegion(new Texture("Textures//speed.png"),512,512);
    private TextureRegion regionRegen = new TextureRegion(new Texture("Textures//regen.png"),512,512);
    private TextureRegion regionRegenWhite = new TextureRegion(new Texture("Textures//regenWhite.png"),512,512);
    private TextureRegion regionSize = new TextureRegion(new Texture("Textures//size.png"),512,512);
    private TextureRegion regionSizeWhite = new TextureRegion(new Texture("Textures//sizeWhite.png"),512,512);
    private static TextureRegion textureRegion;

    public PlayScreen(MainGame game) {

        //set zoom
        zoomInit = ((MainGame.W_Width/MainGame.V_Width)+(MainGame.W_Height/MainGame.V_Height))/2;
        zoom = MainGame.V_Width/MainGame.W_Width + MainGame.V_Height/MainGame.W_Height;
        zoomFinal = zoom;
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
        //stage = new Stage();

        unity = new Stage();
        attack = new Stage();
        element = new Stage();

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);

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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Draw body
        Matrix4 matrix4 = game.batch.getProjectionMatrix().cpy().scale(MainGame.PPM, MainGame.PPM, 0);
        //box2DDebugRenderer.render(world, matrix4);
        game.batch.setProjectionMatrix(camera.combined);

        //update players
        //stage.act();
        unity.act();
        attack.act();
        element.act();

        createAttack();
        explosionCount++;

        //count players
        player = 0;
        neutral = 0;
        bots = 0;
        if(cooldown < 60){
            cooldown++;
        }
        cooldown++;

        //other updates
      //  if(restartCount == 0) {

      //  }
        updateCamera(delta);
        Draw();
        try {
            WinOrLose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateCollision();
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
        unity.dispose();
        attack.dispose();
        element.dispose();
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
            Stop(selectedCell);}}




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

        while(contact.size() > 0) {

            int indexUnity = -1;
            int indexAttack = -1;

            Body bodyA = contact.get(0);
            contact.remove(0);
            contact.trimToSize();

            Body bodyB = contact.get(0);
            contact.remove(0);
            contact.trimToSize();

            if(!bodyA.isBullet()) {
                for (int i = 0; i < unity.getActors().size; i++) {
                    if (((Unity)unity.getActors().get(i)).body == bodyA){
                        indexUnity = i;
                        break;
                    }
                }
            }else{
                for (int i = 0; i < attack.getActors().size; i++) {
                    if (((Attack)attack.getActors().get(i)).body == bodyA){
                        indexAttack = i;
                        break;
                    }
                }
            }

            if(!bodyB.isBullet()) {
                for (int i = 0; i < unity.getActors().size; i++) {
                    if (((Unity)unity.getActors().get(i)).body == bodyB){
                        indexUnity = i;
                        break;
                    }
                }
            }else{
                for (int i = 0; i < attack.getActors().size; i++) {
                    if (((Attack)attack.getActors().get(i)).body == bodyB){
                        indexAttack = i;
                        break;
                    }
                }
            }

            if(indexUnity != -1 && indexAttack != -1) {
                Contact(unity.getActors().get(indexUnity), attack.getActors().get(indexAttack));
            }

/*
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

        */
        }
    }

    private static void Contact(Actor actorA, Actor actorB){

        //if(actorA.getClass() == Unity.class){

            if(((Unity) actorA).team == ((Attack) actorB).team ){

                ((Unity)actorA).actualEnergy = ((Unity)actorA).actualEnergy + ((Attack)actorB).actualEnergy * 0.5f;

            }else {
                ((Unity) actorA).actualEnergy = ((Unity) actorA).actualEnergy - ((Attack) actorB).actualEnergy * 0.75f;
            }

            if(((Unity)actorA).actualEnergy < 0){
                ((Unity)actorA).actualEnergy = (-1)*((Unity)actorA).actualEnergy;
                ((Unity)actorA).team = ((Attack)actorB).team;
                ((Unity)actorA).cooldown = 0;
            }

            ((Attack)actorB).actualEnergy = ((Attack)actorB).actualEnergy - ((Attack)actorB).actualEnergy*0.25f;
            ((Attack)actorB).modifyEnergy = true;
            ((Attack)actorB).inactivity = 0;

            if(((Attack)actorB).actualEnergy <= ((Attack)actorB).baseAttack){
                ((Attack)actorB).remove = true;
            }

            ((Attack) actorB).cooldown = 0;
/*
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
                ((Unity)actorB).cooldown = 0;
            }

            ((Attack)actorA).actualEnergy = ((Attack)actorA).actualEnergy - ((Attack)actorA).actualEnergy*0.3f;
            ((Attack)actorA).modifyEnergy = true;
            ((Attack)actorA).inactivity = 0;

            if(((Attack)actorA).actualEnergy < ((Attack)actorA).baseAttack){
                ((Attack)actorA).remove = true;
            }
        }
        */
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
                    unity.addActor(new Unity(0, 0, Unity.Team.PLAYER));
                    selectedCell = (Unity) unity.getActors().get(0);
                    oneSelected = true;
                } else {
                    attribute = MainGame.playerUnits[random(0,24)];
                    unity.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                            random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.PLAYER));
                }
                player++;
            }

            while (bots == 0 || player == 0) {
                //Bot1
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot1Units[random(0,MainGame.unityNumber - 1)];
                        unity.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT1));
                        bots++;
                    }
                    botActive[0] = true;
                }
                //Bot2
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot2Units[random(0,MainGame.unityNumber - 1)];
                        unity.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT2));
                        bots++;
                    }
                    botActive[1] = true;
                }
                //Bot3
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot3Units[random(0,MainGame.unityNumber - 1)];
                        unity.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT3));
                        bots++;
                    }
                    botActive[2] = true;
                }

                //Bot4
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot4Units[random(0,MainGame.unityNumber - 1)];
                        unity.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT4));
                        bots++;
                    }
                    botActive[3] = true;
                }
                //Bot5
                if (randomBoolean()) {
                    for (int i = 0; i < teamCells; i++) {
                        attribute = MainGame.bot5Units[random(0,MainGame.unityNumber - 1)];
                        unity.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                                random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.BOT5));
                        bots++;
                    }
                    botActive[4] = true;
                }
            }

            //Neutral
            int teamNeutral = random(total / 8, total / 2);
            for (int i = 0; i < teamNeutral; i++) {
                attribute = MainGame.neutralUnits[random(0,MainGame.unityNumber - 1)];
                unity.addActor(new Unity(random(-MainGame.V_Width, MainGame.V_Width),
                        random(-MainGame.V_Height, MainGame.V_Height), Unity.Team.NEUTRAL));
            }

    }

    private void createAttack(){

        if(oneSelected && oneTarget && selectedCell.team == Unity.Team.PLAYER || oneFire || oneMove){
            switch (typeAttack){
                case DEFENSIVE:
                    Defense(true, selectedCell);
                    actions.AddAction(Unity.Team.PLAYER, typeAttack);
                    break;
                case SPEED:
                    Speed(true, selectedCell);
                    actions.AddAction(Unity.Team.PLAYER, typeAttack);
                    break;
                case SIZE:
                    attack.addActor(new Attack(selectedCell, typeAttack, attackDirection, 1,false));
                    actions.AddAction(Unity.Team.PLAYER, typeAttack);
                    break;
                case REGEN:
                    attack.addActor(new Attack(selectedCell, typeAttack, attackDirection, 1,false));
                    actions.AddAction(Unity.Team.PLAYER, typeAttack);
                    break;
                case OFFENSIVE:
                    attack.addActor(new Attack(selectedCell, typeAttack, attackDirection, 1,false));
                    actions.AddAction(Unity.Team.PLAYER, typeAttack);
                    break;
            }

            Clear(selectedCell);
            cooldown = 0;

        }
    }

    private void Explosion(Unity unity){
        int angle = 0;
        explosionCount = 0;

        for (int i = 0; i < 10; i++) {

            attack.addActor(new Attack(unity, null, angle, 0, true));
            angle += 36;

        }

        if(unity.team == Unity.Team.NEUTRAL){
            unity.actualEnergy = unity.maxEnergy * 0.25f;
        }else{
        unity.team = Unity.Team.NEUTRAL;
        unity.actualEnergy = unity.maxEnergy * 0.75f;
        }

        unity.cooldown = 0;
    }

    private void Defense(boolean isPlayer, Unity unity){
        float angle;

        if(isPlayer) {
            angle = attackDirection;
        }else {
            angle = (targetBot.body.getPosition()).sub(unity.body.getPosition()).angle();
            actions.AddAction(unity.team, botAttack);
        }

        switch (unity.status){
            case LOW:
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle, 0, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle + 30, 0, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle - 30, 0, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle + 60, 0, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle- 60, 0, false));
                break;
            case MID:
                attack.addActor(new Attack(selectedCell, Genetic.GenType.DEFENSIVE, angle, 1, false));
                attack.addActor(new Attack(selectedCell, Genetic.GenType.DEFENSIVE, angle + 35, 1, false));
                attack.addActor(new Attack(selectedCell, Genetic.GenType.DEFENSIVE, angle - 35, 1, false));
                attack.addActor(new Attack(selectedCell, Genetic.GenType.DEFENSIVE, angle + 70, 1, false));
                attack.addActor(new Attack(selectedCell, Genetic.GenType.DEFENSIVE, angle - 70, 1, false));
                break;
            case HI:
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle, 2, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle + 40, 2, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle - 40, 2, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle + 80, 2, false));
                attack.addActor(new Attack(unity, Genetic.GenType.DEFENSIVE, angle - 80, 2, false));
                break;
        }

            unity.actualEnergy = unity.actualEnergy * 0.8f;

    }

    private void Speed(boolean isPlayer, Unity unity){
        float angle;

        if(isPlayer) {
            angle = attackDirection;
        }else {
            angle = (targetBot.body.getPosition()).sub(unity.body.getPosition()).angle();
            actions.AddAction(unity.team, botAttack);
        }

            switch (unity.status){
                case LOW:
                    attack.addActor(new Attack(unity, Genetic.GenType.SPEED, angle, 1, false));
                    attack.addActor(new Attack(unity, Genetic.GenType.SPEED, angle, -1, false));
                    break;
                case MID:
                    attack.addActor(new Attack(selectedCell, Genetic.GenType.SPEED, angle, 2, false));
                    attack.addActor(new Attack(selectedCell, Genetic.GenType.SPEED, angle, -2, false));
                    break;
                case HI:
                    attack.addActor(new Attack(unity, Genetic.GenType.SPEED, angle, 3, false));
                    attack.addActor(new Attack(unity, Genetic.GenType.SPEED, angle, -3, false));
                    break;
            }

        unity.actualEnergy = unity.actualEnergy * 0.7f;

    }

    private void Draw(){
        game.batch.begin();

        //draw selected and target
        DrawInfo();

        //draw selected and target
        //DrawInfo();

        //draw light
        for(Actor actor : unity.getActors()) {

                DrawLightCell((Unity) actor);
        }

        for(Actor actor : attack.getActors()) {

                DrawLightAttack((Attack)actor);
        }

        //draw cell
        for(Actor actor : unity.getActors())
        {
            if (((Unity)actor).actualEnergy == ((Unity)actor).maxEnergy && explosionCount > 60 ){
                Explosion((Unity) actor);
            }

            switch (((Unity)actor).team){
                case NEUTRAL: neutral++;
                    break;
                case PLAYER: player++;
                    break;
                case BOT1: bots++; BotAction((Unity) actor);
                    break;
                case BOT2: bots++; BotAction((Unity) actor);
                    break;
                case BOT3: bots++; BotAction((Unity) actor);
                    break;
                case BOT4: bots++; BotAction((Unity) actor);
                    break;
                case BOT5: bots++; BotAction((Unity) actor);
                    break;
            }

            DrawCell((Unity) actor);
            DrawEnergy((Unity) actor);

        }

        numberAttack = 0;
        for(Actor actor : attack.getActors())
        {
            if(((Attack)actor).remove) {
                ((Attack)actor).body.destroyFixture(((Attack)actor).body.getFixtureList().pop());
                world.destroyBody(((Attack)actor).body);
                actor.remove();
            }else{
                numberAttack++;
                DrawAttack((Attack) actor);
            }
        }

        game.batch.end();
    }

    private  void DrawInfo(){

        if(oneSelected){
/*
            game.batch.setColor(
                    selectedCell.getColor().r,
                    selectedCell.getColor().g,
                    selectedCell.getColor().b,
                    selectedCell.actualEnergy/selectedCell.maxEnergy
            );


            game.batch.draw(
                    textureSelect,
                    selectedCell.body.getPosition().x* MainGame.PPM - (selectedCell.baseRadius * MainGame.touch),
                    selectedCell.body.getPosition().y* MainGame.PPM - (selectedCell.baseRadius * MainGame.touch),
                    (selectedCell.baseRadius * 2 * MainGame.touch),
                    (selectedCell.baseRadius * 2 * MainGame.touch));
*/
            if(cooldown < 60){

                game.batch.setColor(
                        selectedCell.getColor().r,
                        selectedCell.getColor().g,
                        selectedCell.getColor().b,
                        selectedCell.getColor().a * (60 - cooldown)/60);

                DrawGen(selectedCell, Genetic.GenType.OFFENSIVE, true);
                DrawGen(selectedCell, Genetic.GenType.DEFENSIVE, true);
                DrawGen(selectedCell, Genetic.GenType.SIZE, true);
                DrawGen(selectedCell, Genetic.GenType.SPEED, true);
                DrawGen(selectedCell, Genetic.GenType.REGEN, true);

            }

            DrawGen(selectedCell, Genetic.GenType.OFFENSIVE, false);
            DrawGen(selectedCell, Genetic.GenType.DEFENSIVE, false);
            DrawGen(selectedCell, Genetic.GenType.SIZE, false);
            DrawGen(selectedCell, Genetic.GenType.SPEED, false);
            DrawGen(selectedCell, Genetic.GenType.REGEN, false);

        }

        if(oneTarget){
/*
            game.batch.setColor(
                    targetCell.getColor().r,
                    targetCell.getColor().g,
                    targetCell.getColor().b,
                    targetCell.actualEnergy/targetCell.maxEnergy);

            game.batch.draw(
                    textureSelect,
                    targetCell.body.getPosition().x * MainGame.PPM - (targetCell.baseRadius * MainGame.touch),
                    targetCell.body.getPosition().y * MainGame.PPM - (targetCell.baseRadius * MainGame.touch),
                    (targetCell.baseRadius * MainGame.touch * 2),
                    (targetCell.baseRadius * MainGame.touch * 2));
*/
            if(cooldown < 60){

                game.batch.setColor(
                        targetCell.getColor().r,
                        targetCell.getColor().g,
                        targetCell.getColor().b,
                        targetCell.getColor().a * (60 - cooldown)/60);

                DrawGen(targetCell, Genetic.GenType.OFFENSIVE, true);
                DrawGen(targetCell, Genetic.GenType.DEFENSIVE, true);
                DrawGen(targetCell, Genetic.GenType.SIZE, true);
                DrawGen(targetCell, Genetic.GenType.SPEED, true);
                DrawGen(targetCell, Genetic.GenType.REGEN, true);

            }

            DrawGen(targetCell, Genetic.GenType.OFFENSIVE, false);
            DrawGen(targetCell, Genetic.GenType.DEFENSIVE, false);
            DrawGen(targetCell, Genetic.GenType.SIZE, false);
            DrawGen(targetCell, Genetic.GenType.SPEED, false);
            DrawGen(targetCell, Genetic.GenType.REGEN, false);
        }
    }

    private void DrawLightAttack(Attack attack){

        //draw effects
        if(attack.cooldown < MainGame.cooldownAttack){

            game.batch.setColor(
                    attack.getColor().r,
                    attack.getColor().g,
                    attack.getColor().b,
                    attack.getColor().a * (MainGame.cooldownAttack - attack.cooldown)/(MainGame.cooldownAttack * 2));

            game.batch.draw(
                    textureCircle,
                    attack.body.getPosition().x* MainGame.PPM - attack.energyRadius,
                    attack.body.getPosition().y* MainGame.PPM - attack.energyRadius,
                    attack.energyRadius * 2,
                    attack.energyRadius * 2);

            game.batch.draw(
                    textureAttack,
                    attack.body.getPosition().x* MainGame.PPM - attack.energyRadius * 4,
                    attack.body.getPosition().y* MainGame.PPM - attack.energyRadius * 4,
                    attack.energyRadius * 8,
                    attack.energyRadius * 8);
        }

        if(attack.type != null){

        game.batch.setColor(attack.getColor());

        switch (attack.type) {
            case OFFENSIVE:
                game.batch.draw(regionAttack,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 2f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 2f,
                        attack.energyRadius * 4f,
                        attack.energyRadius * 4f,
                        1,
                        1,
                        attack.body.getAngle() * 180 / 3.14f);
                break;
            case DEFENSIVE:
                game.batch.draw(regionDefense,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 1.25f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 1.25f,
                        attack.energyRadius * 1.25f,
                        attack.energyRadius * 1.25f,
                        attack.energyRadius * 2.5f,
                        attack.energyRadius * 2.5f,
                        1,
                        1,
                        attack.body.getAngle() * 180 / 3.14f);
                break;
            case SPEED:
                game.batch.draw(regionSpeed,
                        attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 1.4f,
                        attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 1.4f,
                        attack.energyRadius * 1.4f,
                        attack.energyRadius * 1.4f,
                        attack.energyRadius * 2.8f,
                        attack.energyRadius * 2.8f,
                        1,
                        1,
                        attack.body.getAngle() * 180 / 3.14f);
                break;
            }
        }
    }

    private void DrawLightCell(Unity unity){

        //draw effects
        if(unity.cooldown < MainGame.coolDownUnity){

            game.batch.setColor(
                    unity.getColor().r,
                    unity.getColor().g,
                    unity.getColor().b,
                    unity.getColor().a * (MainGame.coolDownUnity - unity.cooldown)/(MainGame.coolDownUnity*2));

            game.batch.draw(
                    textureAttack,
                    unity.body.getPosition().x* MainGame.PPM - (unity.baseRadius + unity.baseRadius) * 2,
                    unity.body.getPosition().y* MainGame.PPM - (unity.baseRadius + unity.baseRadius) * 2,
                    (unity.baseRadius + unity.baseRadius) * 4,
                    (unity.baseRadius + unity.baseRadius) * 4);
        }

        /*
        if(unity.resume[Genetic.GenType.OFFENSIVE.ordinal()] > 5) {


            game.batch.setColor(
                    unity.getColor().r,
                    unity.getColor().g,
                    unity.getColor().b,
                    unity.getColor().a * unity.resume[Genetic.GenType.OFFENSIVE.ordinal()] / 25);

            game.batch.draw(regionAttack,
                    unity.body.getPosition().x * MainGame.PPM - unity.baseRadius * 2f,
                    unity.body.getPosition().y * MainGame.PPM - unity.baseRadius * 2f,
                    unity.baseRadius * 2f,
                    unity.baseRadius * 2f,
                    unity.baseRadius * 4f,
                    unity.baseRadius * 4f,
                    1,
                    1,
                    unity.body.getAngle() * 180 / 3.14f);
        }

        if(unity.resume[Genetic.GenType.DEFENSIVE.ordinal()] > 5) {

            game.batch.setColor(
                    unity.getColor().r,
                    unity.getColor().g,
                    unity.getColor().b,
                    unity.getColor().a * unity.resume[Genetic.GenType.DEFENSIVE.ordinal()] / 25);

            game.batch.draw(regionDefense,
                    unity.body.getPosition().x * MainGame.PPM - unity.baseRadius * 1.25f,
                    unity.body.getPosition().y * MainGame.PPM - unity.baseRadius * 1.25f,
                    unity.baseRadius * 1.25f,
                    unity.baseRadius * 1.25f,
                    unity.baseRadius * 2.5f,
                    unity.baseRadius * 2.5f,
                    1,
                    1,
                    unity.body.getAngle() * 180 / 3.14f);

        }

        if(unity.resume[Genetic.GenType.SPEED.ordinal()] > 5) {

            game.batch.setColor(
                    unity.getColor().r,
                    unity.getColor().g,
                    unity.getColor().b,
                    unity.getColor().a * unity.resume[Genetic.GenType.SPEED.ordinal()] / 25);

            game.batch.draw(regionSpeed,
                    unity.body.getPosition().x * MainGame.PPM - unity.baseRadius * 1.4f,
                    unity.body.getPosition().y * MainGame.PPM - unity.baseRadius * 1.4f,
                    unity.baseRadius * 1.4f,
                    unity.baseRadius * 1.4f,
                    unity.baseRadius * 2.8f,
                    unity.baseRadius * 2.8f,
                    1,
                    1,
                    unity.body.getAngle() * 180 / 3.14f);
        }
        */

        DrawGen(unity, Genetic.GenType.OFFENSIVE);
        DrawGen(unity, Genetic.GenType.DEFENSIVE);
        DrawGen(unity, Genetic.GenType.SPEED);

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

    private void DrawEnergy(Unity unity){

        game.batch.setColor(unity.getColor());

        game.batch.draw(textureCircle,
                unity.body.getPosition().x * MainGame.PPM - unity.radiusEnergy,
                unity.body.getPosition().y * MainGame.PPM - unity.radiusEnergy,
                unity.radiusEnergy * 2,
                unity.radiusEnergy * 2);
/*
        if(unity.resume[Genetic.GenType.SIZE.ordinal()] >= 5) {

            game.batch.setColor(
                    unity.getColor().r,
                    unity.getColor().g,
                    unity.getColor().b,
                    unity.getColor().a * unity.resume[Genetic.GenType.SIZE.ordinal()] / 25);

            game.batch.draw(regionSize,
                    unity.body.getPosition().x * MainGame.PPM - unity.baseRadius,
                    unity.body.getPosition().y * MainGame.PPM - unity.baseRadius,
                    unity.baseRadius,
                    unity.baseRadius,
                    unity.baseRadius * 2f,
                    unity.baseRadius * 2f,
                    1,
                    1,
                    unity.body.getAngle() * 180 / 3.14f);

        }


        if(unity.resume[Genetic.GenType.REGEN.ordinal()] >= 5) {

            game.batch.setColor(
                    unity.getColor().r,
                    unity.getColor().g,
                    unity.getColor().b,
                    unity.getColor().a * unity.resume[Genetic.GenType.REGEN.ordinal()] / 25);

            game.batch.draw(regionRegen,
                    unity.body.getPosition().x * MainGame.PPM - unity.radiusEnergy * 0.75f,
                    unity.body.getPosition().y * MainGame.PPM - unity.radiusEnergy * 0.75f,
                    unity.radiusEnergy * 0.75f,
                    unity.radiusEnergy * 0.75f,
                    unity.radiusEnergy * 1.5f,
                    unity.radiusEnergy * 1.5f,
                    1,
                    1,
                    unity.body.getAngle() * 180 / 3.14f);
        }
*/

        DrawGen(unity, Genetic.GenType.SIZE);
        DrawGen(unity, Genetic.GenType.REGEN);

    }

    private void DrawAttack(Attack attack){

        game.batch.setColor(attack.getColor());

  /*      if(attack.type == null){

            game.batch.draw(textureAttack,
                    attack.body.getPosition().x* MainGame.PPM - attack.energyRadius * 2,
                    attack.body.getPosition().y* MainGame.PPM - attack.energyRadius * 2,
                    attack.energyRadius *4,attack.energyRadius *4);

        }else{*/
            game.batch.draw(textureAttack,
                    attack.body.getPosition().x* MainGame.PPM - attack.energyRadius * 1f,
                    attack.body.getPosition().y* MainGame.PPM - attack.energyRadius * 1f,
                    attack.energyRadius *2,attack.energyRadius *2);
   //     }


        if(attack.type != null) {
            switch (attack.type) {

                case REGEN:
                    game.batch.draw(regionRegen,
                            attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 0.75f,
                            attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 0.75f,
                            attack.energyRadius * 0.75f,
                            attack.energyRadius * 0.75f,
                            attack.energyRadius * 1.5f,
                            attack.energyRadius * 1.5f,
                            1,
                            1,
                            attack.body.getAngle() * 180 / 3.14f + 45);

                    break;

                case SIZE:
                    game.batch.draw(regionSize,
                            attack.body.getPosition().x * MainGame.PPM - attack.energyRadius * 1f,
                            attack.body.getPosition().y * MainGame.PPM - attack.energyRadius * 1f,
                            attack.energyRadius * 1f,
                            attack.energyRadius * 1f,
                            attack.energyRadius * 2f,
                            attack.energyRadius * 2f,
                            1,
                            1,
                            attack.body.getAngle() * 180 / 3.14f + 45);
                    break;
            }
        }
    }

    private void DrawGen(Unity unity, Genetic.GenType genType){

        if((oneSelected && selectedCell == unity) || (oneTarget && targetCell == unity)){

        }else{

            float adjust = 1;
            float energy = 0;
            float genCount = unity.resume[genType.ordinal()];

           // if (genCount > 5) {

                switch (genType) {
                    case SIZE:
                        adjust = 1f;
                        textureRegion = regionSize;
                        break;
                    case SPEED:
                        adjust = 1.4f;
                        textureRegion = regionSpeed;
                        break;
                    case OFFENSIVE:
                        adjust = 2;
                        textureRegion = regionAttack;
                        break;
                    case DEFENSIVE:
                        adjust = 1.25f;
                        textureRegion = regionDefense;
                        break;
                    case REGEN:
                        adjust = 0.75f;
                        textureRegion = regionRegen;
                        energy = unity.baseRadius - unity.radiusEnergy;
                        break;
                }

                game.batch.setColor(
                        unity.getColor().r,
                        unity.getColor().g,
                        unity.getColor().b,
                        unity.getColor().a * genCount / 25);

                game.batch.draw(textureRegion,
                        unity.body.getPosition().x * MainGame.PPM - (unity.baseRadius - energy) * adjust,
                        unity.body.getPosition().y * MainGame.PPM - (unity.baseRadius - energy) * adjust,
                        (unity.baseRadius - energy) * adjust,
                        (unity.baseRadius - energy) * adjust,
                        (unity.baseRadius - energy) * adjust * 2,
                        (unity.baseRadius - energy) * adjust * 2,
                        1,
                        1,
                        unity.body.getAngle() * 180 / 3.14f);
            }
       // }
    }

    private void DrawGen(Attack attack, Genetic.GenType genType){

        float adjust;

        switch (genType){
            case SIZE: adjust = 1f; break;
            case SPEED: adjust = 1.4f; break;
            case OFFENSIVE: adjust = 2; break;
            case DEFENSIVE: adjust = 1.25f; break;
            case REGEN: adjust = 0.75f; break;
        }

    }

    private void DrawGen(Unity unity, Genetic.GenType genType, boolean coolDown){

        float adjust = 1;
        float genCount = unity.resume[genType.ordinal()];

        //if (genCount > 5) {

            switch (genType) {
                case SIZE:
                    adjust = 0.85f;
                    textureRegion = regionSizeWhite;
                    break;
                case SPEED:
                    adjust = 1.15f;
                    textureRegion = regionSpeed;
                    break;
                case OFFENSIVE:
                    adjust = 1.6f;
                    textureRegion = regionAttack;
                    break;
                case DEFENSIVE:
                    adjust = 1f;
                    textureRegion = regionDefense;
                    break;
                case REGEN:
                    adjust = 0.7f;
                    textureRegion = regionRegenWhite;
                    break;
            }

        if(!coolDown) {

            game.batch.setColor(
                    unity.getColor().r,
                    unity.getColor().g,
                    unity.getColor().b,
                    unity.getColor().a * genCount / 25);
        }



        game.batch.draw(textureRegion,
                unity.body.getPosition().x * MainGame.PPM - (unity.baseRadius * MainGame.touch) * adjust,
                unity.body.getPosition().y * MainGame.PPM - (unity.baseRadius * MainGame.touch) * adjust,
                (unity.baseRadius * MainGame.touch) * adjust,
                (unity.baseRadius * MainGame.touch) * adjust,
                (unity.baseRadius * MainGame.touch) * adjust * 2,
                (unity.baseRadius * MainGame.touch) * adjust * 2,
                1,
                1,
                unity.body.getAngle() * 180 / 3.14f);

    }

    private void BotAction(Unity bot) {
        boolean fire = false;

        targetBot = (Unity)(unity.getActors().items)[random(0, unity.getActors().size - 1)];
        botAttack = botAction.getAttack(bot, targetBot);

        if(attack.getActors().size > 0) {
            attackBot = (Attack) (attack.getActors().items)[random(0, attack.getActors().size - 1)];
            botAvoid = botAction.getAvoid(bot, attackBot);
        }

        if (botAttack != null) {

            switch (botAttack) {

                case OFFENSIVE:
                    attack.addActor(new Attack(bot, botAttack, (targetBot.body.getPosition()).sub(bot.body.getPosition()).angle(),0,false));
                    actions.AddAction(bot.team, botAttack);
                    fire = true;
                    break;
                case REGEN:
                    attack.addActor(new Attack(bot, botAttack, (targetBot.body.getPosition()).sub(bot.body.getPosition()).angle(),0,false));
                    actions.AddAction(bot.team, botAttack);
                    fire = true;
                    break;
                case SPEED:
                    Speed(false, bot);
                    actions.AddAction(bot.team, botAttack);
                    fire = true;
                    break;
            }


                if (botAvoid != null && !fire) {
                    switch (botAvoid) {
                        case DEFENSIVE:
                            Defense(false, bot);
                            actions.AddAction(bot.team, botAvoid);
                            break;
                        case SIZE:
                            velocity.set(bot.baseMove, bot.baseMove);
                            bot.body.setLinearVelocity(velocity.setAngle((targetBot.body.getPosition()).sub(bot.body.getPosition()).angle()));
                            attack.addActor(new Attack(bot, botAvoid, (targetBot.body.getPosition()).sub(bot.body.getPosition()).angle(), 0,false));
                            actions.AddAction(bot.team, botAvoid);
                            break;
                    }
                }

        }
    }
}