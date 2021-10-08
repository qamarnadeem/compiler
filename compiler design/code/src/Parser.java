// This file requires several changes

// program -> { function } end
class Program {		 
	public Program() {

		//&& Lexer.nextToken != Token.KEY_END
		
		while (Lexer.nextToken == Token.KEY_INT ) {
			System.out.println("Program: "+ Lexer.nextToken);
			SymTab.initialize(); // initialize for every function parsed
			ByteCode.initialize(); // initialize for every function parsed

			Function f = new Function();

			ByteCode.output(f.header);

			Interpreter.initialize(f.fname, SymTab.idptr - 1, f.p.npars, ByteCode.code, ByteCode.arg, ByteCode.codeptr);
		}
		System.out.println("Program: "+ Lexer.nextToken);
		FunTab.output();
	}
}

//function -> int id '(' [ pars ] ')' '{' body '}'
class Function { 
	String fname; 	// name of the function
	Pars p;
	Body b;
	String header;

	public Function() {
		
		// Fill in code here
		// Must invoke:  FunTab.add(fname);
		// Code ends with following two statements:
		
		Lexer.lex();
		
		if(Lexer.nextToken==Token.ID)
		{
			fname=Lexer.ident;
		    FunTab.add(fname);
		    Lexer.lex();
		}
		while(true){
		
		if(Lexer.nextToken==Token.LEFT_PAREN)
		{
			Lexer.lex();
			p=new Pars();
		    Lexer.lex();
		}
		else if(Lexer.nextToken==Token.LEFT_BRACE){
    		b=new Body();
		}
		else if(Lexer.nextToken==Token.RIGHT_BRACE) {
			break;
		}
		}

		header = "int " + fname + "(" + p.types + ");";
		Lexer.lex();
		return;
	}
}

// pars --> int id { ',' int id }
class Pars { 

	String types = ""; // comma-separated sequence of types, e.g., int,int
	int npars = 0;	   // the number of parameters

	public Pars() {
		
	
		while(Lexer.nextToken!=Token.RIGHT_PAREN)
		{
			if(Lexer.nextToken==Token.KEY_INT)
			{
				if(npars == 0) {
					types=types + (Lexer.ident);
				}
				else if (npars > 0 ){
					types=types + (",") + (Lexer.ident);
				}
				
				Lexer.lex();
				
			}
			else if(Lexer.nextToken==Token.ID)
			{  
				SymTab.add(Lexer.ident);
				npars=npars+1;
				Lexer.lex();
			}
			else if(Lexer.nextToken==Token.COMMA)
				Lexer.lex();
			
		}
		 	// Fill in code here
			// Must insert each id that is parsed 
			// into the symbol table using:
			// SymTab.add(id)
		
	}
}

// body -> [ decls ] stmts
class Body { 
	Decls d;
	Stmts s;

	public Body() {
		// Fill in code here
		Lexer.lex();
		d=new Decls(); 
		s = new Stmts();
		
	}
}

// decls -> int idlist ';'
class Decls { 
	Idlist il;

	public Decls() {
		if(Lexer.nextToken==Token.KEY_INT)
		{
			Lexer.lex();			
			il=new Idlist();
		}
		if(Lexer.nextToken == Token.SEMICOLON)
			Lexer.lex();
	}
}

// idlist -> id { ',' id }
class Idlist { 
	String id;
	Idlist il;

	public Idlist() {
		id=Lexer.ident;
		SymTab.add(id);
		
		Lexer.lex();
		
		if(Lexer.nextToken==Token.COMMA)
		{
			Lexer.lex();
			il=new Idlist();
		}
		// Fill in code here
		// Must insert each id that is parsed
		// into the symbol table using:
		// SymTab.add(id);
	}
}

// stmts -> stmt [ stmts ]
class Stmts { 
	Stmt s;
	Stmts ss;

