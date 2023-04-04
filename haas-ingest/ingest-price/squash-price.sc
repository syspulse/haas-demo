import os._

@main
def main(dir:String,output:String="output",limit:String="2023") = {
  //val dirRegexp = """.*/(\d{4})/(\d{2})/(\d{2})/.*""".r
  val dirRegexp = """.*/(\S+)/([0-9]{4})/([0-9]{2})/([0-9]{2})/(.+)""".r

  os.remove.all(os.Path(output,os.pwd))

  val dirs = os
    .walk(os.Path(dir,os.pwd))
    //.filter(f => f.toIO.isDirectory())
    //.filter(!_.toString.startsWith("."))
    .flatMap( f => {
      // println(f)
      f.toString match {
        case dirRegexp(token,yyyy,mm,dd,file) => Some((token,yyyy,mm,dd,file))
        case _ => None
      }
    })
    .filter(limit == "" || _._2 == limit)
    .collect{
      case (token,yyyy,mm,dd,file) => 
        println(s"${token} -> ${output}/${yyyy}/${mm}/${dd}")

        val outputDir = s"${output}/${yyyy}/${mm}/${dd}"
        // try to create output dir tree
        os.makeDir.all(os.Path(outputDir,os.pwd))

        val f = s"${dir}/${token}/${yyyy}/${mm}/${dd}/${file}"        
        
        val data = os.read(os.Path(f,os.pwd)) + "\n"

        val ext = file.split("\\.").lastOption.getOrElse("data")

        val outputFile = s"${outputDir}/token-${yyyy}-${mm}-${dd}.${ext}"
        os.write.append(os.Path(outputFile ,os.pwd),data)

        outputFile
    }
    .distinct
  
  // println(dirs.mkString("\n"))

  dirs.foreach( f => {
    val r = os.stat(os.Path(f,os.pwd))
    println(s"${f}: ${r.size}")  
  })

  
}