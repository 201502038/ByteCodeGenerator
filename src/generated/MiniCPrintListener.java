package generated;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class MiniCPrintListener extends MiniCBaseListener {

    //요구조건은 보고서에 작성했습니다.
    //출력을 위한 Tree자료구조
    //트리를 전위순회하면서 노드의 텍스트를 저장한다.
    // 터미널 노드를 만나면 원하는 형태로 조합하고 newTexts에 저장한다.
    // 저장된 텍스트는 후위순회를 하면서 출력하기 쉽게 저장되고 이를 부모노드가 호출하는방식으로 구현된다.
    public ParseTreeProperty<String> newTexts = new ParseTreeProperty<>();

    //중첩을 위한 변수 Compoundstmt노드를 만날 때 마다 "...."스트링이 추가된다.
    String nest = "";

    //모든 노드를 거치면서 각자의 텍스트를 저장한다. 터미널노드에 저장된 텍스트를 사용 할 수 있다.
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        //출력을위한 반복문 decl 노드의 수만큼 트리에 저장한 텍스트를 출력한다.
        for (int i = 0; i < ctx.decl().size(); i++) {
            System.out.println(newTexts.get(ctx.decl(i)));
        }
    }

    //        decl	: var_decl
//		        | fun_decl
//	        	| stmt;
    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
        //decl노드는 위와같이 3가지 노드로 나뉘어 진다. 후위순회를 하면서 어떤 노드가 저장되어 있는지 확인하고 이를 저장한다.
        if (ctx.var_decl() != null) {
            newTexts.put(ctx, newTexts.get(ctx.var_decl()));
        } else if (ctx.fun_decl() != null) {
            newTexts.put(ctx, newTexts.get(ctx.fun_decl()));
        }
    }

    //    var_decl	:  type_spec IDENT ';'
//              | type_spec IDENT '=' LITERAL ';'
//              | type_spec IDENT '[' LITERAL ']' ';'	;
    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        String s1 = null, s2 = null, s3 = null, s4 = null, s5 = null;
        if (ctx.getChildCount() == 3) {
            // 'int a ;'
            //type_spec IDENT ';'
            s1 = newTexts.get(ctx.type_spec());
            s2 = ctx.IDENT().toString();
            newTexts.put(ctx, s1 + " " + s2 + ";");
        } else if (ctx.getChildCount() == 5) {
            // int a = 10;
            //type_spec IDENT '=' LITERAL ';'
            s1 = newTexts.get(ctx.type_spec());
            s2 = ctx.IDENT().toString();
            s3 = ctx.getChild(2).getText();
            s4 = ctx.LITERAL().toString();
            s5 = ctx.getChild(4).getText();
            newTexts.put(ctx, s1 + " " + s2 + " " + s3 + " " + s4 + s5);
        } else if (ctx.getChildCount() == 6) {
            // int a[10];
            // type_spec IDENT '[' LITERAL ']' ';'
            s1 = newTexts.get(ctx.type_spec());
            s2 = ctx.IDENT().toString();
            s3 = ctx.getChild(2).getText();
            s4 = ctx.LITERAL().toString();
            s5 = ctx.getChild(4).getText();
            newTexts.put(ctx, s1 + " " + s2 + s3 + s4 + s5 + ";");
        }
    }


    @Override
    public void exitType_spec(MiniCParser.Type_specContext ctx) {
        //자식노드의 텍스트를 부모노드로 전달하기위한 단계
        newTexts.put(ctx, newTexts.get(ctx));
    }


    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        //fun_decl	: type_spec IDENT '(' params ')' compound_stmt;
        String s1 = null, s2 = null, s3 = null;
        s1 = newTexts.get(ctx.type_spec());
        String indent = ctx.IDENT().toString();
        s2 = newTexts.get(ctx.params());
        s3 = newTexts.get(ctx.compound_stmt());
        newTexts.put(ctx, s1 + " " + indent + "(" + s2 + ")" + s3);
    }

    //
    @Override
    public void exitParams(MiniCParser.ParamsContext ctx) {
        String params = "";
        params += newTexts.get(ctx.param(0));
        for (int i = 1; i < ctx.param().size(); i++) {
            params += ", " + newTexts.get(ctx.param(i));
        }
        newTexts.put(ctx, params);
    }


    @Override
    public void exitParam(MiniCParser.ParamContext ctx) {
        //함수인자에 대한 정의 배열,기초자료형의 변수를 받을 수 있다.
        String s1, s2 = null;
        if (ctx.getChildCount() != 4) {
            //기초자료형
            s1 = newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText();
            newTexts.put(ctx, s1);
        } else {
            //배열
            s1 = newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText();
            s2 = ctx.getChild(2).getText() + ctx.getChild(3).getText();
            newTexts.put(ctx, s1 + s2);
        }
    }

    @Override
    public void enterStmt(MiniCParser.StmtContext ctx) {

        newTexts.put(ctx, ctx.getText());
    }

    //    stmt		: expr_stmt