	public Stmts() { 
		// Fill in code here
		s = new Stmt();
		if(Lexer.nextToken == Token.SEMICOLON)
			Lexer.lex();
		if(Lexer.nextToken == Token.ID||
				Lexer.nextToken == Token.LEFT_BRACE||
				Lexer.nextToken == Token.KEY_IF||
				Lexer.nextToken == Token.KEY_WHILE||
				Lexer.nextToken==Token.KEY_RETURN||
				Lexer.nextToken==Token.KEY_PRINT){
			
			ss = new Stmts();
			
		}
	}
}

// stmt -> assign ';' | loop | cond | cmpd | return ';' | print expr ';'
class Stmt {
	Assign a;
	Stmt s;
	Loop lp;
	Cond c;
	Cmpd cm;
	Return r;
	Print p;
	public Stmt() {
		// Fill in code here
		
		switch(Lexer.nextToken){
			case Token.ID:{
				a = new Assign();
				break;
			}
			case Token.KEY_WHILE:{
				lp = new Loop();
				break;
			}
			case Token.KEY_IF:{
				c = new Cond();
				break;
			}
			case Token.LEFT_BRACE:{
				cm = new Cmpd();
				break;
			}
			case Token.KEY_RETURN:{
				r = new Return();
				break;
			}
			case Token.KEY_PRINT:{
				p = new Print();
				break;
			}
			default: { break;}
		}
	}

	public Stmt(int d) {
	  // Leave the body empty.
	  // This helps avoid infinite loop - why? 
	}
}

// assign -> id '=' expr
class Assign extends Stmt { 
	String id;
	Expr e;

	public Assign() {
		super(0); // superclass initialization
		// Fill in code here.
		id = Lexer.ident;
		if(SymTab.index(id) == -1){
			System.err.println("Unknown Identifer!!");
			System.exit(-1);
		}
		Lexer.lex();
		if (Lexer.nextToken == Token.ASSIGN_OP)
			Lexer.lex();
		e = new Expr();
		// End with this statement:
		ByteCode.gen("istore", SymTab.index(id));
	}
}


// loop -> while '(' relexp ')' stmt
class Loop extends Stmt { 
	Relexp b;
	Stmt c;
	public Loop() {
		super(0);
		Lexer.lex(); // skip over 'while'
		Lexer.lex(); // skip over '('
		int boolpoint = ByteCode.str_codeptr;
		b = new Relexp();
		Lexer.lex(); // skip over ')'
		int whilepoint = ByteCode.skip(3);
		c = new Stmt();
		ByteCode.gen_goto(boolpoint);
		ByteCode.skip(2);
		ByteCode.patch(whilepoint, ByteCode.str_codeptr);
	}
}


// cond -> if '(' relexp ')' stmt [ else stmt ]
class Cond extends Stmt { 
	Relexp r;
	Stmt s1;
	Stmt s2;
	int point1;
	int point2;
	int x;
	public Cond() {
		super(0);
		Lexer.lex();
		Lexer.lex();		
		r = new Relexp();
		point1 = ByteCode.str_codeptr;
		ByteCode.skip(3);
		Lexer.lex();
		s1 = new Stmt();
		//&& Lexer.nextToken != Token.KEY_RETURN
		if(Lexer.nextToken != Token.ID && Lexer.nextToken != Token.KEY_RETURN)
			Lexer.lex();
		if(Lexer.nextToken == Token.KEY_ELSE){
			Lexer.lex();
			point2 = ByteCode.str_codeptr;
			ByteCode.gen_if("goto");
			ByteCode.skip(3);
			ByteCode.patch(point1,  ByteCode.str_codeptr);
			s2 = new Stmt();
			ByteCode.patch(point2,  ByteCode.str_codeptr);
			
		}
		else{
			ByteCode.patch(point1,  ByteCode.str_codeptr);
		} 

		// Fill in code here.  Refer to
		// code in class Loop for guidance
	}
}

// cmpd -> '{' stmts '}'
class Cmpd extends Stmt { 
	Stmts s;

	public Cmpd() {
		super(0);
		Lexer.lex();
		s = new Stmts();
		Lexer.lex();
		// Fill in code here
	}
}

// return -> 'return' expr
class Return extends Stmt { 
	Expr e;

	public Return() {
		super(0);
		Lexer.lex();
		e = new Expr();
		// Fill in code here.  End with:
		ByteCode.gen_return();
	}
}

