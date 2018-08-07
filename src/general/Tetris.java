package general;

import evolution.*;
import tetris.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Tetris {
    //private static Generation g;
    private static BetterGeneration g;
    //private static Player p;
    private static BetterPlayer p;
    private static BetterPlayer best_player;

    private static boolean ai_play = true;
    private static int step;
    private static boolean watch_player = false;
    private static Field f;

    private static boolean seeded = true;

    private static int population = 500, hidden_layer = 40, cycles = 5000,
            field_width = 10, field_height = 20,
            cycle = 0, generation = 0, run_throughs = 1;
    private static int[] hidden_layers = new int[]{100,75,25};
    private static float mutation_new_neuron = 0.008f, mutation_del_neuron = 0.005f, mutation = 0.05f, mutation_scale=0.99f, min_mutation=0.01f;
    private static int scale_after_gens = 20;
    private static boolean mutation_decrease = false;

    private static int save_after_gens = 50;
    private static boolean autosave = true;

    private static ArrayList<Double> fitness = new ArrayList();
    private static double max_fitness = Double.MIN_VALUE, current_fitness=0;

    private static boolean autorun = false, drawing_fitness = true;

    private static void setup() {
        //size(405,605);
        if (ai_play) {
            //g = new Generation(population, field_width, field_height, hidden_layer, 6, cycles, mutation_new_neuron, mutation_del_neuron);
            g = new BetterGeneration(population, field_width, field_height, hidden_layers, 6, cycles, mutation_new_neuron, mutation_del_neuron, mutation, run_throughs, seeded);
            step = 0;
            cycle = 0;
            f = new Field(field_width, field_height, 5, false);
        } else {
            f = new Field(field_width, field_height);
        }
    }

    private static void draw() {
       /* //frameRate(1000);
        if (ai_play) {
            //background(50);
            //textSize(30);
            //if (drawing_fitness) draw_fitness();
            //textSize(40);
            if (!autorun) {
                if (step == 0) {
                    System.out.println("Run generation " + generation + "?");
                } else if (step == 1) {
                    frameRate(15);
                    if (cycle < cycles) {
                        boolean[][] field = f.getField();
                        float[] input = new float[field.length*field[0].length];
                        for (int x=0;x<field.length;x++) {
                            for (int y=0;y<field[x].length;y++) {
                                input[x+y*field.length] = field[x][y]?1f:0f;
                            }
                        }
                        float[] output = p.play(input);
                        int max_i = 0;
                        for (int i=1;i<output.length;i++) {
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
                        cycle++;
                    }
                    f.draw();
                } else {
                    System.out.println("Create next generation?");
                }
            } else {
                if (step != 0) {
                    //g = new Generation(g);
                    g = new BetterGeneration(g);
                    generation++;
                    step = 0;
                } else {
                    System.out.println("Generation " + generation);
                    p = g.runGeneration();
                    if (best_player == null || best_player.getFitness() < p.getFitness()) best_player = p;
                    fitness_add(p.getFitness());
                    System.out.println("Best of this gen: " + p.getFitness());
                    System.out.println("Best overall: "+max_fitness);
                    System.out.println("--------------------------------");
                    //g = new Generation(g);
                    g = new BetterGeneration(g);
                    if (mutation > 0.01) {
                        if (generation % 20 == 0) {
                            mutation *= 0.99f;
                            g.setMutation(mutation);
                        }
                    } else if (mutation < 0.01) {
                        mutation = 0.01f;
                        g.setMutation(mutation);
                    }
                    generation++;
                }
            }
        } else {
            f.logic();
            //f.draw();
        }*/

        System.out.println("Generation " + generation);
        p = g.runGeneration();
        if (best_player == null || best_player.getFitness() < p.getFitness()) best_player = p;
        fitness_add(p.getFitness());
        System.out.println("Best of this gen: " + p.getFitness());
        System.out.println("Best overall: "+best_player.getFitness());
        System.out.println("--------------------------------");
        //g = new Generation(g);
        g = new BetterGeneration(g);
        if (mutation_decrease) {
            if (mutation > min_mutation) {
                if (generation % scale_after_gens == 0) {
                    mutation *= mutation_scale;
                    g.setMutation(mutation);
                }
            } else if (mutation < min_mutation) {
                mutation = min_mutation;
                g.setMutation(mutation);
            }
        }
        if (autosave && (generation+1) % save_after_gens == 0) {
            best_player.save_brain("best_brain_gen"+generation+".nn");
        }
        generation++;
        f.logic();
    }

    {
    /*private static void keyPressed() {
        if (!ai_play)
            f.keyPressed(key==CODED?keyCode:key);
        else {
            int val = key==CODED?keyCode:key;
            if (val == ' ') {
                if (step == 0)  {
                    p = g.runGeneration();
                    fitness_add(p.getFitness());
                } else if (step == 1) {
                    cycle = 0;
                    f = new tetris.Field(field_width, field_height, 5, false);
                } else if (step == 2) {
                    g = new Generation(g);
                    generation++;
                }
                step = (step+1)%3;
            } else if (val == ENTER) {
                autorun = !autorun;
            } else if (val == 'd') {
                drawing_fitness = !drawing_fitness;
            }
        }
    }

    private static void draw_fitness() {
        stroke(color(0,255,0));
        for (int i=0;i<fitness.size();i++) {
            line(width-fitness.size()+i,height-fitness.get(i)/(float)max_fitness*100,width-fitness.size()+i,height);
        }
        textSize(14);
        text("Best: "+max_fitness,width-200,height-140);
    }*/
    }

    private static void fitness_add(double fit) {
        current_fitness = fit;
        //fitness.add(fit);
        //if (fitness.size() > width) fitness.remove(0);
        if (fit > max_fitness) max_fitness = fit;
    }

    public static void main(String[] args) {
        boolean active_logging = true;
        autorun = true;
        setup();
        Scanner sc = new Scanner(System.in);
        System.out.print("Activate csv logging? 0=yes, 1=no [yes]");
        if (sc.hasNextInt() && sc.nextInt() == 1) active_logging = false;
        FileWriter fw = null;
        if (active_logging) {
            try {
                fw = new FileWriter(new File("log.csv"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (true) {
            System.out.print("How many generations should be run? ");
            if (sc.hasNextInt()) {
                int gens = sc.nextInt();
                if (gens == -1) break;
                for (int g = 0; g < gens; g++) {
                    draw();
                    if (active_logging) {
                        try {
                            fw.write(generation + ";" + current_fitness+"\n");
                            fw.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        try {
            fw.close();
            best_player.save_brain("best_brain.nn");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}