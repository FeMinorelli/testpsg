package com.mygdx.game.psg.Sprites;

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

public class Attack extends Actor {


    public Unity.Team team;
    public Body body;
    private Vector2 velocity = new Vector2(0,0);
    public Genetic.GenType type;

    public float baseAttack,maxEnergy, actualEnergy, energyRadius, baseRadius, baseMove;
    public boolean remove, modifyEnergy, fixVelocity, explosion;
    private int modify;
    public int cooldown = MainGame.cooldownAttack;

    public int inactivity;

    private CircleShape circleShape = new CircleShape();
    private FixtureDef fixtureDef = new FixtureDef();

    int angle = 0;
    boolean typeIncrement = randomBoolean();

    public Attack(Unity unity, Genetic.GenType type, float angle, int modify, boolean explosion){
        baseMove = unity.baseMove*2;
        baseAttack = unity.baseAttack;
        baseRadius = unity.baseRadius;
        maxEnergy = unity.maxEnergy;
        fixVelocity = false;
        setColor(unity.getColor());
        this.explosion = explosion;
        this.modify = modify;
        this.team = unity.team;
        this.type = type;

        if(explosion){
            actualEnergy = baseAttack + unity.maxEnergy / 10;
            energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
        }else {

            switch (type) {
                case SIZE:
                    actualEnergy = baseAttack + unity.actualEnergy* 0.1f;
                    unity.actualEnergy = unity.actualEnergy * 0.9f;
                    energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
                    break;
                case DEFENSIVE:
                    actualEnergy = baseAttack + unity.actualEnergy * 0.05f;
                    energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
                    break;
                case SPEED:
                    actualEnergy = baseAttack + unity.actualEnergy * 0.15f;
                    energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
                    break;
                case REGEN:
                    actualEnergy = baseAttack + unity.actualEnergy * 0.4f;
                    unity.actualEnergy = unity.actualEnergy * 0.6f;
                    energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
                    break;
                case OFFENSIVE:
                    actualEnergy =  unity.actualEnergy * 0.5f;
                    unity.actualEnergy = unity.actualEnergy * 0.5f;
                    energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
                    break;
            }
        }

        setAttack(unity, angle);
    }

    private void setAttack(Unity unity, float angle){
        if(explosion){
            velocity.set(unity.baseRadius + energyRadius, energyRadius/10).setAngle(angle);
            SetBody(unity, velocity.x, velocity.y, angle);
        }else{

            switch (type) {
                case SIZE:
                    velocity.set(unity.baseRadius + energyRadius,  energyRadius/10).setAngle(angle - 180);
                    SetBody(unity, velocity.x, velocity.y, angle - 180);
                    fixVelocity = true;
                    break;
                case DEFENSIVE:
                    velocity.set(unity.baseRadius + energyRadius,  energyRadius/10).setAngle(angle);
                    SetBody(unity, velocity.x, velocity.y, angle);
                    fixVelocity = true;
                    break;
                case SPEED:
                    velocity.set(unity.baseRadius + energyRadius,  energyRadius/10).setAngle(angle + 20*modify);
                    SetBody(unity, velocity.x, velocity.y, angle);
                    break;
                case OFFENSIVE:
                    velocity.set(unity.baseRadius + energyRadius,  energyRadius/10).setAngle(angle);
                    SetBody(unity, velocity.x, velocity.y, angle);
                    break;
                case REGEN:
                    velocity.set(unity.baseRadius + energyRadius,  energyRadius/10).setAngle(angle);
                    SetBody(unity, velocity.x, velocity.y, angle);
                    fixVelocity = true;
                    break;

            }
        }

        body.setAngularVelocity(random(-5f,5f));
    }

