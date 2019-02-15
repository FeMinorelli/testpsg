package com.mygdx.game.psg.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.game.psg.Engine.Genetic;
import com.mygdx.game.psg.MainGame;
import com.mygdx.game.psg.Screens.PlayScreen;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.badlogic.gdx.math.MathUtils.randomBoolean;

public class Unity extends Actor {

    public enum Team {
        NEUTRAL,
        PLAYER,
        BOT1,
        BOT2,
        BOT3,
        BOT4,
        BOT5
    }

    public enum Status{
        LOW,
        MID,
        HI
    }

    public Team team;
    public Body body;
    public Genetic DNA;
    public Status status;

    private Vector2 bodyPosition, inputPosition, velocity;
    private float  baseRegeneration;
    public  float baseRadius, radiusEnergy, baseAttack, baseMove, actualEnergy, maxEnergy;
    public int[] resume;
    public int cooldown = MainGame.coolDownUnity;

    public Unity(float x, float y, Team team) {

        bodyPosition = new Vector2();
        inputPosition = new Vector2();
        velocity = new Vector2(0,0);
        if(MainGame.load){
            DNA = PlayScreen.attribute;
        }else {
            DNA = new Genetic();
        }

        // 0 = size, 1 = attack, 2 = defense, 3 = speed, 4 = regen
        this.resume = DNA.getResume();
        baseRadius = 50 + 10f * resume[Genetic.GenType.SIZE.ordinal()];
        maxEnergy = CircleArea(baseRadius);

        baseRegeneration = 1f + resume[Genetic.GenType.REGEN.ordinal()];
        baseAttack = CircleArea(10) + resume[Genetic.GenType.OFFENSIVE.ordinal()] * CircleArea(5);
        baseMove = 1f + resume[Genetic.GenType.SPEED.ordinal()] * 0.25f;

        actualEnergy = maxEnergy * resume[Genetic.GenType.DEFENSIVE.ordinal()] * 0.025f;
        radiusEnergy = baseRadius*RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy);

