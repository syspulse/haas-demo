import scala.sys.process.Process
import Dependencies._
import com.typesafe.sbt.packager.docker.DockerAlias
import com.typesafe.sbt.packager.docker._

Global / onChangedBuildSource := ReloadOnSourceChanges

// https://www.scala-sbt.org/1.x/docs/Parallel-Execution.html#Built-in+Tags+and+Rules
Test / parallelExecution := true
//test / parallelExecution := false
// I am sorry sbt, this is stupid ->
// Non-concurrent execution is needed for Server with starting / stopping HttpServer
Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)

licenses := Seq(("ASF2", url("https://www.apache.org/licenses/LICENSE-2.0")))

initialize ~= { _ =>
  System.setProperty("config.file", "conf/application.conf")
}

//fork := true
test / fork := true
run / fork := true
run / connectInput := true

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)
//enablePlugins(JavaAppPackaging, AshScriptPlugin)

// Huge Credits -> https://softwaremill.com/how-to-build-multi-platform-docker-image-with-sbt-and-docker-buildx
lazy val ensureDockerBuildx = taskKey[Unit]("Ensure that docker buildx configuration exists")
lazy val dockerBuildWithBuildx = taskKey[Unit]("Build docker images using buildx")
lazy val dockerBuildxSettings = Seq(
  ensureDockerBuildx := {
    if (Process("docker buildx inspect multi-arch-builder").! == 1) {
      Process("docker buildx create --use --name multi-arch-builder", baseDirectory.value).!
    }
  },
  dockerBuildWithBuildx := {
    streams.value.log("Building and pushing image with Buildx")
    dockerAliases.value.foreach(
      alias => Process("docker buildx build --platform=linux/arm64,linux/amd64 --push -t " +
        alias + " .", baseDirectory.value / "target" / "docker"/ "stage").!
    )
  },
  Docker / publish := Def.sequential(
    Docker / publishLocal,
    ensureDockerBuildx,
    dockerBuildWithBuildx
  ).value
)

val dockerRegistryLocal = Seq(
  dockerRepository := Some("docker.u132.net:5000"),
  dockerUsername := Some("syspulse"),
  // this fixes stupid idea of adding registry in publishLocal 
  dockerAlias := DockerAlias(registryHost=None,username = dockerUsername.value, name = name.value, tag = Some(version.value))
)

val dockerRegistryDockerHub = Seq(
  dockerUsername := Some("syspulse")
)

val sharedConfigDocker = Seq(
  maintainer := "Dev0 <dev0@syspulse.io>",
  // openjdk:8-jre-alpine - NOT WORKING ON RP4+ (arm64). Crashes JVM in kubernetes
  // dockerBaseImage := "openjdk:8u212-jre-alpine3.9", //"openjdk:8-jre-alpine",

  //dockerBaseImage := "openjdk:8-jre-alpine",
  //dockerBaseImage := "openjdk:18-slim",
  //dockerBaseImage := "openjdk-s3fs:18-slim",
  dockerBaseImage := "openjdk-s3fs:11-slim",  // WARNING: this image is needed for JavaScript Nashorn !

  // Add S3 mount options
  // Requires running docker: 
  // --privileged -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY -e S3_BUCKET=haas-data-dev
  bashScriptExtraDefines += """/mount-s3.sh""",
  // bashScriptExtraDefines += """ls -l /mnt/s3/""",
  
  dockerUpdateLatest := true,
  dockerUsername := Some("syspulse"),
  dockerExposedVolumes := Seq(s"${appDockerRoot}/logs",s"${appDockerRoot}/conf",s"${appDockerRoot}/data","/data"),
  //dockerRepository := "docker.io",
  dockerExposedPorts := Seq(8080),

  Docker / defaultLinuxInstallLocation := appDockerRoot,

  // Docker / daemonUserUid := None,
  // Docker / daemonUser := "daemon"

  // Experiments with S3 mount compatibility
  Docker / daemonUserUid := Some("1000"),  
  // Docker / daemonUser := "ubuntu",
  // Docker / daemonGroupGid := Some("1000"),
  // Docker / daemonGroup := "ubuntu",
  
) ++ dockerRegistryLocal

// Spark is not working with openjdk:18-slim (cannot access class sun.nio.ch.DirectBuffer)
// openjdk:8-jre
// Also, Spark has problems with /tmp (java.io.IOException: Failed to create a temp directory (under /tmp) after 10 attempts!)
val sharedConfigDockerSpark = sharedConfigDocker ++ Seq(
  //dockerBaseImage := "openjdk:8-jre-alpine",
  //dockerBaseImage := "openjdk:8-jre-slim",
  dockerBaseImage := "openjdk:11-jre-slim",
  Docker / daemonUser := "root"
)

