// AWS emr-6.6.0   3.2.0
// AWS emr-5.35.0  2.4.8

@main
def main(input:String="./UNI-1000.csv", output:String = "Holders.csv", codec:String = "csv", decimals:Double = 10e18, batch:Int = 100, parallelism:Int = 4) {
  
  println(s"input=${input}, output=${output}, codec=${codec}, batch=${batch}, par=${parallelism}")

  // read from JDBC
  val jdbcDF = ss.read.format("jdbc").option("url", "jdbc:mysql://localhost:3306/haas_db").option("user", "haas_user").option("password", "haas_pass").option("dbtable", "haas_db.HOLDERS").load()
  
  // overwrite results (append == INSERT)
  val hDF = ss.createDataFrame(holders)
  hDF.write.format("jdbc").option("url", "jdbc:mysql://localhost:3306/haas_db").option("user", "haas_user").option("password", "haas_pass").option("dbtable", "haas_db.HOLDERS").mode("overwrite").save()

  // the same
  val dbProp = new java.util.Properties
  Map("driver" -> "com.mysql.jdbc.Driver","user" -> "haas_user","password" -> "haas_pass").foreach{ case(k,v) => dbProp.setProperty(k,v)}
  val dbUrl = "jdbc:mysql://localhost:3306/haas_db"
  val dbTable = "HOLDERS"
  hDF.write.mode("overwrite").jdbc(dbUrl, dbTable, dbProp)

}