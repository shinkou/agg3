import com.github.shinkou.Agg3
import java.io.FileWriter


object AggOp extends Agg3:
  @main def main(fpath: String, args: String*): Unit =
    if 0 == args.length then
      printHelp()
      return
    val colops = args.map(_.split(":")).map(a =>
      if 1 < a.length then
        a.indices.filter(_ > 0).map(i => (a(0), a(i)))
      else
        List((a(0), "count"))
    ).flatten.toList
    val groupby = Option(sys.props("groupby")) match
      case Some(value) => value.split(",").toList
      case None => List()
    val sortby = Option(sys.props("sortby")) match
      case Some(value) => value.split(",").toList.flatMap(s =>
        val l = s.split(":").toList
        if 2 < l.size then
          l.indices.filter(_ > 0).map(i => s"${l(0)}:${l(i)}").toList
        else
          List(s)
      )
      case None => groupby
    val rs = process(colops, groupby, sortby, readCsv(fpath))
    Option(sys.props("savecsvto")) match
      case Some(s) =>
        val writer = FileWriter(s)
        printCsv(writer, colops, groupby, rs)
        writer.close
      case None =>
        printTablet(colops, groupby, rs)
