import java.util.HashMap;
import java.util.Random;

public class NeuralNet {
    private Neuron[] input_layer;
    private Neuron[] output_layer;
    private Neuron[] hidden_layer;

    public NeuralNet(int input_size, int hidden_size, int output_size) {
        input_layer = new Neuron[input_size];
        hidden_layer = new Neuron[hidden_size];
        output_layer = new Neuron[output_size];

        for (int i = 0; i < output_size; i++) {
            output_layer[i] = new Neuron();
        }

        for (int i = 0; i < hidden_size; i++) {
            Neuron n = new Neuron();
            for (Neuron o : output_layer) {
                n.addOutput(o);
            }
            hidden_layer[i] = n;
        }

        for (int i = 0; i < input_size; i++) {
            Neuron n = new Neuron();
            for (Neuron o : hidden_layer) {
                n.addOutput(o);
            }
            input_layer[i] = n;
        }
    }

    public NeuralNet(double[][][] layers) {
        int input_size = layers[0].length;
        int hidden_size = layers[1].length;
        int output_size = layers[1][1].length;

        input_layer = new Neuron[input_size];
        hidden_layer = new Neuron[hidden_size];
        output_layer = new Neuron[output_size];

        for (int o = 0; o < output_size; o++) {
            output_layer[o] = new Neuron();
        }

        for (int h = 0; h < hidden_size; h++) {
            hidden_layer[h] = new Neuron();
            for (int o = 0; o < output_size; o++) {
                hidden_layer[h].addOutput(output_layer[o], layers[1][h][o]);
            }
        }

        for (int i = 0; i < input_size; i++) {
            input_layer[i] = new Neuron();
            for (int h = 0; h < hidden_size; h++) {
                input_layer[i].addOutput(hidden_layer[h], layers[0][i][h]);
            }
        }

    }

    public void reset() {
        for (Neuron n : input_layer) {
            n.reset();
        }

        for (Neuron n : hidden_layer) {
            n.reset();
        }

        for (Neuron n : output_layer) {
            n.reset();
        }
    }

    public double[] feed_forward(double[] inputs) {
        if (inputs.length != input_layer.length) return null;
        double[] output = new double[output_layer.length];

        for (int i = 0; i < inputs.length; i++) {
            input_layer[i].input += inputs[i];
        }

        for (Neuron n : input_layer) {
            n.process();
        }

        for (Neuron n : hidden_layer) {
            n.process();
        }

        for (int i = 0; i < output_layer.length; i++) {
            output[i] = output_layer[i].output();
        }

        return output;
    }

    public int getInputSize() {
        return input_layer.length;
    }

    public int getHiddenSize() {
        return hidden_layer.length;
    }

    public int getOutputSize() {
        return output_layer.length;
    }

    public double[][] getInputToHidden() {
        double[][] weights = new double[getInputSize()][getHiddenSize()];
        for (int i = 0; i < getInputSize(); i++) {
            Neuron in = input_layer[i];
            for (int j = 0; j < getHiddenSize(); j++) {
                Neuron hd = hidden_layer[j];
                weights[i][j] = in.outputs.get(hd);
            }
        }
        return weights;
    }

    public double[][] getHiddenToOutput() {
        double[][] weights = new double[getHiddenSize()][getOutputSize()];
        for (int i = 0; i < getHiddenSize(); i++) {
            Neuron hd = hidden_layer[i];
            for (int j = 0; j < getOutputSize(); j++) {
                Neuron out = output_layer[j];
                weights[i][j] = hd.outputs.get(out);
            }
        }
        return weights;
    }

    private class Neuron {
        private double input;
        private HashMap<Neuron, Double> outputs = new HashMap();
        private Random rg = new Random();

        public void reset() {
            input = 0;
        }

        public void addOutput(Neuron n) {
            addOutput(n, rg.nextDouble() * 2 - 1);
        }

        public void addOutput(Neuron n, double weight) {
            outputs.put(n, weight);
        }

        public double sigmoid(double f) {
            return 1 / (1 + 1 / (double) Math.exp(f));
        }

        private double output() {
            return sigmoid(input);
        }

        public void process() {
            for (Neuron n : outputs.keySet()) {
                n.input += (output() * outputs.get(n));
            }
        }
    }
}