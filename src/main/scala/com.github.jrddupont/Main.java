package com.github.jrddupont;

import com.github.jrddupont.board.Board;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Main {
	public static void main(String[] args) throws AWTException, InterruptedException {
		Point gamePosition = getGamePosition();

		int centerX = gamePosition.x + tilesPosition.x + ((numberOfTiles/2)*tileSize) + tileCenterSize;
		int centerY = gamePosition.y + tilesPosition.y + ((numberOfTiles/2)*tileSize) + tileCenterSize;

		int resetX = gamePosition.x + resetButtonPosition.x;
		int resetY = gamePosition.y + resetButtonPosition.y;

		click(centerX, centerY);
		click(resetX, resetY);

		Thread.sleep(500);

		for(int i = 30; i > 10; i--){
			startSearch(i);
			Thread.sleep(100);
		}
	}

	public static final Color gameBoxColor = new Color(171, 183, 183);
	public static final Color backgroundColor = new Color(107, 107, 107);
	public static final int gameBoxColorInt = gameBoxColor.getRGB();
	public static final int backgroundColorInt = backgroundColor.getRGB();

	public static final int numberOfTiles = 14;
	public static final int tileSize = 27;
	public static final int tileCenterSize = tileSize/2;
	public static final Point tilesPosition = new Point(22, 72);


	public static final Point colorSwatchesPosition = new Point(483, 174);
	public static final Point resetButtonPosition = new Point(545, 285);

	public static final int colorSwatchesWide = 3;
	public static final int colorSwatchesTall = 2;

	public static final int colorSwatchesXSpacing = 60;
	public static final int colorSwatchesYSpacing = 65;

	// http://flashbynight.com/drench/

	public static void startSearch(int steps) throws InterruptedException, AWTException {
		BufferedImage screenCapture = getScreenCapture();
		Point gamePosition = getGamePosition(screenCapture);
		// Generate set of colors
		HashSet<Integer> colorSet = new HashSet<>();
		for(int x = 0; x < numberOfTiles; x++){
			for(int y = 0; y < numberOfTiles; y++){

				int xPos = gamePosition.x + tilesPosition.x + (x*tileSize) + tileCenterSize;
				int yPos = gamePosition.y + tilesPosition.y + (y*tileSize) + tileCenterSize;

				colorSet.add(screenCapture.getRGB(xPos, yPos));
			}
		}

		// Generate color mappings (for faster runtime)
		Map<Integer, Integer> rgbToColorID = new HashMap<>();
		int i = 0;
		for( Integer color : colorSet){
			rgbToColorID.put(color, i);
			i++;
		}

		// Generate a map of color indexs instead of rgb ints
		int[][] colorMap = new int[numberOfTiles][numberOfTiles];
		for(int x = 0; x < numberOfTiles; x++){
			for(int y = 0; y < numberOfTiles; y++){

				int xPos = gamePosition.x + tilesPosition.x + (x*tileSize) + tileCenterSize;
				int yPos = gamePosition.y + tilesPosition.y + (y*tileSize) + tileCenterSize;

				int test = screenCapture.getRGB(xPos, yPos);
				colorMap[x][y] = rgbToColorID.get(test);
			}
		}

		// Since the pink dot is a different color than the tiles, we need a little special logic
		Point[] colorIDToGameCoordinate = new Point[rgbToColorID.size()];
		Point pinkPos = null;
		for(int x = 0; x < colorSwatchesWide; x++){
			for(int y = 0; y < colorSwatchesTall; y++){

				int xPos = gamePosition.x + colorSwatchesPosition.x + (x*colorSwatchesXSpacing);
				int yPos = gamePosition.y + colorSwatchesPosition.y + (y*colorSwatchesYSpacing);

				int color = screenCapture.getRGB(xPos, yPos);

				if(rgbToColorID.containsKey(color)){
					colorIDToGameCoordinate[rgbToColorID.get(color)] = new Point(xPos, yPos);
				} else {
					pinkPos = new Point(xPos, yPos);
				}
			}
		}

		for (int j = 0; j < colorIDToGameCoordinate.length; j++) {
			if(colorIDToGameCoordinate[j] == null){
				colorIDToGameCoordinate[j] = pinkPos;
				break;
			}
		}

		Board board = new Board(colorMap);

		List<BoardNode> finalList = FloodSolver.solve(board, colorSet.size(), steps);
		System.out.println(steps + " " + (finalList.size()-1));
		for(BoardNode step : finalList){
			if(step.clickedColor == -1){
				continue;
			}
			Point clickPoint = colorIDToGameCoordinate[step.clickedColor];
			click(clickPoint.x, clickPoint.y);
			Thread.sleep(50);
		}

		Thread.sleep(1000);

		int xPos = gamePosition.x + tilesPosition.x + ((numberOfTiles/2)*tileSize) + tileCenterSize;
		int yPos = gamePosition.y + tilesPosition.y + ((numberOfTiles/2)*tileSize) + tileCenterSize;

		click(xPos, yPos);

		int resetX = gamePosition.x + resetButtonPosition.x;
		int resetY = gamePosition.y + resetButtonPosition.y;

		bot.mouseMove(resetX,resetY);
	}

	static Robot bot;

	static {
		try {
			bot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static void click(Point point) {
		click(point.x, point.y);
	}

	public static void click(int x, int y) {
		bot.mouseMove(x, y);
		bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	private static BufferedImage getScreenCapture() throws AWTException {
		// Get a full screenshot
		Rectangle screenRect = new Rectangle(0, 0, 0, 0);
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
		}
		return new Robot().createScreenCapture(screenRect);
	}
	private static Point getGamePosition(BufferedImage screenCapture) {
		Point gamePosition = null;
		int previousColor = 0;
		outer:
		for(int x = 0; x < screenCapture.getWidth(); x++){
			for(int y = 0; y < screenCapture.getHeight(); y++){
				int currentColor = screenCapture.getRGB(x, y);
				if(currentColor == gameBoxColorInt && previousColor == backgroundColorInt){
					gamePosition = new Point(x, y);
					break outer;
				}
				previousColor = currentColor;
			}
		}
		assert gamePosition != null;
		return gamePosition;
	}
	private static Point getGamePosition() throws AWTException {
		BufferedImage screenCapture = getScreenCapture();
		return getGamePosition(screenCapture);
	}
}
