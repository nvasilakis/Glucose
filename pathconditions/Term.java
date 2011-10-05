public class Term 
{
    public String op1, operator, op2;
    
    public String toString() {
	return "(" + op1 + " " + operator + " " + op2 + ")";
    }

    public Term(String term) {
	String[] ops = term.split(" ");
	if (ops == null || ops.length != 3) {
	    return;
	}
	op1 = ops[0].substring(1); // get rid of the open paren
	operator = ops[1];
	op2 = ops[2].substring(0, ops[2].length()-1); // get rid of the close paren
    }

    public Term(String o1, String op, String o2) {
	op1 = o1;
	operator = op;
	op2 = o2;
    }
}