val sharedConfig = Seq(
    //retrieveManaged := true,  
    organization    := "io.syspulse",
    scalaVersion    := "2.13.9",
    name            := "haas",
    version         := appVersion,

    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:implicitConversions", "-language:higherKinds", "-language:reflectiveCalls", "-language:postfixOps"),
    javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
    
    crossVersion := CrossVersion.binary,
    resolvers ++= Seq(
      Opts.resolver.sonatypeSnapshots, 
      Opts.resolver.sonatypeReleases,
      "spray repo"         at "https://repo.spray.io/",
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/",
      "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "typesafe repo"      at "https://repo.typesafe.com/typesafe/releases/",
      "confluent repo"     at "https://packages.confluent.io/maven/",
      "consensys repo"     at "https://artifacts.consensys.net/public/maven/maven/",
      "consensys teku"     at "https://artifacts.consensys.net/public/teku/maven/"
    ),
  )

val sharedConfigAssembly = Seq(
  assembly / assemblyMergeStrategy := {
      case x if x.contains("module-info.class") => MergeStrategy.discard
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.first
      case x if x.contains("slf4j/impl/StaticMarkerBinder.class") => MergeStrategy.first
      case x if x.contains("slf4j/impl/StaticMDCBinder.class") => MergeStrategy.first
      case x if x.contains("slf4j/impl/StaticLoggerBinder.class") => MergeStrategy.first
      case x if x.contains("google/protobuf") => MergeStrategy.first
      case x => {
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
      }
  },
  assembly / assemblyExcludedJars := {
    val cp = (assembly / fullClasspath).value
    cp filter { f =>
      f.data.getName.contains("snakeyaml-1.27-android.jar") || 
      f.data.getName.contains("activation-1.1.1") ||
      f.data.getName.contains("jakarta.activation-api-1.2.1") ||
      f.data.getName.contains("jakarta.activation-2.0.1") 
      //|| f.data.getName == "spark-core_2.11-2.0.1.jar"
    }
  },
  
  assembly / test := {}
)

val sharedConfigAssemblySpark = Seq(
  assembly / assemblyMergeStrategy := {
      case x if x.contains("module-info.class") => MergeStrategy.discard
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.first
      case x if x.contains("slf4j/impl/StaticMarkerBinder.class") => MergeStrategy.first
      case x if x.contains("slf4j/impl/StaticMDCBinder.class") => MergeStrategy.first
      case x if x.contains("slf4j/impl/StaticLoggerBinder.class") => MergeStrategy.first
      case x if x.contains("google/protobuf") => MergeStrategy.first
      case x if x.contains("org/apache/spark/unused/UnusedStubClass.class") => MergeStrategy.first
      case x if x.contains("git.properties") => MergeStrategy.discard
      case x if x.contains("mozilla/public-suffix-list.txt") => MergeStrategy.first
      case x => {
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
      }
  },
  assembly / assemblyExcludedJars := {
    val cp = (assembly / fullClasspath).value
    cp filter { f =>
      f.data.getName.contains("snakeyaml-1.27-android.jar") || 
      f.data.getName.contains("jakarta.activation-api-1.2.1") ||
      f.data.getName.contains("jakarta.activation-api-1.1.1") ||
      f.data.getName.contains("jakarta.activation-2.0.1.jar") ||
      f.data.getName.contains("jakarta.annotation-api-1.3.5.jar") ||
      f.data.getName.contains("jakarta.ws.rs-api-2.1.6.jar") ||
      f.data.getName.contains("commons-logging-1.1.3.ja") ||
      f.data.getName.contains("aws-java-sdk-bundle-1.11.563.jar") ||
      f.data.getName.contains("jcl-over-slf4j-1.7.30.jar") ||
      (f.data.getName.contains("netty") && (f.data.getName.contains("4.1.50.Final.jar") || (f.data.getName.contains("netty-all-4.1.68.Final.jar"))))

      //|| f.data.getName == "spark-core_2.11-2.0.1.jar"
    }
  },
  
  assembly / test := {}
)

