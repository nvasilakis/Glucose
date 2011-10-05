import java.io.*;
import java.util.*;

public class CreateAssertion {

    private static final boolean DEBUG = false;

    public static final String QUERY = "QUERY";
    public static final String ASSERT = "ASSERT";
    public static final String ARRAY = "ARRAY";
    public static final String HEX = "hex";
    public static final String SEMICOLON = ";";
    public static final String COMMA = ",";
    public static final String EQ = "=";

    private static AssertionWriter writer = new AssertionWriter();

    /*
     * Create the assertion from a file.
     */
    public static String createAssertion(String filename) 
    {
	HashMap<String, String> variables = new HashMap<String, String>();

	ArrayList<String> assertions = new ArrayList<String>();

	try 
	{
	    Scanner fileReader = new Scanner(new File(filename));

	    boolean readingAssertion = false;

	    while(fileReader.hasNext()) {
		String line = fileReader.nextLine();
		if (DEBUG) System.out.println("\nReading a line: " + line);

		// if the line is declaring a variable, add it to our symbol table
		if (line.contains(ARRAY)) {
		    Scanner lineReader = new Scanner(line);
		    String fullName = lineReader.next();
		    if (DEBUG) System.out.println("FOUND VARIABLE " + fullName);
		    String varName = fullName.substring(0, fullName.lastIndexOf('_'));
		    if (DEBUG) System.out.println("NAME IS " + varName);
		    variables.put(fullName, varName);
		}

		if (line.startsWith(ASSERT)) {
		    writer.reset();
		    readingAssertion = true;
		    // first, consume the "ASSERT( " token
		    line = line.substring(ASSERT.length() + 2); // because of the "( "
		}
		if (readingAssertion) {
		    boolean readingVariable = false;
		    // keep reading up to each '(' character
		    while (line.indexOf('(') != -1) {
			String token = line.substring(0, line.indexOf('(')).trim();
			
			if (!readingVariable) {
			    for (String var: variables.keySet()) {
				if (token.startsWith(var)) {
				    print(variables.get(var));
				    readingVariable = true;
				}
			    }
			}
			
			if (!readingVariable) print(token);

			line = line.substring(line.indexOf('(')+1);
		    }

		    // now print whatever's left over
		    if (!readingVariable) print(line);
		}

		if (readingAssertion && line.endsWith(SEMICOLON)) {
		    String thisAssertion = writer.getAssertion();
		    if (DEBUG) System.out.println("The assertion is : " + thisAssertion);
		    assertions.add(thisAssertion);
		    if (DEBUG) System.out.println("===================================");
		    readingAssertion = false;
		}
		

	    }
	}
	catch (Exception e) { e.printStackTrace(); }

	String assertion = "(";
	for (int i = 0; i < assertions.size() - 1; i++) {
	    assertion += assertions.get(i) + " && ";
	}
	assertion += assertions.get(assertions.size()-1) + ")";

	if (DEBUG) System.out.println("RAW ASSERTION IS " + assertion);

	assertion = AssertionSimplifier.simplify(assertion);

	return assertion;

    }


    private static void print(String line) 
    {
	if (DEBUG) System.out.println("Trying to write " + line);

	if (line == null || line.trim().length() == 0) return;

	line = line.trim();

	if (line.equals(")")) return;


	if (line.equals("32,")) return; 

	if (line.equals(");")) return; 
	/*
	if (line.startsWith(COMMA)) {
	    //print2(COMMA);
	    if (line.length() > 1) {
		line = line.substring(line.indexOf(COMMA)+1);
		print2(line);
	    }
	    return;
	}
	*/
	
	if (line.contains(EQ)) {
	    if (line.equals(EQ)) print2(EQ);
	    else if (line.startsWith(EQ)) {
		print2(EQ);
		print2(line.substring(line.indexOf(EQ)+1));
	    }
	    else if (line.endsWith(EQ)) {
		print2(line.substring(0, line.indexOf(EQ)));
		print2(EQ);
	    }
	    else {
		print2(line.substring(0, line.indexOf(EQ)));
		print2(EQ);
		print2(line.substring(line.indexOf(EQ)+1));
	    }
	    return;
	}
	/*
	if (line.contains(COMMA)) {
	    if (line.equals(COMMA)) print2(COMMA);
	    else if (line.startsWith(COMMA)) {
		print2(COMMA);
		print2(line.substring(line.indexOf(COMMA)+1));
	    }
	    else if (line.endsWith(COMMA)) {
		print2(line.substring(0, line.indexOf(COMMA)));
		print2(COMMA);
	    }
	    else {
		print2(line.substring(0, line.indexOf(COMMA)));
		print2(COMMA);
		print2(line.substring(line.indexOf(COMMA)+1));
	    }
	    return;
	}
	*/

	if (line.contains(COMMA)) {
	    if (line.equals(COMMA)) return;
	    else if (line.startsWith(COMMA)) {
		//print2(COMMA);
		print2(line.substring(line.indexOf(COMMA)+1));
	    }
	    else if (line.endsWith(COMMA)) {
		print2(line.substring(0, line.indexOf(COMMA)));
		//print2(COMMA);
	    }
	    else {
		print2(line.substring(0, line.indexOf(COMMA)));
		//print2(COMMA);
		print2(line.substring(line.indexOf(COMMA)+1));
	    }
	    return;
	}
	

	print2(line);
		
    }

    private static void print2(String line) {

	if (line == null) return;

	line = line.trim();

	if (line.endsWith(")")) line = line.substring(0, line.length()-1);

	// convert numbers from hex
	if (line.contains("hex")) {
	    line = line.substring(4);

	    if (line.equals("FFFFFFFF")) line = "-1";
	    else {
		int num = Integer.parseInt(line, 16);
		//System.out.println("The number is " + num);
		
		line = Integer.toString(num);
	    }
	}

	if (DEBUG) System.out.println("Sending to writer: " + line);
	writer.add(line);
    }


    public static void main(String[] args) {
	if (args == null || args.length == 0) {
	    System.out.println("Please specify a filename");
	    System.exit(0);
	}

	for (String filename : args) {
	    System.out.println("\n==============================================================");
	    System.out.println("FILE: " + filename + "\n" + createAssertion(filename));
	}
    }
    


    





}