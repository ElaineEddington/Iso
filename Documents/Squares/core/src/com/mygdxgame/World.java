package com.mygdxgame;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
//import com.mygdxgame.Block.Color;


public class World {
	public enum Mode {
		MINUTE, ENDUR
	}

	public static final int BLOCKS_WIDTH = 9;
	public static final int BLOCKS_HEIGHT = 9;
	public static final int OFFSET_X = 2;
	public static final int OFFSET_Y = 2;

	private static final float CAMERA_WIDTH = WorldRenderer.CAMERA_WIDTH;
	private static final float CAMERA_HEIGHT = WorldRenderer.CAMERA_HEIGHT;
	private static final int SECONDS_TO_COMPLETE = 60;
	private static final long STARTING_MS_PER_ROW = 2500;

	Mode mode;
	Block[][] blocks;
	List<Vector2> checkedBlocks;
	List<Vector2> blocksToRemove;
	int score, currentLevel, levelScore, totalCleared; 
	long timeRemaining, msecondsPerRow, modeOffset;
	float width, height, blocksWidth, blocksHeight, ppuBlockX, ppuBlockY, numBlocksY;
	boolean isGameDone, isReachingFailure;

	public World(Mode mode) {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		ppuBlockX = width / CAMERA_WIDTH * Block.WIDTH;
		ppuBlockY = height / CAMERA_HEIGHT * Block.HEIGHT;
		numBlocksY = height / ppuBlockY;
		blocksWidth = BLOCKS_WIDTH * ppuBlockX;
		blocksHeight = BLOCKS_HEIGHT * ppuBlockY;
		
		this.mode = mode;
		currentLevel = 0;
		levelScore = 0;
		totalCleared = 0;
		msecondsPerRow = STARTING_MS_PER_ROW;
		timeRemaining = SECONDS_TO_COMPLETE;
		isGameDone = false;
		blocksToRemove = new ArrayList<Vector2>();
		blocks = new Block[BLOCKS_WIDTH][BLOCKS_HEIGHT];

		if (mode == Mode.ENDUR)
			modeOffset = BLOCKS_HEIGHT / 2;
		else
			modeOffset = 0;

		for (int i = 0; i < BLOCKS_WIDTH; i++) {
			for (int j = 0; j < BLOCKS_HEIGHT - modeOffset; j++) {
				blocks[i][j] = new Block(i,j,0);
			}
		}
	}

	// returns random color for new blocks
	public com.mygdxgame.Block.Color getRandColor() {
		int num = MathUtils.random(3);
		switch (num) {
		case 0:
			return Color.BLUE;
		case 1:
			return Color.GREEN;
		case 2:
			return Color.RED;
		case 3:
			return Color.YELLOW;
		default:
			return Color.BLUE;
		}
	}

	// checks the blocks surrounding the block clicked on
	// and adds them to a list of blocks to remove if
	// their colors match
	public void checkSurroundingBlocks(int x, int y) {
		Vector2 current = new Vector2(x, y);
		Vector2 up = new Vector2(x, y + 1);
		Vector2 down = new Vector2(x, y - 1);
		Vector2 left = new Vector2(x - 1, y);
		Vector2 right = new Vector2(x + 1, y);

		checkedBlocks.add(current);
		blocksToRemove.add(current);

		com.mygdxgame.Block.Color c = blocks[x][y].getColor();
		if (!checkedBlocks.contains(left) && left.x >= 0
				&& blocks[x - 1][y] != null) {
			checkedBlocks.add(left);
			if (blocks[x - 1][y].getColor() == c)
				checkSurroundingBlocks(x - 1, y);
		}
		if (!checkedBlocks.contains(down) && down.y >= 0
				&& blocks[x][y - 1] != null) {
			checkedBlocks.add(down);
			if (blocks[x][y - 1].getColor() == c)
				checkSurroundingBlocks(x, y - 1);
		}
		if (!checkedBlocks.contains(right) && right.x < BLOCKS_WIDTH
				&& blocks[x + 1][y] != null) {
			checkedBlocks.add(right);
			if (blocks[x + 1][y].getColor() == c)
				checkSurroundingBlocks(x + 1, y);
		}
		if (!checkedBlocks.contains(up) && up.y < BLOCKS_HEIGHT
				&& blocks[x][y + 1] != null) {
			checkedBlocks.add(up);
			if (blocks[x][y + 1].getColor() == c)
				checkSurroundingBlocks(x, y + 1);
		}
	}

	// remove blocks in the list of blocks to remove
	public void removeBlocks() {
		totalCleared += blocksToRemove.size();
		ArrayList<Integer> xVals = new ArrayList<Integer>();
		for (int i = 0; i < blocksToRemove.size(); i++) {
			Vector2 v = blocksToRemove.get(i);
			if (!xVals.contains((int) v.x))
				xVals.add((int) v.x);
			blocks[(int) v.x][(int) v.y] = null;
		}

		for (int i = 0; i < xVals.size(); i++) {
			int x = xVals.get(i);
			int k = 1;
			for (int j = 0; j < BLOCKS_HEIGHT - k; j++) {
				if (blocks[x][j] == null) {
					while (blocks[x][j + k] == null) { //possibly move this loop outside for loop
						k++;
						if ((j + k) >= BLOCKS_HEIGHT)
							break;
					}

					if ((j + k) < BLOCKS_HEIGHT) {
						blocks[x][j] = blocks[x][j + k];
						blocks[x][j + k] = null;
					}
				}
			}

			if (mode == Mode.MINUTE) {
				for (int j = 1; j <= k; j++) {
					Gdx.app.log("new block", x + ", " + (BLOCKS_HEIGHT - j));
					blocks[x][BLOCKS_HEIGHT - j] = new Block(getBlockPosition(x, BLOCKS_HEIGHT, (k-j)),
							getRandColor());
				}
			}

		}

	}

