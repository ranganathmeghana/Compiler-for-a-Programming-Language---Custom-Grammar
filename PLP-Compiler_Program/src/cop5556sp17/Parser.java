package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
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
import cop5556sp17.AST.WhileStatement;

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
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
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
		Program program=program();
		matchEOF();
		return program;
	}

	Expression expression() throws SyntaxException {
		//TODO
//	term();
//		if(t.isKind(LT)){
//			
//		}
//		else if(t.isKind(LE)){
//			
//		}
//		else if(t.isKind(GT)){
//			
//		}
//		else if(t.isKind(GE)){
//			
//		}
//		else if(t.isKind(EQUAL)){
//			
//		}
//		else if(t.isKind(NOTEQUAL)){
//			
//		}
//		Kind kind=t.kind;
//		switch (kind) {
//		case LT: {
//			consume();
//		}
//			break;
//		case LE: {
//			consume();
//		}
//			break;
//		case GT:
//		{
//			consume();
//		}
//			break;
//		case GE:
//		 {
//			consume();
//		}
//			break;
//		case EQUAL: {
//			consume();
//		}
//			break;
//			
//		case NOTEQUAL:{
//			consume();
//		}
//		break;
//		default:
//			//you will want to provide a more useful error message
//			throw new SyntaxException("illegal factor");
//		}
//		
//		term();
//		throw new UnimplementedFeatureException();
		Token firstToken=t;
		
		Expression expression0=null;
		Expression expression1=null;
		
		expression0= term();
		while(t.isKind(LT)|t.isKind(Kind.LE)|t.isKind(GT)|t.isKind(GE)|t.isKind(EQUAL)|t.isKind(NOTEQUAL)){
			Token op=t;
			relOp();
			expression1=term();
			expression0=new BinaryExpression(firstToken, expression0, op, expression1);
		}
		
		return expression0;
	}

	Expression term() throws SyntaxException {
		//TODO
		Token firstToken=t;
		Expression expression0;
		Expression expression1;
		expression0=elem();
//		Kind kind = t.kind;
//		switch (kind) {
//		case PLUS: {
//			consume();
//		}
//			break;
//		case MINUS: {
//			consume();
//		}
//			break;
//		case OR: {
//			consume();
//		}
//			break;
//		default:
//		throw new UnimplementedFeatureException();}
//		elem();
		while(t.isKind(PLUS)|t.isKind(Kind.MINUS)|t.isKind(OR)){
			Token op=t;
			weakOp();
			expression1=elem();
			expression0=new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0; 
	}

	Expression elem() throws SyntaxException {
		//TODO
		Token firstToken=t;
		Expression expression0;
		Expression expression1;
		expression0=factor();
		while(t.isKind(TIMES)|t.isKind(DIV)|t.isKind(AND)|t.isKind(Kind.MOD)){
			Token op=t;
			StrongOp();
			expression1=factor();
			expression0=new BinaryExpression(firstToken, expression0, op, expression1);
		}	
		
		return expression0;
//		Kind kind = t.kind;
//		switch (kind) {
//		case TIMES: {
//			consume();
//		}
//			break;
//		case DIV: {
//			consume();
//		}
//			break;
//		case AND: {
//			consume();
//		}
//			break;
//		case MOD: {
//			consume();
//		}
//			break;
//		default:
//		throw new UnimplementedFeatureException();
//		}
//		factor();
	}

	Expression factor() throws SyntaxException {
		Expression expression;
		
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			Token firstToken=t;
			consume();
			expression=new IdentExpression(firstToken);
		}
			break;
		case INT_LIT: {
			Token firstToken=t;
			consume();
			expression=new IntLitExpression(firstToken);
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			Token firstToken=t;
			consume();
			expression=new BooleanLitExpression(firstToken);
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			Token firstToken=t;
			consume();
			expression=new ConstantExpression(firstToken);
		}
			break;
		case LPAREN: {
			consume();
			expression=expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return expression;
	}

	Block block() throws SyntaxException {
		//TODO
		Token firstToken=t;
		
		Dec dec;
		Statement statements;
		
		ArrayList<Dec> decList=new ArrayList<Dec>();
		ArrayList<Statement> statementsList=new ArrayList<Statement>();
		match(LBRACE);
		while(!t.isKind(Kind.RBRACE)){
		//while(t.isKind(KW_INTEGER)|t.isKind(Kind.KW_BOOLEAN)|t.isKind(KW_IMAGE)|t.isKind(Kind.KW_FRAME)|t.isKind(Kind.OP_SLEEP)|t.isKind(Kind.KW_WHILE)|t.isKind(Kind.KW_IF)|t.isKind(Kind.IDENT)){
		if(t.isKind(KW_INTEGER)|t.isKind(Kind.KW_BOOLEAN)|t.isKind(KW_IMAGE)|t.isKind(Kind.KW_FRAME))
			
		{
			dec=dec();
			decList.add(dec);
		}
		else
		{
			statements=statement();
			statementsList.add(statements);
		}
		}
		match(RBRACE);
		return new Block(firstToken, decList, statementsList);
	}

	Program program() throws SyntaxException {
		//TODO
		Token firstToken=t;
		Block b;
		ParamDec paramDec;
		ArrayList<ParamDec> paramList=new ArrayList<ParamDec>();
//		if(t.kind.equals(IDENT)){
//			
//			match(IDENT);
//		}
		match(IDENT);
		if(t.isKind(Kind.LBRACE)){
			b=block();
		}
		else if(t.isKind(Kind.KW_URL)|t.isKind(Kind.KW_FILE)|t.isKind(Kind.KW_INTEGER)|t.isKind(Kind.KW_BOOLEAN))
		{
			paramDec=paramDec();
			paramList.add(paramDec);
			while(t.isKind(Kind.COMMA)){
				consume();
				paramDec=paramDec();
				paramList.add(paramDec);
			}
			b=block();
		} else{
			throw new SyntaxException("illegal program");
		}
		return new Program(firstToken, paramList, b);
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		Token firstToken=t; 
		
		Kind kind = t.kind;
		switch (kind) {
		case KW_URL: {
			consume();
		}
			break;
		case KW_FILE: {
			consume();
		}
			break;
		case KW_INTEGER: {
			consume();
		}
			break;
		case KW_BOOLEAN: {
			consume();
		}
		break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal paramdec");
		}
		
		Token ident=t;
		match(IDENT);
		return new ParamDec(firstToken, ident);
		//throw new UnimplementedFeatureException();
	}

	Dec dec() throws SyntaxException {
		//TODO
		Token firstToken=t;
		
		Kind kind = t.kind;
		switch (kind) {
		case KW_INTEGER: {
			consume();
		}
			break;
		case KW_BOOLEAN: {
			consume();
		}
			break;
		case KW_IMAGE: {
			consume();
		}
			break;
		case KW_FRAME: {
			consume();
		}
		break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal dec");
		}
		Token ident=t;
		match(IDENT);
		return new Dec(firstToken, ident);
	//	throw new UnimplementedFeatureException();
	}

	Statement statement() throws SyntaxException {
		//TODO
		Statement statement;
		Token firstToken=t;
		
		if(t.isKind(Kind.OP_SLEEP)){
			consume();
			Expression e = expression();
			match(Kind.SEMI);
			statement = new SleepStatement(firstToken, e);                                        
		}
		else if(t.isKind(Kind.KW_WHILE)){
			statement=whileStatement();
		}
		else if(t.isKind(Kind.KW_IF)){
			statement=ifStatement();
		}
		else if((t.isKind(Kind.IDENT) && (scanner.peek()).isKind(Kind.ASSIGN))){
			
				statement=assign();
				match(SEMI);
			
		}
		else if(t.isKind(Kind.IDENT)|t.isKind(Kind.OP_BLUR)|t.isKind(Kind.KW_SHOW)|t.isKind(Kind.OP_WIDTH))
		{
			statement=chain();
			match(SEMI);
		}
		else{
			throw new SyntaxException("illegal statement");
		}
		
		return statement;
		
		}

	BinaryChain chain() throws SyntaxException {
		//TODO
		BinaryChain binaryChain;
		Token firstToken=t;
		Chain chain1;
		chain1=chainElem();
		Token arrow=t;
		arrowOp();
		ChainElem chain2;
		chain2=chainElem();
		
		binaryChain=new BinaryChain(firstToken, chain1, arrow, chain2);
		
		while(t.isKind(ARROW)||t.isKind(BARARROW)){
			Token arrowOp2=t; 
			consume();
			chain2=chainElem();
			binaryChain=new BinaryChain(firstToken, binaryChain, arrowOp2, chain2);
		}
		
		return binaryChain;
		//throw new UnimplementedFeatureException();
	}

	ChainElem chainElem() throws SyntaxException {
		//TODO
		
		Token firstToken;
		Tuple tuple;
		ChainElem chainElem;
		
		if(t.isKind(IDENT)){
			chainElem = new IdentChain(t);
			match(IDENT);
		}
		else if(t.isKind(Kind.OP_BLUR)|t.isKind(Kind.OP_GRAY)|t.isKind(Kind.OP_CONVOLVE))
		{
			firstToken=t;
			filterOp();
			tuple=arg();
			chainElem = new FilterOpChain(firstToken, tuple);
		}
		else if(t.isKind(Kind.KW_SHOW)|t.isKind(Kind.KW_HIDE)|t.isKind(Kind.KW_MOVE)|t.isKind(Kind.KW_XLOC)|t.isKind(Kind.KW_YLOC))
		{
			firstToken=t;
			frameOp();
			tuple=arg();
			chainElem = new FrameOpChain(firstToken, tuple);
		}
		else if(t.isKind(Kind.OP_WIDTH)|t.isKind(Kind.OP_HEIGHT)|t.isKind(Kind.KW_SCALE))
		{
			firstToken=t;
			imageOp();
			tuple=arg();
			chainElem=new ImageOpChain(firstToken, tuple);
		}
		else{throw new SyntaxException("illegal chainElem");}
		return chainElem;
	}

	Tuple arg() throws SyntaxException {
		//TODO
		Token firstToken=t;
		List<Expression> list=new ArrayList<Expression>();
		Expression expression;
		Kind kind = t.kind;
		switch (kind) {
		case LPAREN: {
			consume();
			expression=expression();
			list.add(expression);
			while(t.isKind(COMMA)){
				consume();
				expression=expression();
				list.add(expression);			}
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			//throw new SyntaxException("illegal arg");
		}
		return new Tuple(firstToken, list);
		//throw new UnimplementedFeatureException();
	}
	
	WhileStatement whileStatement() throws SyntaxException{
	
		Token firstToken=t;
		
		match(KW_WHILE);
		match(LPAREN);
		
		Expression e=expression();
		match(RPAREN);
		Block b= block();
		
		return new WhileStatement(firstToken, e, b);
	}
	
	IfStatement ifStatement() throws SyntaxException{
		
	Token firstToken=t;
		
		match(KW_IF);
		match(Kind.LPAREN);
		Expression e=expression();
		match(Kind.RPAREN);
		Block b=block();
		
		return new IfStatement(firstToken, e, b);
	}
	
	AssignmentStatement assign() throws SyntaxException{
		
		Token firstToken=t;
		
		match(Kind.IDENT);
		//Token secondToken=t;
		IdentLValue var=new IdentLValue(firstToken);
		match(Kind.ASSIGN);
		Expression e=expression();
		return new AssignmentStatement(firstToken, var, e);
		
	}
	
	void arrowOp() throws SyntaxException{
		
		switch (t.kind) {
		case ARROW:{
			consume();
		}
		break;
		
		case BARARROW:{
			consume();
		}
		break;

		default:
			throw new SyntaxException("illegal arrowOp");
		}
	}
	
	void filterOp() throws SyntaxException{
		
		switch(t.kind){
		case OP_BLUR:
		{
			consume();
		}break;
		case OP_GRAY:{
			consume();
		}break;
		case OP_CONVOLVE:{
			consume();
		}break;
		default: throw new SyntaxException("illegal filterOp");
		}
		
	}

	void frameOp() throws SyntaxException{
		
		switch(t.kind){
		case KW_SHOW:
		{
			consume();
		}break;
		case KW_HIDE:{
			consume();
		}break;
		case KW_MOVE:{
			consume();
		}break;
		case KW_XLOC:{
			consume();
		}break;
		case KW_YLOC:{
			consume();
		}break;
		default: throw new SyntaxException("illegal frameOp");
		}	
	}
	
	void imageOp() throws SyntaxException{
		
		Token firstToken=t;
		
		switch(t.kind){
		case OP_WIDTH:{
			consume();
		}break;
		case OP_HEIGHT:{
			consume();
		}break;
		case KW_SCALE:{
			consume();
		}break;
		default :  throw new SyntaxException("illegal imageOp");
		
		}
	}
	
	void relOp() throws SyntaxException{
		Kind kind=t.kind;
		switch (kind) {
		case LT: {
			consume();
		}
			break;
		case LE: {
			consume();
		}
			break;
		case GT:
		{
			consume();
		}
			break;
		case GE:
		 {
			consume();
		}
			break;
		case EQUAL: {
			consume();
		}
			break;
			
		case NOTEQUAL:{
			consume();
		}
		break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal relOp");
		}
	}
	
	void weakOp() throws SyntaxException{
		
		Kind kind = t.kind;
		switch (kind) {
		case PLUS: {
			consume();
		}
			break;
		case MINUS: {
			consume();
		}
			break;
		case OR: {
			consume();
		}
			break;
		default:
			throw new SyntaxException("illegal weakOp");}
		
	}

void StrongOp() throws SyntaxException{
		
		Kind kind = t.kind;
		switch (kind) {
		case TIMES: {
			consume();
		}
			break;
		case DIV: {
			consume();
		}
			break;
		case AND: {
			consume();
		}
			break;
		case MOD: {
			consume();
		}
			break;
			
		default:
			throw new SyntaxException("illegal strongOp");}
		
	}
	
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is co
	 * nsumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
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
