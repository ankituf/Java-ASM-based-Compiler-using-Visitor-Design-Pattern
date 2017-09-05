package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception
	{
		TypeName typname1 = (TypeName) binaryChain.getE0().visit(this,arg);
        TypeName typnam2 = (TypeName) binaryChain.getE1().visit(this,arg);
        Token binaryToken = binaryChain.getE1().getFirstToken();
        switch(binaryChain.getArrow().kind)
        {
        case ARROW:
        {
        	if(typname1.equals(URL) && typnam2.equals(IMAGE))
        	{
        		binaryChain.typname_chain = typnam2;
        	}
        	else if(typname1.equals(FILE) && typnam2.equals(IMAGE))
        	{
        		binaryChain.typname_chain = typnam2;
        	}
        	else if(typname1.equals(FRAME) && binaryChain.getE1() instanceof FrameOpChain)
        	{
        		if(binaryToken.kind.equals(KW_XLOC) || binaryToken.kind.equals(KW_YLOC))
        		{
        			binaryChain.typname_chain = INTEGER;
        		}
        		else if(binaryToken.kind.equals(KW_SHOW) || binaryToken.kind.equals(KW_HIDE) || binaryToken.kind.equals(KW_MOVE))
        		{
        			binaryChain.typname_chain = FRAME;
        		}
        		else 
        			throw new TypeCheckException("Error: in visitBinaryChain ");
        	}
        	else if(typname1.equals(IMAGE) && binaryChain.getE1() instanceof ImageOpChain)
        	{
        		if(binaryToken.kind.equals(OP_WIDTH) || binaryToken.kind.equals(OP_HEIGHT))
        		{
        			binaryChain.typname_chain = INTEGER;
        		}
        		else if(binaryToken.kind.equals(KW_SCALE))
        		{
        			binaryChain.typname_chain = IMAGE;
        		}
        		else 
        			throw new TypeCheckException("Error: in visitBinaryChain ");
        	}
        	else if(typname1.equals(IMAGE) && typnam2.equals(FRAME))
        	{
        		binaryChain.typname_chain = typnam2;
        	}
        	else if(typname1.equals(IMAGE) && typnam2.equals(FILE))
        	{
        		binaryChain.typname_chain = NONE;
        	}
        	else if(typname1.equals(IMAGE) && (binaryChain.getE1() instanceof IdentChain) && typnam2.equals(IMAGE))
        	{
        		binaryChain.typname_chain = IMAGE;
        	}
        	else if(typname1.equals(INTEGER) && (binaryChain.getE1() instanceof IdentChain) && typnam2.equals(INTEGER))
        	{
        		binaryChain.typname_chain = INTEGER;
        	}
        	else if(typname1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)
        	{
        		if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE))
        		{
        			binaryChain.typname_chain = IMAGE;
        		}
        		else 
        			throw new TypeCheckException("Error: in visitBinaryChain ");
        	}

        	else 
        		throw new TypeCheckException("Error: in visitBinaryChain ");

        }
        break;
        case BARARROW:
        {
        	if(typname1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)
        	{
        		if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE))
        		{
        			binaryChain.typname_chain = IMAGE;
        		}
        		else 
        			throw new TypeCheckException("Error: in visitBinaryChain ");
        	}
        	else 
        		throw new TypeCheckException("Error: in visitBinaryChain ");
        }
        break;
        default:
        	throw new TypeCheckException("Error: in visitBinaryChain ");
        }
		return binaryChain.typname_chain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception
	{
		TypeName typname1 = (TypeName) binaryExpression.getE0().visit(this,arg);
        TypeName typname2 = (TypeName) binaryExpression.getE1().visit(this,arg);

        switch(binaryExpression.getOp().kind)
        {
        case TIMES:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else if(typname1.equals(INTEGER) && typname2.equals(IMAGE))
        	{
        		binaryExpression.Typname_expr = typname2;
        	}
        	else if(typname1.equals(IMAGE) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression ");
        }
        break;
        case DIV:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else if(typname1.equals(IMAGE) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException(" Error: in visitBinaryExpression ");
        }
        break;
        case MOD:
        	if(typname1.equals(IMAGE) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException(" Error: in visitBinaryExpression ");
        break;
        case PLUS:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else if(typname1.equals(IMAGE) && typname2.equals(IMAGE))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");
        }
        break;
        case MINUS:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else if(typname1.equals(IMAGE) && typname2.equals(IMAGE))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException(" Error: in visitBinaryExpression ");
        }
        break;
        case LT:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else if(typname1.equals(BOOLEAN) && typname2.equals(BOOLEAN))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");
        }
        break;
        case GT:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else if(typname1.equals(BOOLEAN) && typname2.equals(BOOLEAN))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");
        }
        break;
        case LE:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else if(typname1.equals(BOOLEAN) && typname2.equals(BOOLEAN))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException(" Error: in visitBinaryExpression ");
        }
        break;
        case GE:
        {
        	if(typname1.equals(INTEGER) && typname2.equals(INTEGER))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else if(typname1.equals(BOOLEAN) && typname2.equals(BOOLEAN))
        	{
        		binaryExpression.Typname_expr = typname1;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");
        }
        break;
        case EQUAL:
        {
        	if(typname1.equals(typname2))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");
        }
        break;
        case NOTEQUAL:
        {
        	if(typname1.equals(typname2))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");
        }
        break;
        case AND:
        {
        	if(typname1.equals(typname2))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");

        }
        break;
        case OR:
        {
        	if(typname1.equals(typname2))
        	{
        		binaryExpression.Typname_expr = BOOLEAN;
        	}
        	else
        		throw new TypeCheckException("Error: in visitBinaryExpression  ");

        }
        break;
        default:
        	throw new TypeCheckException(" Error: in visitBinaryExpression ");
        }
        return binaryExpression.Typname_expr;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception 
	{
		ArrayList<Dec> dec_arraylist=block.getDecs();
		ArrayList<Statement> st_arraylist=block.getStatements();
		symtab.enterScope();
		for(Dec d:dec_arraylist)
		{
			d.visit(this, null);
		}
		for(Statement s:st_arraylist)
		{
			s.visit(this, null);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception
	{
		booleanLitExpression.Typname_expr = BOOLEAN;
		return booleanLitExpression.Typname_expr;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception 
	{
		filterOpChain.getArg().visit(this, null);
		if(filterOpChain.getArg().getExprList().size()!=0)
		{
			throw new TypeCheckException(" Error: in  visitFilterOpChain");
		}
		else
		{
			filterOpChain.typname_chain=IMAGE;
		}
		return filterOpChain.typname_chain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception 
	{
		frameOpChain.getArg().visit(this, null);
		Kind frame_token=frameOpChain.getFirstToken().kind;
		frameOpChain.getArg().visit(this, null);
		switch(frame_token)
		{
		
		case KW_YLOC:
		{
			if(frameOpChain.getArg().getExprList().size()==0)
			{
				frameOpChain.typname_chain=INTEGER;
			}
			else
			{
				throw new TypeCheckException("Error: in visitFrameOpChain ");
			}
		}
		break;
		case KW_XLOC:
		{
			if(frameOpChain.getArg().getExprList().size()==0)
			{
				frameOpChain.typname_chain=INTEGER;
			}
			else
			{
				throw new TypeCheckException("Error: in visitFrameOpChain ");
			}
		}
		break;
		case KW_MOVE:
		{
			if(frameOpChain.getArg().getExprList().size()==2)
			{
				frameOpChain.typname_chain=NONE;
			}
			else
			{
				throw new TypeCheckException("Error: in visitFrameOpChain ");
			}
		}
		break;
		case KW_SHOW:
		{
			if(frameOpChain.getArg().getExprList().size()==0)
			{
				frameOpChain.typname_chain=NONE;
			}
			else
			{
				throw new TypeCheckException("Error: in visitFrameOpChain ");
			}
		}
		break;
		case KW_HIDE:
		{
			if(frameOpChain.getArg().getExprList().size()==0)
			{
				frameOpChain.typname_chain=NONE;
			}
			else
			{
				throw new TypeCheckException("Error: in visitFrameOpChain ");
			}
		}
		break;
		default:
			throw new TypeCheckException("Error: in visitFrameOpChain ");
		}
		return frameOpChain.typname_chain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception 
	{
		Dec dec_ident=symtab.lookup(identChain.getFirstToken().getText());
		if(dec_ident!=null)
		{
			identChain.typname_chain=dec_ident.decType;
			identChain.identDec=dec_ident;
		}
		else
		{
			throw new TypeCheckException("Error: in visitIdentChain");
		}
		return identChain.typname_chain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception 
	{
		Dec dec_ident=symtab.lookup(identExpression.getFirstToken().getText());
		if(dec_ident!=null)
		{
			identExpression.Typname_expr=dec_ident.decType;
			identExpression.expressionDec=dec_ident;
		}
		else
		{
			throw new TypeCheckException("Error: in visitIdentExpression");
		}
		return identExpression.Typname_expr;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception 
	{
		Expression expr=ifStatement.getE();
		Block blok=ifStatement.getB();
		if(!expr.visit(this, null).equals(BOOLEAN))
		{
			throw new TypeCheckException("Error: in visitIfStatement");
		}
		blok.visit(this, null);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception 
	{
		intLitExpression.Typname_expr=INTEGER;
		return intLitExpression.Typname_expr;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception 
	{
		Expression slep=sleepStatement.getE();
		if(slep.visit(this, null).equals(INTEGER))
		{
			//do nothing
		}
		else
		{
			throw new TypeCheckException("Error: in visitSleepStatement");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception 
	{
		Expression e=whileStatement.getE();
		Block b=whileStatement.getB();
		if(e.visit(this, null).equals(BOOLEAN))
		{
			//do nothing
		}
		else
		{
			throw new TypeCheckException("Error: in visitWhileStatement");
		}
		b.visit(this, null);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception 
	{
		declaration.decType=Type.getTypeName(declaration.getFirstToken());
		if(symtab.insert(declaration.getIdent().getText(), declaration))
		{
			//do nothing
		}
		else
		{
			throw new TypeCheckException("Error: in visitDec");
		}
		return declaration.decType;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception 
	{
		ArrayList<ParamDec> param_arraylist=program.getParams();
		Block block_arraylist=program.getB();
		for(Dec d:param_arraylist)
		{
			d.visit(this, null);
		}
		block_arraylist.visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception 
	{
		TypeName typname1=(TypeName)assignStatement.getVar().visit(this, null);
		TypeName typname2=(TypeName)assignStatement.getE().visit(this, null);
		if(typname1!=typname2)
		{
			throw new TypeCheckException(" Error in visitAssignmentStatement");
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception 
	{
		Dec ident=symtab.lookup(identX.firstToken.getText());
		if(ident!=null)
		{
			identX.identlvaluedec=ident;
		}
		else
		{
			throw new TypeCheckException("Error in visitIdentLvalue");
		}
		return identX.identlvaluedec.decType;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception 
	{
		paramDec.decType = Type.getTypeName(paramDec.getFirstToken());
		
		if(symtab.insert(paramDec.getIdent().getText(), paramDec))
		{
			
		}
		else
		{
			throw new TypeCheckException("Error in visitParamDec");
		}
		return paramDec.decType;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg)
	{
		constantExpression.Typname_expr = INTEGER;
		return constantExpression.Typname_expr;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception 
	{
		
		Kind image_chain=imageOpChain.getFirstToken().kind;
		imageOpChain.getArg().visit(this, null);
		switch(image_chain)
		{
		case OP_HEIGHT:
		{
			if(imageOpChain.getArg().getExprList().size()==0)
			{
				imageOpChain.typname_chain=INTEGER;
			}
			else
			{
				throw new TypeCheckException("Error in visitImageOpChain");
			}
		}
		break;
		case OP_WIDTH:
		{
			if(imageOpChain.getArg().getExprList().size()==0)
			{
				imageOpChain.typname_chain=INTEGER;
			}
			else
			{
				throw new TypeCheckException("Error in visitImageOpChain");
			}
		}
		break;
		case KW_SCALE:
		{
			if(imageOpChain.getArg().getExprList().size()==1)
			{
				imageOpChain.typname_chain=IMAGE;
			}
			else
			{
				throw new TypeCheckException("Error in visitImageOpChain");
			}
		}
		break;
		default:
		{
			throw new TypeCheckException("Error in visitImageOpChain");
		}
		}
		return imageOpChain.typname_chain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception 
	{
		for(Expression e:tuple.getExprList())
		{
			if(e.visit(this, null).equals(INTEGER))
			{
				
			}
			else
			{
				throw new TypeCheckException("Error in visitTuple");
			}
		}

		return null;
	}


}