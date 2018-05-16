package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
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
import java.util.List;

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

import cop5556sp17.AST.Type;
import cop5556sp17.AST.ASTNode;

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
		// TODO Auto-generated method stub
		
		binaryChain.getE0().visit(this,null);
		binaryChain.getE1().visit(this,null);

		TypeName op1 = binaryChain.getE0().getTypeName();
		TypeName op2 = binaryChain.getE1().getTypeName();
	
		
		if(binaryChain.getArrow().isKind(Kind.BARARROW) && 
				(binaryChain.getE0().getTypeName().isType(TypeName.IMAGE) && 
						binaryChain.getE1() instanceof FilterOpChain && 
						(binaryChain.getE1().getFirstToken().isKind(Kind.OP_GRAY)|| 
								binaryChain.getE1().getFirstToken().isKind(Kind.OP_BLUR)|| 
								binaryChain.getE1().getFirstToken().isKind(Kind.OP_CONVOLVE))))
		{
				binaryChain.setTypeName(TypeName.IMAGE);
				return binaryChain.getTypeName();
			
		}
		
		else if(binaryChain.getArrow().isKind(Kind.ARROW))
		{
			
			if(binaryChain.getE0().getTypeName().isType(IMAGE) && binaryChain.getE1() instanceof FilterOpChain && (binaryChain.getE1().getFirstToken().isKind(Kind.OP_GRAY)|| binaryChain.getE1().getFirstToken().isKind(Kind.OP_BLUR)|| binaryChain.getE1().getFirstToken().isKind(OP_CONVOLVE))){
				binaryChain.setTypeName(TypeName.IMAGE);
				return binaryChain.getTypeName();}
			
			else if(binaryChain.getE0().getTypeName().isType(FILE,URL)){
				
				if(binaryChain.getE1().getTypeName().isType(IMAGE)){
					binaryChain.setTypeName(TypeName.IMAGE);
					return binaryChain.getTypeName();
				
			}else{
				throw new TypeCheckException("invalid chain type");
			}
			
		}
			else if(binaryChain.getE0().getTypeName().equals(TypeName.INTEGER) && binaryChain.getE1() instanceof IdentChain && binaryChain.getE1().getTypeName().equals(TypeName.INTEGER)){
				binaryChain.setTypeName(TypeName.INTEGER);
			}	
			
			else if(binaryChain.getE0().getTypeName().isType(TypeName.FRAME)) {
				if (binaryChain.getE1() instanceof FrameOpChain
						&& (binaryChain.getE1().getFirstToken().isKind(Kind.KW_XLOC) || binaryChain.getE1().getFirstToken().isKind(Kind.KW_YLOC))) {
					
					binaryChain.setTypeName(TypeName.INTEGER);
					return binaryChain.getTypeName();
				}
				
				else if(binaryChain.getE1() instanceof FrameOpChain && (binaryChain.getE1().getFirstToken().isKind(Kind.KW_SHOW)
						|| binaryChain.getE1().getFirstToken().isKind(Kind.KW_HIDE) || binaryChain.getE1().getFirstToken().isKind(Kind.KW_MOVE))) {
					binaryChain.setTypeName(FRAME);
			}else{
				throw new TypeCheckException("invalid chain type");
			}
					
				}
			else if(binaryChain.getE0().getTypeName().isType(IMAGE)){
				if(binaryChain.getE1() instanceof ImageOpChain && (binaryChain.getE1().getFirstToken().isKind(Kind.OP_WIDTH) || binaryChain.getE1().getFirstToken().isKind(Kind.OP_HEIGHT))){
					binaryChain.setTypeName(INTEGER);
					return binaryChain.getTypeName();
				}
				
				else if(binaryChain.getE1().getTypeName().isType(TypeName.FRAME)){
					binaryChain.setTypeName(FRAME);
				}
				
				else if(binaryChain.getE1().getTypeName().isType(TypeName.FILE)) {
					binaryChain.setTypeName(TypeName.NONE);
				}
				else if(binaryChain.getE1() instanceof ImageOpChain && binaryChain.getE1().getFirstToken().isKind(Kind.KW_SCALE)){
					binaryChain.setTypeName(TypeName.IMAGE);
					return binaryChain.getTypeName();
				}
				else if (binaryChain.getE1() instanceof IdentChain && binaryChain.getE1().getTypeName().equals(TypeName.IMAGE)) {
					binaryChain.setTypeName(TypeName.IMAGE);
					return binaryChain.getTypeName();
				}
				else{
					throw new TypeCheckException("invalid chain type");
				}
			} 
			else {
				throw new TypeCheckException("Invalid Chain Type");
			}
			}
		
		else{
			throw new TypeCheckException("invalid chain type");
		}
		return null;		
	}

	
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Expression expr1 = binaryExpression.getE0();
		Expression expr2 = binaryExpression.getE1();
		
		expr1.visit(this, null);
		expr2.visit(this, null);

		if (expr1.getTypeName().equals(INTEGER) && expr2.getTypeName().equals(INTEGER)
				&& (binaryExpression.getOp().isKind(PLUS)||binaryExpression.getOp().isKind(MINUS))) {
			binaryExpression.setTypeName(INTEGER);
			return binaryExpression.getTypeName();
		}

		if (expr1.getTypeName().equals(INTEGER) && expr2.getTypeName().equals(IMAGE) && (binaryExpression.getOp().isKind(TIMES))) {
			binaryExpression.setTypeName(IMAGE);
			return binaryExpression.getTypeName();
		} 
		
		if (expr1.getTypeName().equals(INTEGER) && expr2.getTypeName().equals(INTEGER) && (binaryExpression.getOp().isKind(LT)
				|| binaryExpression.getOp().isKind(GT)||binaryExpression.getOp().isKind(GE)||binaryExpression.getOp().isKind(LE))) {
			binaryExpression.setTypeName(BOOLEAN);
			return binaryExpression.getTypeName();
		}
		
		if (expr1.getTypeName().equals(IMAGE) && expr2.getTypeName().equals(IMAGE)
				&& (binaryExpression.getOp().isKind(PLUS)||binaryExpression.getOp().isKind(MINUS))) {
			binaryExpression.setTypeName(IMAGE);
			return binaryExpression.getTypeName();
		}

	
		if (expr1.getTypeName().equals(INTEGER) && expr2.getTypeName().equals(INTEGER)
				&& (binaryExpression.getOp().isKind(TIMES)||binaryExpression.getOp().isKind(DIV)||binaryExpression.getOp().isKind(MOD))) {
			binaryExpression.setTypeName(INTEGER);
			return binaryExpression.getTypeName();
		}

		 if (expr1.getTypeName().equals(TypeName.IMAGE) && expr2.getTypeName().equals(TypeName.INTEGER) && (binaryExpression.getOp().isKind(Kind.TIMES) || binaryExpression.getOp().isKind(Kind.DIV) || binaryExpression.getOp().isKind(Kind.MOD))) 
		 {
			binaryExpression.setTypeName(TypeName.IMAGE);
			return binaryExpression.getTypeName();
		}
		 
		 if (expr1.getTypeName().equals(BOOLEAN) && expr2.getTypeName().equals(BOOLEAN) && (binaryExpression.getOp().isKind(LT)
					|| binaryExpression.getOp().isKind(GT)||binaryExpression.getOp().isKind(GE)||binaryExpression.getOp().isKind(LE)||binaryExpression.getOp().isKind(Kind.AND)||binaryExpression.getOp().isKind(OR))) {
				binaryExpression.setTypeName(BOOLEAN);
				return binaryExpression.getTypeName();
		}

		 if (binaryExpression.getOp().isKind(EQUAL)||binaryExpression.getOp().isKind(NOTEQUAL)) {

			if (!expr1.getTypeName().isType(expr2.getTypeName())) {
					throw new TypeCheckException("Type mismatch");
				}
				binaryExpression.setTypeName(BOOLEAN);
				return binaryExpression.getTypeName();
		}
			

		if (expr1.getTypeName().equals(TypeName.INTEGER) && expr2.getTypeName().equals(TypeName.INTEGER)
				&& (binaryExpression.getOp().isKind(Kind.TIMES) || binaryExpression.getOp().isKind(Kind.DIV)|| binaryExpression.getOp().isKind(Kind.MOD))) {
			binaryExpression.setTypeName(TypeName.INTEGER);
			return binaryExpression.getTypeName();
		}
		throw new TypeCheckException("invalid Binary Expression Type");

	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		
		List<Dec> listDec=block.getDecs();
		List<Statement> listStatement=block.getStatements();
		
		for(int i=0; i<listDec.size();i++){
			listDec.get(i).visit(this,null);
		}
		
		for(int i=0; i<listStatement.size();i++){
			listStatement.get(i).visit(this,null);
		}
		
		symtab.leaveScope();
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypeName(BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tuple=filterOpChain.getArg();
		if(tuple.getExprList().size()!=0){
			throw new TypeCheckException("Tuple!=0");
		}
		filterOpChain.setTypeName(IMAGE);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token frameOp=frameOpChain.getFirstToken();
		Tuple tuple=frameOpChain.getArg();
		
		
		if (frameOp.isKind(KW_XLOC)||frameOp.isKind(KW_YLOC)) {
			if (tuple.getExprList().size()!=0) {throw new TypeCheckException("Tuple!=0");}
			frameOpChain.setTypeName(INTEGER);
		}
		
		if (frameOp.isKind(KW_SHOW)||frameOp.isKind(KW_HIDE)) {
			if (tuple.getExprList().size()!=0) {
				throw new TypeCheckException("Tuple!=0");
			}
			frameOpChain.setTypeName(NONE);
		} 
		
		if (frameOp.isKind(KW_MOVE)) {
			if (tuple.getExprList().size()!=2) {
				throw new TypeCheckException("Tuple!=2");
			}
			frameOpChain.setTypeName(NONE);
			tuple.visit(this, null);
		}
		
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
	
		if (symtab.lookup(identChain.getFirstToken().getText()) == null) 
		{
		throw new TypeCheckException("Ident:" + identChain.getFirstToken().getText() + "' not declared or not visible");
		}
		identChain.setTypeName(Type.getTypeName(symtab.lookup(identChain.getFirstToken().getText()).getType()));
		identChain.setDec(symtab.lookup(identChain.getFirstToken().getText()));
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
	
		if (symtab.lookup(identExpression.getFirstToken().getText()) == null) {
			throw new TypeCheckException("Ident:" + identExpression.getFirstToken().getText() + "' not declared or not visible");}
		identExpression.setTypeName(Type.getTypeName(symtab.lookup(identExpression.getFirstToken().getText()).getType()));
		identExpression.setDec(symtab.lookup(identExpression.getFirstToken().getText()));
		return null;
		
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
	
		ifStatement.getE().visit(this, null);
		if(!TypeName.BOOLEAN.equals(ifStatement.getE().getTypeName())){
			throw new TypeCheckException("not a boolean");
		}
		ifStatement.getB().visit(this, null);
	return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		sleepStatement.getE().visit(this, null);
		if(!TypeName.INTEGER.equals(sleepStatement.getE().getTypeName())){
			throw new TypeCheckException("not an integer");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		whileStatement.getE().visit(this, null);
		if(!TypeName.BOOLEAN.equals(whileStatement.getE().getTypeName())){
			throw new TypeCheckException("not a boolean");
		}
		whileStatement.getB().visit(this, null);
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		boolean bool=symtab.insert(declaration.getIdent().getText(), declaration);
		if(!bool){
			throw new TypeCheckException("already in this scope");
		}
		declaration.setTypeName(Type.getTypeName(declaration.getType()));
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		List<ParamDec> listParam=program.getParams();
		
		for(int i=0; i<listParam.size();i++){
			listParam.get(i).visit(this, null);
		}
		
		program.getB().visit(this, null);
		
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
	
		
		assignStatement.getVar().visit(this, null);
		assignStatement.getE().visit(this, null);
		if (!assignStatement.getVar().getDec().getTypeName().equals(assignStatement.getE().getTypeName())) {
			throw new TypeCheckException("Types dont match");}
		return null;
	}
	
	

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
	
		if (symtab.lookup(identX.getFirstToken().getText()) == null) {
			throw new TypeCheckException("Ident:" + identX.getFirstToken().getText() + "' not declared or not visible");}
		symtab.lookup(identX.getFirstToken().getText()).setTypeName(Type.getTypeName(symtab.lookup(identX.getFirstToken().getText()).getType()));
		identX.setDec(symtab.lookup(identX.getFirstToken().getText()));
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		
		if(!symtab.insert(paramDec.getIdent().getText(), paramDec)){
			throw new TypeCheckException("already in this scope");
		}
		
		paramDec.setTypeName(Type.getTypeName(paramDec.getType()));
		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
	
		if(imageOpChain.getFirstToken().isKind(OP_WIDTH)||imageOpChain.getFirstToken().isKind(OP_HEIGHT)){
			if(imageOpChain.getArg().getExprList().size()!=0){throw new TypeCheckException("Tuple size!=0");}
			imageOpChain.setTypeName(INTEGER);}
		else if(imageOpChain.getFirstToken().isKind(KW_SCALE)){
			if(imageOpChain.getArg().getExprList().size()!=1){
				throw new TypeCheckException("Tuple!=1");}
			imageOpChain.setTypeName(IMAGE);
			imageOpChain.getArg().visit(this, null);
		}	return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		List<Expression> listExpr= tuple.getExprList();
		for (int i=0;i<listExpr.size();i++) {
			listExpr.get(i).visit(this, null);
			if (!TypeName.INTEGER.equals(listExpr.get(i).getTypeName())) {
				throw new TypeCheckException("not an integer");}}
		return null;}
}
