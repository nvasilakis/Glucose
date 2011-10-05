import java.util.*;

public class AssertionWriter 
{
    private static HashMap<String, String> map;
    public static final String LT = "SBVLT";
    public static final String LE = "SBVLE";
    public static final String NOT = "NOT";
    public static final String FALSE = "FALSE";
    public static final String EQ = "=";
    public static final String ADD = "BVPLUS";
    public static final String SUB = "BVSUB";
    public static final String MULT = "BVMULT";
    public static final String DIV = "BVDIV";

    static {
	map = new HashMap<String, String>();
	map.put(LT, "<");
	map.put(LE, "<=");
	map.put(NOT, "!");
	map.put(EQ, "==");
	map.put(ADD, "+");
	map.put(SUB, "-");
	map.put(MULT, "*");
	map.put(DIV, "/");
    }

    private static boolean DEBUG = false;

    private Stack<String> stack = new Stack<String>();

    private String assertion = "";

    private int count = 0, depth = 0;

    private boolean isNotAssertion = false;
    private boolean isReturned = false;

    public void reset() {
	if (DEBUG) System.out.println("\n");
	assertion = "";
	isNotAssertion = false;
	isReturned = false;
    }

    public void add(String s) {

	if (DEBUG) System.out.println("Writer: " + s);

	if (s.equals(NOT)) {
	    isNotAssertion = true;
	    return;
	}

	if (s.equals(EQ)) {
	    assertion += map.get(EQ);
	    return;
	}

	if (isOperator(s)) {
	    //System.out.println("pushing " + s);
	    stack.push(s);
	    count = 2;
	    depth++;
	    return;
	}

	if (count > 0) {
	    //System.out.println("pushing " + s);
	    stack.push(s);
	    count--;

	    if (count == 0) {
		removeFromStack();
	    }
	    return;
	}

	assertion += s;
	return;
       	
    }


    private void removeFromStack() {
	//System.out.println("Depth:" + depth + " Count" + count);
	String operand2 = stack.pop(); // second operand
	String operand1 = stack.pop(); // first operand
	String operator = stack.pop(); // operator
	
	//String stmt = "(" + operand1 + " " + map.get(operator) + " " + operand2 + ")";
	String stmt = operand1 + " " + map.get(operator) + " " + operand2;
	
	depth--;
	
	if (depth == 0) assertion += stmt;
	else { 
	    stmt = "(" + stmt + ")";
	    if (isOperator(stack.peek())) {
		stack.push(stmt);
		count = 1;
	    }
	    else {
		stack.push(stmt);
		removeFromStack();
	    }
	}
    }


    private boolean isOperator(String s) {
	return (s.equals(LT) || s.equals(LE) || s.equals(ADD) || s.equals(SUB) || s.equals(MULT) || s.equals(DIV));
    }

    public String getAssertion() {
	if (!isReturned) {
	    assertion = "(" + assertion + ")";
	    if (isNotAssertion) assertion = map.get(NOT) + assertion;
	    isReturned = true;
	}
	return assertion;
    }

    public static void main(String[] args) {
	AssertionWriter a = new AssertionWriter();
	
	a.add(LT);
	a.add(SUB);
	a.add("last");
	a.add("bg");
	a.add("100");
	System.out.println(a.getAssertion());
	System.out.println(a.getAssertion());

	a.reset();

	a.add("x");
	a.add(EQ);
	a.add("y");
	System.out.println(a.getAssertion());

	a.reset();
	

	a.add(LT);
	a.add(ADD);
	a.add("X");
	a.add("Y");
	a.add(ADD);
	a.add(ADD);
	a.add("A");
	a.add("B");
	a.add("C");

	System.out.println(a.getAssertion());

    }


}