// print -> 'print' expr
class Print extends Stmt { 
	Expr e;

	public Print() {
		super(0);
		Lexer.lex();
		e = new Expr();
		// Fill in code here.  End with:
		ByteCode.gen_print();
	}
}

// relexp -> expr ('<' | '>' | '<=' | '>=' | '==' | '!= ') expr
class Relexp { 
	Expr e1;
	Expr e2;
	String op = "";

	public Relexp() {
		// Fill in code here
		e1 = new Expr();
		
		switch (Lexer.nextToken){
			case Token.LESSER_OP:{
				Lexer.lex();
				e2=new Expr();
				ByteCode.gen_if("<");
				break;
			}
			case Token.GREATER_OP:{
				Lexer.lex();
				e2=new Expr();
				ByteCode.gen_if(">");
				break;
			}
			case Token.LESSEQ_OP:{
				Lexer.lex();
				e2=new Expr();
				ByteCode.gen_if("<=");
				break;
			}
			case Token.GREATEREQ_OP:{
				Lexer.lex();
				e2=new Expr();
				ByteCode.gen_if(">=");
				break;
			}
			case Token.EQ_OP:{
				Lexer.lex();
				e2=new Expr();
				ByteCode.gen_if("==");
				break;
			}
			case Token.NOT_EQ:{
				Lexer.lex();
				e2=new Expr();
				ByteCode.gen_if("!=");
				break;
			}
		
		}
		
	}
}

// expr -> term (+ | -) expr | term
class Expr { 
	Term t;
	Expr e;
	char op;

	public Expr() {
		// Fill in code here
		t = new Term();
		if(Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP){
			op = Lexer.nextChar;
			Lexer.lex();
			e = new Expr();
			ByteCode.gen(op);
		}
	}
}

// term -> factor (* | /) term | factor
class Term { 
	Factor f;
	Term t;
	char op;

	public Term() {
		// Fill in code here
		f = new Factor();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			op = Lexer.nextChar;
			Lexer.lex();     // scan over op
			t = new Term();
			ByteCode.gen(op);
		}
	}
}

// factor -> int_lit | id | '(' expr ')' | funcall
class Factor { 
	int i;
	String id;
	Funcall fc;
	Expr e;
	
	public Factor() {
		// Fill in code here
		
		switch (Lexer.nextToken){
			case Token.INT_LIT:{
				i = Lexer.intValue;
				Lexer.lex();
				if(i > 127){
					ByteCode.gen("sipush", i);
				}
				else if(i > 5){
					ByteCode.gen("bipush", i);
				}
				else
					ByteCode.gen("iconst", i);
				break;
			}
			case Token.ID:{
				
				id = Lexer.ident;
				if(FunTab.index(id) != -1) {
					fc = new Funcall(id);
				}
				else {
					ByteCode.gen("iload", SymTab.index(id));
					Lexer.lex();
				}
				//ByteCode.skip(1);
				break;
			}
			case Token.LEFT_PAREN:{
				Lexer.lex();
				e = new Expr();
				Lexer.lex();
				break;
			}
			default: {
				break;
			}
		}
		
	}
}

// funcall -> id '(' [ exprlist ] ')'
class Funcall { 
	String id;
	ExprList el;

	public Funcall(String id) {
		this.id = id;
		Lexer.lex(); // (
		ByteCode.gen("aload", 0);
		el = new ExprList();
		Lexer.lex(); // skip over the );
		int funid = FunTab.index(id);
		ByteCode.gen_invoke(funid);
		ByteCode.skip(2);
	}
}


// exprlist -> expr [ , exprlist ]
class ExprList { 
	Expr e;
	ExprList el;
	// Fill in code here
	public ExprList() {
		while(Lexer.nextToken != Token.RIGHT_PAREN && Lexer.nextToken != Token.SEMICOLON){
			if(Lexer.nextToken == Token.LEFT_PAREN)
				Lexer.lex();
			e = new Expr();
			if(Lexer.nextToken == Token.COMMA)
				Lexer.lex();	
			el  = new ExprList();	
		}
	}
}

