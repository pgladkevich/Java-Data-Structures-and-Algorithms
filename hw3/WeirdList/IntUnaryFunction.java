/* DO NOT CHANGE THIS FILE. */


/** An IntUnaryFunction represents a function from
 *  integers to integers.
 *  @author P. N. Hilfinger
 */
public interface IntUnaryFunction {
    /** Return the result of applying this function to X. */
    int apply(int x);

    class setValFunc1 implements IntUnaryFunction {
        @Override
        public int apply (int x) {
            return x;
        }
    }

    class addFunc implements IntUnaryFunction {
        private int n;

        public void setN (int n) {
            this.n = n;
        }

        @Override
        public int apply(int x) {
            return x + n;
        }
    }

    class sumFunc implements IntUnaryFunction {
        private int total = 0;

        @Override
        public int apply(int x) {
            return total +=x ;
        }

        public int returnT () {
            return total;
        }
    }
}