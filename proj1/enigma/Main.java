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
        Machine m = readConfig();
        try {
            while (_input.hasNextLine()) {
                String curr = _input.nextLine().trim();
                if (curr.compareTo("") == 0) {
                    _output.println();
                } else if ("*".compareTo("" + curr.charAt(0)) == 0) {
                    this.setUp(m, curr);
                }
                else {
                    _output.println(m.convert(curr));
                }
            }
            //_output.println();
        }
        catch (NoSuchElementException excp) {
            throw error("Input file truncated");
        }
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
             _allRotors = new ArrayList<Rotor>();
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
             /* _allRotors has to be rotors that have a name containing any non-blank characters
                other than parentheses, followed by R, N, or M. If the config file doesn't
                have at least one R and M then it should throw an error. */
             checkRotorConfiguration();

        /* Now that I have a machine without any selected rotors now I need
        to return a machine with all of the information*/
            return new Machine(_alphabet, _S, _P, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            if (name.contains("(") || name.contains(")")) {
                throw error("Rotor name can't have '(' or ')'.");
            }
            String rTNOTCH = _config.next();
            String rTYPE = rTNOTCH.substring(0,1);
            if (!"MNR".contains(rTYPE)) {
                throw error("Not a valid type of rotor. Must be" +
                        "either 'M', 'N', or 'R'."); }
            if ("NR".contains(rTYPE) && rTNOTCH.length() > 1) {
                throw error("Only M rotors can have notches.");
            }
            String notches = ""; StringBuilder cycles = new StringBuilder();
            if (rTNOTCH.length() > 1) {
                notches = rTNOTCH.substring(1);
                for (int i = 0; i<notches.length(); i += 1) {
                    if (!this._alphabet.contains(notches.charAt(i))) {
                        throw error("One of the notches was not in" +
                                "the alphabet.");
                    }
                }
            }
            while (_config.hasNext(" *\\((.*?)\\) *")) {
               cycles.append(_config.next());
            }
            if (rTYPE.compareTo("R") == 0) {
                return new Reflector(name, new Permutation(cycles.toString(),
                        this._alphabet));
            } else if (rTYPE.compareTo("N") == 0) {
                return new FixedRotor(name, new Permutation(cycles.toString(),
                        this._alphabet));
            } else {
                return new MovingRotor(name, new Permutation(cycles.toString(),
                        this._alphabet), notches);
            }
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
        if (reflectors < 1) { throw error("Machine needs at least " +
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
        try {
            Scanner s = new Scanner(settings);
            s.next();
            String[] rotors = new String[_S];
            for (int i = 0; i < _S; i += 1) {
                rotors[i] = s.next();
            }
            String setSTRING = s.next();
            StringBuilder cycles = new StringBuilder();
            while (s.hasNext(" *\\((.*?)\\) *")) {
                cycles.append(s.next());
            }
            M.insertRotors(rotors);
            M.setRotors(setSTRING);
            M.setPlugboard(new Permutation(cycles.toString(), this._alphabet));
        }
        catch (NoSuchElementException excp) {
            throw error("Input settings string truncated");
        }
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
