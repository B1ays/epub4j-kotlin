plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  applyDefaultHierarchyTemplate()
  jvm {
    withJava()
    compilations {
      val main = getByName("main")
      tasks {
        register<Jar>("buildFatJar2") {
          group = "application"
          dependsOn(build)
          manifest {
            attributes["Main-Class"] = "io.documentnode.epub4j.domain.Book"
          }
          from(
            configurations.getByName("runtimeClasspath")
              .filterNot {
                it.path.contains("org.jetbrains.kotlin") ||
                it.path.contains("org.jetbrains")
              }
              .map { if (it.isDirectory) it else zipTree(it) },
            main.output.classesDirs
          )
          duplicatesStrategy = DuplicatesStrategy.EXCLUDE
          archiveBaseName.set("${project.name}-fat2")
        }
      }
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        //implementation("org.kobjects.ktxml:core:0.2.3")
        implementation("net.sf.kxml:kxml2:2.3.0")
        implementation("xmlpull:xmlpull:1.1.3.4d_b4_min")
      }
    }
  }
}

tasks.withType<Javadoc> {
  (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
  (options as StandardJavadocDocletOptions).addStringOption("sourcepath", "")
  (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
}
