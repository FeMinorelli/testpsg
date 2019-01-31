package com.mygdx.game.psg.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.game.psg.MainGame;
import com.mygdx.game.psg.Screens.MenuScreen;

import static com.mygdx.game.psg.Sprites.Button.TypeButton.*;
import static com.badlogic.gdx.math.MathUtils.random;

public class Button extends Actor {

    public enum TypeButton {
        START,
        CONTINUE,
        COLOR,
        MENU,
        NEW,
        EFFECT
    }

    public TypeButton button;
    public Body body;
    private Vector2 bodyPosition, inputPosition, velocity;
    public  float baseRadius, baseMove,actualEnergy, maxEnergy, radiusEnergy;
    private int index;
    public boolean title;

public Button(float x, float y, Button.TypeButton button, int index, boolean title){
    this.title = title;
    baseMove = 100;
    setColor(MainGame.colors.get(index));

    if(button == TypeButton.EFFECT){
        baseRadius = random(10,125);
    }else{
        baseRadius = 300;
    }

    maxEnergy = CircleArea(baseRadius);
    actualEnergy = 0;
    radiusEnergy = baseRadius * RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy);

    bodyPosition = new Vector2();
    inputPosition = new Vector2();
    velocity = new Vector2(0,0);



    setX(x);
    setY(y);

    this.index = index;
    this.button = button;

    //setColor();
    setButton();
    }

    public void setButton(){

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX() / MainGame.PPM, getY() / MainGame.PPM);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = MenuScreen.world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(baseRadius / MainGame.PPM);

        fixtureDef.shape = circleShape;
        if(button == TypeButton.EFFECT){
            fixtureDef.density = 0.0001f;
            velocity.set(baseMove, baseMove);
            body.setLinearVelocity(velocity.setToRandomDirection());
        }else{
            fixtureDef.density = 10000f;
        }
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 1f;
        body.createFixture(fixtureDef);


    }

    @Override
    public void act(float delta) {

        DelimiterBorder();

        if(button != TypeButton.EFFECT){
            Select();
        }

        if(button == EFFECT && body.getLinearVelocity().len() < 1){
            velocity.set(baseMove, baseMove);
            body.setLinearVelocity(velocity.setToRandomDirection());
        }

        Regeneration();

        setColor(MainGame.colors.get(this.index));

        radiusEnergy = baseRadius * RadiusEnergy(actualEnergy)/RadiusEnergy(maxEnergy);

        super.act(delta);
    }

    private void Select() {
        if (Gdx.input.justTouched() && isTouched()){
            if(MenuScreen.oneSelected){
                if(MenuScreen.button == this){
                    MenuScreen.oneSelected = false;
                }else{
                    MenuScreen.oneSelected = true;
                    MenuScreen.button = this;
                }
            }else {
                MenuScreen.oneSelected = true;
                MenuScreen.button = this;
            }
        }
    }

    private Vector2 BodyPosition(Vector2 bodyPosition) {

        bodyPosition.set(
                body.getPosition().x * MainGame.PPM
                        + (MainGame.W_Width/2) * MenuScreen.zoom
                        - MenuScreen.positionCamera.x,

                body.getPosition().y * MainGame.PPM
                        + (MainGame.W_Height/2) * MenuScreen.zoom
                        - MenuScreen.positionCamera.y);

        return bodyPosition;
    }

    private Vector2 InputPosition(Vector2 inputPosition) {

        inputPosition.set(Gdx.input.getX() * MenuScreen.zoom, (MainGame.W_Height - Gdx.input.getY()) * MenuScreen.zoom);

        return inputPosition;
    }

    private boolean isTouched() {

        return (Gdx.input.justTouched() &&
                (BodyPosition(bodyPosition).dst(InputPosition(inputPosition)) < baseRadius ||
                        BodyPosition(bodyPosition).dst(InputPosition(inputPosition)) < baseRadius));
    }

    private void DelimiterBorder() {
        if (body.getPosition().x * MainGame.PPM - baseRadius < -MainGame.M_Width) {
            if(body.getLinearVelocity().x < 0){
                body.setLinearVelocity((-1)*body.getLinearVelocity().x, body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x + baseMove*0.01f, body.getLinearVelocity().y);
        }
        if (body.getPosition().x * MainGame.PPM + baseRadius > MainGame.M_Width) {

            if(body.getLinearVelocity().x > 0){
                body.setLinearVelocity((-1)*body.getLinearVelocity().x, body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x - baseMove*0.01f, body.getLinearVelocity().y);
        }
        if (body.getPosition().y * MainGame.PPM - baseRadius < -MainGame.M_Height) {
            if(body.getLinearVelocity().y < 0){
                body.setLinearVelocity(body.getLinearVelocity().x, (-1)*body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x, body.getLinearVelocity().y + baseMove*0.01f);
        }
        if (body.getPosition().y * MainGame.PPM + baseRadius > MainGame.M_Height) {
            if(body.getLinearVelocity().y > 0){
                body.setLinearVelocity(body.getLinearVelocity().x, (-1)*body.getLinearVelocity().y);
            }
            body.setLinearVelocity(body.getLinearVelocity().x, body.getLinearVelocity().y - baseMove*0.01f);
        }
    }

    private static float CircleArea(float radius){

        return radius*radius*3.14f;
    }

    private static float RadiusEnergy(float energy){

        return (float)Math.sqrt(energy*3.14f);
    }

    private void Regeneration(){
        if(MenuScreen.oneSelected){
            if(MenuScreen.button == this){
                actualEnergy += 1000 + actualEnergy*0.05f;
            }
        }else{
            actualEnergy -= 500 + actualEnergy*0.05f;
        }

        if(button == EFFECT){
            actualEnergy += random(150,300);
            if(actualEnergy > maxEnergy) {
                actualEnergy = maxEnergy - 1;
            }
        }

        if(actualEnergy > maxEnergy){
            actualEnergy = maxEnergy;
            MenuScreen.oneSelected = false;

            switch (button){
                case COLOR:
                    MainGame.controler = MainGame.Controler.START;
                    Color color = MainGame.colors.remove(index);
                    MainGame.colors.add(0,color);
                    MainGame.colors.trimToSize();
                    break;
                case MENU: MainGame.controler = MainGame.Controler.MENU; break;
                case NEW: MainGame.controler = MainGame.Controler.COLOR; MainGame.load = false; break;
                case CONTINUE: MainGame.controler = MainGame.Controler.COLOR; MainGame.load = true; break;
                case START: MainGame.controler = MainGame.Controler.START;  MainGame.altered = true; break;
            }
        }

        if(actualEnergy < 0 || (button == COLOR && MainGame.controler != MainGame.Controler.COLOR)){
            actualEnergy = 0;
        }
    }

}
