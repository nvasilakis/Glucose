import java.util.*;

public class AssertionSimplifier 
{
    public static final String LT = "<";
    public static final String LE = "<=";
    public static final String GT = ">";
    public static final String GE = ">=";
    public static final String EQ = "==";
    public static final String NE = "!=";

    private static boolean DEBUG = false;


    public static String simplify(String orig) {

	try {
	    String play = new String(orig);

	    // remove outer parentheses
	    play = play.substring(1, play.length()-1);
	    if (DEBUG) System.out.println("PLAY: " + play);

	    // tokenize on the "&&" chars
	    String[] toks = play.split(" && ");

	    if (toks == null || toks.length == 0) {
		if (DEBUG) System.out.println("No tokens found");
		return orig;
	    }

	    // terms that we don't want to mess with
	    ArrayList<String> ignore = new ArrayList<String>();
	    // terms that we might want to simplify
	    ArrayList<Term> consider = new ArrayList<Term>();
	    // terms that we've simplified
	    ArrayList<Term> simplified = new ArrayList<Term>();


	    // Go through and weed out anything that we don't want to deal with
	    for (String tok : toks) {
		if (DEBUG) System.out.println("\nTOKEN: " + tok);

		// change anything that is negated
		if (tok.startsWith("!")) {
		    if (DEBUG) System.out.println("Negating " + tok);
		    tok = tok.substring(1); // omit the first character
		    if (tok.contains(GE)) tok = tok.replace(GE, LT);
		    else if (tok.contains(GT)) tok = tok.replace(GT, LE);
		    else if (tok.contains(LE)) tok = tok.replace(LE, GT);
		    else if (tok.contains(LT)) tok = tok.replace(LT, GE);
		    else if (tok.contains(EQ)) tok = tok.replace(EQ, NE);
		    else if (tok.contains(NE)) tok = tok.replace(NE, EQ);
		    else {
			if (DEBUG) System.out.println("Couldn't negate " + tok);
			ignore.add("!" + tok);
			continue;
		    }
		    
		    if (DEBUG) System.out.println("Changed to " + tok);
		}

		/*
		// ignore anything that is negated
		if (tok.startsWith("!")) { 
		    if (DEBUG) System.out.println("Ignoring " + tok);
		    ignore.add(tok);
		    continue;
		}
		*/
		
		// ignore anything with multiple parentheses
		if (tok.indexOf(')') != tok.lastIndexOf(')')) {
		    if (DEBUG) System.out.println("Ignoring " + tok);
		    ignore.add(tok);
		    continue;
		}

		// at this point, we should have something in the form
		// (op1 operator op2)
		Term t = new Term(tok);
		if (t.op1 == null) {
		    if (DEBUG) System.out.println("Ignoring " + tok);
		    ignore.add(tok);
		    continue;
		}

		// ignore anything that compares two variables
		if (isLiteral(t.op1) == false && isLiteral(t.op2) == false)
		{
		    if (DEBUG) System.out.println("Ignoring " + tok);
		    ignore.add(tok);
		    continue;
		}
		// if it's in the form literal-operator-variable, switch it
		if (isLiteral(t.op1)) {
		    if (DEBUG) System.out.println("Switching " + tok);
		    if (t.operator.equals(LT)) t.operator = GT;
		    else if (t.operator.equals(LE)) t.operator = GE;
		    else if (t.operator.equals(GT)) t.operator = LT;
		    else if (t.operator.equals(GE)) t.operator = LE;

		    String temp = t.op1;
		    t.op1 = t.op2;
		    t.op2 = temp;
		}
		
		// if we get here, then add it to the list of considerations
		consider.add(t);
		
	    }
	    
	    if (DEBUG) System.out.println();

	    // map the variable name to its term(s)
	    HashMap<String, ArrayList<Term>> terms = new HashMap<String, ArrayList<Term>>();

	    // Now partition the others based on variable name
	    for (Term t : consider) {

		// first time we're seeing this key
		if (terms.containsKey(t.op1) == false) {
		    if (DEBUG) System.out.println("Creating key " + t.op1 + " and adding " + t);
		    ArrayList<Term> a = new ArrayList<Term>();
		    a.add(t);
		    terms.put(t.op1, a);
		}
		else {
		    if (DEBUG) System.out.println("Adding " + t + " to key " + t.op1);
		    terms.get(t.op1).add(t);
		}

	    }

	    if (DEBUG) System.out.println();

	    // now try to combine based on the key
	    for (String key : terms.keySet()) {

		ArrayList<Term> a = terms.get(key);

		// ignore it if there's only one term
		if (a.size() == 1) {
		    if (DEBUG) System.out.println("Ignoring " + a.get(0));
		    ignore.add(a.get(0).toString());
		    continue;
		}		    
	       
		if (DEBUG) {
		    for (Term t : a) System.out.println("Found term " + t + " for key " + key);
		}

		// loop through and look for any equals signs
		boolean foundEquals = false;
		for (Term t : a) {
		    if (t.operator.equals(EQ)) {
			// add it to the simplified list
			if (DEBUG) System.out.println("Found equals sign " + t);
			simplified.add(t);
			foundEquals = true;
			break;
		    }
		}
		// ignore the others for this variable
		if (foundEquals) continue;

		// combine any terms with similar operators
		ArrayList<Term> lte = new ArrayList<Term>();
		ArrayList<Term> gte = new ArrayList<Term>();
		for (Term t : a) {
		    if (t.operator.equals(LT) || t.operator.equals(LE)) lte.add(t);
		    else gte.add(t); // is it okay to assume this??
		}

		// now add the simplified terms to the list
		Term s;
		if (lte.size() == 0 || ((s = combine(lte, true)) == null)) {
		    for (Term t : lte) {
			if (DEBUG) System.out.println("Ignoring " + t);
			ignore.add(t.toString());
		    }
		}
		else { 
		    if (DEBUG) System.out.println("Adding " + s + " to simplified list");
		    simplified.add(s); 
		}

		if (gte.size() == 0 || ((s = combine(gte, false)) == null)) {
		    for (Term t : gte) {
			if (DEBUG) System.out.println("Ignoring " + t);
			ignore.add(t.toString());
		    }
		}
		else { 
		    if (DEBUG) System.out.println("Adding " + s + " to simplified list");
		    simplified.add(s); 
		}		
	    }
	    

	    // if we made it here, then put everything together
	    String ret = "(";
	    // start with the terms we're ignoring
	    if (ignore.size() > 0) {
		for (int i = 0; i < ignore.size()-1; i++) {
		    ret += ignore.get(i) + " && ";
		}
		ret += ignore.get(ignore.size()-1);
	    }

	    // then add the ones we've simplified
	    if (simplified.size() > 0) {
		if (ignore.size() > 0) ret += " && ";

		for (int i = 0; i < simplified.size()-1; i++) {
		    ret += simplified.get(i).toString() + " && ";
		}
		ret += simplified.get(simplified.size()-1);
	    }

	    // don't forget the closing curly brace!
	    return ret + ")";
	}
	catch (Exception e) { e.printStackTrace(); }

	return orig;
    }

