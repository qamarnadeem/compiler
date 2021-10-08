// No changes needed here

class FunCode {
	String fname; // function name
	int num_pars; // number of parameters
	int num_locals; // number of local variables
	String[] code; // the array of opcodes for each function
	int[] arg; // corresponding operand for each opcode (sometimes no operand)
	int size; // total number of opcodes
}

public class Interpreter {

	// Following declarations are for all the functions that were parsed

	public static FunCode[] fun_code = new FunCode[50]; // max 50 functions
	public static int funptr = 0; // actual number of functions

	// Following declarations are for the current function being executed

	static int currentfun; // current function being executed

	static String[] code; // the opcodes for current function
	static int[] arg; // the corresponding operands for each opcode
	static int codeptr; // current bytecode being executed

	static int[] stack; // runtime stack
	static int stackptr; // top of the stack
	static int base; // base of the topmost stack frame
	static int old_base; // previous base

	// the parser invokes this function to send info to the interpreter

	public static void initialize(String fname, int nlocals, int npars, String[] fcode, int[] farg, int fsize) {

		FunCode bc = new FunCode();
		bc.fname = fname;
		bc.num_pars = npars;
		bc.num_locals = nlocals - npars;
		bc.code = fcode;
		bc.arg = farg;
		bc.size = fsize;
		fun_code[funptr] = bc;
		funptr++;
	}

	// execution starts with 'go'

	public static void go() {

		currentfun = funptr - 1; // top-level call is the last function

		code = fun_code[currentfun].code;
		arg = fun_code[currentfun].arg;

		stack = new int[1000]; // make a new runtime stack
		codeptr = 0; // start at the first bytecode
		base = 0;
		old_base = 0;
		stackptr = 2; // the first two locations are for 'dl' and 'rp'

		while (true) { // execution ends at 'ireturn' instruction from 'main'

			if (code[codeptr] == null) // there could be null entries in the bytecodes
				codeptr++;
			else
				switch (code[codeptr]) {
				case "aload":
					aload();
					break;
				case "invokevirtual":
					invokevirtual();
					break;
				case "ireturn":
					if (base == 0) { // return from main
						System.out.println("\nanswer = " + stack[stackptr - 1]);
						return;
					}
					ireturn();
					break;
				case "iprint":
					System.out.println(stack[stackptr - 1]);
					stackptr--;
					codeptr++;
					break;
				case "iconst":
					push(arg[codeptr]);
					codeptr++;
					break;
				case "istore":
					stack[base + 2 + arg[codeptr] - 1] = stack[stackptr - 1];
					pop();
					codeptr++;
					break;
				case "iload":
					push(stack[base + 2 + arg[codeptr] - 1]);
					codeptr++;
					break;
				case "bipush":
					push(arg[codeptr]);
					codeptr = codeptr + 2;
					break;
				case "sipush":
					push(arg[codeptr]);
					codeptr = codeptr + 3;
					break;
				case "imul":
					arith("imul");
					codeptr++;
					break;
				case "iadd":
					arith("iadd");
					codeptr++;
					break;
				case "isub":
					arith("isub");
					codeptr++;
					break;
				case "idiv":
					arith("idiv");
					codeptr++;
					break;
				case "if_icmple":
					if_icmp("le");
					break;
				case "if_icmplt":
					if_icmp("lt");
					break;
				case "if_icmpge":
					if_icmp("ge");
					break;
				case "if_icmpgt":
					if_icmp("gt");
					break;
				case "if_icmpeq":
					if_icmp("eq");
					break;
				case "if_icmpne":
					if_icmp("ne");
					break;
				case "goto":
					codeptr = arg[codeptr];
					break;
				default:
					codeptr++;
				}
		}
	}

	private static void aload() {
		stack[stackptr] = base;
		old_base = stackptr;
		stackptr = stackptr + 2; // leave space for dl and rp
		codeptr++;
	}

	private static void invokevirtual() {
		stack[old_base + 1] = 10000 * currentfun + codeptr + 3; // set return-point; was aload+1
		currentfun = arg[codeptr];
		code = fun_code[currentfun].code; // get code for function
		arg = fun_code[currentfun].arg;
		stackptr = stackptr + fun_code[currentfun].num_locals;
		base = old_base;
		codeptr = 0;
	}

	private static void ireturn() {
		old_base = stack[base];
		codeptr = stack[base + 1] % 10000;
		currentfun = stack[base + 1] / 10000;
		code = fun_code[currentfun].code;
		arg = fun_code[currentfun].arg;
		stack[base] = stack[stackptr - 1]; // transmit result
		stackptr = base + 1;
		base = old_base; // reset the base
	}

	public static void push(int v) {
		stack[stackptr] = v;
		stackptr++;
	}

	public static void pop() {
		stackptr--;
	}

	public static void if_icmp(String op) {
		int a1 = stack[stackptr - 2];
		int a2 = stack[stackptr - 1];
		stackptr = stackptr - 2;
		codeptr = codeptr + 3; // assuming condition is false
		switch (op) {
		case "le":
			if (a1 <= a2)
				codeptr = arg[codeptr - 3];
			break;
		case "lt":
			if (a1 < a2)
				codeptr = arg[codeptr - 3];
			break;
		case "ge":
			if (a1 >= a2)
				codeptr = arg[codeptr - 3];
			break;
		case "gt":
			if (a1 > a2)
				codeptr = arg[codeptr - 3];
			break;
		case "eq":
			if (a1 == a2)
				codeptr = arg[codeptr - 3];
			break;
		case "ne":
			if (a1 != a2)
				codeptr = arg[codeptr - 3];
			break;
		}
	}

	public static void arith(String op) {
		int a1 = stack[stackptr - 2];
		int a2 = stack[stackptr - 1];
		stackptr = stackptr - 2;
		switch (op) {
		case "iadd":
			stack[stackptr] = a1 + a2;
			break;
		case "isub":
			stack[stackptr] = a1 - a2;
			break;
		case "imul":
			stack[stackptr] = a1 * a2;
			break;
		case "idiv":
			stack[stackptr] = a1 / a2;
			break;
		}
		stackptr++;
	}
}