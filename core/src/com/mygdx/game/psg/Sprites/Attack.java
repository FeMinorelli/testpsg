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

public class Attack extends Actor {


    public Unity.Team team;
    public Body body;
    private Vector2 velocity = new Vector2(0,0);
    public Genetic.GenType type;

    public float baseAttack,maxEnergy, actualEnergy, energyRadius, baseRadius, baseMove;
    public boolean remove, modifyEnergy, fixVelocity;

    private  int resize;
    public int inactivity;

    private CircleShape circleShape = new CircleShape();
    private FixtureDef fixtureDef = new FixtureDef();

    public Attack(Unity cell, Unity target, Genetic.GenType type, float angle){
        baseMove = cell.baseMove*2;
        baseAttack = cell.baseAttack;
        baseRadius = cell.baseRadius;
        maxEnergy = cell.maxEnergy;
        fixVelocity = false;
        setColor(cell.getColor());
        this.team = cell.team;
        this.type = type;


        switch (type){
            case SIZE:
                actualEnergy = 3f * cell.baseAttack + cell.maxEnergy/10;
                energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);break;
            case REGEN:
                cell.actualEnergy = cell.actualEnergy * 0.6f;
                actualEnergy = 4f * baseAttack + cell.actualEnergy * 0.4f;
                energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy); break;
            case SPEED:
                cell.actualEnergy = cell.actualEnergy * 0.5f;
                actualEnergy = 5f * baseAttack + cell.actualEnergy * 0.5f;
                energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);break;
            case OFFENSIVE:
                cell.actualEnergy = cell.actualEnergy * 0.4f;
                actualEnergy = 7f * baseAttack + cell.actualEnergy * 0.6f;
                energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy); break;
            case DEFENSIVE:
                actualEnergy = 2f * baseAttack + cell.actualEnergy * 0.2f;
                energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);break;
        }

        setAttack(cell, angle);
    }

    private void setAttack(Unity cell, float angle){

        switch (type){
            case SIZE: velocity.set(cell.baseRadius + (energyRadius * 3.14f),cell.baseRadius + (energyRadius * 3.14f)).setAngle(angle);break;
            case DEFENSIVE: velocity.set(cell.baseRadius + energyRadius, 1).setAngle(angle); fixVelocity = true; break;
            case SPEED: velocity.set(cell.baseRadius + energyRadius,1).setAngle(angle);break;
            case OFFENSIVE: velocity.set(cell.baseRadius + energyRadius,1).setAngle(angle);break;
            case REGEN: velocity.set(cell.baseRadius + energyRadius, 1).setAngle(angle);break;


        }

        SetBody(cell, velocity.x, velocity.y, angle);
        body.setAngularVelocity(random(-2f,2f));
    }

    @Override
    public void act(float delta) {
        if(fixVelocity){
            body.setLinearVelocity(velocity);
        }

        DelimiterBorder();
        RefactorEnergy(delta);
        RefactorFixture();
    }

    private float RadiusEnergy(float energy){

        return (float)Math.sqrt(energy*(float)Math.PI);
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

    private FixtureDef SetFixtureDef(){

        circleShape.setRadius((baseRadius * RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy)) /MainGame.PPM);
        fixtureDef.shape = circleShape;

        switch (type){
            case REGEN:
                fixtureDef.isSensor = true;
            case OFFENSIVE:
                fixtureDef.density = 1.5f;
                fixtureDef.friction = 0.5f;
                fixtureDef.restitution = 0.5f;break;
            case SPEED:
                fixtureDef.density = 0.01f;
                fixtureDef.friction = 1f;
                fixtureDef.restitution = 1.1f;break;
            case DEFENSIVE:
                fixtureDef.density = 3f;
                fixtureDef.friction = 1f ;
                fixtureDef.restitution = 0.1f ;break;
            case SIZE:
                fixtureDef.density = 0.2f;
                fixtureDef.friction = 0.2f;
                fixtureDef.restitution = 0.2f;break;
        }

        return fixtureDef;
    }

    private  void SetBody(Unity cell, float x, float y, float angle){
        BodyDef bodyDef = new BodyDef();

        bodyDef.position.set(cell.body.getPosition().x + x/MainGame.PPM,
                cell.body.getPosition().y + y/MainGame.PPM);

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = PlayScreen.world.createBody(bodyDef);


        body.createFixture(SetFixtureDef());
        body.setBullet(true);

        switch (type){
            case SIZE:
                velocity.set(baseMove*3, baseMove*3).setAngle(angle);
                body.setLinearVelocity(velocity);break;
            case DEFENSIVE:
                velocity.set(baseMove/6, baseMove/6).setAngle(angle);
                body.setLinearVelocity(velocity);break;
            case SPEED:
                velocity.set(baseMove*2, baseMove*2).setAngle(angle);
                body.setLinearVelocity(velocity);break;
            case OFFENSIVE:
                velocity.set(baseMove, baseMove).setAngle(angle);
                body.setLinearVelocity(velocity);break;
            case REGEN:
                velocity.set(baseMove/2, baseMove/2).setAngle(angle);
                body.setLinearVelocity(velocity);break;
        }
    }

    private void RefactorFixture(){
        if(resize > 15 || modifyEnergy) {

            body.destroyFixture(body.getFixtureList().pop());
            body.createFixture(SetFixtureDef());

            energyRadius = baseRadius * RadiusEnergy(actualEnergy) / RadiusEnergy(maxEnergy);
            modifyEnergy = false;
            resize = 0;
        }
    }

    private void RefactorEnergy(float delta){

        inactivity++;
        resize++;

        switch (type){
            case SPEED:
                if (energyRadius < 100) {
                actualEnergy = actualEnergy + baseAttack/5 * baseMove * delta ; }break;
            case OFFENSIVE:
                if (energyRadius > 100) {
                    actualEnergy = actualEnergy + 20 * PlayScreen.numberAttack * delta; } break;
            case REGEN:
                    actualEnergy = actualEnergy + actualEnergy * 0.1f * delta; break;
            case DEFENSIVE:
                if (inactivity > 100) {
                    actualEnergy = actualEnergy - actualEnergy * 0.01f * delta; } break;
            case SIZE:
                actualEnergy = actualEnergy - 20 * PlayScreen.numberAttack * delta; break;
        }



        if (inactivity > 200) {
            actualEnergy = actualEnergy - 10*PlayScreen.numberAttack*delta;
        }


        if (energyRadius < 50) {
            actualEnergy = actualEnergy - 10*PlayScreen.numberAttack*delta;
        }

        if (energyRadius > 100) {
            actualEnergy = actualEnergy - 10*PlayScreen.numberAttack*delta;
        }

        if(actualEnergy < PlayScreen.numberAttack*delta){
            actualEnergy = 1;
            remove = true;
        }
    }

}


