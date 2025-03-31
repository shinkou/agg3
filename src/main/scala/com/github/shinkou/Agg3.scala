package com.github.shinkou

import com.opencsv.{CSVReaderHeaderAwareBuilder, CSVWriterBuilder, RFC4180Parser}
import java.io.{FileReader, Writer}
import scala.jdk.CollectionConverters._


trait Agg3:
  val reInt = """^(?:[1-9][0-9]*|0)$""".r
  val reDbl = """^(?:[1-9][0-9]*|0)\.[0-9]+$""".r


  def printHelp() =
      println("""
Usage:
  agg3 [ PROP ... ] CSV_FILE COL_OPS1 [ COL_OPS2 [ COL_OP3 ... ] ]

Where:
  CSV_FILE    the path to the CSV file
  COL_OPS<n>  column-operations in the form of:
                column_name [ ":" operation ]*

While PROP can be:
  groupby     column names for grouping
              e.g. "-Dgroupby=colA", "-Dgroupby=colA,colB"
  sortby      column(-operations) for sorting
              e.g. "-Dsortby=col1:max:min,col2:avg,col3:sum,colA,colB"
  savecsvto   save results in CSV with the given file path
              e.g. "-Dsavecsvto=out.csv"

Examples:
  agg3 src/test/resources/data.csv col1:avg col2:sum
  agg3 -Dgroupby=colA,colB -Dsortby=col1:max ../docs/data.csv col1:max:min
  agg3 -Dgroupby=colA -Dsavecsvto=out.csv /tmp/data.csv col1:avg col2:sum
""")


  def process(
    colops: List[(String, String)],
    groupby: List[String],
    sortby: List[String],
    data: List[Map[String, String]]
  ): List[(List[String], List[(String, AnyVal)])] =
    val colTypes = findColTypes(data, colops.map(_._1))
    val grouped = if 0 < groupby.length then
      data.groupBy(r => groupby.map(r(_)).toList)
    else
      Map(List() -> data)
    val res = grouped.map {case(k, v) => (
      k,
      colops.map {case (col, op) => (
        s"$op($col)",
        colTypes(col) match
          case 2 =>
            op match
              case "avg" | "mean" => v.map(_(col).toDouble).sum / v.length
              case "count" => v.length
              case "max" => v.map(_(col).toDouble).max
              case "min" => v.map(_(col).toDouble).min
              case "sum" => v.map(_(col).toDouble).sum
              case _ => throw IllegalArgumentException("Invalid operation")
          case 1 =>
            op match
              case "avg" | "mean" => v.map(_(col).toDouble).sum / v.length
              case "count" => v.length
              case "max" => v.map(_(col).toInt).max
              case "min" => v.map(_(col).toInt).min
              case "sum" => v.map(_(col).toInt).sum
              case _ => throw IllegalArgumentException("Invalid operation")
          case _ =>
            op match
              case "count" => v.length
              case _ => throw IllegalArgumentException("Invalid operation")
      )}.toList
    )}.toList
    res.sortWith((a, b) =>
      cmpRows(a, b, colops.map((k, v) => s"$k:$v").toList, groupby, sortby, 0)
    )


  def cmpRows(
    a: (List[String], List[(String, AnyVal)]),
    b: (List[String], List[(String, AnyVal)]),
    colops: List[String],
    groupby: List[String],
    sortby: List[String],
    wk: Int
  ): Boolean =
    if wk >= sortby.length then
      return false
    val col = sortby(wk)
    if groupby.contains(col) then
      val i = groupby.indexOf(col)
      if a._1(i) == b._1(i) then
        return cmpRows(a, b, colops, groupby, sortby, wk + 1)
      else
        return a._1(i) < b._1(i)
    else if colops.contains(col) then
      val i = colops.indexOf(col)
      if a._2(i)._2 == b._2(i)._2 then
        return cmpRows(a, b, colops, groupby, sortby, wk + 1)
      else
        a._2(i)._2 match
          case x: Int => b._2(i)._2 match
            case y: Int =>
              if x == y then
                return cmpRows(a, b, colops, groupby, sortby, wk + 1)
              else
                return x > y
          case x: Double => b._2(i)._2 match
            case y: Double =>
              if x == y then
                return cmpRows(a, b, colops, groupby, sortby, wk + 1)
              else
                return x > y
    false


  def readCsv(fpath: String): List[Map[String, String]] =
    val reader = CSVReaderHeaderAwareBuilder(FileReader(fpath)).withCSVParser(RFC4180Parser()).build
    var maps: List[Map[String, String]] = List()
    var row = reader.readMap()
    while null != row do
      maps :+= row.asScala.toMap
      row = reader.readMap()
    maps


  def printCsv(
    writer: Writer,
    colops: List[(String, String)],
    groupby: List[String],
    data: List[(List[String], List[(String, AnyVal)])]
  ) =
    val csvwriter = CSVWriterBuilder(writer).withParser(RFC4180Parser()).build
    val header = groupby.map(_ => "") ++ colops.map(t => s"${t._2}(${t._1})")
    csvwriter.writeNext(header.toArray)
    val rs = data.map((k, v) => (k ++ v.map(_._2.toString)).toArray).toList
    csvwriter.writeAll(rs.asJava)
    csvwriter.flush


  def printTablet(
    colops: List[(String, String)],
    groupby: List[String],
    data: List[(List[String], List[(String, AnyVal)])]
  ) =
    val header = groupby.map(_ => "") ++ colops.map(t => s"${t._2}(${t._1})")
    val rs = data.map((k, v) => (k ++ v.map(_._2.toString)).toArray).toList
    val colWidths = header.indices.map(i => (header(i) :: rs.map(_(i))).map(_.length).max)
    println("+" + colWidths.map(w => "-" * w).mkString("+") + "+")
    println("|" + header.indices.map(i => header(i).padTo(colWidths(i), ' ')).mkString("|") + "|")
    println("+" + colWidths.map(w => "-" * w).mkString("+") + "+")
    rs.foreach(r => println("|" +
      r.indices.map(i =>
        if i < groupby.length then
          r(i).padTo(colWidths(i), ' ')
        else
          r(i).reverse.padTo(colWidths(i), ' ').reverse
      ).mkString("|")
     + "|"))
    println("+" + colWidths.map(w => "-" * w).mkString("+") + "+")


  def findColTypes(
    data: List[Map[String, String]],
    colnames: List[String]
  ): Map[String, Int] =
    val colTypes = colnames.map(c => (c, data.map(_(c)).map(intDblStr).max))
    colTypes.toMap


  def intDblStr(s: String): Int = s match
    case reInt(_*) => 1
    case reDbl(_*) => 2
    case _ => 0
