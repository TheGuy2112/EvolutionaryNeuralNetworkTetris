import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Player implements Comparable<Player> {
    private NeuralNet brain;
    private long fitness;

    public Player(int input_size, int hidden_size, int output_size) {
        brain = new NeuralNet(input_size, hidden_size, output_size);
    }

    public Player(Player parent1, Player parent2, double mutation_new_neuron, double mutation_del_neuron) {
        double[][][] weights = new double[2][][];

        int neuron_change = 0;
        double rand = (new Random()).nextDouble();
        if (rand < mutation_del_neuron) neuron_change = -1;
        else if (rand < mutation_new_neuron) neuron_change = 1;

        if (parent1.brain.getHiddenSize() == parent2.brain.getHiddenSize()) {
            weights[0] = cross(parent1.brain.getInputToHidden(), parent2.brain.getInputToHidden());
            weights[1] = cross(parent1.brain.getHiddenToOutput(), parent2.brain.getHiddenToOutput());
        } else {
            Random rg = new Random();
            int min_size = Math.min(parent1.brain.getHiddenSize(), parent2.brain.getHiddenSize());
            int max_size = Math.max(parent1.brain.getHiddenSize(), parent2.brain.getHiddenSize());
            int diff = max_size - min_size;
            int size = min_size;
            for (int i = 0; i < diff; i++) {
                if (rg.nextDouble() < 0.5) size++;
            }

            ArrayList<Integer> indices = new ArrayList();
            for (int i = 0; i < max_size; i++) {
                indices.add(i);
            }

            for (int i = 0; i < max_size - size; i++) {
                indices.remove((int) (rg.nextDouble() * indices.size()));
            }

            double[][] p1 = parent1.brain.getInputToHidden();
            double[][] p2 = parent2.brain.getInputToHidden();

            Double[][] arr1 = new Double[parent1.brain.getInputSize()][size];
            Double[][] arr2 = new Double[parent1.brain.getInputSize()][size];

            for (int i = 0; i < parent1.brain.getInputSize(); i++) {
                for (int h = 0; h < size; h++) {
                    int h_idx = indices.get(h);
                    arr1[i][h] = h_idx < p1[i].length ? p1[i][h_idx] : null;
                    arr2[i][h] = h_idx < p2[i].length ? p2[i][h_idx] : null;
                }
            }

            weights[0] = cross(arr1, arr2);

            p1 = parent1.brain.getHiddenToOutput();
            p2 = parent2.brain.getHiddenToOutput();

            arr1 = new Double[size][parent1.brain.getOutputSize()];
            arr2 = new Double[size][parent1.brain.getOutputSize()];

            for (int i = 0; i < size; i++) {
                for (int h = 0; h < parent1.brain.getOutputSize(); h++) {
                    int h_idx = indices.get(i);
                    arr1[i][h] = h_idx < p1.length ? p1[h_idx][h] : null;
                    arr2[i][h] = h_idx < p2.length ? p2[h_idx][h] : null;
                }
            }

            weights[1] = cross(arr1, arr2);
        }

        if (neuron_change == -1) {
            for (int i = 0; i < weights[0].length; i++) {
                weights[0][i] = Arrays.copyOfRange(weights[0][i], 0, weights[1].length - 1);
            }
            for (int h = 0; h < weights[1].length - 1; h++) {
                weights[1][h] = Arrays.copyOfRange(weights[1][h], 0, weights[1][0].length);
            }
        } else if (neuron_change == 1) {
            Random rg = new Random();
            double[][] new_weights_0 = new double[weights[0].length][weights[1].length + 1];
            double[][] new_weights_1 = new double[weights[1].length + 1][weights[1][0].length];

            for (int i = 0; i < weights[0].length; i++) {
                for (int h = 0; h < weights[1].length; h++) {
                    new_weights_0[i][h] = weights[0][i][h];
                }
                new_weights_0[i][weights[1].length] = rg.nextDouble() * 2 - 1;
            }

            for (int h = 0; h < weights[1].length; h++) {
                for (int o = 0; o < weights[1][0].length; o++) {
                    new_weights_1[h][o] = weights[1][h][o];
                }
            }
            for (int o = 0; o < weights[1][0].length; o++) {
                new_weights_1[weights[1].length][o] = rg.nextDouble() * 2 - 1;
            }
            weights[0] = new_weights_0;
            weights[1] = new_weights_1;
        }

        brain = new NeuralNet(weights);
    }

    public double[][] cross(double[][] arr1, double[][] arr2) {
        Random rg = new Random();
        double[][] out = new double[arr1.length][arr1[0].length];
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr1[i].length; j++) {
                out[i][j] = rg.nextDouble() < 0.5 ? arr1[i][j] : arr2[i][j];
            }
        }
        return out;
    }

    public double[][] cross(Double[][] arr1, Double[][] arr2) {
        Random rg = new Random();
        double[][] out = new double[arr1.length][arr1[0].length];
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr1[i].length; j++) {
                if (arr1[i][j] == null) out[i][j] = arr2[i][j];
                else if (arr2[i][j] == null) out[i][j] = arr1[i][j];
                else out[i][j] = rg.nextDouble() < 0.5 ? arr1[i][j] : arr2[i][j];
            }
        }
        return out;
    }

    public double[] play(double[] inputs) {
        brain.reset();
        return brain.feed_forward(inputs);
    }

    public void setFitness(long fitness) {
        this.fitness = fitness;
    }

    public long getFitness() {
        return fitness;
    }

    @Override
    public int compareTo(Player p) { return p.fitness==fitness?0:(int)((p.fitness - fitness)/(Math.abs(p.fitness-fitness))); }
}