    @Override
    public void act(float delta) {
        if(fixVelocity){
            body.setLinearVelocity(velocity);
        }

        DelimiterBorder();
        RefactorEnergy(delta);
        RefactorFixture();

        if(cooldown < MainGame.cooldownAttack){
            cooldown++;
        }

        if(type == Genetic.GenType.OFFENSIVE && !remove) {

            if (angle == 60 || angle == -60) {
                typeIncrement = !typeIncrement;
            }

            body.setLinearVelocity(body.getLinearVelocity().x * 1.0025f, body.getLinearVelocity().y * 1.0025f);

            if (typeIncrement) {
                body.setLinearVelocity(body.getLinearVelocity().setAngle(body.getLinearVelocity().angle() + 5));
                angle += 5;
            } else {
                body.setLinearVelocity(body.getLinearVelocity().setAngle(body.getLinearVelocity().angle() - 5));
                angle -= 5;
            }
        }
    }



    private FixtureDef SetFixtureDef(){

        circleShape.setRadius((baseRadius * RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy)) /MainGame.PPM);
        fixtureDef.shape = circleShape;

        if(explosion){
            fixtureDef.density = 0.1f;
            fixtureDef.friction = 0.1f;
            fixtureDef.restitution = 0.1f;
        }else {

            switch (type) {
                case REGEN:
                    fixtureDef.density = 0.2f;
                    fixtureDef.friction = 0.2f + modify;
                    fixtureDef.restitution = 0.2f;
                    break;
                case OFFENSIVE:
                    fixtureDef.density = 1f;
                    fixtureDef.friction = 1f;
                    fixtureDef.restitution = 1f;
                    break;
                case SPEED:
                    fixtureDef.density = 0.1f;
                    if(modify > 0) {
                        fixtureDef.friction = 1f - 0.3f * modify;
                        fixtureDef.restitution = 1f + 0.1f * modify;
                    }else{
                        fixtureDef.friction = 1f + 0.3f * modify;
                        fixtureDef.restitution = 1f - 0.1f * modify;
                    }
                    break;
                case DEFENSIVE:
                    if(modify > 0) {
                        fixtureDef.density = 1f + modify;
                        fixtureDef.friction = 1f + modify;
                    }else{
                        fixtureDef.density = 1f - modify;
                        fixtureDef.friction = 1f - modify;
                    }
                    fixtureDef.restitution = 0.1f;
                    break;
                case SIZE:
                    fixtureDef.density = 1.5f;
                    fixtureDef.friction = 0.1f;
                    fixtureDef.restitution = 1f;
                    break;
            }
        }

