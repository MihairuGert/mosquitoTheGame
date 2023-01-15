package com.mihairu.game;

import static com.mihairu.game.Game.*;

import com.badlogic.gdx.math.MathUtils;

public class Mosquito {
    float x, y;
    float width, height;
    float vx, vy;
    int phase, qPhase = 10;

    boolean isAlive = true;
    boolean isActive = true;

    Mosquito(){
        x = SCR_WIDTH/2f;
        y = SCR_HEIGHT/2f;
        width = height = MathUtils.random(50, 150);
        vx = MathUtils.random(-5f, 5);
        vy = MathUtils.random(-5f, 5);
        phase = MathUtils.random(0,qPhase-1);
    }

    float getX(){
        return x-width/2;
    }

    float getY(){
        return y-height/2;
    }

    void move() {
        x += vx;
        y += vy;
        if(isAlive) {
            outBounds2();
            changePhase(isActive);
        }
    }

    void changePhase(boolean isActive){
        if(isActive)phase = ++phase%qPhase;
    }

    void outBounds2(){
        if(x<0+width/2 || x>SCR_WIDTH-width/2) vx = -vx;
        if(y<0+height/2 || y>SCR_HEIGHT-height/2) vy = -vy;
    }

    void outBounds1(){
        if(x<0-width/2) x = SCR_WIDTH+width/2;
        if(x>SCR_WIDTH+width/2) x = 0-width/2;
        if(y<0-height/2) y = SCR_HEIGHT+height/2;
        if(y>SCR_HEIGHT+height/2) y = 0-height/2;
    }

    boolean isFlip(){
        return vx>0;
    }

    boolean hit(float touchX, float touchY){
        if(touchX>x-width/2 && touchX<x+width/2 && touchY>y-height/2 && touchY<y+height/2){
            vx = 0;
            vy = -8;
            isAlive = false;
            phase = 10;
            return true;
        }
        else return false;
    }
}
