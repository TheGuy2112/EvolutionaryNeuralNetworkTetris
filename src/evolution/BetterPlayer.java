package evolution;

import general.Matrix;
import general.NeuralNet1;

import java.util.Random;

public class BetterPlayer implements Comparable<BetterPlayer>{
    private NeuralNet1 brain;
    private double fitness;

    public BetterPlayer(BetterPlayer bp) {
        brain = new NeuralNet1(bp.brain.getLayerSizes());
    }

    public BetterPlayer(int input_size, int[] hidden_layers, int output_size) {
        int[] layers = new int[hidden_layers.length+2];
        layers[0] = input_size;
        layers[layers.length-1] = output_size;
        for (int i=0;i<hidden_layers.length;i++) {
            layers[i+1] = hidden_layers[i];
        }
        brain = new NeuralNet1(layers);
    }

    public BetterPlayer(BetterPlayer parent1, BetterPlayer parent2, float mutation_new_neuron, float mutation_del_neuron, float mutation) {
        Matrix[] brain1 = parent1.brain.getLayers();
        Matrix[] brain2 = parent2.brain.getLayers();

        Matrix[] new_brain = new Matrix[brain1.length];

        Random rg = new Random();
        int in_add = 0;
        int out_add, rows, cols;

        rows = rg.nextFloat()<0?brain1[0].rows():brain2[0].rows();
        for (int i=0;i<new_brain.length-1;i++) {
            cols = rg.nextFloat()<0?brain1[i].cols():brain2[i].cols();

            if (rg.nextFloat() < mutation_del_neuron) cols--;
            if (rg.nextFloat() < mutation_new_neuron) cols++;

            new_brain[i] = cross(brain1[i],brain2[i], rows, cols, mutation);

            rows = cols;
        }
        new_brain[brain1.length-1] = cross(brain1[brain1.length-1],brain2[brain1.length-1],rows, brain1[brain1.length-1].cols(), mutation);

        brain = new NeuralNet1(new_brain);
    }

    private static Matrix cross(Matrix m1, Matrix m2, int rows, int cols, float mutation) {
        Random rg = new Random();
        Matrix n = new Matrix(rows, cols);
        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                Double val1 = m1.valueAt(r,c);
                Double val2 = m2.valueAt(r,c);
                Double val = 0d;
                if (rg.nextFloat() <= mutation) n.setValueAt(rg.nextFloat()*2-1,r,c);
                else {
                    if (val1 == null) val = (val2 == null ? rg.nextFloat() * 2 - 1 : val2);
                    else if (val2 == null) val = (val1 == null ? rg.nextFloat() * 2 - 1 : val1);
                    else {
                        if (rg.nextFloat()<0.5)
                            val = (rg.nextFloat()<0.5?val1:val2);
                        else
                            val = (val1+val2)/2;
                    }
                    val+=(rg.nextFloat()<mutation?rg.nextFloat()*0.5-0.25:0);
                    n.setValueAt(val, r, c);
                }
            }
        }
        return n;
    }

    public double[] play(double[] inputs) {
        return brain.feed_forward(inputs);
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public void save_brain(String file) {
        brain.save(file);
    }

    @Override
    public int compareTo(BetterPlayer p) { return p.fitness==fitness?0:(int)((p.fitness - fitness)/(Math.abs(p.fitness-fitness))); }
}
