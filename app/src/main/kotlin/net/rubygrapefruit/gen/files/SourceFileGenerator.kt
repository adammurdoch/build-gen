package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.specs.JvmClassName
import java.io.PrintWriter
import java.nio.file.Path

class SourceFileGenerator(private val textFileGenerator: TextFileGenerator) {
    fun java(srcDir: Path, className: JvmClassName): JavaSourceFileBuilder {
        return JavaSourceFileBuilderImpl(srcDir, className)
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
        private val imports = mutableListOf<String>()
        private var extends: String? = null
        private var isAbstract = false
        private val implements = mutableListOf<String>()
        private val methods = mutableListOf<MethodImpl>()

        override fun imports(name: String) {
            imports.add(name)
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

        override fun method(signature: String, statements: JavaSourceFileBuilder.Statements.() -> Unit) {
            val body = StringBuilder()
            body.append(signature.trim())
            body.append(" {\n")
            statements(MethodBodyImpl(body, "    "))
            body.append("}")
            methods.add(MethodImpl(body.toString()))
        }

        override fun complete() {
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
                        print(import)
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

    private class MethodBodyImpl(
        val body: StringBuilder,
        val indent: String
    ) : JavaSourceFileBuilder.Statements {
        override fun log(text: String) {
            methodCall("System.out.println(\"$text\")")
        }

        override fun methodCall(text: String) {
            body.append(indent)
            body.append(text.trim())
            if (!text.endsWith(';')) {
                body.append(';')
            }
            body.append('\n')
        }

        override fun variableDefinition(type: String, name: String, initializer: String?) {
            body.append(indent)
            body.append(type);
            body.append(" ");
            body.append(name);
            if (initializer != null) {
                body.append(" = ")
                body.append(initializer)
            }
            body.append(";\n");
        }

        override fun statements(text: String) {
            for (line in text.lines()) {
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
            builder(MethodBodyImpl(body, "$indent    "))
            body.append(indent)
            body.append("}\n")
        }
    }
}