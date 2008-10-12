/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.InputStreamReader;

import clojure.lang.Compiler;
import clojure.lang.Namespace;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**
 *
 * @author 
 */
public class Main {

    private static final String MAINCLJ = "main.clj";
    private static final Namespace NS = Namespace.findOrCreate(Symbol.create("main"));
    private static final Var MAIN = Var.intern(NS, Symbol.create("main"));    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        try {
            Compiler.load(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(MAINCLJ)));
            MAIN.invoke(args);
        } catch(Exception e) {
            e.printStackTrace();
        }           
    }

}
