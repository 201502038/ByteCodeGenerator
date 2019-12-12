package listener.main;

import java.util.Hashtable;

import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.If_stmtContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import listener.main.SymbolTable;
import listener.main.SymbolTable.VarInfo;

public class BytecodeGenListenerHelper {
	
	static private int current_stack_size;
	static private int max_stack_size;
	// <boolean functions>
	
	static void plus_stack_size(int x) {
		current_stack_size += x;
		if(max_stack_size < current_stack_size) max_stack_size = current_stack_size;
	}
	
	static void plus_stack_size(String str) {
		int length = str.length();
		int i = 0;
		for(i=0; i < length; i++) {
			if(str.charAt(i) == '(') break;
		}
		int start = i;
		for(i = start; i < length; i++) {
			if(str.charAt(i) == ')') break;
		}
		int end = i;
		plus_stack_size(start - end + 1);
		if(str.charAt(length-1) == 'I') plus_stack_size(1);
	}
	
	
	static void reset_stack_size() {
		current_stack_size = 0;
		max_stack_size = 0;
	}
		
	static boolean isFunDecl(MiniCParser.ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof MiniCParser.Fun_declContext;
	}
	
	// type_spec IDENT '[' ']'
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}
	
	// global vars
	static int initVal(Var_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	// var_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	// local_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static int initVal(Local_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}
	
	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	
	static boolean isVoidF(Fun_declContext ctx) {
		return ctx.getChildCount() == 4 ;
	}
	
	static boolean isIntReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() ==3;
	}


	static boolean isVoidReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}
	
	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return String.valueOf(max_stack_size + 5);
	}
	
	static String getLocalVarSize(SymbolTable table) {
		return String.valueOf(table.getLocalSize());
	}

	static String getTypeText(Type_specContext typespec) {
		return typespec.getText();
	}

	// params
	static String getParamName(ParamContext param) {
		return param.IDENT().getText();
	}
	
	static String getParamTypesText(ParamsContext params) {
		String typeText = "";
		
		for(int i = 0; i < params.param().size(); i++) {
			MiniCParser.Type_specContext typespec = (MiniCParser.Type_specContext)  params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}
	
	static String getLocalVarName(Local_declContext local_decl) {
		return local_decl.IDENT().getText();
	}
	
	static String getFunName(Fun_declContext ctx) {
		return ctx.IDENT().getText();
	}
	
	static String getFunName(ExprContext ctx) {
		return ctx.getChild(0).getText();
	}
	
	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() == 5;
	}
	
	static String getFunProlog() {
		 return ".class public " + getCurrentClassName() + "\n.super java/lang/Object\n.method public <init>()V\naload_0\ninvokenonvirtual java/lang/Object/<init>()V\nreturn\n.end method\n";
		// ...
		// invokenonvirtual java/lang/Object/<init>()
		// return
		// .end method"
	}
	
	static String getCurrentClassName() {
		return "Test";
	}
}
