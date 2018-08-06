import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class BetterGeneration {
    private ArrayList<BetterPlayer> players;
    private ArrayList<Field> fields;
    private int cycles, field_width, field_height, input_size, output_size, run_throughs;
    private float mutation_new_neuron, mutation_del_neuron, mutation;

    public BetterGeneration(int generation_size, int field_width, int field_height, int[] hidden_layers, int output_size, int cycles, float mutation_new_neuron, float mutation_del_neuron, float mutation, int run_throughs) {
        input_size = field_width * field_height * 2;
        players = new ArrayList();
        fields = new ArrayList();
        for (int i = 0; i < generation_size; i++) {
            players.add(new BetterPlayer(input_size, hidden_layers, output_size));
            fields.add(new Field(field_width, field_height, 5, false));
        }
        this.cycles = cycles;
        this.output_size = output_size;
        this.field_width = field_width;
        this.field_height = field_height;
        this.mutation_new_neuron = mutation_new_neuron;
        this.mutation_del_neuron = mutation_del_neuron;
        this.mutation = mutation;
        this.run_throughs = run_throughs;
    }

    public BetterGeneration(BetterGeneration g) {
        int generation_size = g.players.size();
        cycles = g.cycles;
        input_size = g.input_size;
        output_size = g.output_size;
        field_width = g.field_width;
        field_height = g.field_height;
        mutation_new_neuron = g.mutation_new_neuron;
        mutation_del_neuron = g.mutation_del_neuron;
        mutation = g.mutation;
        run_throughs = g.run_throughs;
        players = new ArrayList();
        fields = new ArrayList();
        Random rg = new Random();

        Collections.sort(g.players);

        /*
        long add = 0;
        long min = g.players.stream().max(Comparator.naturalOrder()).get().getFitness();
        if (min < 0)
            add = -min;
        */

        /*
        long fitness_sum = 0;
        for (BetterPlayer p : g.players) {
            fitness_sum += (p.getFitness()>0?p.getFitness():0);
        }
        */

        float[] summed_distribution = new float[generation_size];
        float dist_sum = 0;
        for (int p=0;p<generation_size;p++) {
            //long fit = g.players.get(p).getFitness();
            //fit = fit>0?fit:0;
            double f = gauss(p,0,g.players.size()/10)*2;
            dist_sum += f;
            summed_distribution[p] = dist_sum>=1?1:dist_sum;
            //System.out.println(dist_sum);
        }

        g.decimate();
        for (int i = 0; i < generation_size; i++) {
            float rand1 = rg.nextFloat();
            float rand2 = rg.nextFloat();

            int i1 = 0;
            int i2 = 0;

            while (i1 < g.players.size()-1 && rand1 > summed_distribution[i1]) {
                i1++;
            }
            while (i2 < g.players.size()-1 && rand2 > summed_distribution[i2]) {
                i2++;
            }
            while (i2 < g.players.size()-1 && i2 == i1) {
                i2++;
            }

            players.add(new BetterPlayer(g.players.get(i1), g.players.get(i2), mutation_new_neuron, mutation_del_neuron, mutation));
            fields.add(new Field(g.field_width, g.field_height, 5, false));
        }
    }

    private double gauss(double x, double m, double s) {
        return (1/Math.sqrt((2*Math.PI*Math.pow(s,2))))*Math.exp(-(Math.pow(x-m,2))/(2*Math.pow(s,2)));
    }

    public BetterPlayer runGeneration() {
        BigDecimal avg = new BigDecimal("0");
        double min = Double.MAX_VALUE;
        for (int p = 0; p < players.size(); p++) {
            BetterPlayer player = players.get(p);
            long fitness_sum = 0;
            for (int rt=0;rt<run_throughs;rt++) {
                Field f = new Field(field_width, field_height, 5, false);
                int c;
                for (c = 0; c < cycles; c++) {
                    double[] output = player.play(f.toInputArray());
                    int max_i = 0;
                    for (int i = 1; i < output.length; i++) {
                        if (output[i] > output[max_i]) max_i = i;
                    }
                    switch (max_i) {
                        case 0:
                            f.keyPressed(0);
                            break;
                        case 1:
                            f.keyPressed(1);
                            break;
                        case 2:
                            f.keyPressed(' ');
                            break;
                        case 3:
                            f.keyPressed('a');
                            break;
                        case 4:
                            f.keyPressed('d');
                            break;
                        case 5:
                            f.keyPressed(2);
                            break;
                        case 6:
                            break;
                    }
                    f.logic();
                    if (f.isGameOver()) break;
                }
                fitness_sum += f.getFitness();
            }
            double fitness = fitness_sum/run_throughs;
            if (fitness < min) min = fitness;
            player.setFitness(fitness);
            avg = avg.add(new BigDecimal("" + fitness));
        }
        System.out.println("Min fitness: "+min);
        avg = avg.divide(new BigDecimal(""+players.size()));
        System.out.println("Avg fitness: "+avg);
        Collections.sort(players);
        return players.get(0);
    }

    public void decimate() {
        ArrayList<Integer> removes = new ArrayList();
        Random rg = new Random();
        for (int i = 0; i < players.size(); i++) {
            if (rg.nextFloat() < decimateProbability(i)) removes.add(i);
        }

        int del_size = players.size()/2;

        if (removes.size() < del_size) {
            for (int p = players.size() - 1; p > 0 && removes.size() < del_size; p--) {
                if (!removes.contains(p)) removes.add(p);
            }
        }

        Collections.sort(removes);

        for (int r=removes.get(removes.size()-1);removes.size()>1;r=removes.get(removes.size()-1)) {
            players.remove(r);
            removes.remove(removes.size()-1);
        }
        //players.add(new BetterPlayer(players.get(0)));
    }

    public float decimateProbability(int n) {
        return (n * (float)n) / (players.size() * players.size());
    }

    public void setMutation(float mutation) {
        this.mutation = mutation;
    }
}
