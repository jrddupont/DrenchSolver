package com.github.jrddupont.board;

import java.awt.*;

public class Region {
	int color;
	int regionID;
	int volume = 0;
	Point center;

	public Region(int color, int regionID, Point center){
		this.color = color;
		this.regionID = regionID;
		this.center = center;
	}
}
