package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.specs.JvmClassName
import java.io.PrintWriter
import java.nio.file.Path

class SourceFileGenerator(private val textFileGenerator: TextFileGenerator) {
    fun java(srcDir: Path, className: JvmClassName, builder: JavaSourceFileBuilder.() -> Unit) {
        val src = JavaSourceFileBuilderImpl(srcDir, className)
        builder(src)
        src.complete()
    }

    private class MethodImpl(val text: String) {
        fun writeContext(writer: PrintWriter) {
            for (line in text.lines()) {
                writer.print("    ")
                writer.println(line)
            }
        }
    }

    private inner class JavaSourceFileBuilderImpl(val srcDir: Path, val className: JvmClassName) : JavaSourceFileBuilder {
        private val imports = LinkedHashSet<JvmClassName>()
        private var extends: String? = null
        private var isAbstract = false
        private val implements = mutableListOf<String>()
        private val methods = mutableListOf<MethodImpl>()

        override fun imports(name: String) {
            imports(JvmClassName(name))
        }

        override fun imports(name: JvmClassName) {
            if (name.name != "void" && !name.packageName.startsWith("java.lang") && name.packageName != className.packageName) {
                imports.add(name)
            }
        }

        fun addImportsFor(type: JvmType) {
            type.visitTypes { t -> imports(t) }
        }

        override fun implements(name: String) {
            implements.add(name)
        }

        override fun extends(name: String) {
            extends = name
        }

        override fun abstractMethod(text: String) {
            isAbstract = true
            method(text)
        }

        override fun method(text: String) {
            methods.add(MethodImpl(text))
        }

        override fun method(name: String, param1: String, paramType1: JvmType, param2: String, paramType2: JvmType, builder: JavaSourceFileBuilder.MethodBuilder.(LocalVariable, LocalVariable) -> Unit) {
            addImportsFor(paramType1)
            addImportsFor(paramType2)

            val builderImpl = MethodBuilderImpl(this, StringBuilder())
            builder(builderImpl, LocalVariable(param1, paramType1), LocalVariable(param2, paramType2))

            val body = StringBuilder()
            methodPrefix(body, builderImpl, name)
            body.append("(")
            body.append(paramType1.typeDeclaration)
            body.append(" ")
            body.append(param1)
            body.append(", ")
            body.append(paramType2.typeDeclaration)
            body.append(" ")
            body.append(param2)
            body.append(")")
            methodSuffix(body, builderImpl)
            body.append(" {\n")
            body.append(builderImpl.body)
            body.append("}\n")
            methods.add(MethodImpl(body.toString()))
        }

        override fun method(name: String, param1: String, paramType1: JvmType, builder: JavaSourceFileBuilder.MethodBuilder.(LocalVariable) -> Unit) {
            addImportsFor(paramType1)

            val builderImpl = MethodBuilderImpl(this, StringBuilder())
            builder(builderImpl, LocalVariable(param1, paramType1))

            val body = StringBuilder()
            methodPrefix(body, builderImpl, name)
            body.append("(")
            body.append(paramType1.typeDeclaration)
            body.append(" ")
            body.append(param1)
            body.append(")")
            methodSuffix(body, builderImpl)
            body.append(" {\n")
            body.append(builderImpl.body)
            body.append("}\n")
            methods.add(MethodImpl(body.toString()))
        }

        override fun method(name: String, builder: JavaSourceFileBuilder.MethodBuilder.() -> Unit) {
            val builderImpl = MethodBuilderImpl(this, StringBuilder())
            builder(builderImpl)

            val body = StringBuilder()
            methodPrefix(body, builderImpl, name)
            body.append("()")
            methodSuffix(body, builderImpl)
            body.append(" {\n")
            body.append(builderImpl.body)
            body.append("}\n")
            methods.add(MethodImpl(body.toString()))
        }

        override fun staticMethod(name: String, param1: String, paramType1: JvmType, builder: JavaSourceFileBuilder.MethodBuilder.(LocalVariable) -> Unit) {
            addImportsFor(paramType1)

            val builderImpl = MethodBuilderImpl(this, StringBuilder())
            builder(builderImpl, LocalVariable(param1, paramType1))

            val body = StringBuilder()
            body.append("static ")
            methodPrefix(body, builderImpl, name)
            body.append("(")
            body.append(paramType1.typeDeclaration)
            body.append(" ")
            body.append(param1)
            body.append(")")
            methodSuffix(body, builderImpl)
            body.append(" {\n")
            body.append(builderImpl.body)
            body.append("}\n")
            methods.add(MethodImpl(body.toString()))
        }

        private fun methodPrefix(body: StringBuilder, builder: MethodBuilderImpl, name: String) {
            addImportsFor(builder.returnType)
            for (annotation in builder.annotations) {
                addImportsFor(annotation)
            }

            for (annotation in builder.annotations) {
                body.append("@")
                body.append(annotation.typeDeclaration)
                body.append("\n")
            }

            body.append(builder.visibility)
            body.append(" ")
            body.append(builder.returnType.typeDeclaration)
            body.append(" ")
            body.append(name)
        }

        private fun methodSuffix(body: StringBuilder, builder: MethodBuilderImpl) {
            for (exception in builder.exceptions) {
                addImportsFor(exception)
            }

            if (builder.exceptions.isNotEmpty()) {
                body.append(" throws ")
                body.append(builder.exceptions.joinToString(", ") { it.typeDeclaration })
            }
        }

        fun complete() {
            textFileGenerator.file(srcDir.resolve(className.name.replace(".", "/") + ".java")) {
                println("// GENERATED FILE")
                if (className.packageName.isNotEmpty()) {
                    println()
                    print("package ")
                    print(className.packageName)
                    println(";")
                }
                if (imports.isNotEmpty()) {
                    println()
                    for (import in imports) {
                        print("import ")
                        print(import.name)
                        println(";")
                    }
                }
                println()
                print("public")
                if (isAbstract) {
                    print(" abstract")
                }
                print(" class ")
                print(className.simpleName)
                extends?.let {
                    print(" extends ")
                    print(extends)
                }
                if (implements.isNotEmpty()) {
                    print(" implements ")
                    print(implements.joinToString(", "))
                }
                println(" {")
                for (method in methods) {
                    method.writeContext(this)
                }
                println("}")
            }
        }
    }

