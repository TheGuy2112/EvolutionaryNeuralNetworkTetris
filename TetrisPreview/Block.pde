import java.util.Random;

//Represents an abstract tile on the field with coordinates and color
public class Block {
  private int x, y;
  private Tile t;
  private color c;
  
  public Block(Tile t, int x, int y, color c) {
    this.t = t;
    this.x = x;
    this.y = y;
    this.c = c;
  }
  
  public Block(Tile t, int field_width, color c) {
     this(t,(new Random()).nextInt(field_width-t.getWidth()),0,c); 
  }
  
  public Block(Tile t, int field_width) {
    this(t,field_width,color((new Random()).nextInt(185)+50,(new Random()).nextInt(185)+50,(new Random()).nextInt(185)+50)); 
  }
  
  public Block(int field_width) {
    this(Tile.randomTile(),field_width); 
  }
  
  private void move(int dx, int dy) {
    x+=dx;
    y+=dy;
  }
  
  public void moveLeft() {
    move(-1,0); 
  }
  
  public void moveRight() {
    move(1,0); 
  }
  
  public void moveDown() {
    move(0,1); 
  }
  
  public void moveUp() {
    move(0,-1); 
  }
  
  public void rotateClockwise() {
    t.rotateClockwise(); 
  }
  
  public void rotateCounterClockwise() {
    t.rotateCounterClockwise(); 
  }
  
  public boolean[][] getTile() {
    return t.getTile(); 
  }
    
  public color getColor() {
    return c; 
  }
  
  public int getX() {
    return x; 
  }
  
  public int getY() {
    return y; 
  }
  
  public int getWidth() {
    return t.getWidth(); 
  }
  
  public int getHeight() {
    return t.getHeight();
  }
  
  public void setColor(color c) {
    this.c = c; 
  }
}