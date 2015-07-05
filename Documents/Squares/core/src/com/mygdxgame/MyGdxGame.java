package com.mygdxgame;

import com.badlogic.gdx.Game;


public class MyGdxGame extends Game{
	public MenuScreen menuScreen;
	public GameScreen gameScreen;
	public ScoreScreen scoreScreen;
	public StatsScreen statsScreen;
	
	@Override
	public void create() {
		menuScreen = new MenuScreen(this);
		gameScreen = new GameScreen(this);
		scoreScreen = new ScoreScreen(this);
		statsScreen = new StatsScreen(this);
		setScreen(menuScreen);
	}

}