    private class MethodBuilderImpl(
        val classBuilder: JavaSourceFileBuilderImpl,
        val body: StringBuilder
    ) : JavaSourceFileBuilder.MethodBuilder {
        var visibility = "public"
        var returnType: JvmType = JvmType.voidType
        val annotations = mutableListOf<RawType>()
        val exceptions = mutableListOf<JvmType>()

        override fun private() {
            visibility = "private"
        }

        override fun returnType(type: JvmType) {
            returnType = type
        }

        override fun annotation(type: RawType) {
            annotations.add(type)
        }

        override fun throwsException(type: JvmType) {
            exceptions.add(type)
        }

        override fun body(builder: JavaSourceFileBuilder.Statements.() -> Unit) {
            builder(StatementsImpl(classBuilder, body, "    "))
        }
    }

    private class StatementsImpl(
        val classBuilder: JavaSourceFileBuilderImpl,
        val body: StringBuilder,
        val indent: String
    ) : JavaSourceFileBuilder.Statements {
        override fun log(text: String) {
            methodCall("System.out.println(\"$text\")")
        }

        override fun methodCall(literal: String) {
            body.append(indent)
            body.append(literal.trim())
            if (!literal.endsWith(';')) {
                body.append(';')
            }
            body.append('\n')
        }

        override fun variableDefinition(type: JvmType, name: String, initializer: Expression?): LocalVariable {
            classBuilder.addImportsFor(type)
            body.append(indent)
            body.append(type.typeDeclaration);
            body.append(" ");
            body.append(name);
            if (initializer != null) {
                body.append(" = ")
                body.append(initializer.literal)
            }
            body.append(";\n");
            return LocalVariable(name, type)
        }

        override fun statements(literals: String) {
            for (line in literals.lines()) {
                body.append(indent)
                body.append(line.trim())
                body.append('\n')
            }
        }

        override fun ifStatement(condition: String, builder: JavaSourceFileBuilder.Statements.() -> Unit) {
            body.append(indent)
            body.append("if (")
            body.append(condition)
            body.append(") {\n")
            builder(StatementsImpl(classBuilder, body, "$indent    "))
            body.append(indent)
            body.append("}\n")
        }

        override fun iterate(type: String, itemName: String, valuesExpression: String, builder: JavaSourceFileBuilder.Statements.() -> Unit) {
            body.append(indent)
            body.append("for (")
            body.append(type)
            body.append(" ")
            body.append(itemName)
            body.append(": ")
            body.append(valuesExpression)
            body.append(") {\n")
            builder(StatementsImpl(classBuilder, body, "$indent    "))
            body.append(indent)
            body.append("}\n")
        }

        override fun returnValue(expression: String) {
            body.append(indent)
            body.append("return ")
            body.append(expression)
            body.append(";\n")
        }
    }
}