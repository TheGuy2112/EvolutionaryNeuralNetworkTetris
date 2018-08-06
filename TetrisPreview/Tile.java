import java.util.Random;

//Represents an abstract tile
public class Tile {
  private boolean[][] grid;
  private int rotation; //90 deg steps counter clockwise
  
  public Tile(boolean[][] grid) {
    this(grid, 0);
  }
  
  public Tile(boolean[][] grid, int rotation) {
    this.grid = grid;
    this.rotation = rotation;
  }
  
  public static Tile Square() {
    return new Tile(new boolean[][]{
          new boolean[]{true, true},
          new boolean[]{true, true}
        },1);
  }
  
  public static Tile L() {
    return new Tile(new boolean[][]{
          new boolean[]{true, false},
          new boolean[]{true, false},
          new boolean[]{true, true}
        },1);
  }
  
  public static Tile L_mirrored() {
      return new Tile(new boolean[][]{
        new boolean[]{false, true},
        new boolean[]{false, true},
        new boolean[]{true, true}
      },1);
  }
  
  public static Tile Cross() {
      return new Tile(new boolean[][]{
        new boolean[]{false, true, false},
        new boolean[]{true, true, true}
      },3); 
  }
  
  public static Tile Line() {
    return new Tile(new boolean[][]{
      new boolean[]{true},
      new boolean[]{true},
      new boolean[]{true},
      new boolean[]{true}
    },1);
  }
  
  public static Tile S() {
    return new Tile(new boolean[][]{
      new boolean[]{false, true, true},
      new boolean[]{true, true, false}
    },1);
  }
  
  public static Tile S_mirrored() {
    return new Tile(new boolean[][]{
      new boolean[]{true, true, false},
      new boolean[]{false, true, true}
    },1);
  }
  
  public static Tile randomTile() {
    int t = (int)(((new Random()).nextFloat())*7);
    switch (t) {
      case 0:
        return Square();
      case 1:
        return L();
      case 2:
        return L_mirrored();
      case 3:
        return Cross();
      case 4:
        return Line();
      case 5:
        return S();
      case 6:
        return S_mirrored();
    }
    return null;
  }
  
  public int getWidth() {
    if (rotation%2 == 0) return grid.length; 
    else return grid[0].length;
  }
  
  public int getHeight() {
    if (rotation%2 == 0) return grid[0].length; 
    else return grid.length;
  }
  
  public void rotateClockwise() {
    rotation = (rotation-1)%4;
    while(rotation < 0) rotation+=4;
  }
  
  public void rotateCounterClockwise() {
    rotation = (rotation+1)%4;
  }
  
  public boolean[][] getTile() {
    if (rotation == 0) return grid;  
    
    boolean[][] new_grid = null;
    if (rotation == 1) {
      int w = grid[0].length;
      int h = grid.length;
      new_grid = new boolean[w][h];
      for (int x=0;x<w;x++) {
        for (int y=0;y<h;y++) {
          new_grid[x][y] = grid[h-y-1][x];
        }
      }
    } else if (rotation == 2) {
      int w = grid.length;
      int h = grid[0].length;
      new_grid = new boolean[w][h];
      for (int x=0;x<w;x++) {
        for (int y=0;y<h;y++) {
          new_grid[x][y] = grid[w-x-1][h-y-1];
        }
      }
    } else if (rotation == 3) {
      int w = grid[0].length;
      int h = grid.length;
      new_grid = new boolean[grid[0].length][grid.length];
      for (int x=0;x<w;x++) {
        for (int y=0;y<h;y++) {
          new_grid[x][y] = grid[y][w-x-1];
        }
      }
    }
    return new_grid;
  }
}