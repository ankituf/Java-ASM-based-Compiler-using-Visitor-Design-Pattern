package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	public TypeName Typname_expr;
//	public Dec expressionDec;
//	public TypeName val;
	
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;
	
	public TypeName getType(){
		return Typname_expr;
	}

	public TypeName getTypeName() {
		// TODO Auto-generated method stub
		return Typname_expr;
	}
	

}
