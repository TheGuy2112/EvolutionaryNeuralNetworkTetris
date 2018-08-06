import java.util.Arrays;

class Player{
  private NeuralNet1 brain;
  
  public Player(String brain_file) {
    brain = new NeuralNet1(brain_file);
  }

  public double[] play(double[] inputs) {
    return brain.feed_forward(inputs);
  }
}