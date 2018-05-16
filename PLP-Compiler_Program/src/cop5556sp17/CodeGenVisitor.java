package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
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

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 * 
	 * 
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
	
	int slotVariable=1;
	int fieldIndex = 0;
	

	MethodVisitor mv; 

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
		for (ParamDec dec : params)
			dec.visit(this, fieldIndex++);
			
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
//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
	//	assert false : "not yet implemented";
	
	binaryChain.getE0().visit(this, 0);
	
	if(binaryChain.getArrow().isKind(BARARROW)){
		binaryChain.getE1().visit(this, 2);
	}
	else{
		binaryChain.getE1().visit(this, 1);
	}
	
	return null;
	
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		
		Token op;
		
		Label label1;
		Label label2;

		
		op=binaryExpression.getOp();
		
		(binaryExpression.getE0()).visit(this, arg);
		(binaryExpression.getE1()).visit(this, arg);
		
		if(op.kind==Kind.PLUS)
		{
			
			if(binaryExpression.getE0().getTypeName()==TypeName.INTEGER){
			
			mv.visitInsn(Opcodes.IADD);}
			
			else
			{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			}
		}
		else if(op.kind==Kind.MINUS){
			
			if(binaryExpression.getE0().getTypeName()==TypeName.INTEGER)
			{
			mv.visitInsn(Opcodes.ISUB);
			}
			else
			{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}
		}
		else if(op.kind==Kind.TIMES)
		{
			if((binaryExpression.getE0().getTypeName()==TypeName.INTEGER)&& (binaryExpression.getE1().getTypeName()==TypeName.INTEGER))
			{
			mv.visitInsn(Opcodes.IMUL);
			}
			
			else if((binaryExpression.getE0().getTypeName()==TypeName.INTEGER)&& (binaryExpression.getE1().getTypeName()==TypeName.IMAGE))
			{
			mv.visitInsn(Opcodes.SWAP);	
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}
			else{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}
		}
		
		else if(op.kind==Kind.DIV){
			if((binaryExpression.getE0().getTypeName()==TypeName.INTEGER)&& (binaryExpression.getE1().getTypeName()==TypeName.INTEGER)){
				mv.visitInsn(Opcodes.IDIV);
			}
			
			else{
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			}
		}
		
		else if(op.kind==Kind.MOD){
			if((binaryExpression.getE0().getTypeName()==TypeName.INTEGER)&& (binaryExpression.getE1().getTypeName()==TypeName.INTEGER)){
				mv.visitInsn(Opcodes.IREM);
			}
			else{
				
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			}
			
		}
		
		else if(op.kind==(Kind.OR)){
			mv.visitInsn(IOR);
			
		}
		
		else if(op.kind==Kind.AND){
			mv.visitInsn(IAND);
		}
		
		
		else if(op.kind==Kind.LT){
			Label l1=new Label();
			mv.visitJumpInsn(Opcodes.IF_ICMPGE, l1);
			mv.visitInsn(Opcodes.ICONST_1);
			Label l2=new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(l2);
			
		}
		
		else if(op.kind==Kind.LE){
			Label l1=new Label();
			mv.visitJumpInsn(Opcodes.IF_ICMPGT, l1);
			mv.visitInsn(Opcodes.ICONST_1);
			Label l2=new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(l2);
			
			
		}
		
		
		
		else if(op.kind==(Kind.GE))
		{
			Label l1=new Label();
			mv.visitJumpInsn(Opcodes.IF_ICMPLT, l1);
			mv.visitInsn(Opcodes.ICONST_1);
			Label l2=new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(l2);
		}
		
		else if(op.kind==Kind.GT){
			Label l1=new Label();
			mv.visitJumpInsn(Opcodes.IF_ICMPLE, l1);
			mv.visitInsn(Opcodes.ICONST_1);
			Label l2=new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(l2);
			
		}
		
		else if(op.kind==(Kind.EQUAL))
		{
			
			if (binaryExpression.getE0().getTypeName() == TypeName.INTEGER || binaryExpression.getE0().getTypeName() == TypeName.BOOLEAN){
				
				Label l1 = new Label();
				mv.visitJumpInsn(Opcodes.IF_ICMPNE, l1);
				mv.visitInsn(Opcodes.ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(Opcodes.ICONST_0);
				mv.visitLabel(l2);
			}
			
			else
			
			{
				Label l1 = new Label();
				  mv.visitJumpInsn(Opcodes.IF_ACMPNE, l1);
				  mv.visitInsn(ICONST_1);
				  Label l2 = new Label();
				  mv.visitJumpInsn(GOTO, l2);
				  mv.visitLabel(l1);
				  mv.visitInsn(ICONST_0);
				  mv.visitLabel(l2);
			}
			
		}
		
		else if(op.kind==(Kind.NOTEQUAL))
		{
			if(binaryExpression.getE0().getTypeName()==TypeName.INTEGER || binaryExpression.getE0().getTypeName()==TypeName.BOOLEAN)
			{
				Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IF_ICMPEQ, l1);
			mv.visitInsn(Opcodes.ICONST_0);
			label2 = new Label();
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitLabel(label2);
			}
			else{
				Label l1 = new Label();
				mv.visitJumpInsn(Opcodes.IF_ACMPEQ, l1);
				mv.visitInsn(Opcodes.ICONST_1);
				label2 = new Label();
				mv.visitJumpInsn(GOTO, label2);
				mv.visitLabel(l1);
				mv.visitInsn(Opcodes.ICONST_0);
				mv.visitLabel(label2);
			}
		}
		else{
			return null;
		}
		
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		ArrayList<Dec> list1;
		ArrayList<Statement> list2;
		
		Label begin;
		Label end;

		String name;
		String type; 
		
		list1 = block.getDecs();
		list2 = block.getStatements();
		
		for (int i=0;i <list1.size(); i++) {
			list1.get(i).visit(this, slotVariable++);	
		}
		begin = new Label();
		mv.visitLabel(begin);
		
		
		for (int i=0; i<list2.size(); i++) {
			list2.get(i).visit(this, arg);
			if(list2.get(i) instanceof BinaryChain){
				mv.visitInsn(Opcodes.POP);
			}
			
			
		}
		end = new Label();
		mv.visitLabel(end);
		
		for (int i=0;i<list1.size();i++) { 
			Dec listOb=list1.get(i); 
			mv.visitLocalVariable(listOb.getIdent().getText(), listOb.getTypeName().getJVMTypeDesc(), null, begin, end,  listOb.getSlot());
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		
		int val;
		
		if(booleanLitExpression.getValue()==true){
			val=1;
		}
		else{
			val=0;
		}
		
		mv.visitLdcInsn(val);
		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		//assert false : "not yet implemented";
		if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT))
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);
		} 
		else if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH))
		
		{
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);
		}
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		int varSlot;	
		
		varSlot = ((Integer)arg).intValue();
		declaration.setSlot(varSlot);
		
		
		if(declaration.getType().isKind(Kind.KW_IMAGE)||declaration.getType().isKind(KW_FRAME)){
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitVarInsn(Opcodes.ASTORE,varSlot);
		}

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		
		Kind kindFirst=filterOpChain.getFirstToken().kind;
		
		if(kindFirst==Kind.OP_GRAY){
			
			if(((Integer)arg).intValue()!=3){
				mv.visitInsn(Opcodes.ACONST_NULL);
			}
			else{
				mv.visitInsn(Opcodes.DUP);
			}
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);	
		}
		
		if(kindFirst==Kind.OP_BLUR){
			
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		
		if(kindFirst==Kind.OP_CONVOLVE){
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		
		frameOpChain.getArg().visit(this, arg);
		
		Kind kindFirst=frameOpChain.getFirstToken().kind;
		
		
		if(kindFirst==Kind.KW_SHOW){
			
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", "()Lcop5556sp17/PLPRuntimeFrame;",
					false); 
		}
		
		 if(kindFirst==Kind.KW_HIDE){
			
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", "()Lcop5556sp17/PLPRuntimeFrame;",
					false);	
		}
		
		
		
		 if(kindFirst==Kind.KW_XLOC){
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal",PLPRuntimeFrame.getXValDesc ,
					false);	
		}
		 
		 if (frameOpChain.getFirstToken().kind == KW_MOVE) {

             mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,
            		 false);

    }
		
		 if(kindFirst==Kind.KW_YLOC){
			
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,
					false);
		}
		
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		//assert false : "not yet implemented"
		
		int i=((int)arg);
		
		if(i>0)
		
		{
			if(identChain.getTypeName().equals(TypeName.FILE))
			{
			if (identChain.getDec() instanceof ParamDec) 
			{
				mv.visitVarInsn(Opcodes.ALOAD, 0);

				mv.visitFieldInsn(Opcodes.GETFIELD, className, identChain.getFirstToken().getText(),
						identChain.getTypeName().getJVMTypeDesc());
				}

				else{
					
					mv.visitVarInsn(Opcodes.ALOAD, identChain.getDec().getSlot());
				}
			
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write",
					"(Ljava/awt/image/BufferedImage;Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
			
			}
			
		 if (identChain.getTypeName().equals(TypeName.FRAME)) {
				mv.visitVarInsn(Opcodes.ALOAD, identChain.getDec().getSlot());
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame",
						PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(Opcodes.DUP);
				mv.visitVarInsn(Opcodes.ASTORE, identChain.getDec().getSlot());
			}
			
			mv.visitInsn(Opcodes.DUP);
			
			if (identChain.getTypeName().equals(TypeName.IMAGE) || identChain.getTypeName().equals(TypeName.INTEGER)
					|| identChain.getTypeName().equals(TypeName.BOOLEAN)) {
				if (identChain.getDec() instanceof ParamDec) {
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitInsn(Opcodes.SWAP);
					mv.visitFieldInsn(Opcodes.PUTFIELD, className, identChain.getFirstToken().getText(),
							identChain.getTypeName().getJVMTypeDesc());
				} else {
					if (identChain.getTypeName().equals(TypeName.IMAGE)) {
						mv.visitVarInsn(Opcodes.ASTORE, identChain.getDec().getSlot());
					} if (identChain.getTypeName().equals(TypeName.INTEGER)
							|| identChain.getTypeName().equals(TypeName.BOOLEAN)) {
						mv.visitVarInsn(Opcodes.ISTORE, identChain.getDec().getSlot());
					}
				}
			}
			
		}
		
		else
		{
			if (identChain.getDec() instanceof ParamDec) {
				mv.visitVarInsn(Opcodes.ALOAD, 0);

				mv.visitFieldInsn(Opcodes.GETFIELD, className, identChain.getFirstToken().getText(),
						identChain.getTypeName().getJVMTypeDesc());
				if (identChain.getTypeName() == TypeName.URL) {
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL",
							PLPRuntimeImageIO.readFromURLSig, false);
				}
				if (identChain.getTypeName() == TypeName.FILE) {
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile",
							PLPRuntimeImageIO.readFromFileDesc, false);
				}

			}
			
			else {
				if (identChain.getTypeName().equals(TypeName.INTEGER) || identChain.getTypeName().equals(TypeName.BOOLEAN)) {
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
				} else {

					if (identChain.getTypeName() == FRAME) {
						mv.visitVarInsn(Opcodes.ALOAD, identChain.getDec().getSlot());
					}   if (identChain.getTypeName() == TypeName.FILE) {
						mv.visitVarInsn(Opcodes.ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile",
								PLPRuntimeImageIO.readFromFileDesc, false);
					} if (identChain.getTypeName() == TypeName.IMAGE) {
						mv.visitVarInsn(Opcodes.ALOAD, identChain.getDec().getSlot());
					}
					
					if (identChain.getTypeName() == URL) {
						mv.visitVarInsn(Opcodes.ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL",
								PLPRuntimeImageIO.readFromURLSig, false);
					}}}}	

		return null;
	}

	//doubt
	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		String name;
		String type;
		
		TypeName decType;
				
		
		Dec dec;
		dec = identExpression.getDec();
		
		if (identExpression.getDec() instanceof ParamDec) 
		{	
			name = dec.getIdent().getText();
			type = dec.getTypeName().getJVMTypeDesc();
			mv.visitVarInsn(Opcodes.ALOAD, 0);	
			mv.visitFieldInsn(Opcodes.GETFIELD, className, name, type);
		} 
		else 
		{	
			if(dec.getTypeName()==TypeName.INTEGER||dec.getTypeName()==TypeName.BOOLEAN)
			{
				mv.visitVarInsn(Opcodes.ILOAD, dec.getSlot());
			}
			else if(dec.getTypeName()==TypeName.IMAGE||dec.getTypeName()==TypeName.FRAME||dec.getTypeName()==TypeName.URL||dec.getTypeName()==TypeName.FILE)	
				
			{
			mv.visitVarInsn(Opcodes.ALOAD, dec.getSlot());	
			}
			else if(dec.getTypeName()==TypeName.NONE){
				
				return null;
			}
			
			return null;
		}

		
		return null;
	}

	//doubt
	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		
		String name;
		String type;
		
		Dec dec;
		dec = identX.getDec();
		
		if (dec instanceof ParamDec) 
		{	
			name = dec.getIdent().getText();
			type = dec.getTypeName().getJVMTypeDesc();
			mv.visitVarInsn(Opcodes.ALOAD, 0);	
			mv.visitInsn(Opcodes.SWAP);			
			mv.visitFieldInsn(Opcodes.PUTFIELD, className, name, type);
		} 
		else 
		{	
			
			if(dec.getTypeName()==TypeName.INTEGER||dec.getTypeName()==TypeName.BOOLEAN)
			{
				mv.visitVarInsn(Opcodes.ISTORE, dec.getSlot());
			}
			else if(dec.getTypeName()==TypeName.IMAGE)	
				
			{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
						"(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			mv.visitVarInsn(Opcodes.ASTORE, dec.getSlot());	
			}
			else if(dec.getTypeName()==TypeName.NONE){
				
				mv.visitVarInsn(Opcodes.ASTORE, dec.getSlot());	
			}
			
			
			else{
				mv.visitVarInsn(Opcodes.ASTORE, dec.getSlot());	
			}
			
		}

		
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		
		Label after;
		
		ifStatement.getE().visit(this, arg);	
		after = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, after);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(after);	

		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		imageOpChain.getArg().visit(this, arg);
		
	Kind kindImg=	imageOpChain.getFirstToken().kind;
		
		
		if(kindImg==Kind.OP_WIDTH){
			
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
		}
		 if(kindImg==Kind.OP_HEIGHT){
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
		}
		if(kindImg==Kind.KW_SCALE){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		int value;

		value = intLitExpression.value;
		mv.visitLdcInsn(new Integer(value));	

		
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		
		FieldVisitor fv;
		int index = (Integer) arg;
		TypeName decType;
		
		decType = paramDec.getTypeName();
		
		
		String name;
		String type;
		name=paramDec.getIdent().getText();
		type=decType.getJVMTypeDesc();
		fv = cw.visitField(0, paramDec.getIdent().getText(), decType.getJVMTypeDesc(), null, null);
		fv.visitEnd();
		
		
		mv.visitVarInsn(Opcodes.ALOAD, 0); 
		
		if (paramDec.getTypeName() == TypeName.INTEGER) {
			
			mv.visitVarInsn(Opcodes.ALOAD, 1); 
			mv.visitLdcInsn(index); 
			mv.visitInsn(Opcodes.AALOAD);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
		} 
		if (paramDec.getTypeName() == TypeName.FILE) {

			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(Opcodes.ALOAD, 1); 
			mv.visitLdcInsn(index);
			mv.visitInsn(Opcodes.AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
		}
		if (paramDec.getTypeName() == TypeName.BOOLEAN) {
			
			mv.visitVarInsn(Opcodes.ALOAD, 1); 
			mv.visitLdcInsn(index); 
			mv.visitInsn(Opcodes.AALOAD);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z",
					false);
		} 
		if (paramDec.getTypeName() == TypeName.URL) {
			
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitLdcInsn(index);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
					
		} 
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(),paramDec.getTypeName().getJVMTypeDesc());
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		//assert false : "not yet implemented";
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(Opcodes.I2L);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
	//	assert false : "not yet implemented";
	
		List<Expression> expr=tuple.getExprList();
		
		for(int i=0; i<expr.size();i++){
			(expr.get(i)).visit(this, arg);
		}
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		
		
		Label l1;
		Label l2;
		
		l1 = new Label();
		mv.visitJumpInsn(Opcodes.GOTO, l1);
		l2 = new Label();
		mv.visitLabel(l2);
		(whileStatement.getB()).visit(this, arg);
		mv.visitLabel(l1);	
	
		(whileStatement.getE()).visit(this, arg);	
		mv.visitJumpInsn(Opcodes.IFNE, l2);

		return null;
	}

}
