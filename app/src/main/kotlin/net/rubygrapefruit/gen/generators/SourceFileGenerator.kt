package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.JvmClassName
import java.io.PrintWriter
import java.nio.file.Path

class SourceFileGenerator(private val textFileGenerator: TextFileGenerator) {
    private class JavaSourceFileBuilderImpl(val className: JvmClassName) : JavaSourceFileBuilder {
        private val imports = mutableListOf<String>()
        private val implements = mutableListOf<String>()
        private val methods = mutableListOf<String>()

        override fun imports(name: String) {
            imports.add(name)
        }

        override fun implements(name: String) {
            implements.add(name)
        }

        override fun method(text: String) {
            methods.add(text)
        }

        fun run(writer: PrintWriter, body: JavaSourceFileBuilder.() -> Unit) {
            body(this)
            if (className.packageName.isNotEmpty()) {
                writer.println()
                writer.print("package ")
                writer.print(className.packageName)
                writer.println(";")
            }
            if (imports.isNotEmpty()) {
                writer.println()
                for (import in imports) {
                    writer.print("import ")
                    writer.print(import)
                    writer.println(";")
                }
            }
            writer.println()
            writer.print("public class ")
            writer.print(className.simpleName)
            if (implements.isNotEmpty()) {
                writer.print(" implements ")
                writer.print(implements.joinToString(", "))
            }
            writer.println(" {")
            for (method in methods) {
                writer.println(method)
            }
            writer.println("}")
        }
    }

    fun java(srcDir: Path, className: JvmClassName, body: JavaSourceFileBuilder.() -> Unit) {
        textFileGenerator.file(srcDir.resolve(className.name.replace(".", "/") + ".java")) {
            JavaSourceFileBuilderImpl(className).run(this, body)
        }
    }
}