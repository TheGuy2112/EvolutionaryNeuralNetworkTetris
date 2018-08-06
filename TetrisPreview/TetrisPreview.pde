Player p;
Field f;

String file = "C:\\Users\\Patric Gruber\\Desktop\\TetrisPreview\\best_brain.nn";

int field_width=10, field_height=20;

void setup() {
  size(405,605);
  p = new Player(file);
  f = new Field(field_width, field_height,5,false);
}

void draw() {
  frameRate(15);
  double[] output = p.play(f.toInputArray());
  int max_i = 0;
  for (int i=1;i<output.length;i++) {
    //println(output[i] +">"+output[max_i]);
    if (output[i] > output[max_i]) max_i = i; 
  }
  println(max_i);
  switch (max_i) {
    case 0:
      f.keyPressed(RIGHT);
      break;
    case 1:
      f.keyPressed(LEFT);
      break;
    case 2:
      f.keyPressed(DOWN);
      break;
    case 3:
      f.keyPressed('a');
      break;
    case 4:
      f.keyPressed('d');
      break;
    case 5:
      f.keyPressed(' ');
      break;
    case 6:
      break;
  }
  f.logic();
  f.draw();
}