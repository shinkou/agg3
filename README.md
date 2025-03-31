# agg3

agg3 is a small tool which aggregates and summaries data from a given source

## How to Build

agg comes with numerous ways of building a package which fits your needs.
Thanks to [sbt-assembly][1] and [sbt-native-packager][2], whether you just
want to wrap everything in a uber JAR, or build a package which you can
install on your local machine, it can be done in a single command. The only
thing you need is to make sure you have [sbt][3] installed.

### Uber-JAR Way

Change to the top level of the checked out directory and issue the following
command:

```
sbt assembly
```

The resulting uber-JAR "agg3-assembly-x.y.z.jar" should be under the
"target/scala-3.6.4/" directory, where "x.y.z" will be the version number.

### Univeral Packager

#### Universally Executable in a ZIP

Issue this at the top level checked out directory:

```
sbt Universal/packageBin
```

A zip file "agg3-x.y.z.zip" will be generated under the "target/universal/"
directory. Simply extract it to a location you want, and use the "bin/agg3",
or "bin/agg3.bat" for Windows, executable script to run.

#### Installable Debian Package

Issue the following at the top level checked out directory:

```
sbt Debian/packageBin
```

A Debian package "agg3\_x.y.z\_all.deb" should be found under the "target"
directory.

## How to Run

The general command line to run is:

```
agg3 [ PROP ... ] CSV_FILEPATH COL_OPS1 [ COL_OPS2 [ COL_OPS3 ... ] ]
```

where

| Metavar       | Description                                                        | Examples                                     |
|---------------|--------------------------------------------------------------------|----------------------------------------------|
| CSV\_FILEPATH | file path of the CSV                                               | "../Documents/rawdata.csv"                   |
| COL\_OPS<n>   | column-operations in the form of: column\_name [ ":" operation ]\* | "order:count", "score:avg", "salary:max:min" |

Available operations are: "avg", "count", "max", "min", and "sum"

and **PROP** are

| Property Name | Description                                          | Examples                                        |
|---------------|------------------------------------------------------|-------------------------------------------------|
| groupby       | comma separated column names for grouping            | "-Dgroupby=gender", "-Dgroupby=make,model,year" |
| sortby        | comma separated column(-operation) names for sorting | "-Dsortby=gender,age:max,age:min"               |
| savecsvto     | file path to save the results as CSV                 | "-Dsavecsvto=/home/jwick/Documents/report.csv"  |

### Notes on Column-operations

When there are multiple operations attached to a column, agg will perform
the operations on that same column. If nothing is attached to a column, it
will have the default **count** operation when specifying aggregations.

Here are some examples:

| Column-operation | Result Aggregation(s)        | Remark        |
|------------------|------------------------------|---------------|
| order:count      | "count(order)"               |               |
| score:avg        | "avg(score)"                 |               |
| salary:max:min   | "max(salary)", "min(salary)" |               |
| quantity:sum     | "sum(quantity)"              |               |
| quantity         | "count(quantity)"            | not in sortby |

---
[1]: https://github.com/sbt/sbt-assembly
[2]: https://www.scala-sbt.org/sbt-native-packager/
[3]: https://www.scala-sbt.org/
