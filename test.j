.class public Test
.super java/lang/Object
.method public <init>()V
aload_0
invokenonvirtual java/lang/Object/<init>()V
return
.end method
.method public static sum(II)I
.limit stack 32
.limit locals 8
iload 0 
iload 1 
iadd 
ireturn
.end method
.method public static minus(II)I
.limit stack 32
.limit locals 8
iload 0 
iload 1 
isub 
ireturn
.end method
.method public static main([Ljava/lang/String;)V
.limit stack 32
.limit locals 8
ldc 10
istore 0
ldc 1
istore 1
getstatic java/lang/System/out Ljava/io/PrintStream; 
iload 0 
iload 1 
invokestatic Test/sum(II)I
invokevirtual java/io/PrintStream/println(I)V
getstatic java/lang/System/out Ljava/io/PrintStream; 
iload 0 
iload 1 
invokestatic Test/minus(II)I
invokevirtual java/io/PrintStream/println(I)V
return
.end method
