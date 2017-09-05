package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;

public class Parser {
	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	Scanner scanner;
	Token t;
	
	Parser(Scanner scanner) 
	{
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		Program pr=program();
		matchEOF();
		return pr;
	}
	Expression expression() throws SyntaxException 
	{
		Token f=t;
		Expression ex1=null;
		Expression ex2=null;
		if(FACTOR())
		{
			ex1=term();
			while(RELOP())
			{
				Token to=null;
				to=relOp();
				if(FACTOR())
				{
				ex2=term();
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in expression at"+t.getLinePos());
				}
				ex1=new BinaryExpression(f,ex1,to,ex2);
			}
			return ex1;
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in expression at"+t.getLinePos());
		}
	}

	Token relOp() throws SyntaxException {
		Token tok=null;
		if(RELOP())
		{
			tok=t;
			consume();
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in relOp at"+t.getLinePos());
		}
		return tok;
	}
	
	Expression term() throws SyntaxException {
		Token f=t;
		Expression ex1=null;
		Token to=null;
		Expression ex2=null;
		if(FACTOR())
		{
			ex1=elem();
			while(WEAKOP())
			{
				to=weakOp();
				if(FACTOR())
				{
				ex2=elem();
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in term at"+t.getLinePos());
				}
			ex1=new BinaryExpression(f, ex1, to, ex2);
			}
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in term at"+t.getLinePos());
		}
		return ex1;
	}
	
	Token weakOp() throws SyntaxException {
		Token tok=null;
		if(WEAKOP())
		{
			tok=t;
			consume();
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in weakOp at"+t.getLinePos());
		}
		return tok;
	}
	
	Expression elem() throws SyntaxException {
		Token f=t;
		Expression ex1=null;
		Token to=null;
		Expression ex2=null;
		if(FACTOR())
		{
			ex1=factor();
			while(STRONGOP())
			{
				to=strongOp();
				if(FACTOR())
				{
					ex2=factor();
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in elem at"+t.getLinePos());
				}
				ex1=new BinaryExpression(f, ex1, to, ex2);
			}
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in elem at"+t.getLinePos());
		}
		return ex1;
	}
	