	// makes a new row of blocks from the bottom
	public void makeNewRow() {
		for (int i = 0; i < BLOCKS_WIDTH; i++) {
			for (int j = BLOCKS_HEIGHT - 1; j > 0; j--) {
				if (j == BLOCKS_HEIGHT - 1 && blocks[i][j] != null)
					isGameDone = true;
				blocks[i][j] = blocks[i][j - 1];
			}
		}

		for (int j = 0; j < BLOCKS_WIDTH; j++) {
			blocks[j][0] = new Block(new Vector2(j + OFFSET_X, OFFSET_Y - 1),
					getRandColor());
		}
	}

	//checks for empty columns and moves blocks together
	public void checkForEmptyCols() {
		for (int i = 0; i < BLOCKS_WIDTH; i++) {
			if (blocks[i][0] == null) {
				if (i < BLOCKS_WIDTH / 2) {
					for (int k = i; k >= 0; k--) {
						for (int j = 0; j < BLOCKS_HEIGHT; j++) {
							if (k == 0) {
								blocks[k][j] = null;
							} else
								blocks[k][j] = blocks[k - 1][j];

						}
					}
				} else {
					for (int k = i; k <= BLOCKS_WIDTH-1; k++) {
						for (int j = 0; j < BLOCKS_HEIGHT; j++) {
							if (k == BLOCKS_WIDTH-1) {
								blocks[k][j] = null;
							} else
								blocks[k][j] = blocks[k + 1][j];

						}
					}
				}
			}
		}
	}
	
	public void setBlocksPressed(){
		for (int i = 0; i < blocksToRemove.size(); i++) {
			Vector2 v = blocksToRemove.get(i);
			blocks[(int) v.x][(int) v.y].setPressed(true);
		}
	}
	
	public void setBlocksUnPressed(){
		for(int i = 0; i < BLOCKS_WIDTH; i++){
			for(int j = 0; j < BLOCKS_HEIGHT; j++){
				if(blocks[i][j]!=null)
					blocks[i][j].setPressed(false);
			}
		}
	}

	// gets screen coordinates and translates them
	public Vector2 getCoords(int x, int y) {
		System.out.println(x + " " +y);
		int x2 = (int) (x / ppuBlockX) - OFFSET_X;
		int y2 = ((int) numBlocksY - 1) - ((int) (y / ppuBlockY) + OFFSET_Y);
		return new Vector2(x2, y2);
	}

	// clears list of blocks to remove
	public void resetBlocksToRemove() {
		blocksToRemove.clear();
	}

	// adds value from clearing blocks to total score
	public void addToScore(int score) {
		levelScore += score;
		this.score += score;
		if (levelScore < 0)
			levelScore = 0;
		if (this.score < 0)
			this.score = 0;
	}
	
	public void decTimeRemaining(){
		timeRemaining--;
		if (timeRemaining <= 0) {
			isGameDone = true;
		}
	}
	
	// computes the number of milliseconds for a new row to spawn 
	// based on the current level
	public void computeMilliSecondsPerRow() {
		long sub = 100;
		long num = msecondsPerRow - sub;

		if (num <= 1000)
			msecondsPerRow = 1000;
		else
			msecondsPerRow = num;
	}

	//changes the level if necessary (based on score) and returns true, otherwise returns false
	public boolean computeCurrentLevel() {
		if (levelScore >= 50) {
			levelScore -= 50;
			currentLevel++;
			return true;
		}
		else
			return false;
	}
	
	/* Getters and Setters */
	
	public Vector2 getBlockPosition(int x, int y, int removalOffset){
		return new Vector2(x + OFFSET_X, y + OFFSET_Y + removalOffset);
	}
	
	public void setCheckedBlocks(List<Vector2> list) {
		this.checkedBlocks = list;
	}
	
	public boolean isGameDone() {
		return isGameDone;
	}
	
	public List<Vector2> getBlocksToRemove() {
		return blocksToRemove;
	}
	
	public Block[][] getBlocks() {
		return blocks;
	}
	
	public long getMilliSecondsPerRow() {
		return msecondsPerRow;
	}	
	
	public int getCurrentLevel() {
		return currentLevel;
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public int getScore() {
		return score;
	}

	public long getTimeRemaining() {
		return timeRemaining;
	}

	public int getTotalCleared() {
		return totalCleared;
	}
	public void setIsReachingFailure(boolean b){
		isReachingFailure = b;
	}
	public boolean isReachingFailure(){
		return isReachingFailure;
	}
}
