/**
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is "EINRC-4 / Meta Project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2002 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Jaanus Heinlaid (TietoEnator)
 */

package eionet.util;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This is a utility class for retrieveing command line options.
 * It expects each option to be formatted like<BR>
 *    -option value
 * To use it, construct a new instance of it, by giving usage
 * guide as an input parameter for the constructor.
 * Then add allowable options and their allowable values by
 * using the <I>add()</I> method. Note that the allowable values
 * have to be comma separated.
 * Finally call <I>parse()</I> with command line arguments array
 * as input parameter. This will parse the command line arguments
 * and you can get the passed options by calling <I>get()</I> with
 * option name as an input parameter.
 *
 * @author Jaanus Heinlaid
 */

public class Options {

    private Hashtable map = new Hashtable();
    private Hashtable options = new Hashtable();
    private String errMsg = "";
    private String usage = "";

/**
 * Constructor. Usage guide is input parameter.
 *
 */
    public Options(String usage) {
        this.usage = usage == null ? usage : ("\n" + usage);
    }

/**
 * Add allowable option with allowable values (comma separated).
 *
 */
    public void add(String option, String sValues) {

        if (option == null)
            return;
        if (sValues == null)
            sValues = "";

        StringTokenizer tokenizer = new StringTokenizer(sValues, ",");
        Vector values = new Vector();
        while (tokenizer.hasMoreTokens()) {
            values.add((tokenizer.nextToken()).trim());
        }

        map.put(option, values);
    }

/**
 * Parse command line arguments to find out the passed options.
 *
 */
    public boolean parse(String[] args) {

        boolean eofoptions = false;
        boolean error = false;
        int i;

        for (i = 0; !eofoptions && !error && i < args.length; i++) {

            switch(args[i].charAt(0)) {
                case '-' :
                    String option = args[i].substring(1);
                    if (map.containsKey(option)) {
                        i++;
                        Vector values = (Vector) map.get(option);
                        if (values.contains(args[i]) || values.size() == 0) {
                            options.put(option, args[i]);
                        } else {
                            error = true;
                            break;
                        }
                    } else {
                        error = true;
                        break;
                    }
                    break;
                default :
                    i--;
                    eofoptions = true;
                    break;
            }
        }

        if (error)
            errMsg = "Illegal option or option value " + args[i - 1] + " !";

        return error;
    }

/**
 * Get value of the specified option.
 *
 */
    public String get(String option) {
        return (String) options.get(option);
    }

    public String getErrorMsg() {
        return errMsg + usage;
    }

    /* Main function for testing
    public static void main(String[] args) {
        Options opts = new Options("");
        opts.add("kala", "ahven, angerjas");
        opts.add("auto", "ford, nissan, bmw");
        opts.add("loom", null);
        if (opts.parse(args)) {
            System.out.println(opts.getErrorMsg());
        }

        System.out.println("kala\t" + opts.get("kala"));
        System.out.println("auto\t" + opts.get("auto"));
        System.out.println("loom\t" + opts.get("loom"));
    }
    */
}