	Token strongOp() throws SyntaxException {
		Token tok=null;
		if(STRONGOP())
		{
			tok=t;
			consume();
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in strongOp at"+t.getLinePos());
		}
		return tok;
	}
	void arrowOp() throws SyntaxException {
		Kind kind=t.kind;
		if(kind==ARROW||kind==BARARROW)
		{
			consume();
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in arrowOp at"+t.getLinePos());
		}
	}
	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Expression e=null;
		switch (kind) {
		case IDENT: {
			e=new IdentExpression(t);
			consume();
		}
			break;
		case INT_LIT: {
			try {
				e=new IntLitExpression(t);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			consume();
		}
			break;
		case KW_TRUE:	{
			e=new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_FALSE: {
			e=new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:	{
			e=new ConstantExpression(t);
			consume();
		}
			break;
		case KW_SCREENHEIGHT: {
			e=new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e=expression();
			match(RPAREN);
		}
			break;
		default:
			throw new SyntaxException("illegal token "+t.kind +" in factor at"+t.getLinePos());
		}
		return e;
	}

	Block block() throws SyntaxException {
		Kind kind = t.kind;
		Token f=t;
		ArrayList<Dec> de=new ArrayList<Dec>();
		ArrayList<Statement> st=new ArrayList<Statement>();
		if(kind==LBRACE)
		{
			consume();
			while(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_IMAGE||t.kind==KW_FRAME||t.kind==OP_SLEEP||t.kind==KW_WHILE||t.kind==KW_IF||t.kind==IDENT||t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE||t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC||t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
			{
			if(t.kind==KW_INTEGER||t.kind==KW_BOOLEAN||t.kind==KW_IMAGE||t.kind==KW_FRAME)
			{
				de.add(dec());
			}
			else if(t.kind==OP_SLEEP||t.kind==KW_WHILE||t.kind==KW_IF||t.kind==IDENT||t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE||t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC||t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
			{
				st.add(statement());
			}
			}
				match(RBRACE);
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in block at"+t.getLinePos());
		}
		return new Block(f, de, st);
	}

	Program program() throws SyntaxException {
		Kind kind = t.kind;
		Token f=t;
		ArrayList<ParamDec>pd=new ArrayList<ParamDec>();
		Block b=null;
		if(kind==IDENT){
			consume();
		
			if(t.kind==LBRACE)
			{
				b=block();
			}
			else if(t.kind==KW_URL||t.kind==KW_FILE||t.kind==KW_INTEGER||t.kind==KW_BOOLEAN)
			{
				pd.add(paramDec());
				while(t.kind==COMMA)
				{
					consume();
					if(t.kind==KW_URL||t.kind==KW_FILE||t.kind==KW_INTEGER||t.kind==KW_BOOLEAN)
					{
						pd.add(paramDec());
					}
					else
					{
						throw new SyntaxException("illegal token "+t.kind +" in program at"+t.getLinePos());
					}
				}
				if(t.kind==LBRACE){
					b=block();
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in program at"+t.getLinePos());
				}
					
			}
			else
			{
				throw new SyntaxException("illegal token "+t.kind +" in program at"+t.getLinePos());
			}
			
		}
		else{
			throw new SyntaxException("illegal token "+t.kind +" in program at"+t.getLinePos());
		}
		return new Program(f, pd, b);
	}
	ParamDec paramDec() throws SyntaxException {
		Token f=t;
		Kind kind = t.kind;
		ParamDec p=null;
		if(kind==KW_URL||kind==KW_FILE||kind==KW_INTEGER||kind==KW_BOOLEAN)
		{
			consume();
			if(t.kind==IDENT)
			{
				p=new ParamDec(f, t);
				consume();
			}
			else
			{
				throw new SyntaxException("illegal token "+t.kind +" in paramDec at"+t.getLinePos());
			}
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in paramDec at"+t.getLinePos());
		}
		return p;
	}
	Dec dec() throws SyntaxException {
		Kind kind = t.kind;
		Token f=t;
		Dec d=null;
		if(kind==KW_INTEGER||kind==KW_BOOLEAN||kind==KW_IMAGE||kind==KW_FRAME)
		{
			consume();
			if(t.kind==IDENT)
			{
				
				d=new Dec(f, t);
				consume();
				
			}
			else
			{
				throw new SyntaxException("illegal token "+t.kind +" in dec at"+t.getLinePos());
			}
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in dec at"+t.getLinePos());
		}
		return d;
	}
	Statement statement() throws SyntaxException {
		Kind kind = t.kind;
		Token f=t;
		Expression ex=null;
		
		if(kind==OP_SLEEP)
		{
			consume();
			ex=expression();
			match(SEMI);
			return new SleepStatement(f, ex);
		}
		else if(kind==KW_WHILE)
		{
			
			return whileStatement();
		}
		else if(kind==KW_IF)
		{
			return ifStatement();
		}
		else if(kind==IDENT && scanner.peek().kind==ASSIGN)
		{
			IdentLValue ilv=new IdentLValue(t);
			ex=assign();
			match(SEMI);
			return new AssignmentStatement(f, ilv, ex);
			
		}
		else if(kind==IDENT||kind==OP_BLUR||kind==OP_GRAY||kind==OP_CONVOLVE||kind==KW_SHOW||kind==KW_HIDE||kind==KW_MOVE||kind==KW_XLOC||kind==KW_YLOC||kind==OP_WIDTH||kind==OP_HEIGHT||kind==KW_SCALE)
		{
			Statement bc=null;
			bc=chain();
			match(SEMI);
			return bc;
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in statement at"+t.getLinePos());
		}
	}
	WhileStatement whileStatement() throws SyntaxException {
		Kind kind = t.kind;
		Token f=t;
		Expression ex=null;
		Block b=null;
		if(kind==KW_WHILE)
		{
			consume();
			if(t.kind==LPAREN)
			{
				consume();
				if(FACTOR())
				{
					ex=expression();
					match(Kind.RPAREN);
						if(t.kind==LBRACE)
						{
							b=block();
						}
						else
						{
							throw new SyntaxException("illegal token "+t.kind +" in whileStatement at"+t.getLinePos());
						}
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in whileStatement at"+t.getLinePos());
				}
			}
			else
			{
				throw new SyntaxException("illegal token "+t.kind +" in whileStatement at"+t.getLinePos());
			}
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in whileStatement at"+t.getLinePos());
		}
		return new WhileStatement(f, ex, b);
	}

	IfStatement ifStatement() throws SyntaxException {
		Kind kind = t.kind;
		Token f=t;
		Expression ex=null;
		Block b=null;
		if(kind==KW_IF)
		{
			consume();
			if(t.kind==LPAREN)
			{
				consume();
				if(FACTOR())
				{
					ex=expression();
					if(t.kind==RPAREN)
					{
						consume();
						if(t.kind==LBRACE)
						{
							b=block();
						}
						else
						{
							throw new SyntaxException("illegal token "+t.kind +" in ifStatement at"+t.getLinePos());
						}
					}
					else
					{
						throw new SyntaxException("illegal token "+t.kind +" in ifStatement at"+t.getLinePos());
					}
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in ifStatement at"+t.getLinePos());
				}
			}
			else
			{
				throw new SyntaxException("illegal token "+t.kind +" in ifStatement at"+t.getLinePos());
			}
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in ifStatement at"+t.getLinePos());
		}
		return new IfStatement(f, ex, b);
	}
	Chain chain() throws SyntaxException {
		Chain ch=null;
		ChainElem chem=null;
		Chain chem0=null;
		Token f=null;
		Token first=t;
		Kind kind=t.kind;
		if(kind==IDENT||kind==OP_BLUR||kind==OP_GRAY||kind==Kind.OP_CONVOLVE||kind==KW_SHOW||kind==KW_HIDE||kind==KW_MOVE||kind==KW_XLOC||kind==KW_YLOC||kind==OP_WIDTH||kind==OP_HEIGHT||kind==KW_SCALE)
		{
			chem0=chainElem();
			f=t;
			arrowOp();
			chem=chainElem();
			ch=new BinaryChain(first, chem0, f, chem);
			while(t.kind==ARROW||t.kind==Kind.BARARROW)
			{
				f=t;
				arrowOp();
				 
				if(t.kind==IDENT||t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==Kind.OP_CONVOLVE||t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC||t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
				{
					chem=chainElem();
					ch=new BinaryChain(first, ch, f, chem);
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in chain at"+t.getLinePos());
				}
			}
			return ch;
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in chain at"+t.getLinePos());
		}
	}
	Expression assign() throws SyntaxException {
		Kind kind=t.kind;
		Expression ex=null;
		if(kind==IDENT)
		{
			consume();
			if(t.kind==ASSIGN)
			{
				consume();
				if(FACTOR())
				{
					ex=expression();
				}
				else
				{
					throw new SyntaxException("illegal token "+t.kind +" in assign at"+t.getLinePos());
				}
			}
			else
			{
				throw new SyntaxException("illegal token "+t.kind +" in assign at"+t.getLinePos());
			}
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in assign at"+t.getLinePos());
		}
		return ex;
	}
	ChainElem chainElem() throws SyntaxException {
		Kind kind=t.kind;
		Token f=t;
		if(kind==IDENT)
		{	
			IdentChain ic=null;
			ic=new IdentChain(f);
			consume();
			return ic;
		}
		else if(kind==OP_BLUR||kind==OP_GRAY||kind==Kind.OP_CONVOLVE)
		{
			FilterOpChain foc=null;
			filterOp();
			Tuple tup=null;
			tup=arg();
			foc=new FilterOpChain(f, tup);
			return foc;
		}
		else if(kind==KW_SHOW||kind==KW_HIDE||kind==KW_MOVE||kind==KW_XLOC||kind==KW_YLOC)
		{
			FrameOpChain froc=null;
			frameOp();
			Tuple tup=null;
			tup=arg();
			froc=new FrameOpChain(f,tup);
			return froc;
		}
		else if(kind==OP_WIDTH||kind==OP_HEIGHT||kind==KW_SCALE)
		{
			ImageOpChain ioc=null;
			imageOp();
			Tuple tup=null;
			tup=arg();
			ioc=new ImageOpChain(f,tup);
			return ioc;
		}
		else{
			throw new SyntaxException("illegal token "+t.kind +" in chainElem at"+t.getLinePos());
		}
	}
	void filterOp() throws SyntaxException {
		Kind kind=t.kind;
		 if(kind==OP_BLUR||kind==OP_GRAY||kind==Kind.OP_CONVOLVE)
			{
				consume();
			}
		 else
		 {
			 throw new SyntaxException("illegal token "+t.kind +" in filterOp at"+t.getLinePos());
		 }
	}
	void frameOp() throws SyntaxException {
		Kind kind=t.kind;
		if(kind==KW_SHOW||kind==KW_HIDE||kind==KW_MOVE||kind==KW_XLOC||kind==KW_YLOC)
		{
			consume();
		}
		else
		{
			throw new SyntaxException("illegal token "+t.kind +" in frameOp at"+t.getLinePos());
		}
	}
	void imageOp() throws SyntaxException {
		Kind kind=t.kind;
		 if(kind==OP_WIDTH||kind==OP_HEIGHT||kind==KW_SCALE)
			{
				consume();
			}
			else{
				throw new SyntaxException("illegal token "+t.kind +" in imageOp at"+t.getLinePos());
			}
	}
	Tuple arg() throws SyntaxException {
		Token f=t;
		Kind kind=t.kind;
		List <Expression> ex=new ArrayList<Expression>();
		if(kind==Kind.LPAREN)
		{
			consume();
			if(FACTOR())
			{
				ex.add(expression());
				
				while(t.kind==COMMA)
				{
					consume();
					if(FACTOR())
					{
						ex.add(expression());
					}
					else
					{
						
						throw new SyntaxException("illegal token "+t.kind +" in arg at"+t.getLinePos());
					}
				}
				match(RPAREN);
			}
			
		}
		return new Tuple(f, ex);
	}
	boolean FACTOR(){
		Kind kind= t.kind;
		if(kind==IDENT||kind==INT_LIT||kind==KW_TRUE||kind==KW_FALSE||kind==KW_SCREENWIDTH||kind==KW_SCREENHEIGHT||kind==LPAREN)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	boolean RELOP(){
		Kind kind= t.kind;
		if(kind==LT||kind==LE||kind==GT||kind==GE||kind==EQUAL||kind==NOTEQUAL)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	boolean WEAKOP(){
		Kind kind= t.kind;
		if(kind==PLUS||kind==MINUS||kind==OR)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	boolean STRONGOP(){
		Kind kind= t.kind;
		if(kind==TIMES||kind==DIV||kind==AND||kind==MOD)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind.equals(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind.equals(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind+"at"+t.getLinePos());
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