def appDockerConfig(appName:String,appMainClass:String) = 
  Seq(
    name := appName,

    run / mainClass := Some(appMainClass),
    assembly / mainClass := Some(appMainClass),
    Compile / mainClass := Some(appMainClass), // <-- This is very important for DockerPlugin generated stage1 script!
    assembly / assemblyJarName := jarPrefix + appName + "-" + "assembly" + "-"+  appVersion + ".jar",

    Universal / mappings += file(baseDirectory.value.getAbsolutePath+"/conf/application.conf") -> "conf/application.conf",
    Universal / mappings += file(baseDirectory.value.getAbsolutePath+"/conf/logback.xml") -> "conf/logback.xml",
    bashScriptExtraDefines += s"""addJava "-Dconfig.file=${appDockerRoot}/conf/application.conf"""",
    bashScriptExtraDefines += s"""addJava "-Dlogback.configurationFile=${appDockerRoot}/conf/logback.xml"""",   
  )

def appAssemblyConfig(appName:String,appMainClass:String) = 
  Seq(
    name := appName,
    run / mainClass := Some(appMainClass),
    assembly / mainClass := Some(appMainClass),
    Compile / mainClass := Some(appMainClass),
    assembly / assemblyJarName := jarPrefix + appName + "-" + "assembly" + "-"+  appVersion + ".jar",
  )

// ======================================================================================= Modules ==============================

lazy val root = (project in file("."))
  .aggregate(
      haas_core, 
      haas_token, 
      ingest_coingecko, 
      ingest_eth,
      ingest_token,
      ingest_price,
      ingest_mempool,
      circ_core,
      circ_harvest,
      haas_circ,
      haas_intercept,
      haas_abi
  )
  .dependsOn(
    haas_core, 
      haas_token, 
      ingest_coingecko, 
      ingest_eth,
      ingest_token,
      ingest_price,
      ingest_mempool,
      circ_core,
      circ_harvest,
      haas_circ,
      haas_intercept,
      haas_abi
  )
  .disablePlugins(sbtassembly.AssemblyPlugin) // this is needed to prevent generating useless assembly and merge error
  .settings(
    
    sharedConfig,
    sharedConfigDocker,
    dockerBuildxSettings
  )

lazy val haas_core = (project in file("haas-core"))
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .settings (
      sharedConfig,
      name := "haas-core",
      libraryDependencies ++= 
        Seq(
          libSkelCore,
          libUUID, 
          libScalaTest % "test"
        ),
    )

lazy val ingest_coingecko = (project in file("haas-ingest/ingest-coingecko"))
  .dependsOn(haas_core)
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .settings (
    sharedConfig,
    
    name := "ingest-coingecko",
    
    libraryDependencies ++= Seq(
      libSkelCore,
      libUpickleLib,
      libScalaTest % "test"
    ),
     
  )

lazy val haas_token = (project in file("haas-token"))
  .dependsOn(haas_core,ingest_coingecko)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (

    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    appDockerConfig("haas-token","io.syspulse.haas.token.App"),

    libraryDependencies ++= libHttp ++ libDB ++ libTest ++ Seq(  
      libSkelCore,
      libSkelAuthCore,
      libSkelIngest,
      libSkelIngestElastic,
      libElastic4s,
    ),    
  )

lazy val ingest_token = (project in file("haas-ingest/ingest-token"))
  .dependsOn(haas_core,ingest_coingecko)
  .enablePlugins(JavaAppPackaging)
  .settings (
    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    //appAssemblyConfig("ingest-token","io.syspulse.haas.ingest.cg.App"),
    appDockerConfig("ingest-token","io.syspulse.haas.ingest.token.App"),
    
    libraryDependencies ++= libHttp ++ libAkka ++ libAlpakka ++ libPrometheus ++ Seq(
      libSkelCore,
      libSkelIngest,
      libSkelIngestFlow,
      libSkelIngestElastic,
      libUpickleLib,
      libSkelSerde
    ),
     
  )

lazy val ingest_eth = (project in file("haas-ingest/ingest-eth"))
  .dependsOn(haas_core)
  .enablePlugins(JavaAppPackaging)
  .settings (
    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    //appAssemblyConfig("ingest-eth","io.syspulse.haas.ingest.eth.App"),
    appDockerConfig("ingest-eth","io.syspulse.haas.ingest.eth.App"),
    
    libraryDependencies ++= libHttp ++ libAkka ++ libAlpakka ++ libPrometheus ++ Seq(
      libSkelCore,
      libSkelIngest,
      libSkelIngestFlow,
      libSkelDSL,
      libSkelNotify,
      libUpickleLib,

      libCsv,
      libSkelSerde,

      libSkelCrypto,
      libEthAbi,
      libScalaTest % "test"
    ),
     
  )

