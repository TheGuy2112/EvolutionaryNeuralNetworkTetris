import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class NeuralNet1 {
    private Matrix[] layer_weights;

    public NeuralNet1(int[] layer_sizes) {
        layer_weights = new Matrix[layer_sizes.length-1];
        for (int i=0;i<layer_sizes.length-1;i++) {
            layer_weights[i] = Matrix.randomMatrix(layer_sizes[i],layer_sizes[i+1],-1,1);
        }
    }

    public NeuralNet1(Matrix[] layers) {
        this.layer_weights = layers;
    }

    public NeuralNet1(String file) {
        load(file);
    }

    public Matrix[] getLayers() {
        return layer_weights;
    }

    public int[] getLayerSizes() {
        int[] layer_sizes = new int[layer_weights.length+1];

        for (int i=0;i<layer_sizes.length-1;i++) {
            layer_sizes[i] = layer_weights[i].rows();
        }
        layer_sizes[layer_sizes.length-1] = layer_weights[layer_weights.length-1].cols();
        return layer_sizes;
    }

    public int getInputSize() {
        return layer_weights[0].rows();
    }

    public int getOutputSize() {
        return layer_weights[layer_weights.length-1].cols();
    }

    public double[] feed_forward(double[] inputs) {
        Matrix input = Matrix.rowVectorFromArray(inputs);
        for (int layer=0;layer<layer_weights.length;layer++) {
            input = (input.mult(layer_weights[layer])).sigmoid();
        }
        return input.toArray()[0];
    }

    public Matrix feed_forward(Matrix inputs) {
        Matrix input = inputs.copy();

        if (inputs.rows() != 1 && inputs.cols() == 1) input.transpose();
        else if (inputs.rows() != 1) return null;

        for (int layer=0;layer<layer_weights.length;layer++) {
            input = (input.mult(layer_weights[layer])).sigmoid();
        }
        return input;
    }

    public void printInfo() {
        System.out.println("Input neurons: "+layer_weights[0].rows());
        for (int layer=1;layer<layer_weights.length;layer++) {
            System.out.println("Hidden neurons: "+layer_weights[layer].rows());
        }
        System.out.println("Output neurons: "+layer_weights[layer_weights.length-1].cols());
    }

    public void printWeights() {
        for (int l=0;l<layer_weights.length;l++) {
            Matrix layer = layer_weights[l];
            for (int r=0;r<layer.rows();r++) {
                System.out.print("L"+l+"N"+r+": ");
                Matrix row = layer.rowAt(r);
                for (int c=0;c<row.cols();c++) {
                    System.out.print(row.valueAt(0,c)+" ");
                }
                System.out.println();
            }
        }
    }

    @Override
    public String toString() {
        String out = "";
        for (int l=0;l<layer_weights.length;l++) {
            Matrix layer = layer_weights[l];
            for (int r=0;r<layer.rows();r++) {
                out+=("L"+l+"N"+r+" ");
                Matrix row = layer.rowAt(r);
                for (int c=0;c<row.cols();c++) {
                    out+=(row.valueAt(0,c)+" ");
                }
                out+="\n";
            }
        }
        return out;
    }

    public void save(String file) {
        try {
            FileWriter fw = new FileWriter(new File(file));
            fw.write(this+"");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(String file) {
        try {
            Scanner sc = new Scanner(new File(file));
            ArrayList<ArrayList<ArrayList<Double>>> layers = new ArrayList();
            int last_layer = -1;
            int last_neuron = -1;
            while (sc.hasNextLine()) {
                String[] split = sc.nextLine().split(" ");
                int curr_layer = Integer.valueOf(split[0].split("L")[1].split("N")[0]);
                int curr_neuron = Integer.valueOf(split[0].split("N")[1]);
                if (curr_layer != last_layer) {
                    layers.add(new ArrayList());
                }
                for (int i=1;i<split.length;i++) {
                    double weight = Double.parseDouble(split[i]);
                    if (curr_neuron != last_neuron)
                        layers.get(curr_layer).add(new ArrayList());
                    layers.get(curr_layer).get(curr_neuron).add(weight);
                    last_neuron = curr_neuron;
                }
                last_layer = curr_layer;
            }
            Matrix[] m_layers = new Matrix[layers.size()];
            for (int i=0;i<m_layers.length;i++) {
                int rows = layers.get(i).size();
                int cols = layers.get(i).get(0).size();
                m_layers[i] = new Matrix(rows,cols);
                for (int r=0;r<rows;r++) {
                    for (int c=0;c<cols;c++) {
                        m_layers[i].setValueAt(layers.get(i).get(r).get(c),r,c);
                    }
                }
            }
            this.layer_weights = m_layers;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        NeuralNet1 nn = new NeuralNet1(new int[]{2,3,4});
        nn.save("try.nn");
        NeuralNet1 nn1 = new NeuralNet1("try.nn");

        System.out.println(nn);
        System.out.println(nn1);
    }
}