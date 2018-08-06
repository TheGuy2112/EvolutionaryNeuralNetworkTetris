import java.awt.*;
import java.util.ArrayList;

public class Field {
    private int field_width, field_height;
    private Color[][] grid;
    private Block current;
    private Block next;
    private Block saved;
    private int score;
    private long fallMillis;
    private long lastMillis = -1;
    private boolean game_over = false;
    private boolean timed;

    private final static int grid_size = 25, grid_margin = 2;

    public Field() {
        this(10, 20);
    }

    public Field(int field_width, int field_height) {
        this(field_width, field_height, 200);
    }

    public Field(int field_width, int field_height, int fallMillis) {
        this(field_width, field_height, fallMillis, true);
    }

    public Field(int field_width, int field_height, int fallMillis, boolean timed) {
        this.field_width = field_width;
        this.field_height = field_height;
        grid = new Color[field_width][field_height];
        for (int x = 0; x < field_width; x++) {
            for (int y = 0; y < field_height; y++) {
                grid[x][y] = Color.white;
            }
        }
        score = 0;
        spawnBlock();
        spawnBlock();
        this.fallMillis = fallMillis;
        this.timed = timed;
        if (!timed) lastMillis = 0;
    }

    public int gridWidth() {
        return grid.length;
    }

    public int gridHeight() {
        return grid[0].length;
    }

    public void spawnBlock() {
        current = next;
        next = new Block(field_width);
    }

    {
    /*
    public void draw() {
        background(50);
        noStroke();
        draw_field();
        draw_current_block();
        draw_saved_block();
        draw_next_block();
        draw_score();
    }

    private void draw_field() {
        for (int x=0;x<gridWidth();x++) {
            for (int y=0;y<gridHeight();y++) {
                fill(grid[x][y]);
                int gx = grid_margin+(grid_size+grid_margin)*x;
                int gy = grid_margin+(grid_size+grid_margin)*y;
                rect(gx,gy,grid_size,grid_size);
            }
        }
    }

    private void draw_current_block() {
        draw_block(current, grid_margin+current.getX()*(grid_size+grid_margin), grid_margin+current.getY()*(grid_size+grid_margin));
    }

    private void draw_next_block() {
        boolean[][] tile = next.getTile();
        int left = field_width*(grid_size+grid_margin)+grid_margin*2;
        int top = grid_margin;
        float margin_left = ((4-tile.length)/2f)*(grid_size+grid_margin)+grid_margin;
        float margin_top = ((4-tile[0].length)/2f)*(grid_size+grid_margin)+grid_margin;
        fill(255);
        rect(left,top,(grid_size+grid_margin)*4+grid_margin,(grid_size+grid_margin)*4+grid_margin);
        draw_block(next, left+margin_left, top+margin_top);
    }

    private void draw_block(Block b, float margin_x, float margin_y) {
        fill(b.getColor());
        boolean[][] tile = b.getTile();
        for (int x=0;x<tile.length;x++) {
            for (int y=0;y<tile[x].length;y++) {
                float gx = margin_x+(grid_size+grid_margin)*x;
                float gy = margin_y+(grid_size+grid_margin)*y;
                if (tile[x][y])
                    rect(gx,gy,grid_size,grid_size);
            }
        }
    }

    private void draw_saved_block() {
        fill(200);
        int left = field_width*(grid_size+grid_margin)+grid_margin*2;
        int top = grid_margin+(grid_size+grid_margin)*4+grid_margin+2*grid_margin;
        rect(left,top,(grid_size+grid_margin)*4+grid_margin,(grid_size+grid_margin)*4+grid_margin);
        if (saved!=null) {
            boolean[][] tile = saved.getTile();
            float margin_left = ((4-tile.length)/2f)*(grid_size+grid_margin)+grid_margin;
            float margin_top = ((4-tile[0].length)/2f)*(grid_size+grid_margin)+grid_margin;
            draw_block(saved, left+margin_left, top+margin_top);
        }
    }

    private void draw_score() {
        fill(255);
        textSize(30);
        text("Score: "+score, 10, height-40,width,height);
    }
    */
    }

    public void logic() {
        if (!game_over) {
            if (timed) {
                if (lastMillis == -1) lastMillis = System.currentTimeMillis();
                else {
                    if (System.currentTimeMillis() - lastMillis >= fallMillis) {
                        current.moveDown();
                        checkDownCollision();
                        lastMillis = System.currentTimeMillis();
                    }
                }
            } else {
                lastMillis++;
                if (lastMillis == fallMillis) {
                    current.moveDown();
                    checkDownCollision();
                    lastMillis = 0;
                }
            }
            checkScore();
        }
    }

