package general;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class Matrix {
    private int rows, cols;
    private double arr[];

    public Matrix(int rows, int columns) {
        this.rows = Math.abs(rows);
        this.cols = Math.abs(columns);
        arr = new double[rows*columns];
        for (int i=0;i<arr.length;i++) arr[i] = 0;
    }

    private static Matrix createFromArray(double[] arr, int rows, int cols) {
        if (arr == null) return null;
        Matrix m = new Matrix(rows, cols);
        if (rows*cols != arr.length) return null;
        else {
            for (int r=0;r<rows;r++) {
                for (int c=0;c<cols;c++) {
                    m.arr[r*cols+c] = arr[r*cols+c];
                }
            }
        }
        return m;
    }

    public static Matrix fromArray(double[][] arr) {
        if (arr == null) return null;
        Matrix m = new Matrix(arr.length,arr[0].length);
        for (int r=0;r<arr.length;r++) {
            for (int c=0;c<arr[0].length;c++) {
                m.arr[r*arr[0].length+c] = arr[r][c];
            }
        }
        return m;
    }

    public static Matrix identity(int rows) {
        rows = Math.abs(rows);
        Matrix m = new Matrix(rows,rows);
        for (int r=0;r<rows;r++) {
            m.arr[r*rows+r] = 1;
        }
        return m;
    }

    public static Matrix fromString(String s) {
        if (s==null || s.length()==0) return null;
        String[] rows = s.split(";");
        double[][] elements = new double[rows.length][];

        for (int i=0;i<rows.length;i++) {
            String[] splitted = rows[i].split(",");
            elements[i] = new double[splitted.length];
            for (int j=0;j<splitted.length;j++) {
                elements[i][j] = Double.parseDouble(splitted[j]);
            }
        }
        return Matrix.fromArray(elements);
    }

    public static Matrix randomMatrix(int rows, int cols, double min, double max) {
        Random rg = new Random();
        Matrix m = new Matrix(rows,cols);
        for (int i=0;i<m.arr.length;i++) {
            m.arr[i] = rg.nextFloat()*(max-min)+min;
        }
        return m;
    }

    public static Matrix rowVectorFromArray(double[] arr) {
        if (arr == null) return null;
        double[][] mat = new double[1][arr.length];
        mat[0] = arr;
        return fromArray(mat);
    }

    public static Matrix columnVectorFromArray(double[] arr) {
        return rowVectorFromArray(arr).transpose();
    }

    public Matrix copy() {
        Matrix m = new Matrix(rows,cols);
        m.arr = Arrays.copyOf(arr,arr.length);
        return m;
    }

    public int rows() {return rows;}

    public int cols() {return cols;}

    public Matrix rowAt(int row) {
        if (row > rows-1) return null;
        double[] arr = new double[cols];
        for (int c=0;c<cols;c++) {
            arr[c] = this.arr[row*cols+c];
        }
        return createFromArray(arr,1,cols);
    }

    public Matrix colAt(int col) {
        if (col > cols-1) return null;
        double[] arr = new double[rows];
        for (int r=0;r<rows;r++) {
            arr[r] = this.arr[r*cols+col];
        }
        return createFromArray(arr,rows,1);
    }

    public Double valueAt(int row, int col) {
        if (row > rows-1 || col > cols-1) return null;
        return arr[row*cols+col];
    }

    public void setValueAt(double value, int row, int col) {
        if (row > rows-1 || col > cols-1) return;
        arr[row*cols+col] = value;
    }

    public double[][] toArray() {
        double[][] arr = new double[rows][cols];
        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                arr[r][c] = this.arr[r*cols+c];
            }
        }
        return arr;
    }

    public Matrix add(Matrix m) {
        if (m==null || m.rows != rows || m.cols != cols) return null;
        Matrix copy = m.copy();
        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                copy.arr[r*cols+c] += arr[r*cols+c];
            }
        }
        return copy;
    }

    public Matrix mult(Matrix m) {
        if (m==null || cols != m.rows) return null;
        Matrix result = new Matrix(rows,m.cols);
        for (int m_c=0;m_c<m.cols;m_c++) {
            for (int r=0;r<rows;r++) {
                double sum = 0;
                for (int c=0;c<cols;c++) {
                    sum+=arr[r*cols+c]*m.arr[c*m.cols+m_c];
                }
                result.arr[r*m.cols+m_c] = sum;
            }
        }
        return result;
    }

    private Matrix multRowPart(Matrix m, int row_start, int row_end) {
        if (m==null || cols != m.rows || row_end > rows-1 || row_start < 0) return null;
        Matrix result = new Matrix(row_end+1-row_start,m.cols);
        for (int m_c=0;m_c<m.cols;m_c++) {
            for (int r=row_start;r<row_end+1;r++) {
                double sum = 0;
                for (int c=0;c<cols;c++) {
                    sum+=arr[r*cols+c]*m.arr[c*m.cols+m_c];
                }
                result.arr[(r-row_start)*m.cols+m_c] = sum;
            }
        }
        return result;
    }

    public Matrix multMT(Matrix m) {
        if (m==null || cols != m.rows) return null;
        int threads = 4;
        Matrix[] result = new Matrix[threads];
        int rows_per_thread = rows/threads;
        Thread[] thread_arr = new Thread[threads];

        Semaphore s = new Semaphore(4);

        for (int t=0;t<threads;t++) {
            thread_arr[t] = new Thread(new MultiplicationRunnable(s,t,rows_per_thread,this,m, result));
            thread_arr[t].start();
        }

        for (int t=0;t<threads;t++) {
            try {
                s.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Matrix ret = result[0];

        for (int t=1;t<threads;t++) {
            ret = ret.attach(result[t],2);
        }

        return ret;
    }

    public Matrix multElements(Matrix m) {
        if (m==null || m.rows != rows || m.cols != cols) return null;
        Matrix result = m.copy();
        for (int i=0;i<arr.length;i++) {
            result.arr[i] *= arr[i];
        }
        return result;
    }

    public Matrix scale(double d) {
        Matrix m = this.copy();
        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                m.arr[r*cols+c] *= d;
            }
         }
         return m;
    }

    public Matrix transpose() {
        Matrix m = new Matrix(cols,rows);
        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                m.arr[c*rows+r] = arr[r*cols+c];
            }
        }
        return m;
    }

    public Matrix inverse() {
        //TODO
        return null;
    }

    public boolean equals(Matrix m) {
        if (m==null || m.rows != rows || m.cols != cols) return false;
        boolean equal = true;
        for (int i=0;i<arr.length&&equal;i++) {
            equal&=arr[i]==m.arr[i];
        }
        return equal;
    }

    public Matrix attach(Matrix m, int position) {
        //0 ... top
        //1 ... right
        //2 ... bottom
        //3 ... left
        if ( m==null || position%2==1 && m.rows != rows || position%2==0 && m.cols != cols) return null;

        Matrix ret = null;
        if (position==0) return m.attach(this,2);
        else if (position==3) return m.attach(this,1);
        else if (position==1) {
            ret = new Matrix(rows,cols+m.cols);
            for (int r=0;r<rows;r++) {
                for (int c=0;c<cols;c++) {
                    ret.arr[r*ret.cols+c] = arr[r*cols+c];
                }
                for (int c=0;c<m.cols;c++) {
                    ret.arr[r*ret.cols+c+cols] = m.arr[r*m.cols+c];
                }
            }
        } else if (position==2) {
            ret = new Matrix(rows+m.rows,cols);
            for (int c=0;c<cols;c++) {
                for (int r=0;r<rows;r++) {
                    ret.arr[r*cols+c] = arr[r*cols+c];
                }
                for (int r=0;r<m.rows;r++) {
                    ret.arr[(r+rows)*cols+c] = m.arr[r*cols+c];
                }
            }
        }
        return ret;
    }

    public Matrix getMatrixPart(int start_row, int start_column, int end_row, int end_column) {
        if (start_row < 0 || start_column < 0 || end_row > rows-1 || end_column > cols-1) return null;
        Matrix m = new Matrix(end_row+1-start_row,end_column+1-start_column);
        for (int r=start_row;r<end_row+1;r++) {
            for (int c=start_column;c<end_column;c++) {
                m.arr[(r-start_row)*m.cols+(c-start_column)] = arr[r*cols+c];
            }
        }
        return m;
    }

    public Matrix doFunction(Function<Double, Double> fn) {
        Matrix out = new Matrix(rows, cols);
        for (int i=0;i<rows*cols;i++) {
            out.arr[i] = fn.apply(arr[i]);
        }
        return out;
    }

    @Override
    public String toString() {
        String[][] values = new String[rows][cols];
        String ret = rows+"x"+cols+"-general.Matrix:\n";

        int margin = 2;
        int max_width = Integer.MIN_VALUE;
        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                String val = String.valueOf(arr[r*cols+c]);
                values[r][c] = val;
                if (max_width < val.length()) max_width = val.length();
            }
        }

        max_width += margin;

        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                String val = values[r][c];
                for (int w=0;w<max_width-val.length();w++) {
                    ret+=" ";
                }
                ret+=val;
            }
            ret+="\n";
        }

        return ret;
    }

    public Matrix sigmoid() {
        Matrix m = new Matrix(rows,cols);
        for (int r=0;r<rows;r++) {
            for (int c=0;c<cols;c++) {
                m.arr[c*rows+r] = 1/(1+(1/Math.exp(arr[c*rows+r])));
            }
        }
        return m;
    }

    private class MultiplicationRunnable implements Runnable {

        private Matrix m1, m2;
        private Matrix[] result;
        private int t, rows_per_thread;
        private Semaphore s;

        public MultiplicationRunnable(Semaphore s, int t, int rows_per_thread, Matrix m1, Matrix m2, Matrix[] result) {
            this.m1 = m1;
            this.m2 = m2;
            this.result = result;
            this.t = t;
            this.s = s;
            this.rows_per_thread = rows_per_thread;
            try {
                s.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            result[t] = m1.multRowPart(m2,t*rows_per_thread,(t+1)*rows_per_thread-1);
            s.release();
        }
    }

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        Matrix m1 = Matrix.randomMatrix(1000,1000,0,1);
        Matrix m2 = Matrix.randomMatrix(1000,1000,0,1);
        //general.Matrix m3 = general.Matrix.randomMatrix(5000,5000,0,1);
        //general.Matrix m4 = general.Matrix.randomMatrix(5000,5000,0,1);
        long creation_time = (System.currentTimeMillis()-time)/5;

        time = System.currentTimeMillis();
        Matrix result = m1.add(m2);
        long add_time = System.currentTimeMillis()-time;

        time = System.currentTimeMillis();
        result = m1.mult(m2);
        long matrix_mult_time = System.currentTimeMillis()-time;

        time = System.currentTimeMillis();
        Matrix result1 = m1.multMT(m2);
        long matrix_multMT_time = System.currentTimeMillis()-time;

        boolean equal = result.equals(result1);

        time = System.currentTimeMillis();
        result = m1.multElements(m2);
        long mult_time = System.currentTimeMillis()-time;

        time = System.currentTimeMillis();
        result = m1.attach(m2,0);
        result = m1.attach(m2,1);
        result = m1.attach(m2,2);
        result = m1.attach(m2,3);
        long attach_time = (System.currentTimeMillis()-time)/4;

        time = System.currentTimeMillis();
        result = m1.transpose();
        result = m2.transpose();
        long transpose_time = (System.currentTimeMillis()-time)/2;

        System.out.println("Time for random creation:"+creation_time+"ms");
        System.out.println("Time for addition:"+add_time+"ms");
        System.out.println("Time for matrix multiplication:"+matrix_mult_time+"ms");
        System.out.println("Time for multi-threaded matrix multiplication:"+matrix_multMT_time+"ms");
        System.out.println("equal: "+equal);
        System.out.println("Time for element-wise multiplication:"+mult_time+"ms");
        System.out.println("Time for attaching:"+attach_time+"ms");
        System.out.println("Time for transposing:"+transpose_time+"ms");

        Matrix m5 = Matrix.randomMatrix(3,3,-10,10);
        System.out.println(m5);
        Function<Double,Double> sigmoid = d->(1/(1+(1/Math.exp(d))));
        m5 = m5.doFunction(sigmoid);
        System.out.println(m5);
    }
}