        return fixtureDef;
    }

    private  void SetBody(Unity unity, float x, float y, float angle){
        BodyDef bodyDef = new BodyDef();

        bodyDef.position.set(unity.body.getPosition().x + x/MainGame.PPM,
                unity.body.getPosition().y + y/MainGame.PPM);

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = PlayScreen.world.createBody(bodyDef);


        body.createFixture(SetFixtureDef());
        body.setBullet(true);

        if(explosion){
            velocity.set(baseMove * 4, baseMove * 4).setAngle(angle);
            body.setLinearVelocity(velocity);
        }else {
            switch (type) {
                case SIZE:
                    velocity.set(0,0);
                    body.setLinearVelocity(velocity);
                    break;
                case DEFENSIVE:
                    if(modify < 0){
                        velocity.set(baseMove / (6 + modify * 2), baseMove / (6 + modify * 2)).setAngle(angle);
                    }else{
                        velocity.set(baseMove / (6 - modify * 2), baseMove / (6 - modify * 2)).setAngle(angle);
                    }

                    body.setLinearVelocity(velocity);
                    break;
                case SPEED:
                    if(modify < 0){
                        velocity.set(baseMove * 2 * (-1)*modify, baseMove * 2 * (-1)*modify).setAngle(angle);
                    }else{
                        velocity.set(baseMove * 2*modify, baseMove * 2*modify).setAngle(angle);
                    }

                    body.setLinearVelocity(velocity);
                    break;
                case OFFENSIVE:
                    velocity.set(baseMove, baseMove).setAngle(angle);
                    body.setLinearVelocity(velocity);
                    break;
                case REGEN:
                    velocity.set(baseMove / 2, baseMove / 2).setAngle(angle);
                    body.setLinearVelocity(velocity);
                    break;
            }
        }
    }

    private float RadiusEnergy(float energy){

        return (float)Math.sqrt(energy*(float)Math.PI);
    }

    private void RefactorFixture(){
            body.destroyFixture(body.getFixtureList().pop());
            body.createFixture(SetFixtureDef());
            energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
    }

    private void RefactorEnergy(float delta){

        inactivity++;

        if(explosion){
            if (energyRadius < maxEnergy/2 && inactivity < 25) {
                actualEnergy = actualEnergy + maxEnergy * 0.01f * delta;
            }else{
                actualEnergy = actualEnergy - 60 * PlayScreen.numberAttack*delta;
            }
        }else {

            switch (type) {
                case SPEED:
                    if (energyRadius < maxEnergy/2 && inactivity < 50) {
                        actualEnergy = actualEnergy + maxEnergy * 0.04f * delta;
                    }else{
                        actualEnergy = actualEnergy - 10 * PlayScreen.numberAttack*delta;
                    }
                    break;
                case OFFENSIVE:
                    if (energyRadius < maxEnergy/2 && inactivity < 100) {
                        actualEnergy = actualEnergy + maxEnergy * 0.03f  * delta;
                    }else{
                        actualEnergy = actualEnergy - 30 * PlayScreen.numberAttack*delta;
                    }
                    break;
                case REGEN:
                    if (energyRadius < maxEnergy/2 && inactivity < 200) {
                        actualEnergy = actualEnergy + maxEnergy * 0.06f * delta;
                    }else{
                        actualEnergy = actualEnergy - 40 * PlayScreen.numberAttack*delta;
                    }
                    break;
                case DEFENSIVE:
                    if (energyRadius < maxEnergy/2 && inactivity < 150) {
                        actualEnergy = actualEnergy + maxEnergy * 0.02f * delta;
                    }else{
                        actualEnergy = actualEnergy - 20 * PlayScreen.numberAttack*delta;
                    }
                    break;
                case SIZE:
                    if(actualEnergy < maxEnergy/2 && inactivity < 250) {
                        actualEnergy = actualEnergy + maxEnergy * 0.05f * delta;
                    }else{
                        actualEnergy = actualEnergy - 50 * PlayScreen.numberAttack*delta;
                    }
                    break;
            }
        }

        if(actualEnergy < PlayScreen.numberAttack*delta){
            actualEnergy = 1;
            remove = true;
        }

        if(actualEnergy > maxEnergy){
            actualEnergy = maxEnergy;
        }
    }

    private void DelimiterBorder() {

        if (body.getPosition().x * MainGame.PPM - baseRadius*RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy) < -MainGame.V_Width) {
            if(body.getLinearVelocity().x < 0){
                velocity.set((-1)*body.getLinearVelocity().x, body.getLinearVelocity().y);
                body.setLinearVelocity(velocity);
            }
        }
        if (body.getPosition().x * MainGame.PPM + baseRadius*RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy) > MainGame.V_Width) {
            if(body.getLinearVelocity().x > 0){
                velocity.set((-1)*body.getLinearVelocity().x, body.getLinearVelocity().y);
                body.setLinearVelocity(velocity);
            }
        }
        if (body.getPosition().y * MainGame.PPM - baseRadius*RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy) < -MainGame.V_Height) {
            if(body.getLinearVelocity().y < 0){
                velocity.set(body.getLinearVelocity().x, (-1)*body.getLinearVelocity().y);
                body.setLinearVelocity(velocity);
            }
        }
        if (body.getPosition().y * MainGame.PPM + baseRadius*RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy) > MainGame.V_Height) {
            if(body.getLinearVelocity().y > 0){
                velocity.set(body.getLinearVelocity().x, (-1)*body.getLinearVelocity().y);
                body.setLinearVelocity(velocity);
            }
        }
    }
}


