package generated;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.MiniCParser.ArgsContext;
import generated.MiniCParser.Compound_stmtContext;
import generated.MiniCParser.DeclContext;
import generated.MiniCParser.Expr_stmtContext;
import generated.MiniCParser.If_stmtContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.ProgramContext;
import generated.MiniCParser.Return_stmtContext;
import generated.MiniCParser.StmtContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import generated.MiniCParser.While_stmtContext;

public class MiniCPrintListener extends MiniCBaseListener { 
	int compound_stmt_count = 0;
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	
	private boolean isBinaryOperation(MiniCParser.ExprContext ctx) { 
		return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr();
	}
	
	private String makeblank(int number) { 
		String blank = "\n";
		for(int i = 0; i < number; i++) {
			blank = blank + "....";
		}
		return blank;
	}
	
	@Override
	public void exitProgram(ProgramContext ctx) {
		String program = newTexts.get(ctx.decl(0));
		for(int i = 1; i < ctx.decl().size(); i++) {
			program = program + "\n" + newTexts.get(ctx.decl(i));
		}
		newTexts.put(ctx, program);
		System.out.println(newTexts.get(ctx));
	}
	
	@Override
	public void exitDecl(DeclContext ctx) {
		newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
	}
	
	@Override
	public void exitVar_decl(Var_declContext ctx) {
		String var_decl = newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText();
		if (ctx.getChildCount() == 5) { // type_spec IDENT = LITERAL ;
			var_decl = var_decl + " " + ctx.getChild(2).getText() + " " + ctx.LITERAL().getText()
					+ ctx.getChild(4).getText();
		} else if (ctx.getChildCount() == 6) { // type_spec IDENT [ LITERAL ] ;
			var_decl = var_decl + " " + ctx.getChild(2).getText() + " " + ctx.LITERAL().getText() + " "
					+ ctx.getChild(4).getText() + ctx.getChild(5).getText();
		} else { // type_spec IDENT ;
			var_decl = var_decl + ctx.getChild(2);
		}
		newTexts.put(ctx, var_decl);
	}
	
	@Override
	public void exitType_spec(Type_specContext ctx) {
		newTexts.put(ctx, ctx.getChild(0).getText());
	}
	