lazy val circ_core = (project in file("haas-circ/circ-core"))
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .dependsOn(haas_core)
  .settings (
    sharedConfig,
    name := "circ-core",
    libraryDependencies ++= 
      Seq(
        libSkelCore,
        libUUID,
      ),
  )

lazy val circ_harvest = (project in file("haas-circ/circ-harvest"))
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .dependsOn(circ_core)
  .settings (
    sharedConfig,
    name := "circ-harvest",
    libraryDependencies ++= 
      Seq(
        libSkelCore,
        libUUID,

        libScalaTest % "test"
      ),
  )


lazy val haas_circ = (project in file("haas-circ"))
  .dependsOn(haas_core,circ_core)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (

    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    appDockerConfig("haas-circ","io.syspulse.haas.circ.App"),

    libraryDependencies ++= libHttp ++ libDB ++ libTest ++ Seq(  
      libSkelCore,
      libSkelCli,
      libSkelAuthCore,
      libSkelSyslogCore,
      libSkelJobCore,
    ),    
  )

lazy val ingest_price = (project in file("haas-ingest/ingest-price"))
  .dependsOn(haas_core,ingest_coingecko)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (
    
    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    //appAssemblyConfig("ingest-price","io.syspulse.haas.ingest.price.App"),
    appDockerConfig("ingest-price","io.syspulse.haas.ingest.price.App"),
    
    libraryDependencies ++= libHttp ++ libAkka ++ libAlpakka ++ libPrometheus ++ Seq(
      libSkelCore,
      libSkelIngest,
      libSkelIngestFlow,
      libSkelIngestElastic,
      libUpickleLib,

      libSkelSerde
    ),
     
  )

lazy val haas_intercept = (project in file("haas-intercept"))
  .dependsOn(haas_core,ingest_eth)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (

    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    appDockerConfig("haas-intercept","io.syspulse.haas.intercept.App"),

    libraryDependencies ++= libHttp ++ libAkka ++ libAlpakka ++ libPrometheus ++ Seq(
      libSkelCore,
      libSkelAuthCore,
      libSkelIngest,
      libSkelIngestFlow,
      libSkelDSL,
      libSkelNotifyCore,
      libSkelNotify,
      libUpickleLib,

      libSkelCrypto,
      libEthAbi,
      libScalaTest % "test",

      libSkelSerde
    ),
  )

lazy val haas_abi = (project in file("haas-abi"))
  .dependsOn(haas_core)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (

    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    appDockerConfig("haas-abi","io.syspulse.haas.abi.App"),

    libraryDependencies ++= libHttp ++ libAkka ++ libAlpakka ++ libPrometheus ++ Seq(
      libSkelCore,
      libSkelAuthCore,
      libSkelIngest,
      libSkelIngestFlow,
      
      libSkelCrypto,
      libEthAbi,
      libScalaTest % "test"
    ),
  )

lazy val ingest_mempool = (project in file("haas-ingest/ingest-mempool"))
  .dependsOn(haas_core,ingest_coingecko)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (
    
    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    //appAssemblyConfig("ingest-mempool","io.syspulse.haas.ingest.mempool.App"),
    appDockerConfig("ingest-mempool","io.syspulse.haas.ingest.mempool.App"),
    
    libraryDependencies ++= libHttp ++ libAkka ++ libAlpakka ++ libPrometheus ++ Seq(
      libSkelCore,
      libSkelIngest,
      libSkelIngestFlow,
      libSkelIngestElastic,
      libUpickleLib,

      libSkelSerde
    ),
     
  )

lazy val haas_holders = (project in file("haas-holders"))
  .dependsOn(haas_core)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (

    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    appDockerConfig("haas-holders","io.syspulse.haas.holder.App"),

    libraryDependencies ++= libHttp ++ libAkka ++ libAlpakka ++ libPrometheus ++ Seq(
      libSkelCore,
      libSkelAuthCore,
      libSkelIngest,
      libSkelIngestFlow,
      
      libSkelCrypto,
      libEthAbi,
      libScalaTest % "test"
    ),
  )

  lazy val haas_supply = (project in file("haas-supply"))
  .dependsOn(haas_core)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings (

    sharedConfig,
    sharedConfigAssembly,
    sharedConfigDocker,
    dockerBuildxSettings,

    appDockerConfig("haas-supply","io.syspulse.haas.supply.App"),

    libraryDependencies ++= libHttp ++ libDB ++ libTest ++ Seq(  
      libSkelCore,
      libSkelCli,
      libSkelAuthCore,
    ),    
  )