package com.mihairu.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.TimeUtils;
import com.sun.org.apache.xpath.internal.axes.WalkingIterator;

import java.util.concurrent.TimeUnit;

import javax.sound.midi.spi.SoundbankReader;

public class Game extends ApplicationAdapter {
	public static final int SCR_WIDTH = 1280, SCR_HEIGHT = 720;

	SpriteBatch batch;
	OrthographicCamera camera;
	Vector3 touchK;
	BitmapFont font;

	Texture[] imgMosquito = new Texture[11];
	Texture imgBG;
	Sound[] sndMosq = new Sound[2];

	Mosquito[] mosquito = new Mosquito[25];
	int frags;

	Player[] players = new Player[6];
	Player player;

	long timeStart, timeCurrent;
	boolean gameOver = false;

	@Override
	public void create() {
		//creation of system objects
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCR_WIDTH, SCR_HEIGHT);
		touchK = new Vector3();
		font = new BitmapFont();

		//textures
		imgBG = new Texture("background.jpg");

		generateFont();

		for (int i = 0; i < sndMosq.length; i++) {
			sndMosq[i] = Gdx.audio.newSound(Gdx.files.internal("sounds/hitSound" + i + ".wav"));
		}

		for (int i = 0; i < imgMosquito.length; i++) {
			imgMosquito[i] = new Texture("mosquitoAnimation/mosq" + i + ".png");
		}

		for (int i = 0; i < players.length; i++) {
			players[i] = new Player("Unknown comrade", 0);
		}

		player = new Player("Ivan", 0);

		gameStart();

		timeStart = TimeUtils.millis();
	}

	@Override
	public void render() {
		//touching the screen
		if (Gdx.input.justTouched()) {
			touchK.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchK);

			if(gameOver){
				gameStart();
			}else
			{
				for (int i = mosquito.length - 1; i >= 0; i--) {
					if (mosquito[i].isAlive) {
						if (mosquito[i].hit(touchK.x, touchK.y)) {
							frags++;
							sndMosq[0].play();

							if (frags == mosquito.length) {
								gameOver();
							}
							break;
						}
					}
				}
			}
		}

		//events
		for (int i = 0; i < mosquito.length; i++) {
			mosquito[i].move();
		}
		if (!gameOver) {
			timeCurrent = TimeUtils.millis() - timeStart; //timer
		}


		//graphics
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(imgBG, 0, 0, SCR_WIDTH, SCR_HEIGHT);
		for (int i = 0; i < mosquito.length; i++) {
			batch.draw(imgMosquito[mosquito[i].phase], mosquito[i].getX(), mosquito[i].getY(), mosquito[i].width, mosquito[i].height, 0, 0, 500, 500, mosquito[i].isFlip(), false);
		}
		font.draw(batch, "комаров расстреляно:" + frags, 10, SCR_HEIGHT - 10);
		font.draw(batch, timeToString(timeCurrent), SCR_WIDTH - 150, SCR_HEIGHT - 10);

		if(gameOver)
			font.draw(batch, tableOfRecordsToString(),SCR_WIDTH/3f,SCR_HEIGHT/4f*3f);


		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		for (int i = 0; i < imgMosquito.length; i++) {
			imgMosquito[i].dispose();
		}
	}

	void generateFont() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Molot.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.color = new Color(1, 1, 0.4f, 1);
		parameter.size = 32;
		parameter.borderColor = Color.BLACK;
		parameter.borderWidth = 3;
		String FONT_CHARS = "";
		for (int i = 0x20; i < 0x7B; i++)
			FONT_CHARS += (char) i; // цифры и весь англ
		for (int i = 0x401; i < 0x452; i++)
			FONT_CHARS += (char) i; // русские
		parameter.characters = FONT_CHARS;
		font = generator.generateFont(parameter);
	}

	String timeToString(long time) {
		return time / 1000 / 60 / 60 + ":" + time / 1000 / 60 % 60 / 10 +
				time / 1000 / 60 % 60 % 10 + ":" + time / 1000 % 60 / 10 +
				time / 1000 % 60 % 10;
	}

	void gameOver() {
		gameOver = true;
		player.time=timeCurrent;

		class MyInputListener implements Input.TextInputListener{

			@Override
			public void input(String text) {
				player.name = text;
				players[players.length-1].time=player.time;
				players[players.length-1].name=player.name;
				sortPlayers();
				saveTableOfRecords();
			}

			@Override
			public void canceled() {

			}
		}

		Gdx.input.getTextInput(new MyInputListener(), "Ввидите имя", player.name, "");

	}

	void gameStart(){
		gameOver = false;
		frags = 0;
		timeStart = TimeUtils.millis();
		for (int i = 0; i < mosquito.length; i++) {
			mosquito[i]= new Mosquito();
		}
	}

	void sortPlayers(){
		for (int i = 0; i < players.length; i++)
			if (players[i].time == 0)
				players[i].time = Long.MAX_VALUE;

		for (int j = 0; j < players.length; j++) {
			for (int i = 0; i < players.length-1; i++) {
				if (players[i].time > players[i + 1].time) {
					Player c = players[i];
					players[i] = players[i + 1];
					players[i + 1] = c;
				}
			}
		}

		for (int i = 0; i < players.length; i++)
			if (players[i].time == Long.MAX_VALUE)
				players[i].time = 0;
	}

	String tableOfRecordsToString(){
		String s = "";
		for (int i = 0; i < players.length-1; i++) {
			s += players[i].name + "......"+timeToString(players[i].time) +"\n";
		}
		return s;
	}

	void saveTableOfRecords(){
		try {
			Preferences pref = Gdx.app.getPreferences("TableOfRecords");
			for (int i = 0; i < players.length; i++) {
				pref.putString("name" + i, players[i].name);
				pref.putLong("time" + i, players[i].time);
			}
			pref.flush();
		} catch (Exception e){
			
		}

	}
}