    public void keyPressed(int value) {
        if (!game_over) {
            if (value == 0) {
                current.moveRight();
                if (current.getX() + current.getWidth() > field_width || checkMoveCollision()) current.moveLeft();
            } else if (value == 1) {
                current.moveLeft();
                if (current.getX() < 0 || checkMoveCollision()) current.moveRight();
            } else if (value == 2) {
                current.moveDown();
                checkDownCollision();
            } else if (value == 'a') {
                current.rotateCounterClockwise();
                while (current.getX() < 0) current.moveRight();
                while (current.getX() + current.getWidth() > field_width) current.moveLeft();
                if (!checkDownCollision() && checkMoveCollision()) current.rotateCounterClockwise();
            } else if (value == 'd') {
                current.rotateClockwise();
                while (current.getX() < 0) current.moveRight();
                while (current.getX() + current.getWidth() > field_width) current.moveLeft();
                if (!checkDownCollision() && checkMoveCollision()) current.rotateCounterClockwise();
            } else if (value == ' ') {
                if (saved == null) {
                    saved = new Block(new Tile(current.getTile()), field_width, current.getColor());
                    spawnBlock();
                } else {
                    current = saved;
                    saved = null;
                }
                score -= 50;
            }
            score -= 10;
        }
    }

    public void fixOnField(Block b) {
        boolean[][] t = b.getTile();
        for (int x = 0; x < current.getWidth(); x++) {
            for (int y = 0; y < current.getHeight(); y++) {
                if (t[x][y] && x + current.getX() >= 0 && x + current.getX() < field_width && y + current.getY() >= 0 && y + current.getY() < field_height)
                    grid[x + current.getX()][y + current.getY()] = current.getColor();
            }
        }
        score += 10;
    }

    private boolean checkMoveCollision() {
        boolean[][] t = current.getTile();
        for (int x = 0; x < current.getWidth(); x++) {
            for (int y = 0; y < current.getHeight(); y++) {
                if (current.getX() >= 0 && current.getY() >= 0 && t[x][y] && grid[x + current.getX()][y + current.getY()] != Color.white)
                    return true;
            }
        }
        return false;
    }

    private boolean checkDownCollision() {
        //collision with floor
        if (current.getY() + current.getHeight() > field_height) {
            current.moveUp();
            if (current.getY() <= 0) {
                game_over();
            } else {
                fixOnField(current);
                spawnBlock();
            }
            return true;
        } else {
            boolean[][] t = current.getTile();
            //collision with any previous block
            for (int x = 0; x < current.getWidth(); x++) {
                for (int y = 0; y < current.getHeight(); y++) {
                    if (t[x][y] && grid[x + current.getX()][y + current.getY()] != Color.white) {
                        current.moveUp();
                        if (current.getY() <= 0) {
                            game_over();
                        } else {
                            fixOnField(current);
                            spawnBlock();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void checkScore() {
        ArrayList<Integer> full_lines = new ArrayList();
        for (int y = 0; y < field_height; y++) {
            boolean full = true;
            for (int x = 0; x < field_width; x++) {
                if (grid[x][y] == Color.white) {
                    full = false;
                    break;
                }
            }
            if (full) full_lines.add(y);
        }

        score += (int) (Math.pow(full_lines.size(), 1.2) * 150);
        for (int line : full_lines) {
            for (int x = 0; x < field_width; x++) {
                grid[x][line] = Color.white;
            }
        }

        if (full_lines.size() > 0)
            move_field_down(full_lines);
    }

    private void move_field_down(ArrayList<Integer> full_lines) {
        int move = 0;
        for (int y = field_height - 1; y > 0; y--) {
            if (full_lines.size() > 0 && y == full_lines.get(full_lines.size() - 1)) {
                move += 1;
                full_lines.remove(full_lines.size() - 1);
                continue;
            }
            for (int x = 0; x < field_width; x++) {
                grid[x][y + move] = grid[x][y];
            }
        }
    }

    private void game_over() {
        game_over = true;
        while (checkMoveCollision()) current.moveUp();
    }

    public double[] toInputArray() {
        double[] out = new double[field_width*field_height*2];

        for (int x = 0; x < field_width; x++) {
            for (int y = 0; y < field_height; y++) {
                out[x+y*field_width] = (grid[x][y] != Color.white)?1d:0d;
            }
        }

        boolean[][] t = current.getTile();
        for (int x = 0; x < current.getWidth(); x++) {
            for (int y = 0; y < current.getHeight(); y++) {
                if (x + current.getX() >= 0 && x + current.getX() < field_width && y + current.getY() >= 0 && y + current.getY() < field_height)
                    out[(field_width*field_height)+(x + current.getX())+(y + current.getY())*field_width] = (t[x][y])?1d:0;
            }
        }

        return out;
    }

    public boolean isGameOver() {
        return game_over;
    }

    public int getScore() {
        return score;
    }

    public long getFitness() {
        long s = score;
        /*for (int y = 0; y < field_height; y++) {
            boolean empty = true;
            for (int x = 0; x < field_width; x++) {
                if (grid[x][y] != Color.white) {
                    empty = false;
                    break;
                }
            }
            if (!empty) {
                s += (field_height - y)*10;
                break;
            }
        }*/
        return s;
    }
}