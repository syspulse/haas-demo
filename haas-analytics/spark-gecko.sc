// run with ammonite prefed with spark:
// amm-spark3

import org.apache.spark.sql.functions.desc
import spark.implicits._
import org.apache.spark.sql.functions._

val filter = "Ethereum"
val data = "/mnt/share/data/haas/gecko/tokens-10"

// read all data
val df = spark.read.format("json").option("inferSchema", "true").load(data).cache

// show all categories

def getCat(df:DataFrame) = {
  // with collection into CSV String
  // val cats = df.select("categories").dropDuplicates.withColumn("categories",concat_ws(",",col("categories"))).collect
  // with explosion
  val cats = df.select("categories").dropDuplicates.select($"categories",explode($"categories")).select(col("col").as("category")).dropDuplicates.collect
  cats.map(_.getString(0))
}
def showCat(df:DataFrame) = {
  getCat(df).sorted.foreach(r => println(r))
}

def getProjects(df:DataFrame,filter:String = "") = {
  // explode categories and links.homepage
  val df2 = df.withColumn("categories",explode($"categories")).select("id","categories","links").cache

  val df3 = df2.withColumn("links",explode($"links.homepage")).withColumnRenamed("links","homepage").where("homepage != 'null'").where("homepage != ''")

  val df4 = df2.withColumn("links",explode($"links.repos_url.github")).withColumnRenamed("links","repo").where("repo != ''").dropDuplicates("id").select("id","repo")
  val df5 = df2.withColumn("links",explode($"links.repos_url.bitbucket")).withColumnRenamed("links","repo").where("repo != ''").dropDuplicates("id").select("id","repo")

  //val df6 = df3.unionByName(df3,true).unionByName(df4,true).where("homepage != 'null'").where("homepage != ''").show

  // val df7 = df3.filter(col("categories").contains(filter)).where("homepage != 'null'").where("homepage != ''")
  val df7 = df3.filter(col("categories").contains(filter))

  val df8 = df7.as("t").join(df4.as("repo"),col("t.id") === col("repo.id"),"left").select("t.id","t.categories","t.homepage","repo.repo").withColumnRenamed("repo","github")
  val df9 = df8.as("t").join(df5.as("repo"),col("t.id") === col("repo.id"),"left").select("t.id","t.categories","t.homepage","t.github","repo.repo").withColumnRenamed("repo","bitbucket")
  
  // multiple homepages !
  df9.dropDuplicates("id")  
}

def saveProjectToCsv(df:DataFrame,filter:String = "") = {
  val prj = if(filter.isEmpty()) "all" else filter
  val dfp = getProjects(df,filter)

  // subdirectory !
  dfp.coalesce(1).write.format("csv").option("header","true").save(s"${prj}/")
  // create file from subdir
  os.move(os.list(os.pwd / prj).filter(_.toString.endsWith(".csv")).head,os.pwd / s"${prj}.csv")
  os.remove.all(os.pwd / prj)
}

//getProjects(df,"Polkadot").show(1000,false)
