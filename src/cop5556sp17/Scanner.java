package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;

public class Scanner {
	
	ArrayList <Integer> line_start=new ArrayList<Integer>();
	public HashMap<String,Kind> hm =new HashMap<String,Kind>();
	public static enum State{
		START,
		IDENT_START,
		INTEGER_LITERAL,
		COMMENT,
		DIV_OP,
		NOT_OP,
		AFTER_EQ,
		LESS_OP,
		GREAT_OP,
		MINUS_OP,
		OR_OP
	}
	/**
	 * Kind enum
	 */
	public static enum Kind {

		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;
		
		String getText() {
			return text;
		}
	}
	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message){
			super(message);
		}
	}
	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}


	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {   //done!!!		//TODO IMPLEMENT THIS
			if(kind==Kind.INT_LIT||kind==Kind.IDENT)
				return chars.substring(pos,pos+length);
			else
				return kind.getText();
		}

		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			int line=0;
			int pos_line=0;
			if(line_start.size()>=2)
			{
			for(int k=0;k<line_start.size()-1;k++)
			{
				if(pos>=line_start.get(k) && pos < line_start.get(k+1))
				{
					line=k;
					pos_line=pos-line_start.get(k);
				}
				else if(pos>=line_start.get(line_start.size()-1))
				{
					line=line_start.size()-1;
					pos_line=pos-line_start.get(line_start.size()-1);
				}
			}
			}
			else
			{
				line=0;
				pos_line=pos;
			}
			LinePos l=new LinePos(line,pos_line);			
			return l;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}
		
		public boolean isKind(Kind kind) 
		{
		// TODO Auto-generated method stub
		return this.kind.equals(kind);
		}
		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 * @throws IllegalNumberException 
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			
			
			long a=Long.parseLong((chars.substring(pos, pos+length)));
			if(a>Integer.MAX_VALUE)
			{
				//TODO throw Exception
				throw new NumberFormatException("");
			}
			else
				return (Integer.parseInt(chars.substring(pos, pos+length)));
		}
		 @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
	}




	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		line_start.add(0);
		hm.put("integer",Kind.KW_INTEGER);
		hm.put("boolean",Kind.KW_BOOLEAN);
		hm.put("image",Kind.KW_IMAGE);
		hm.put("url",Kind.KW_URL);
		hm.put("file",Kind.KW_FILE);
		hm.put("frame",Kind.KW_FRAME);
		hm.put("while",Kind.KW_WHILE);
		hm.put("if",Kind.KW_IF);
		hm.put("sleep",Kind.OP_SLEEP);
		hm.put("screenheight",Kind.KW_SCREENHEIGHT);
		hm.put("screenwidth",Kind.KW_SCREENWIDTH);
		hm.put("gray",Kind.OP_GRAY);
		hm.put("convolve",Kind.OP_CONVOLVE);
		hm.put("blur",Kind.OP_BLUR);
		hm.put("scale",Kind.KW_SCALE);
		hm.put("width",Kind.OP_WIDTH);
		hm.put("height",Kind.OP_HEIGHT);
		hm.put("xloc",Kind.KW_XLOC);
		hm.put("yloc",Kind.KW_YLOC);
		hm.put("hide",Kind.KW_HIDE);
		hm.put("show",Kind.KW_SHOW);
		hm.put("move",Kind.KW_MOVE);
		hm.put("true",Kind.KW_TRUE);
		hm.put("false",Kind.KW_FALSE);
		hm.put("EOF",Kind.EOF);
	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	State state ;
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0;
		int length=chars.length();
		state = State.START;
		int startPos = 0;
		int ch;
		while (pos <= length) {
			ch = pos < length ? chars.charAt(pos) : -1;
			switch (state) {
			case START:
			{
				pos = skipWhiteSpace(pos);
				ch = pos < length ? chars.charAt(pos) : -1;
				startPos = pos;
				switch (ch) {
				case -1: 
				{
					tokens.add(new Token(Kind.EOF, pos, 0));
					pos++;
				}break;

				case '+': 
				{
					tokens.add(new Token(Kind.PLUS, startPos, 1));
					pos++;
				}break;

				case '*':
				{
					tokens.add(new Token(Kind.TIMES, startPos, 1));
					pos++;
				}break;

				case '=':
				{
					state = State.AFTER_EQ;
					pos++;
				}break;
				
				case '0':
				{
					tokens.add(new Token(Kind.INT_LIT,startPos, 1));
					pos++;
					
				}break;
				
				case '/':
				{
					state = State.DIV_OP;
					pos++;
				}break;
				
				case '!':
				{
					state = State.NOT_OP;
					pos++;
				}break;

				case '%':
				{
					tokens.add(new Token(Kind.MOD, startPos, 1));
					pos++;
				} break;

				case '&':
				{
					tokens.add(new Token(Kind.AND, startPos, 1));
					pos++;
				} break;

				case ';':
				{
					tokens.add(new Token(Kind.SEMI, startPos, 1));
					pos++;
				} break;
				
				case ',':
				{
					tokens.add(new Token(Kind.COMMA, startPos, 1));
					pos++;
				}break;
				
				case '(':
				{
					tokens.add(new Token(Kind.LPAREN, startPos, 1));
					pos++;
				}break;
				
				case ')':
				{
					tokens.add(new Token(Kind.RPAREN, startPos, 1));
					pos++;
				}break;
				
				case '{':
				{
					tokens.add(new Token(Kind.LBRACE, startPos, 1));
					pos++;
				}break;

				case '}':
				{
					tokens.add(new Token(Kind.RBRACE, startPos, 1));
					pos++;
					//	System.out.println("RBRACE");
					break;
				}
				case '|':
				{
					state = State.OR_OP;
					pos++;
					break;
				}
				case '-':
				{
					state = State.MINUS_OP;
					pos++;
					break;
				}
				case '<':
				{
					state = State.LESS_OP;
					pos++;
					break;
				}
				case '>':
				{
					state = State.GREAT_OP;
					pos++;
					break;
				}

				default: {
					if (Character.isDigit(ch)) 
					{
						{
						state = State.INTEGER_LITERAL;
						pos++;
						}
						} 
					else if (Character.isJavaIdentifierStart(ch)) {
						state = State.IDENT_START;
						pos++;
					} 
					else {throw new IllegalCharException(
							String.valueOf(chars.charAt(pos)));
					}
				}
				break;
				} 

			}
			break;
			case IDENT_START :
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case -1: 
				{
					state=State.START;
					if(hm.containsKey(chars.substring(startPos, pos).toString()))
					{
						tokens.add(new Token(hm.get(chars.substring(startPos, pos)), startPos, pos-startPos));
					}
					else
					{
						tokens.add(new Token(Kind.IDENT, startPos, pos-startPos));
					}
					break;
				}
				default: {
					if (Character.isJavaIdentifierPart(ch))
					{
						pos++;
						state=State.IDENT_START;
					}
					//TODO add condition to check length of integer.
					else
					{
						if(hm.containsKey(chars.substring(startPos, pos).toString()))
						{
							tokens.add(new Token(hm.get(chars.substring(startPos, pos)), startPos, pos-startPos));
						}
						else
						{
							tokens.add(new Token(Kind.IDENT, startPos, pos-startPos));
						}
						state=State.START;
					}
					break;
				}
				}
				
			}
			break;

			case INTEGER_LITERAL:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				
				switch (ch) {
				case -1: 
				{
					state=State.START;
					long a=Long.parseLong((chars.substring(startPos, pos)));
					if(a>Integer.MAX_VALUE)
					{
						//TODO throw Exception
						throw new IllegalNumberException("");
					}
					else
					{
					tokens.add(new Token(Kind.INT_LIT, startPos, pos-startPos));
					}
					break;
				}
				default: { if(ch=='\n')
				{
					line_start.add(pos);
				}
				if (Character.isDigit(ch))
				{

					pos++;
					state=State.INTEGER_LITERAL;
				}
				//TODO add condition to check length of integer.
				else
				{
					tokens.add(new Token(Kind.INT_LIT, startPos, pos-startPos));
					state=State.START;
				}
				}
				}
			}
			break;
			case NOT_OP:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case -1: 
				{
					tokens.add(new Token(Kind.NOT,startPos,1));
					state=State.START;
				}
				break;
				case '=':
				{
					tokens.add(new Token(Kind.NOTEQUAL,startPos,2));
					pos++;
					state=State.START;
				}
				break;
				default:
				{
					tokens.add(new Token(Kind.NOT,startPos,1));
					state=State.START;
				}
				}
			}
			break;
			
			case GREAT_OP:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case -1: 
				{
					tokens.add(new Token(Kind.GT,startPos,1));
					state=State.START;

				}
				break;
				case '=':
				{
					
					tokens.add(new Token(Kind.GE,startPos,2));
					pos++;
					state=State.START;
				}
				break;
				default:
				{
					tokens.add(new Token(Kind.GT,startPos,1));
					state=State.START;
				}
				}
			}
			break;
			case AFTER_EQ:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				
				case '=':
				{
					tokens.add(new Token(Kind.EQUAL,startPos,2));
					pos++;
					state=State.START;
				}
				break;
				default:
				{
					throw new IllegalCharException("");
					
				}
				}
			}
			break;
			case OR_OP:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case -1: 
				{
					tokens.add(new Token(Kind.OR,startPos,1));
					state=State.START;
				}
				break;
				case '-':
				{
					if(chars.charAt(pos+1)=='>')
					{
						tokens.add(new Token(Kind.BARARROW,startPos,3));
						pos++;
						state=State.START;
						pos++;
					}
					else
					{
						tokens.add(new Token(Kind.OR,startPos,1));
						state=State.MINUS_OP;
						pos++;
					}
				}
				break;
				default:
				{
					tokens.add(new Token(Kind.OR,startPos,1));
					state=State.START;
				}
				}
			}
			break;
			case MINUS_OP:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				startPos = pos-1;
				switch (ch) {
				case -1: 
				{
					tokens.add(new Token(Kind.MINUS,startPos,1));
					state=State.START;
				}
				break;
				case '>':
				{
					tokens.add(new Token(Kind.ARROW,startPos,2));
					pos++;
					state=State.START;
				}
				break;
				default:
				{
					tokens.add(new Token(Kind.MINUS,startPos,1));
					state=State.START;
				}
				}
			}
			break;
			
			case LESS_OP:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				startPos = pos-1;
				switch (ch) {
				case -1: 
				{
					tokens.add(new Token(Kind.MINUS,startPos,1));
					state=State.START;
				}
				break;
				case '-':
				{
					tokens.add(new Token(Kind.ASSIGN,startPos,2));
					pos++;
					state=State.START;
				}
				break;
				case '=':
				{
					tokens.add(new Token(Kind.LE,startPos,2));
					pos++;
					state=State.START;
				}
				break;
				default:
				{
					tokens.add(new Token(Kind.LT,startPos,1));
					state=State.START;
				}
				}
			}
			break;
			
			case DIV_OP:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case -1: 
				{
					tokens.add(new Token(Kind.DIV,startPos,1));
					state=State.START;
				}
				break;
				case '*':
				{
					state=State.COMMENT;
					pos++;
				}
				break;
				default:
				{
					tokens.add(new Token(Kind.DIV,startPos,1));
					state=State.START;
				}
				}
			}
			break;
			case COMMENT:
			{
				ch = pos < length ? chars.charAt(pos) : -1;
				switch (ch) {
				case -1: 
				{
					state=State.START;
				}
				break;
				case '*':
				{
					if( (pos+1) >= length)
						{
						state=State.START;
						pos++;
						}
					else{
					if(chars.charAt(pos+1)=='/')
					{
						state=State.START;
						pos=pos+2;
					}
					else
					{
						state=State.COMMENT;
						pos++;
					}
					}
				}
				break;
				default:
				{
					if(ch=='\n')
					{
						line_start.add(pos);
					}
					pos++;
				}
				}
			}
			break;
			default:
			{
				
			//	throw new IllegalCharException(String.valueOf(chars.charAt(pos)));
			}
			assert false;
				
			}// switch(state)
		} // while

		//TODO IMPLEMENT THIS!!!!
		//tokens.add(new Token(Kind.EOF,pos,0));

		return this;  
	}

	private int skipWhiteSpace(int pos) {
		// TODO Auto-generated method stub
		int ch = pos < chars.length() ? chars.charAt(pos) : -1;
		boolean white = true;
		while(white)
		{
			if(Character.isWhitespace(ch))
			{
				pos++;
				if(ch=='\n')
				{
					line_start.add(pos);
				}
			 ch = pos < chars.length() ? chars.charAt(pos) : -1;
			}
			else
			{
				state=State.START;
				white=false;
				
			}
		}

		return pos;
	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek()
	{
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}



	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * s
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
	}


}
