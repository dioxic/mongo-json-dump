# MongoJsonDump

A highly performance json dump tool for MongoDB.

## Usage

```
Usage: mongojsondump [--includeSystem] [--batchSize=<batchSize>]
                     [--limit=<limit>] [-o=<outputDir>] [--uri=<uri>]
                     [--nsExclude[=<nsExclude>...]]...
      --batchSize=<batchSize>
                             cursor batch size
      --includeSystem        include system databases (config, admin, local)
      --limit=<limit>        limit the document per collection
      --nsExclude[=<nsExclude>...]
                             exclude matching namespaces
  -o, --output=<outputDir>   output directory
      --uri=<uri>            mongodb uri
```