    private static boolean isLiteral(String s) {
	if (s == null) return false;

	for (int i = 0; i < s.length(); i++) {
	    if (s.charAt(i) < '0' || s.charAt(i) > '9') return false;
	}

	return true;
    }

    private static Term combine(ArrayList<Term> terms, boolean findSmallest) {

	if (terms == null || terms.size() == 0) return null;

	String variable = terms.get(0).op1; // we know they'll have the same variable

	int returnOperand = Integer.parseInt(terms.get(0).op2); // the one we're looking for
	String returnOperator = terms.get(0).operator; // the operator we'll return
	for (Term t : terms) {
	    if (DEBUG) System.out.println("Trying to combine " + t);
	    int i = Integer.parseInt(t.op2);
	    if ((findSmallest && i < returnOperand) || (!findSmallest && i > returnOperand)) {
		if (DEBUG) System.out.println(t + " looks like a candidate");
		returnOperand = i;
		returnOperator = t.operator;
	    }
	}
	if (returnOperator == null) return null;
	else return new Term(variable, returnOperator, Integer.toString(returnOperand));

    }

    public static void main(String[] args) {
	
	//String a = "((x <= 47) && (a >= 11) && ((a + b) > 9) && (x <= 20) && (4 < a) && (c < a) && (b > 8))";
	String a = "((x > 3) && !(x < 7) && (x < 11) && !((a + b) > 44))";
	System.out.println("Assertion:  " + a);
	System.out.println("Simplified: " + simplify(a));
    }

}