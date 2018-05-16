package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	HashMap <Integer, Object []> h;
	
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
	
	public static enum State{
		
		START,AFTER_OR, BARARROW,AFTER_EQ,AFTER_NOT_EQ,AFTER_GREATER_THAN,AFTER_LESSER_THAN,AFTER_MINUS,IN_DIGIT,IN_IDENT,COMMENT,COMMENT_NEXT,AFTER_STAR;
		
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
		public String token;
		public LinePos linePosition;
		

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
		
		
		
		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			return token;
			//return null;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS
			return linePosition;
		}
		
		public boolean isKind(Kind kind){
			
			return this.kind.equals(kind);
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			
			Object []info = h.get(pos);
			
			if (info==null)
			{
				//System.out.println("got null object array when "+pos+" used as key");
			}
			token=(String)info[0];
			linePosition=(LinePos)info[1];
			
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			int k=Integer.parseInt(token);
			
			return k;
		}
		
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		h=new HashMap<>();
		
		//chars=chars+"    ";
		//System.out.println("string len "+chars.length());

	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		int startingPos = 0;
		State state=State.START;
		int line=0;
		int columnNumber=0;
		String token=null;
		int lenght=chars.length();
		//chars=chars+"   ";
		
		String input=chars+"  ";
		
		Map<String,Kind> keywordMap=new HashMap<String,Kind>();
		keywordMap.put("integer", Kind.KW_INTEGER);
		keywordMap.put("boolean", Kind.KW_BOOLEAN);
		keywordMap.put("image", Kind.KW_IMAGE);
		keywordMap.put("url", Kind.KW_URL);
		keywordMap.put("file", Kind.KW_FILE);
		keywordMap.put("frame", Kind.KW_FRAME);
		keywordMap.put("while", Kind.KW_WHILE);
		keywordMap.put("if", Kind.KW_IF);
		keywordMap.put("true", Kind.KW_TRUE);
		keywordMap.put("false", Kind.KW_FALSE);
		keywordMap.put("blur", Kind.OP_BLUR);
		keywordMap.put("gray", Kind.OP_GRAY);
		keywordMap.put("convolve", Kind.OP_CONVOLVE);
		keywordMap.put("screenheight", Kind.KW_SCREENHEIGHT);
		keywordMap.put("screenwidth", Kind.KW_SCREENWIDTH);
		keywordMap.put("width", Kind.OP_WIDTH);
		keywordMap.put("height", Kind.OP_HEIGHT);
		keywordMap.put("xloc", Kind.KW_XLOC);
		keywordMap.put("yloc", Kind.KW_YLOC);
		keywordMap.put("hide", Kind.KW_HIDE);
		keywordMap.put("show", Kind.KW_SHOW);
		keywordMap.put("move", Kind.KW_MOVE);
		keywordMap.put("sleep", Kind.OP_SLEEP);
		keywordMap.put("scale", Kind.KW_SCALE);
		keywordMap.put("eof", Kind.EOF);
		
		char temp='a';
		
		while(pos<=lenght)
		{
			//System.out.println("pos "+pos+" line "+line+"col "+columnNumber);
			
			
		switch(state)
		{
		
		case START:{
			
		char ch=input.charAt(pos);
		temp=ch;
		startingPos=pos;
		
		if(Character.isWhitespace(ch)){
		if(ch=='\n'){
			line++;
			columnNumber=0;
			pos++;
		}
		else
		{
			pos++;
			columnNumber++;
		}
		break;
		}
		
		switch(ch){
		
		case ';': {
		token=";";
		LinePos linePos=new LinePos(line, columnNumber);
		
		Object info[]=new Object[2];
		info[0]=token;
		info[1]=linePos;
		
		h.put(pos, info);
		
		tokens.add(new Token(Kind.SEMI,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case '/': {
			pos++;
			columnNumber++;
			state=State.COMMENT;
			
		}break;
		
		case '+': {
		token="+";	
		LinePos linePos=new LinePos(line, columnNumber);
		Object info[]=new Object[2];
		info[0]=token;
		info[1]=linePos;
		
		h.put(pos, info);
		tokens.add(new Token(Kind.PLUS,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case '%': {
			token="%";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.MOD,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case '&': {
			token="&";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.AND,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		
		case '*': {
			token="*";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.TIMES,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case '}': {
			token="}";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.RBRACE,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case '{': {
			token="{";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.LBRACE,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case '(': {
			token="(";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.LPAREN,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case ')': {
			token=")";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.RPAREN,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case ',': {
			token=",";	
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
		tokens.add(new Token(Kind.COMMA,pos,1));
		pos++;
		columnNumber++;
		state=State.START;
		}break;
		
		case '|': {
			
			pos++;
			columnNumber++;
			state=State.AFTER_OR;
			
			
		}break;
		
		case '=':
		{
			state=State.AFTER_EQ;
			pos++;
			columnNumber++;
		}break;
		
		case '!':{
			state=State.AFTER_NOT_EQ;
			pos++;
			columnNumber++;
		}break;
		
		case '>' :{
			
			state=State.AFTER_GREATER_THAN;
			pos++;
			columnNumber++;
		}break;
		
		case '<': {
			
			state=State.AFTER_LESSER_THAN;
			pos++;
			columnNumber++;
		}break;
		
		case '-': {
			state=State.AFTER_MINUS;
			pos++;
			columnNumber++;
		}break;
		
		case '0':
		{
			token="0";
			LinePos linePos=new LinePos(line, columnNumber);
			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(pos, info);
			tokens.add(new Token(Kind.INT_LIT,pos,1));
			pos++;
			columnNumber++;
			state=State.START;
		}break;
		
		default:{
		
			
			if(Character.isDigit(input.charAt(pos))){
				pos++;
				//columnNumber++;
				state=State.IN_DIGIT;
			}
			
			else if(Character.isJavaIdentifierStart(input.charAt(pos))){
				pos++;
				//columnNumber++;
				state=State.IN_IDENT;
			}
			else{
				throw new IllegalCharException(""+ch);
			}
			
		}break;
		
		}
		
		break;
		}
		
		case AFTER_OR:
		{
			
			char ch=input.charAt(pos);
			if(ch=='-'){
				pos++;
				columnNumber++;
				state=State.BARARROW;
				
			}
			
			else{
				LinePos linePos=new LinePos(line, columnNumber-1);
			token="|";

			Object info[]=new Object[2];
			info[0]=token;
			info[1]=linePos;
			
			h.put(startingPos, info); //changed from pos
			tokens.add(new Token(Kind.OR,startingPos,1));
			state=State.START;
			}
		}break;
		
		case BARARROW:{
			char ch=input.charAt(pos);
			if(ch=='>'){
				
				pos++;
				token=input.substring(startingPos,pos);
				LinePos linePos=new LinePos(line, columnNumber);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here from pos-2
				tokens.add(new Token(Kind.BARARROW,startingPos,pos-startingPos));
				columnNumber++;
				
				state=State.START;
			}
			
			else{
				LinePos linePos1=new LinePos(line, columnNumber-2);
				token="|";
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos1;
				
				h.put(pos-2, info);
				tokens.add(new Token(Kind.OR,pos-2,1));
				
				LinePos linePos2=new LinePos(line, columnNumber-1);
				token="-";
				Object info1[]=new Object[2];
				info1[0]=token;
				info1[1]=linePos2;
				
				h.put(pos-1, info1);
			    tokens.add(new Token(Kind.MINUS,pos-1,1));
				
				state=State.START;
			}
		}break;
		
		case AFTER_EQ:{
			char ch=input.charAt(pos);
			if(ch=='='){
				pos++;
				token=input.substring(startingPos,pos);
				LinePos linePos=new LinePos(line, columnNumber-1);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here
				tokens.add(new Token(Kind.EQUAL,startingPos,pos-startingPos));
				columnNumber++;
				state=State.START;
			}
			
			else{
				throw new IllegalCharException("=");
			}
		}break;
		
		case AFTER_NOT_EQ:{
			char ch=input.charAt(pos);
			if(ch=='='){
				pos++;
				columnNumber++;
				token=input.substring(startingPos,pos);
				LinePos linePos=new LinePos(line, columnNumber-1);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here
				tokens.add(new Token(Kind.NOTEQUAL,startingPos,pos-startingPos));
				
				state=State.START;
			}
			
			else{
				token="!";
				LinePos linePos=new LinePos(line, columnNumber-1);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(pos-1, info);
				tokens.add(new Token(Kind.NOT,startingPos,pos-startingPos));
				state=State.START;
				
			}
		}break;
		
		case AFTER_GREATER_THAN:{
			if(input.charAt(pos)=='='){
				pos++;
				token=input.substring(startingPos,pos);
				LinePos linePos=new LinePos(line, columnNumber-1);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here
				
				tokens.add(new Token(Kind.GE,startingPos,pos-startingPos));
				columnNumber++;
				
				state=State.START;
			}
			else{
				LinePos linePos=new LinePos(line, columnNumber-1);
				token=">";
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(pos-1, info);
				
				
				tokens.add(new Token(Kind.GT,startingPos,pos-startingPos));
				state=State.START;
			}
		}break;
		
		case AFTER_LESSER_THAN:{
			if(input.charAt(pos)=='='){
				pos++;
				LinePos linePos=new LinePos(line, columnNumber-1);
				token=input.substring(startingPos,pos);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here
				tokens.add(new Token(Kind.LE,startingPos,pos-startingPos));
				columnNumber++;
				
				state=State.START;
			}
			
			else if(input.charAt(pos)=='-'){
				pos++;
				LinePos linePos=new LinePos(line, columnNumber-1);
				Object info[]=new Object[2];
				info[0]="<-";
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here
				tokens.add(new Token(Kind.ASSIGN,startingPos,pos-startingPos));
				columnNumber++;
				
				state=State.START;
			}
			
			else{
				LinePos linePos=new LinePos(line, columnNumber-1);
				token="<";
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(pos-1, info);
				tokens.add(new Token(Kind.LT,startingPos,pos-startingPos));
				state=State.START;
			}
		}break;
		
		case AFTER_MINUS:
		{
			if(input.charAt(pos)=='>'){
				pos++;
				LinePos linePos=new LinePos(line, columnNumber-1);
				token="->";
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here
				tokens.add(new Token(Kind.ARROW,startingPos,pos-startingPos));
				columnNumber++;
				
				state=State.START;
			}
			
			else{
				LinePos linePos=new LinePos(line, columnNumber-1);
				token="-";
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info); //changed here
				
				tokens.add(new Token(Kind.MINUS,startingPos,pos-startingPos));
				state=State.START;
			}
		    
		}break;
		
		case COMMENT:{
			if(input.charAt(pos)=='*'){
				pos++;
				columnNumber++;
				state=State.COMMENT_NEXT;
				
			}
			else{
				token="/";
				LinePos linePos=new LinePos(line, columnNumber-1);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(pos-1, info);
				tokens.add(new Token(Kind.DIV,startingPos,1));
				state=State.START;
			}
		}break;
		
		case COMMENT_NEXT:{
			if(input.charAt(pos)=='*'){
				pos++;
				columnNumber++;
				state=State.AFTER_STAR;
			}
			else{
				
				if (input.charAt(pos)=='\n')
				{
					line++;
					columnNumber=0;
					pos++;
					state=State.COMMENT_NEXT;
				}
				else
				{
					pos++;
					columnNumber++;
					state=State.COMMENT_NEXT;
				}
			}
		}
		
		case AFTER_STAR:{
			if(input.charAt(pos)=='/'){
				pos++;
				columnNumber++;
				state=State.START;
		}
		else{
			
			if (input.charAt(pos)=='*')
			{
				columnNumber++;
				pos++;
				state=State.AFTER_STAR;
			}
			
			else if (input.charAt(pos)=='\n')
			{
				line++;
				columnNumber=0;
				pos++;
				state=State.COMMENT_NEXT;
			}
			else
			{
				pos++;
				columnNumber++;
				state=State.COMMENT_NEXT;
			}
			
			}
		}break;
		
		
		
		case IN_DIGIT:{
			if(Character.isDigit(input.charAt(pos))){
				pos++;
				//System.out.println("string len "+input.length()+" pos="+pos);
				//columnNumber++;
				state=State.IN_DIGIT;
			}
			
			else{
				token=input.substring(startingPos,pos);
				LinePos linePos=new LinePos(line, columnNumber);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info);
				
				try
				{
					Integer.parseInt(token);
				}
				catch(Exception e)
				{
					throw new IllegalNumberException("too big number");
				}
				
				tokens.add(new Token(Kind.INT_LIT,startingPos,pos-startingPos));
				columnNumber=columnNumber+(pos-startingPos);
				state=State.START;
			}
		}break;
		
		case IN_IDENT:
		{
		if(Character.isJavaIdentifierPart(input.charAt(pos))){
			pos++;
			//columnNumber++;
			state=State.IN_IDENT;
		}
		
		else{
			if(keywordMap.containsKey(input.substring(startingPos,pos))){
				Kind kind=keywordMap.get(input.substring(startingPos,pos));
				token=input.substring(startingPos, pos);
				LinePos linePos=new LinePos(line, columnNumber);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info);
				tokens.add(new Token(kind,startingPos,pos-startingPos));
				}
			else{
				LinePos linePos=new LinePos(line, columnNumber);
				token=input.substring(startingPos, pos);
				Object info[]=new Object[2];
				info[0]=token;
				info[1]=linePos;
				
				h.put(startingPos, info);
			tokens.add(new Token(Kind.IDENT,startingPos,pos-startingPos));
			}
			
			state=State.START;
			columnNumber=columnNumber+(pos-startingPos);
		}
		
		}
		break;
		
		/*default:{
			System.out.println("yesssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
			throw new IllegalCharException(temp+"");
		}*/
		
		}
		
		
		
		}
		
		LinePos linePos=new LinePos(line, columnNumber);
		token="EOF";
		Object info[]=new Object[2];
		info[0]=token;
		info[1]=linePos;
		
		//System.out.println("starttingpos for eof "+startingPos);
		h.put(startingPos, info);
		
		tokens.add(new Token(Kind.EOF,startingPos,3));
		return this;  
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
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		
		return t.linePosition;
	}


}
