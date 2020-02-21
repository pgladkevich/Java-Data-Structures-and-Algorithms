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
    class setValFunc2 implements IntUnaryFunction {
        public int val;
        public setValFunc2 (int n) {
            this.val = n;
        }

        @Override
        public int apply (int n) {
            return n;
        }
    }

    class addFunc implements IntUnaryFunction {
        @Override
        public int apply(int x) {
            setValFunc2 val2 = new setValFunc2();
            return x + setValFunc2.val;m
        }
//        private int _n;
//        public addFunc(int _n) {
//            this._n;
//        }
//        @Override
//        public int apply(int x) {
//            return x;
//        }
    }
}