        this.team = team;
        this.status = Status(this);
        setX(x);
        setY(y);
        setColor();
        setCell();
    }

    private void setCell() {

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX() / MainGame.PPM, getY() / MainGame.PPM);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = PlayScreen.world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(baseRadius / MainGame.PPM);

        fixtureDef.shape = circleShape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 1f;
        body.createFixture(fixtureDef);

        if(team != Team.PLAYER) {
            body.setLinearVelocity(velocity.setToRandomDirection());
        }

        body.setAngularVelocity(random(-2f,2f));
    }

    @Override
    public void act(float delta) {


        if(PlayScreen.oneSelected && PlayScreen.selectedCell.team != Unity.Team.PLAYER){
            PlayScreen.oneSelected = false;
        }

        if(PlayScreen.oneTarget && PlayScreen.targetCell.team == Unity.Team.PLAYER && !PlayScreen.oneSelected){
            PlayScreen.oneTarget = false;
        }

        DelimiterBorder();
        if(PlayScreen.cooldown > 60) {
            SelectOrTarget();
            MoveOrAttack();
        }
        Regeneration(delta);
        setColor();

        if(cooldown < MainGame.coolDownUnity){
            cooldown++;
        }

    }

    private Vector2 BodyPosition(Vector2 bodyPosition) {

        bodyPosition.set(
                            body.getPosition().x * MainGame.PPM
                            + (MainGame.W_Width/2) * PlayScreen.zoom
                            - PlayScreen.positionCamera.x,

                             body.getPosition().y * MainGame.PPM
                            + (MainGame.W_Height/2) * PlayScreen.zoom
                            - PlayScreen.positionCamera.y);

        return bodyPosition;
    }

    private Vector2 InputPosition(Vector2 inputPosition) {

        inputPosition.set(Gdx.input.getX() * PlayScreen.zoom, (MainGame.W_Height - Gdx.input.getY()) * PlayScreen.zoom);

        return inputPosition;
    }

    private boolean isTouched() {

        return (Gdx.input.justTouched() &&
                (BodyPosition(bodyPosition).dst(InputPosition(inputPosition)) < baseRadius ||
                 BodyPosition(bodyPosition).dst(InputPosition(inputPosition)) < baseRadius));
    }

    private void DelimiterBorder() {
        if (body.getPosition().x * MainGame.PPM - baseRadius < -MainGame.V_Width) {
            if(body.getLinearVelocity().x < 0){
            body.setLinearVelocity((-1)*body.getLinearVelocity().x, body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x + baseMove*0.01f, body.getLinearVelocity().y);
        }
        if (body.getPosition().x * MainGame.PPM + baseRadius > MainGame.V_Width) {

            if(body.getLinearVelocity().x > 0){
               body.setLinearVelocity((-1)*body.getLinearVelocity().x, body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x - baseMove*0.01f, body.getLinearVelocity().y);
        }
        if (body.getPosition().y * MainGame.PPM - baseRadius < -MainGame.V_Height) {
            if(body.getLinearVelocity().y < 0){
                body.setLinearVelocity(body.getLinearVelocity().x, (-1)*body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x, body.getLinearVelocity().y + baseMove*0.01f);
        }
        if (body.getPosition().y * MainGame.PPM + baseRadius > MainGame.V_Height) {
            if(body.getLinearVelocity().y > 0){
                body.setLinearVelocity(body.getLinearVelocity().x, (-1)*body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x, body.getLinearVelocity().y - baseMove*0.01f);
        }
    }

    private void SelectOrTarget() {
        if (Gdx.input.justTouched() && isTouched()){
            Select();
            PlayScreen.cooldown = 0;
            if(PlayScreen.oneSelected && PlayScreen.oneTarget && !PlayScreen.oneFire) {

                PlayScreen.attackDirection = PlayScreen.targetCell.body.getPosition().sub(PlayScreen.selectedCell.body.getPosition()).angle();

                if (PlayScreen.targetCell.team == Team.PLAYER) {

                    PlayScreen.typeAttack = Genetic.GenType.REGEN;

                } else {

                    if (PlayScreen.targetCell.team == Team.NEUTRAL) {

                        PlayScreen.typeAttack = Genetic.GenType.SPEED;

                    } else {

                        PlayScreen.typeAttack = Genetic.GenType.OFFENSIVE;

                    }
                }
            }
        }
    }

    private void MoveOrAttack() {

        if(Gdx.input.justTouched() && PlayScreen.oneSelected && PlayScreen.selectedCell == this && team == Team.PLAYER) {
            if(!isTouched()) {
                velocity.set(baseMove, baseMove).setAngle(InputPosition(inputPosition).sub(BodyPosition(bodyPosition)).angle());
                body.setLinearVelocity(velocity);
                if(!PlayScreen.oneTarget) {
                    PlayScreen.attackDirection = InputPosition(inputPosition).sub(BodyPosition(bodyPosition)).angle();
                    PlayScreen.typeAttack = Genetic.GenType.SIZE;
                    PlayScreen.oneMove = true;
                }
            }

            if (InputPosition(inputPosition).dst(BodyPosition(bodyPosition)) > baseRadius &&
                    InputPosition(inputPosition).dst(BodyPosition(bodyPosition)) < (baseRadius * MainGame.touch)) {
                PlayScreen.attackDirection = InputPosition(inputPosition).sub(BodyPosition(bodyPosition)).angle();
                PlayScreen.typeAttack = Genetic.GenType.DEFENSIVE;
                PlayScreen.oneFire = true;
            }
        }
    }

    private static float CircleArea(float radius){

        return radius*radius*3.14f;
    }

    private static float RadiusEnergy(float energy){

        return (float)Math.sqrt(energy*3.14f);
    }

    public static void Clear(Unity unity) {
        if(unity.team == Team.PLAYER) {
            Stop(unity);
            PlayScreen.oneFire = false;
            PlayScreen.oneMove = false;
            PlayScreen.oneTarget = false;

            if(unity.actualEnergy < unity.maxEnergy * 0.05 && PlayScreen.typeAttack != Genetic.GenType.SIZE){
                PlayScreen.oneSelected = false;
            }

            if(PlayScreen.typeAttack == Genetic.GenType.REGEN){
                PlayScreen.oneSelected = true;
                PlayScreen.selectedCell = PlayScreen.targetCell;
            }
        }
    }

    private void Regeneration(float delta){

        if(team == Team.NEUTRAL){
            actualEnergy += (baseRegeneration + PlayScreen.player + PlayScreen.bots - PlayScreen.neutral) * delta;
        }else {
            actualEnergy += (baseRegeneration - PlayScreen.numberAttack) * delta;
        }

        if(actualEnergy > maxEnergy){
            actualEnergy = maxEnergy;
        }

        if(actualEnergy < 0){
            actualEnergy = 0;
        }

        radiusEnergy = baseRadius * RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy);

        if(actualEnergy < maxEnergy * 0.3f){
            status = Status.LOW;
        }else{
            if(actualEnergy > maxEnergy * 0.7f){
                status = Status.HI;
            }else{
                status = Status.MID;
            }
        }
    }

    private void setColor(){
        switch (team){
            case PLAYER: setColor(MainGame.colors.get(0)); break;
            case BOT1: setColor(MainGame.colors.get(1)); break;
            case BOT2: setColor(MainGame.colors.get(2)); break;
            case BOT3: setColor(MainGame.colors.get(3)); break;
            case BOT4: setColor(MainGame.colors.get(4)); break;
            case BOT5: setColor(MainGame.colors.get(5)); break;
            case NEUTRAL: setColor(MainGame.colors.get(6)); break;
        }
    }

    private void Select() {
        if(!PlayScreen.oneSelected && !PlayScreen.oneTarget){
            if(team == Team.PLAYER) {
                PlayScreen.selectedCell = this;
                PlayScreen.oneSelected = true;
                Stop(this);
            }else {
                PlayScreen.oneTarget = true;
                PlayScreen.targetCell = this;
            }
        }else{

        if (PlayScreen.oneSelected && !PlayScreen.oneTarget) {
                if(PlayScreen.selectedCell == this){
                    PlayScreen.oneSelected = false;
                }else{
                    PlayScreen.oneTarget = true;
                    PlayScreen.targetCell = this;
                }
            } else {

                if (!PlayScreen.oneSelected && PlayScreen.oneTarget) {
                    if (PlayScreen.targetCell == this) {
                        PlayScreen.oneTarget = false;
                    } else {
                        if (team == Team.PLAYER) {
                            PlayScreen.selectedCell = this;
                            PlayScreen.oneSelected = true;
                            }
                        else {
                            PlayScreen.targetCell = this;
                            PlayScreen.oneTarget = true;
                        }
                    }
                }
            }
        }
    }

    public static void Stop(Unity unity){
        if(unity.team == Team.PLAYER && PlayScreen.typeAttack != Genetic.GenType.SIZE)
        unity.body.setLinearVelocity(0,0);
        unity.body.setAngularVelocity(0);
    }

    public static Status Status(Unity unity){

        if(unity.actualEnergy < unity.maxEnergy * 0.01f){
            return Status.LOW;
        }

        if(unity.actualEnergy > unity.maxEnergy * 0.8f){
            return Status.HI;
        }

        return Status.MID;
    }

}

