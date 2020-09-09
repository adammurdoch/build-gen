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
            for (line in text.trim().lines().filter { it.isNotBlank() }) {
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

        override fun complete() {
            textFileGenerator.file(srcDir.resolve(className.name.replace(".", "/") + ".java")) {
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
}