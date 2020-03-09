package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Pavel Gladkevich
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        // Need to call printMessageLine and then send the results to _output
        // There should be a newline at the end of every output
    }


    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
             /* the information in _config will be in the order alphabet, numRotors, numPawls, rotors info.
     Information will consistently be in that order, but will not have
     a consistent format and we should check for everything on one line, everything on separate lines,
     some things on the same, some things separate, etc etc. and make sure we have error checks along the way. */

             /*  Need to figure out how to account for cycles that span multiple lines:

         am using scanner to determine the configuration and I am having trouble getting over this issue:
        Sometimes a rotor's permutation extends to the next line, such as:
        B R       (AE) (BN) (CK) (DQ) (FU) (GY) (HW) (IJ) (LO) (MP)
           (RX) (SZ) (TV) */

             _alphabet = new Alphabet(_config.next());
             /* this has to be an alphabet of ASCII characters not including *()
                 and it must have no whitespace inside of it */
             if (_alphabet.contains('*') || _alphabet.contains('(') ||
                     _alphabet.contains(')')) { throw error("" +
                     "The input alphabet contained not allowed characters" +
                     " '*', '(', or ')'."); }
            // this has to be two integers S P where S > 1 && P > 0
             for (int i = 0; i < 2; i += 1) {
                 if (!_config.hasNextInt()) { throw error("Either " +
                         "S or P were not configured properly"); }
                 if (i==0) { _S =_config.nextInt(); }
                 else { _P = _config.nextInt(); if (_P >= _S || _P < 1
                         || _S < 2) { throw error("Either " +
                             "S or P were not configured properly"); } }
             }
             while (_config.hasNext()) {
                 _allRotors.add(readRotor());
             }
             checkRotorConfiguration();

                /* this has to be rotors that have a name containing any non-blank characters
                other than parentheses, followed by R, N, or M. If the config file doesn't
                have at least one R and M then it should throw an error.

                R has to be followed by (Cycles) where each of the letters in the Cycles must be
                in the alphabet. Nothing after that line.*/

        /* After I have created a machine without any selected rotors now I need
        to */
            return new Machine(_alphabet, 2, 1, null);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            _config

            return null;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Checks for different incorrect scenarios of _allRotors */
    private void checkRotorConfiguration() {
        if (_allRotors == null || _allRotors.size() < _S) {
            throw error("Not enough rotors added to meet " +
                    "the minimum of S"); }
        int moving = 0;
        int reflectors = 0;
        int fixed = 0;
        for (Rotor r : _allRotors) {
            if (r.reflecting()) {
                reflectors += 1;
            } else if( r.rotates() ) {
                moving += 1;
            } else {
                fixed += 1;
            }
        }
        if (reflectors < 2) { throw error("Machine needs at least " +
                "one reflector"); } else if (moving < _P) {
            throw error("Not enough moving rotors added to meet  " +
                    "the minimum of _P"); } else if (_S - 1 - _P > fixed) {
            throw error("Not enough fixed rotors added to meet" +
                    "the minimum required.");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        // FIXME
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        // FIXME
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;
    /** Rotors used in this machine. */
    private  ArrayList<Rotor> _allRotors;
    /** Source of input messages. */
    private Scanner _input;
    /** Source of machine configuration. */
    private Scanner _config;
    /** File for encoded/decoded messages. */
    private PrintStream _output;
    /** Number of Rotors. */
    private int _S;
    /** Number of Pawls. */
    private int _P;
}
