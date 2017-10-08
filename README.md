# Kubera

Kubera is a automatic backup system for PostgreSQL databases that uploads the backups to Dropbox.


## Usage

Configuration is based on ENV vars:
* `DATABASES` holds a space-separated list of DB urls that will be backed up.
* `SCHEDULE` should be a string in Quartz cron expression format: http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html
* `DROPBOX_TOKEN` should be a Dropbox application token: https://www.dropbox.com/developers/apps

### Example

```bash
$ export DATABASES="postgres://example.com/database1 postgres://example.com/database2"
$ export SCHEDULE="0 0 * * * ?" # Do one backup per hour
$ export DROPBOX_TOKEN="SAMPLE_TOKEN"
$ java -jar kubera-clojure-0.1.0-standalone.jar 
```

## License

See `LICENSE`.
