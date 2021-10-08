// No changes needed here

// There are three classes here:  FunTab, SymTab, ByteCode

class FunTab {
	
	public static String[] fun = new String[50];
	public static int funptr = 0;

	public static void add(String s) {
		fun[funptr] = s;
		funptr++;
	}

	public static int index(String s) {
		for (int i = 0; i < funptr; i++)
			if (s.equals(fun[i]))
				return i;
		
		return -1; // should not happen unless you call
				   // a function before it is defined
	}
	
	public static void output() {
		System.out.println("\n Defined functions:\n");
		for (int i = 0; i < funptr; i++) {
			System.out.println(fun[i] + ": " + i);
		}
	}
}

//==========================================

class SymTab {
	
	static String[] id;
	static int idptr;   // start from 1

	public static void initialize() {
		id = new String[50];
		idptr = 1;
	}
	public static void add(String s) {
		id[idptr] = s;
		idptr++;
	}

	public static int index(String s) {
		for (int i = 1; i < idptr; i++)
			if (s.equals(id[i]))
				return i;
		
		return -1;    // this should not happen
	}
}

//===================================================================

// The ByteCode class has a dual role: (i) prepare the 'code' and 'arg' arrays
// for the interpreter; and (ii) prepare the 'str_code' array for printing
// the bytecodes.

public class ByteCode {

	public static String[] code; // for execution
	public static int[] arg;
	public static int codeptr;
	

	public static String[] str_code; // for output
	public static int str_codeptr;

	public static void initialize() {
		codeptr = 0;
		code = new String[500];
		arg = new int[500];
		
		str_codeptr = 0;
		str_code = new String[500];
	}

	public static void gen(char op) {
		gen(op_str_code(op), -1);
	}

	public static String op_str_code(char c) {
		switch (c) {
		case '+':
			return "iadd";
		case '-':
			return "isub";
		case '*':
			return "imul";
		case '/':
			return "idiv";
		default:
			return "";
		}
	}
	
	
	// for arith opcodes, iload, and istore
	
	public static void gen(String opc, int n) {
		if (n == -1) { // arith opcode has no operand: n = -1
			code[codeptr] = opc;
			codeptr++;
			str_code[str_codeptr] = opc;
			str_codeptr++;
		} else { // there is an operand
			int k;
			String c;
			if (opc == "iload" || opc == "istore")
				k = 4;
			else
				k = 6;
			if (n < k)
				c = opc + "_" + n;
			else
				c = opc + " " + n;
			code[codeptr] = opc;
			arg[codeptr] = n;
			str_code[str_codeptr] = c;
			if (n < k)
				str_codeptr++;
			else
				str_codeptr = str_codeptr + 2;
			codeptr = str_codeptr;
		}
	}
	
	public static void gen_if(String rop) {
		String instr = "";
		if (rop == "==")
			instr = "if_icmpne";
		if (rop == "<")
			instr = "if_icmpge";
		if (rop == ">")
			instr = "if_icmple";
		if (rop == "!=")
			instr = "if_icmpeq";
		if (rop == "<=")
			instr = "if_icmpgt";
		if (rop == ">=")
			instr = "if_icmplt";
		if (rop == "goto")
			instr = "goto";
		code[codeptr] = instr;
		str_code[str_codeptr] = instr + " ";
	}
	
	
	public static void gen_goto(int n) {
		code[codeptr] = "goto";
		str_code[str_codeptr] = "goto ";
		if (n == -2) {
			codeptr++;
			str_codeptr++;
		} else {
			arg[codeptr] = n;
			str_code[str_codeptr] = "goto " + n;
		    codeptr++;
		    str_codeptr++;
		}
	}

	// to fix forward jumps
	
	public static void patch(int i, int dest) {
		arg[i] = dest;
		str_code[i] = str_code[i] + dest;
	}

	public static int skip(int n) {
		codeptr = codeptr + n;
		str_codeptr = str_codeptr + n;
		return str_codeptr - n;
	}

	
	public static void gen_invoke (int n)  {
		code[codeptr] = "invokevirtual";
		arg[codeptr] = n;
		str_code[str_codeptr] = "invokevirtual #" + n;
	    codeptr++;
	    str_codeptr++;
	}
	
	
	public static void gen_return() {
		code[codeptr] = "ireturn";
		str_code[str_codeptr] = "ireturn";
		codeptr++;
		str_codeptr++;
	    return;
	}
	
	public static void gen_print() {
		code[codeptr] = "iprint";
		str_code[str_codeptr] = "iprint";
		codeptr++;
		str_codeptr++;
	    return;
	}
	
	
	// this method outputs all byte codes
	
	public static void output(String header) {
		System.out.println("\n" + header + "\n" + "  Code:");
		for (int i = 0; i < str_codeptr; i++)
			if (str_code[i] != null && str_code[i] != "") {
				System.out.println("     " + i + ": " + str_code[i]);
			}
	}


}
