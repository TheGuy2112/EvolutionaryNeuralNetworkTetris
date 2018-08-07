package evolution;

import tetris.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Generation {
    private ArrayList<Player> players;
    private ArrayList<Field> fields;
    private int cycles, field_width, field_height, input_size, output_size;
    private float mutation_new_neuron, mutation_del_neuron;

    public Generation(int generation_size, int field_width, int field_height, int hidden_size, int output_size, int cycles, float mutation_new_neuron, float mutation_del_neuron) {
        input_size = field_width * field_height;
        players = new ArrayList();
        fields = new ArrayList();
        for (int i = 0; i < generation_size; i++) {
            players.add(new Player(input_size, hidden_size, output_size));
            fields.add(new Field(field_width, field_height, 5, false));
        }
        this.cycles = cycles;
        this.output_size = output_size;
        this.field_width = field_width;
        this.field_height = field_height;
        this.mutation_new_neuron = mutation_new_neuron;
        this.mutation_del_neuron = mutation_del_neuron;
    }

    public Generation(Generation g) {
        int generation_size = g.players.size();
        cycles = g.cycles;
        input_size = g.input_size;
        output_size = g.output_size;
        field_width = g.field_width;
        field_height = g.field_height;
        mutation_new_neuron = g.mutation_new_neuron;
        mutation_del_neuron = g.mutation_del_neuron;
        players = new ArrayList();
        fields = new ArrayList();
        Random rg = new Random();

        Collections.sort(g.players);

        long fitness_sum = 0;
        for (Player p : g.players) {
            fitness_sum+= (p.getFitness()>0?p.getFitness():0);
        }

        float[] summed_distribution = new float[generation_size];
        float dist_sum = 0;
        for (int p=0;p<generation_size;p++) {
            long fit = g.players.get(p).getFitness();
            dist_sum += (fit>0?fit:0)/(float)fitness_sum;
            summed_distribution[p] = dist_sum;
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

            players.add(new Player(g.players.get(i1), g.players.get(i2), mutation_new_neuron, mutation_del_neuron));
            fields.add(new Field(g.field_width, g.field_height, 5, false));
        }
    }

    public Player runGeneration() {
        for (int p = 0; p < players.size(); p++) {
            //if (p % 10 == 0) System.out.println("Player " + p + "/" + (players.size() - 1));
            Player player = players.get(p);
            Field f = fields.get(p);
            for (int c = 0; c < cycles; c++) {
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
                        f.keyPressed(2);
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
                }
                f.logic();
                if (f.isGameOver()) {
                    break;
                }
            }
            player.setFitness(f.getFitness());
        }
        Collections.sort(players);
        return players.get(0);
    }

    public void decimate() {
        ArrayList<Integer> removes = new ArrayList();
        Random rg = new Random();
        for (int i = 0; i < players.size(); i++) {
            if (rg.nextFloat() < decimateProbability(i)) removes.add(i);
        }

        if (removes.size() < players.size() / 2) {
            for (int p = players.size() - 1; p > 0 && removes.size() <= players.size() / 2; p--) {
                if (!removes.contains(p)) removes.add(p);
            }
        }

        Collections.sort(removes);

        for (int r=removes.get(removes.size()-1);removes.size()>1;r=removes.get(removes.size()-1)) {
            //System.out.println(""+r);
            players.remove(r);
            removes.remove(removes.size()-1);
        }
        /*for (int i=0;i<players.size()/2;i++) {
            players.remove(players.size()-1);
        }*/
    }

    public float decimateProbability(int n) {
        return (n * (float)n) / (players.size() * players.size());
    }
}