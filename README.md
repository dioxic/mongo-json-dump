# MongoJsonDump

A highly performance json dump tool for MongoDB.

## Usage

```
Usage: mongojsondump [--includeSystem] [--concurrency=<concurrency>]
                     [-o=<outputDir>] [--uri=<uri>] [--nsExclude
                     [=<nsExclude>...]]...
      --concurrency=<concurrency>
                             concurrency factor
      --includeSystem        include system databases (config, admin, local)
      --nsExclude[=<nsExclude>...]
                             exclude matching namespaces
  -o, --output=<outputDir>   output directory
      --uri=<uri>            mongodb uri
```
## Testing

Load 10,000,000 docs into an Atlas M30 instance (data size = 3.75GB)

Using an m3.xLarge (8 cores) AWS instance in the same region:
- mongodump took 53s (default settings)
- bsondump took 2m50s (roughly 3x the mongodump time)
- 



