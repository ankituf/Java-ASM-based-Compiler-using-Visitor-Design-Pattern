package cop5556sp17;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements cop5556sp17.AST.ASTVisitor, Opcodes {

	
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int pointer=1;
	int iterator=0;
	MethodVisitor mv; // visitor of method currently under construction

	
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		
		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec paramDec : params)
			paramDec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		//mv.visitLocalVariable("args", "[Ljava/lang/String;", null, startRun, endRun, 1);
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception 
	{
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		TypeName expressionType = assignStatement.getE().getType();
		TypeName im=TypeName.IMAGE;
		
		if(expressionType.isType(im))
		{
			mv.visitInsn(DUP);
		}
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		//Object toPass;
		Chain bc0=binaryChain.getE0();
		Chain bc1=binaryChain.getE1();
		
		if(bc0 instanceof FilterOpChain)
			bc0.visit(this, binaryChain.getArrow().kind);
		else
		   bc0.visit(this, 1);
		
		if(bc0.getTypeName().equals(TypeName.URL)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
		}
		else if(bc0.getTypeName().equals(TypeName.FILE)){

			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
		}
		if(bc1 instanceof FilterOpChain)
			bc1.visit(this, binaryChain.getArrow().kind);
		else
			bc1.visit(this, 2);
		return null;
	}



	
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
		Label L_Start = new Label();
		Label L_END = new Label();
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		TypeName exType0 = binaryExpression.getE0().getType();
		TypeName exType1 = binaryExpression.getE1().getType();
		
		Kind op = binaryExpression.getOp().kind;
		
		switch(op)
		{
		case MOD:
		{
			if (exType0.equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod",
						PLPRuntimeImageOps.modSig, false);
			} else 
				mv.visitInsn(IREM);
		}
			break;
		case OR:
		{
			mv.visitInsn(IOR);
		}
		break;
		case MINUS:
		{
			if(exType0.isType(IMAGE))
				{
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub",
							 PLPRuntimeImageOps.subSig,false);
				}else
				mv.visitInsn(ISUB);
		}
		break;
		case PLUS:
		{
			if(exType0.isType(IMAGE))
			{
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add",
						PLPRuntimeImageOps.addSig,
						false);
			}else
			mv.visitInsn(IADD);
		}
		break;
		case TIMES:
		{
			if (exType0.equals(TypeName.IMAGE)) {
			////Swapping values in stack
			//mv.visitInsn(SWAP);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul",
					PLPRuntimeImageOps.mulSig, false);
		} else if (exType1.equals(TypeName.IMAGE)) {
			mv.visitInsn(SWAP);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul",
					PLPRuntimeImageOps.mulSig,false);
		} else 
		mv.visitInsn(IMUL);
			
		}
		break;
		case AND:
		{
			mv.visitInsn(IAND);
		}
		break;
		case DIV:
		{
			if (exType0.equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div",
						PLPRuntimeImageOps.divSig,false);
			} else
			mv.visitInsn(IDIV);
		}
		break;
		case LT:
		{
			mv.visitJumpInsn(IF_ICMPLT,L_Start);
			mv.visitLdcInsn(false);
		}
		break;
		case LE:
		{
			mv.visitJumpInsn(IF_ICMPLE,L_Start);
			mv.visitLdcInsn(false);
		}
		break;
		case GT:
		{
			mv.visitJumpInsn(IF_ICMPGT,L_Start);
			mv.visitLdcInsn(false);	
		}
		break;
		case GE:
		{
			mv.visitJumpInsn(IF_ICMPGE,L_Start);
			mv.visitLdcInsn(false);
		}
		break;
		case EQUAL:
		{
			if(binaryExpression.getType().equals(IMAGE))
				mv.visitJumpInsn(IF_ACMPEQ ,L_Start);
			else
				mv.visitJumpInsn(IF_ICMPEQ,L_Start);
				mv.visitLdcInsn(false);
		}
		break;
		case NOTEQUAL:
		{
			if(binaryExpression.getType().equals(IMAGE))
			mv.visitJumpInsn(IF_ACMPNE ,L_Start);
			else
			mv.visitJumpInsn(IF_ICMPNE,L_Start);
			mv.visitLdcInsn(false);
		}
		break;
		default:
		break;
		}
		
		mv.visitJumpInsn(GOTO, L_END);
		mv.visitLabel(L_Start);
		mv.visitLdcInsn(true);
		mv.visitLabel(L_END);
		return null;
		
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		Label L_Start=new Label();
		Label L_End=new Label();
		
		for(Dec d: block.getDecs())
		{
			TypeName de=d.getTypeName();
			if(de.isType(TypeName.IMAGE,TypeName.FRAME))
			{
				mv.visitInsn(ACONST_NULL);
				mv.visitVarInsn(ASTORE,pointer);
			}
			d.visit(this, arg);
			mv.visitLocalVariable(d.getIdent().getText(), d.getTypeName().getJVMTypeDesc(), null, L_Start, L_End, d.getSlot());
		}
		
		mv.visitLabel(L_Start);
		for(Statement  s: block.getStatements())
		{		
			if(s instanceof AssignmentStatement)
				{
				
					if(((AssignmentStatement)s).getVar().getDec() instanceof ParamDec)
						mv.visitVarInsn(ALOAD,0);	
				}
			s.visit(this, arg);
			if((s) instanceof BinaryChain)
				mv.visitInsn(POP);	
		}
		mv.visitLabel(L_End);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		Token ce=constantExpression.getFirstToken();
		if(ce.isKind(Kind.KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}
		else if(ce.isKind(Kind.KW_SCREENWIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setSlot(pointer);
		pointer++;
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//filterOpChain.getArg().visit(this, arg);
		//mv.visitInsn(ACONST_NULL);
		Kind kindOfop = (Kind) arg; //passing from chain
		
		if(kindOfop.equals(BARARROW) && filterOpChain.getFirstToken().kind.equals(Kind.OP_GRAY))
		{
			mv.visitInsn(DUP);
		}
		else
		mv.visitInsn(ACONST_NULL);
		if(filterOpChain.getFirstToken().kind.equals(Kind.OP_BLUR)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(filterOpChain.getFirstToken().kind.equals(Kind.OP_CONVOLVE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}
		if(filterOpChain.getFirstToken().kind.equals(Kind.OP_GRAY)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		Token ft=frameOpChain.getFirstToken();
		if(!ft.kind.equals(Kind.KW_MOVE))
		{
			mv.visitInsn(DUP);
		}
		if(ft.kind.equals(Kind.KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL,  PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		}
		else if(ft.kind.equals(Kind.KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL,  PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}
		else if(ft.kind.equals(Kind.KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL,  PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}
		else if(ft.kind.equals(Kind.KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL,  PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}
		else if(ft.kind.equals(Kind.KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL,  PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
	
		Integer part = (Integer) arg;
		if(part==1){ //Left 
			if(identChain.getDec() instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(), ((TypeName) identChain.getTypeName()).getJVMTypeDesc());
			} else {
				if (identChain.getTypeName().equals(TypeName.INTEGER) || identChain.getTypeName().equals(TypeName.BOOLEAN)) {
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
				}else if(identChain.getTypeName().equals(TypeName.FILE) || identChain.getTypeName().equals(TypeName.IMAGE)){
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				}
			}
		}

		else{
			if(identChain.getTypeName().equals(TypeName.FILE)){
				if(identChain.getDec() instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(), ((TypeName) identChain.getTypeName()).getJVMTypeDesc());
				}
				else {
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				}

				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",PLPRuntimeImageIO.writeImageDesc, false);
			}
			else if(identChain.getTypeName().equals(TypeName.FRAME)){
				if(identChain.getDec() instanceof ParamDec){
					
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(), ((TypeName) identChain.getTypeName()).getJVMTypeDesc());
				
				}
				else {
					
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				}
				
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
						PLPRuntimeFrame.createOrSetFrameSig, false);
			}
			
			mv.visitInsn(DUP);
			TypeName tpn=identChain.getTypeName();
			if (tpn.equals(TypeName.IMAGE))
			{
				
				if (identChain.getDec() instanceof ParamDec) {
					
					
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							((TypeName) identChain.getTypeName()).getJVMTypeDesc());
				} else {
					
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
				}
				
			}
			else if( tpn.equals(TypeName.INTEGER)){
				if (identChain.getDec() instanceof ParamDec) {
					// class variable
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							((TypeName) identChain.getTypeName()).getJVMTypeDesc());
				} 
				else {
					
						mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
				}
			}
			else if( tpn.equals(TypeName.BOOLEAN)){
				if (identChain.getDec() instanceof ParamDec) {
					// class variable
					
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							((TypeName) identChain.getTypeName()).getJVMTypeDesc());
				} 
				else
				{
					
						mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
				}
				
			}
			else if (tpn.equals(TypeName.FRAME)) {
				if (identChain.getDec() instanceof ParamDec) {
					// class variable
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							((TypeName) identChain.getTypeName()).getJVMTypeDesc());
				} 
				else {
					
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
				}
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		if (identExpression.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);

			mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(),
					identExpression.getType().getJVMTypeDesc());
		} else {
			
			if (identExpression.getType().equals(TypeName.INTEGER)){
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
			}
			else if (identExpression.getType().equals(TypeName.BOOLEAN)) 
			{
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
			} 
			else if (identExpression.getType().equals(TypeName.IMAGE))
			{
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
			}
			else if( identExpression.getType().equals(TypeName.FRAME))
			{
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
			}
			else if( identExpression.getType().equals(TypeName.FILE))
			{
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
			}
			else if( identExpression.getType().equals(TypeName.URL)) 
			{
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
			}
			
		}
		return null;
	}
	
	
	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//MethodVisitor mvs = (MethodVisitor) arg;
		if (identX.getDec() instanceof ParamDec) {
			mv.visitFieldInsn(PUTFIELD, className, identX.getFirstToken().getText(),
					identX.getDec().decType.getJVMTypeDesc());
		} else {
			if (identX.getDec().decType.equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
						PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
			} else if (identX.getDec().decType.equals(TypeName.INTEGER))
			{
				mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
			}
			else if( identX.getDec().decType.equals(TypeName.BOOLEAN)) {
				mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
			}
			
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception 
	{
		ifStatement.getE().visit(this, arg);
		Label L_AFTER=new Label();
		mv.visitJumpInsn(IFEQ,L_AFTER);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(L_AFTER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		Token t=imageOpChain.getFirstToken();
		if(t.isKind(Kind.OP_WIDTH)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
		}
		else if(t.isKind(Kind.OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
		}
		else if(t.isKind(Kind.KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		}
		return null;
	}

	
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception 
	{
		
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception 
	{
		//For assignment 5, only needs to handle integers and booleans
		
		if(paramDec.getTypeName().equals(TypeName.INTEGER))
		{
			FieldVisitor fv=cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, null);
			fv.visitEnd();
			
		}
		else if(paramDec.getTypeName().equals(TypeName.BOOLEAN))
		{
			FieldVisitor fv=cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, null);
			fv.visitEnd();
			
		}
		else if(paramDec.getTypeName().equals(TypeName.FILE))
		{
			FieldVisitor fv=cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
			fv.visitEnd();
			
		}
		else if(paramDec.getTypeName().equals(TypeName.URL))
		{
			FieldVisitor fv=cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
			fv.visitEnd();
			
		}
		
		
		if(paramDec.getTypeName().equals(TypeName.INTEGER))
		{
			//paramDec.setSlot(pointer++);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",false);
			mv.visitFieldInsn(PUTFIELD,className, paramDec.getIdent().getText(), "I");
		}
		else if(paramDec.getTypeName().equals(TypeName.BOOLEAN))
		{
			//paramDec.setSlot(pointer++);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",false);
			mv.visitFieldInsn(PUTFIELD,className, paramDec.getIdent().getText(), "Z");
		}else if(paramDec.getTypeName().equals(TypeName.FILE))
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}else if(paramDec.getTypeName().equals(TypeName.URL))
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL",  PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
		
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for(Expression e: tuple.getExprList()){
			e.visit(this, arg);
		}
		return null;
	}

	
	
	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception 
	{
		Label L_out=new Label();
		mv.visitJumpInsn(GOTO, L_out);
		Label L_in=new Label();
		mv.visitLabel(L_in);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(L_out);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE,L_in);
		return null;
	}

}