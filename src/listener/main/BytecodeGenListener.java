package listener.main;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.SymbolTable.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.util.Hashtable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.ProgramContext;
import generated.MiniCParser.StmtContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	SymbolTable symbolTable = new SymbolTable();

	int tab = 0;
	int label = 0;

	// program : decl+

	@Override
	public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
		symbolTable.initFunDecl();
		
		String fname = getFunName(ctx);
		ParamsContext params;
		reset_stack_size();
		
		if (fname.equals("main")) {
			symbolTable.putLocalVar("args", Type.INTARRAY);
		} else {
			symbolTable.putFunSpecStr(ctx);
			params = (MiniCParser.ParamsContext) ctx.getChild(3);
			symbolTable.putParams(params);
		}
	}

	// var_decl : type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec
	// IDENT '[' LITERAL ']' ';'
	@Override
	public void enterVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();

		if (isArrayDecl(ctx)) { // type_spec IDENT '[' LITERAL ']' ';'
			symbolTable.putGlobalVar(varName, Type.INTARRAY);
		} else if (isDeclWithInit(ctx)) { // type_spec IDENT '=' LITERAL ';'
			symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));
		} else { // simple decl
			symbolTable.putGlobalVar(varName, Type.INT);
		}
	}

	@Override
	public void enterLocal_decl(MiniCParser.Local_declContext ctx) {
		if (isArrayDecl(ctx)) {
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
		} else if (isDeclWithInit(ctx)) {
			symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
		} else { // simple decl
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
		}
	}

	@Override
	public void exitProgram(MiniCParser.ProgramContext ctx) {
		String classProlog = getFunProlog();

		String fun_decl = "", var_decl = "";

		for (int i = 0; i < ctx.getChildCount(); i++) { // prgram : decl+ (decl있는 만큼 반복)
			if (isFunDecl(ctx, i)) // decl -> fun_decl
				fun_decl += newTexts.get(ctx.decl(i));
			else // decl -> var_decl
				var_decl += newTexts.get(ctx.decl(i));
		}

		newTexts.put(ctx, classProlog + var_decl + fun_decl);

		FileWriter filewriter;
		try {
			filewriter = new FileWriter("test.j");
			BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
			bufferedwriter.write(newTexts.get(ctx));
			bufferedwriter.close();
			filewriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	// decl : var_decl | fun_decl
	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String decl = "";
		if (ctx.getChildCount() == 1) {
			if (ctx.var_decl() != null) // var_decl
				decl += newTexts.get(ctx.var_decl());
			else // fun_decl
				decl += newTexts.get(ctx.fun_decl());
		}
		newTexts.put(ctx, decl);
	}

	// stmt : expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
	@Override
	public void exitStmt(MiniCParser.StmtContext ctx) {
		String stmt = "";
		if (ctx.getChildCount() > 0) {
			if (ctx.expr_stmt() != null) // expr_stmt
				stmt += newTexts.get(ctx.expr_stmt());
			else if (ctx.compound_stmt() != null) // compound_stmt
				stmt += newTexts.get(ctx.compound_stmt());
			else if (ctx.if_stmt() != null) // if_stmt
				stmt += newTexts.get(ctx.if_stmt());
			else if (ctx.while_stmt() != null) // while_stmt
				stmt += newTexts.get(ctx.while_stmt());
			else // return_stmt
				stmt += newTexts.get(ctx.return_stmt());
		}
		newTexts.put(ctx, stmt);
	}

	// expr_stmt : expr ';'
	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String stmt = "";
		if (ctx.getChildCount() == 2) {
			stmt += newTexts.get(ctx.expr()); // expr
		}
		newTexts.put(ctx, stmt);
	}

	// while_stmt : WHILE '(' expr ')' stmt
	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		String stmt = ""; // 입력된 ctx의 전체 명령어를 저장할 변수
		String expr = newTexts.get(ctx.expr()); // 조건식 명령어를 저장할 변수
		String whilestmt = newTexts.get(ctx.stmt()); // 실행문 명령어를 저장할 변수
		
		//라벨 생성
		String exit = symbolTable.newLabel(); 
		String loop = symbolTable.newLabel();

		if (ctx.getChildCount() == 5) {
			plus_stack_size(-1);
			stmt += loop + ": \n" + expr + "ifeq " + exit + "\n" + whilestmt + "goto " + loop + "\n" + exit
					+ ": \n";
		}
		newTexts.put(ctx, stmt);
	}

	// type_spec IDENT '(' params ')' compound_stmt
	@Override
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
		
		String stmt = "";
		
		if(ctx.getChildCount() == 6) {
			stmt += funcHeader(ctx, ctx.IDENT().getText()) + newTexts.get(ctx.compound_stmt()) + ".end method\n";
			// stmt += 함수 헤더 + 함수 실행문 + .end method
		}
		reset_stack_size();
		newTexts.put(ctx, stmt);
	}

	private String funcHeader(MiniCParser.Fun_declContext ctx, String fname) {
		return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n" + ".limit stack "
				+ getStackSize(ctx) + "\n" + ".limit locals " + getLocalVarSize(symbolTable) + "\n";

	}

	@Override
	public void exitVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		String varDecl = "";

		if (isDeclWithInit(ctx)) {
			varDecl += "putfield " + varName + "\n";
			// v. initialization => Later! skip now..:
		}
		newTexts.put(ctx, varDecl);
	}

	@Override
	public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
		String varDecl = "";

		if (isDeclWithInit(ctx)) {
			String vId = symbolTable.getVarId(ctx);
			varDecl += "ldc " + ctx.LITERAL().getText() + "\n" + "istore " + vId + "\n";
			plus_stack_size(1);
			plus_stack_size(-1);
		}

		newTexts.put(ctx, varDecl);
	}

	// compound_stmt : '{' local_decl* stmt* '}'
	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		String stmt = "";
		
		for(int i = 0; i < ctx.local_decl().size(); i++) { // local_decl의 개수만큼 반복
			stmt += newTexts.get(ctx.local_decl(i));
		}
		for(int i = 0; i < ctx.stmt().size(); i++) { // stmt의 개수만큼 반복
			stmt += newTexts.get(ctx.stmt(i));
		}
		
		newTexts.put(ctx, stmt);
	}

	// if_stmt : IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String stmt = "";
		String condExpr = newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt(0));

		String lend = symbolTable.newLabel();
		String lelse = symbolTable.newLabel();
		if (noElse(ctx)) {
			plus_stack_size(-1);
			stmt += condExpr + "ifeq " + lend + "\n" + thenStmt + lend + ":" + "\n";
		} else {
			plus_stack_size(-1);
			String elseStmt = newTexts.get(ctx.stmt(1));
			stmt += condExpr + "ifeq " + lelse + "\n" + thenStmt + "goto " + lend + "\n" + lelse + ": \n"
					+ elseStmt + lend + ":" + "\n";
		}
		newTexts.put(ctx, stmt);
	}

	// return_stmt : RETURN ';' | RETURN expr ';'
	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		String stmt = "";
		
		if(ctx.getChildCount() == 2) { // RETURN ;
			stmt += "return\n";
		}
		else { // RETURN expr ;
			plus_stack_size(-1);
			stmt += newTexts.get(ctx.expr()) + "ireturn\n";
		}
		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitExpr(MiniCParser.ExprContext ctx) {
		String expr = "";

		if (ctx.getChildCount() <= 0) {
			newTexts.put(ctx, "");
			return;
		}

		if (ctx.getChildCount() == 1) { // IDENT | LITERAL
			if (ctx.IDENT() != null) {
				String idName = ctx.IDENT().getText();
				if (symbolTable.getVarType(idName) == Type.INT) {
					plus_stack_size(1);
					expr += "iload " + symbolTable.getVarId(idName) + " \n";
				}
				// else // Type int array => Later! skip now..
				// expr += " lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
			} else if (ctx.LITERAL() != null) {
				String literalStr = ctx.LITERAL().getText();
				plus_stack_size(1);
				expr += "ldc " + literalStr + " \n";
			}
		} else if (ctx.getChildCount() == 2) { // UnaryOperation
			expr = handleUnaryExpr(ctx, expr);
		} else if (ctx.getChildCount() == 3) {
			if (ctx.getChild(0).getText().equals("(")) { // '(' expr ')'
				expr = newTexts.get(ctx.expr(0));

			} else if (ctx.getChild(1).getText().equals("=")) { // IDENT '=' expr
				plus_stack_size(-1);
				expr = newTexts.get(ctx.expr(0)) + "istore " + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";

			} else { // binary operation
				expr = handleBinExpr(ctx, expr);

			}
		}
		// IDENT '(' args ')' | IDENT '[' expr ']'
		else if (ctx.getChildCount() == 4) {
			if (ctx.args() != null) { // function calls
				expr = handleFunCall(ctx, expr);
			} else { // expr
				// Arrays: TODO
			}
		}
		// IDENT '[' expr ']' '=' expr
		else { // Arrays: TODO */
		}
		newTexts.put(ctx, expr);
	}

	private String handleUnaryExpr(MiniCParser.ExprContext ctx, String expr) {
		String l1 = symbolTable.newLabel();
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		expr += newTexts.get(ctx.expr(0));
		switch (ctx.getChild(0).getText()) {
		case "-":
			expr += "           ineg \n";
			break;
		case "--":
			plus_stack_size(1);
			plus_stack_size(-1);
			expr += "ldc 1" + "\n" + "isub" + "\n";
			break;
		case "++":
			plus_stack_size(1);
			plus_stack_size(-1);
			expr += "ldc 1" + "\n" + "iadd" + "\n";
			break;
		case "!":
			expr += "ifeq " + l2 + "\n" + l1 + ": \n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ": \n" + "ldc 1"
					+ "\n" + lend + ": " + "\n";
			break;
		}
		return expr;
	}

	private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) {
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();

		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));

		switch (ctx.getChild(1).getText()) {
		case "*":
			plus_stack_size(-1);
			expr += "imul \n";
			break;
		case "/":
			plus_stack_size(-1);
			expr += "idiv \n";
			break;
		case "%":
			plus_stack_size(-1);
			expr += "irem \n";
			break;
		case "+": // expr(0) expr(1) iadd
			plus_stack_size(-1);
			expr += "iadd \n";
			break;
		case "-":
			plus_stack_size(-1);
			expr += "isub \n";
			break;
			
		// 참일경우 스택에 1이 쌓음, 거짓일 경우 스택에 0을 쌓음 
		case "==":
			plus_stack_size(-1);
			expr += "isub " + "\n" + "ifeq l2" + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ": \n" + "ldc 1"
					+ "\n" + lend + ": " + "\n";
			break;
		case "!=":
			plus_stack_size(-1);
			expr += "isub " + "\n" + "ifne l2" + "\n" + "ldc 0" + "\n" + "goto " + lend + "\n" + l2 + ": \n" + "ldc 1"
					+ "\n" + lend + ": " + "\n";
			break;
		case "<=":
			plus_stack_size(-1);
			expr += "if_icmple "+ l2 + "\n" + "ldc 0\n" + "goto " + lend + "\n" + l2 + ": \n" + "ldc 1\n" + lend + ": \n"; 
			break;
		case "<":
			plus_stack_size(-1);
			expr += "if_icmplt "+ l2 + "\n" + "ldc 0\n" + "goto " + lend + "\n" + l2 + ": \n" + "ldc 1\n" + lend + ": \n";
			break;

		case ">=":
			plus_stack_size(-1);
			expr += "if_icmpge "+ l2 + "\n" + "ldc 0\n" + "goto " + lend + "\n" + l2 + ": \n" + "ldc 1\n" + lend + ": \n";
			break;

		case ">":
			plus_stack_size(-1);
			expr += "if_icmpgt "+ l2 + "\n" + "ldc 0\n" + "goto " + lend + "\n" + l2 + ": \n" + "ldc 1\n" + lend + ": \n";
			break;

		case "and":
			plus_stack_size(-1);
			expr += "ifne " + lend + "\n" + "pop" + "\n" + "ldc 0" + "\n" + lend + ": " + "\n";
			break;
		case "or":
			plus_stack_size(-1);
			expr += "ifeq " + lend + "\n" + "pop" + "\n" + "ldc 1" + "\n" + lend + ": " + "\n";
			break;

		}
		return expr;
	}

	private String handleFunCall(MiniCParser.ExprContext ctx, String expr) {
		String fname = getFunName(ctx);

		if (fname.equals("_print")) { // System.out.println
			plus_stack_size(1);
			plus_stack_size(-1);
			expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n" + newTexts.get(ctx.args())
					+ "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
		} else {
			plus_stack_size(symbolTable.getFunSpecStr(fname));
			expr = newTexts.get(ctx.args()) + "invokestatic " + getCurrentClassName() + "/"
					+ symbolTable.getFunSpecStr(fname) + "\n";
		}

		return expr;

	}

	// args : expr (',' expr)* | ;
	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {

		String argsStr = "";

		for (int i = 0; i < ctx.expr().size(); i++) {
			argsStr += newTexts.get(ctx.expr(i));
		}
		newTexts.put(ctx, argsStr);
	}

}
