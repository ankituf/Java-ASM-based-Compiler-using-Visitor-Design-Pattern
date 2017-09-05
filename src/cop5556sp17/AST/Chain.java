package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;


public abstract class Chain extends Statement {
	public TypeName typname_chain;
	public Dec identDec;
	
	
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
//
//	public TypeName getTypename() {
//		// TODO Auto-generated method stub
//		return chainType;
//	}

	public void setTypeName(TypeName typeName)
	{
	this.typname_chain=typeName;
	}

	public TypeName getTypeName() {
		// TODO Auto-generated method stub
		return typname_chain;
	}
	
	public Dec getDec()
	{
		return identDec;
	}



	
	
	
	
	
	

}