	@Override
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
		String fun_decl = newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText() + " " + ctx.getChild(2).getText()
				+ newTexts.get(ctx.params()) + ctx.getChild(4).getText() + " " + newTexts.get(ctx.compound_stmt());
		newTexts.put(ctx, fun_decl);
	}

	@Override
	public void exitParams(ParamsContext ctx) {
		String params = "";
		if (ctx.getChildCount() == 0) {

		} else if (ctx.getChild(0) == ctx.VOID()) { // VOID
			params = ctx.VOID().getText();
		} else { // param(, param)*
			params = newTexts.get(ctx.param(0));
			for (int i = 1; i < ctx.param().size(); i++) {
				params = params + ", " + newTexts.get(ctx.param(i));
			}
		}
		newTexts.put(ctx, params);
	}

	@Override
	public void exitParam(ParamContext ctx) {
		// type_spec IDENT
		String param = newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText();
		if (ctx.getChildCount() == 4) { // type_spec IDENT [ ]
			param = param + " " + ctx.getChild(2) + ctx.getChild(3);
		}
		newTexts.put(ctx, param);
	}

	@Override
	public void exitStmt(StmtContext ctx) { 
		String stmt;
		stmt = newTexts.get(ctx.getChild(0)); 
		newTexts.put(ctx, stmt);
	}

	@Override
	public void exitExpr_stmt(Expr_stmtContext ctx) { // expr ;
		String expr_stmt = newTexts.get(ctx.expr()) + ctx.getChild(1);
		newTexts.put(ctx, expr_stmt);
	}

	@Override
	public void exitWhile_stmt(While_stmtContext ctx) { //WHILE ( expr ) stmt
		String while_stmt = ctx.WHILE().getText() + " " + ctx.getChild(1).getText() + newTexts.get(ctx.expr())
				+ ctx.getChild(3) + " " + newTexts.get(ctx.stmt());
		newTexts.put(ctx, while_stmt);
	}

	@Override
	public void enterCompound_stmt(Compound_stmtContext ctx) {
		compound_stmt_count++;
	}
	
	@Override
	public void exitCompound_stmt(Compound_stmtContext ctx) {
		String compound_stmt = makeblank(compound_stmt_count-1) + ctx.getChild(0).getText();
		for (int i = 0; i < ctx.local_decl().size(); i++) { 
			compound_stmt = compound_stmt + makeblank(compound_stmt_count) + newTexts.get(ctx.local_decl(i));
		}
		for (int i = 0; i < ctx.stmt().size(); i++) {
			compound_stmt = compound_stmt + makeblank(compound_stmt_count) + newTexts.get(ctx.stmt(i));
		}
		compound_stmt = compound_stmt + makeblank(compound_stmt_count-1) + ctx.getChild(ctx.getChildCount() - 1).getText();
		compound_stmt_count--;
		newTexts.put(ctx, compound_stmt);
	}

	@Override
	public void exitLocal_decl(Local_declContext ctx) {
		String local_decl = newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText();
		if (ctx.getChildCount() == 5) { // type_spec IDENT = LITERAL ;
			local_decl = local_decl + " " + ctx.getChild(2).getText() + " " + ctx.LITERAL().getText()
					+ ctx.getChild(4).getText();
		} else if (ctx.getChildCount() == 6) { // type_spec IDENT [ LITERAL ] ;
			local_decl = local_decl + " " + ctx.getChild(2).getText() + " " + ctx.LITERAL().getText() + " "
					+ ctx.getChild(4).getText() + ctx.getChild(5).getText();
		} else { // type_spec IDENT ;
			local_decl = local_decl + ctx.getChild(2);
		}
		newTexts.put(ctx, local_decl);
	}

	@Override
	public void exitIf_stmt(If_stmtContext ctx) {
		// IF ( expr ) stmt
		String if_stmt = ctx.IF().getText() + " " + ctx.getChild(1).getText() + newTexts.get(ctx.expr())
				+ ctx.getChild(3).getText() + newTexts.get(ctx.stmt(0));
		if (ctx.getChildCount() == 7) { // IF ( expr ) stmt ELSE stmt
			if_stmt = if_stmt + " " + ctx.ELSE().getText() + " " + newTexts.get(ctx.stmt(1));
		}
		newTexts.put(ctx, if_stmt);
	}

	@Override
	public void exitReturn_stmt(Return_stmtContext ctx) {
		String return_stmt;
		if (ctx.getChildCount() == 2) { // RETURN ;
			return_stmt = ctx.RETURN().getText() + ctx.getChild(1).getText();
		} else { // RETURN expr ; 
			return_stmt = ctx.RETURN().getText() + " " + newTexts.get(ctx.expr()) + ctx.getChild(2).getText();
		}
		newTexts.put(ctx, return_stmt);
	}

	@Override
	public void exitExpr(MiniCParser.ExprContext ctx) {
		String s1 = null, s2 = null, s3 = null, op1 = null, op2 = null, op3 = null;

		if (isBinaryOperation(ctx)) { 
			if (ctx.getChild(0) == ctx.IDENT()) { 
				s1 = ctx.IDENT().getText();
				s2 = newTexts.get(ctx.expr(0));
				op1 = ctx.getChild(1).getText();
				newTexts.put(ctx, s1 + " " + op1 + " " + s2);
			} else { 
				s1 = newTexts.get(ctx.expr(0));
				s2 = newTexts.get(ctx.expr(1));
				op1 = ctx.getChild(1).getText();
				newTexts.put(ctx, s1 + " " + op1 + " " + s2);
			}
		} else if (ctx.getChildCount() == 2) { 
			op1 = ctx.getChild(0).getText();
			s1 = newTexts.get(ctx.expr(0));
			newTexts.put(ctx, op1 + " " + s1);
		} else if (ctx.getChildCount() == 3) {
			op1 = ctx.getChild(0).getText();
			op2 = ctx.getChild(2).getText();
			s1 = newTexts.get(ctx.expr(0));
			newTexts.put(ctx, op1 + s1 + s2);
		} else if (ctx.getChildCount() == 4) {
			s1 = ctx.IDENT().getText();
			op1 = ctx.getChild(1).getText();
			s2 = newTexts.get(ctx.getChild(2));
			op2 = ctx.getChild(3).getText();
			newTexts.put(ctx, s1 + " " + op1 + s2 + op2);
		} else if (ctx.getChildCount() == 6) {
			s1 = ctx.IDENT().getText();
			op1 = ctx.getChild(1).getText();
			s2 = newTexts.get(ctx.expr(0));
			op2 = ctx.getChild(3).getText();
			op3 = ctx.getChild(4).getText();
			s3 = newTexts.get(ctx.expr(1));
			newTexts.put(ctx, s1 + " " + op1 + s2 + op2 + " " + op3 + " " + s3);
		} else {
			s1 = ctx.getChild(0).getText();
			newTexts.put(ctx, s1);
		}
	}

	@Override
	public void exitArgs(ArgsContext ctx) { // args : expr (, expr)*
		String args = newTexts.get(ctx.expr(0));
		for (int i = 1; i < ctx.expr().size(); i++) {
			args = args + ", " + newTexts.get(ctx.expr(i)); 
		}
		newTexts.put(ctx, args);
	}

	@Override
	public void enterFun_decl(MiniCParser.Fun_declContext ctx) {

	}

}