//		        | compound_stmt
//		        | if_stmt
//		        | while_stmt
//		        | return_stmt
    //stmt는 5가지 노드로 나뉘어 질 수 있다.
    // 후위순회를 하면서 어떤노드에 값이 있는지 확인하고 이를 가져온다.
    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) {
        if (ctx.compound_stmt() != null) {
            newTexts.put(ctx, newTexts.get(ctx.compound_stmt()));
        } else if (ctx.expr_stmt() != null) {
            newTexts.put(ctx, newTexts.get(ctx.expr_stmt()));
        } else if (ctx.if_stmt() != null) {
            newTexts.put(ctx, newTexts.get(ctx.if_stmt()));
        } else if (ctx.while_stmt() != null) {
            newTexts.put(ctx, newTexts.get(ctx.while_stmt()));
        } else {
            newTexts.put(ctx, newTexts.get(ctx.return_stmt()));
        }
    }


    // a; 변수 선언
    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        String s1 = null, expr = null;
        s1 = ctx.getChild(1).getText(); //;
        expr = newTexts.get(ctx.expr());
        newTexts.put(ctx, expr + s1 + "\n");
    }


    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        //while ( expr ) stmt
        String s1 = null, s2 = null, expr = null, s4 = null, stmt = null;
        s1 = ctx.getChild(0).getText();
        s2 = ctx.getChild(1).getText();
        expr = newTexts.get(ctx.expr());
        s4 = ctx.getChild(3).getText();
        stmt = newTexts.get(ctx.stmt());
        newTexts.put(ctx, s1 + s2 + expr + s4 + stmt);
    }

    @Override
    public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        newTexts.put(ctx, ctx.getText());
        nest += "....";
    }

    //    compound_stmt: '{' local_decl* stmt* '}'
    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        String s1 = null, s2 = null, op = "";
        //{ 'expr+stmt' }
        s1 = ctx.start.getText();

        //지역변수를 출력하기 위해 저장
        if (ctx.local_decl() != null) {
            for (int i = 0; i < ctx.local_decl().size(); i++) {
                op += nest + newTexts.get(ctx.local_decl(i));
            }

        }
        //중첩된 stmt를 호출하기 위해 저장
        if (ctx.stmt() != null) {
            for (int i = 0; i < ctx.stmt().size(); i++) {
                op += nest + newTexts.get(ctx.stmt(i));
            }
        }
        //중첩을 빠져나가면서 후위순회를 하기 때문에, ....을 제거 한다.
        nest = nest.substring(4);
        s2 = ctx.stop.getText();
        newTexts.put(ctx, "\n" + nest + s1 + "\n" + op + nest + s2 + "\n");
    }


    //    local_decl	: type_spec IDENT ';'
//                  | type_spec IDENT '=' LITERAL ';'
//                  | type_spec IDENT '[' LITERAL ']' ';'
    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String s1 = null, s2 = null, s3 = null, s4 = null, s5 = null;
        if (ctx.getChildCount() == 3) {
            //type_spec IDENT ';'
            s1 = newTexts.get(ctx.type_spec());
            s2 = ctx.IDENT().toString();
            newTexts.put(ctx, s1 + " " + s2 + ";\n");
        } else if (ctx.getChildCount() == 5) {
            //type_spec IDENT '=' LITERAL ';'
            s1 = newTexts.get(ctx.type_spec());
            s2 = ctx.IDENT().toString();
            s3 = ctx.getChild(2).getText();
            s4 = ctx.LITERAL().toString();
            s5 = ctx.getChild(4).getText();
            newTexts.put(ctx, s1 + " " + s2 + " " + s3 + " " + s4 + s5 + "\n");
        } else if (ctx.getChildCount() == 6) {
            //type_spec IDENT '[' LITERAL ']' ';'
            s1 = newTexts.get(ctx.type_spec());
            s2 = ctx.IDENT().toString();
            s3 = ctx.getChild(2).getText();
            s4 = ctx.LITERAL().toString();
            s5 = ctx.getChild(4).getText();
            newTexts.put(ctx, s1 + " " + s2 + s3 + s4 + s5 + ";\n");
        }
    }


    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        //if ( expr ) stmt
        String s1 = null, s2 = null, expr = null, s4 = null, stmt = "";
        s1 = ctx.getChild(0).getText();
        s2 = ctx.getChild(1).getText();
        expr = newTexts.get(ctx.expr());
        s4 = ctx.getChild(3).getText();
        if (ctx.stmt(0) != null) stmt = newTexts.get(ctx.stmt(0));
        //else문이 있으면 출력해준다.
        for (int i = 1; i < ctx.stmt().size(); i++) {
            stmt += nest + "else" + newTexts.get(ctx.stmt(i));
        }
        newTexts.put(ctx, s1 + s2 + expr + s4 + stmt);
    }


    //return 값을 출력한다.
    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        String s1 = null, expr = null;
        s1 = ctx.RETURN().toString();
        expr = newTexts.get(ctx.expr());
        newTexts.put(ctx, s1 + " " + expr + ";\n");
    }

    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) {
        String s1 = null, s2 = null, op = null;
        if (isBinaryOperation(ctx)) {
            if (ctx.expr(1) != null) {

                    //expr '+' expr
                    s1 = newTexts.get(ctx.expr(0));
                    s2 = newTexts.get(ctx.expr(1));
                    op = ctx.getChild(1).getText();
                    newTexts.put(ctx, s1 + " " + op + " " + s2);

            } else {
                //'string' '+' expr
                s1 = ctx.getChild(0).getText();
                s2 = newTexts.get(ctx.expr(0));
                op = ctx.getChild(1).getText();
                newTexts.put(ctx, s1 + " " + op + " " + s2);
            }
        }else{
            if (ctx.getChildCount() == 6) {
                s1 = newTexts.get(ctx.expr(0));
                s2 = newTexts.get(ctx.expr(1));
                newTexts.put(ctx, ctx.getChild(0) + "[" + s1 + "]" +" = "+ s2);
            }
        }
    }

    //이진 연산자인지 확인하는 함수
    private boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr();
    }


}
