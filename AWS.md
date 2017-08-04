
Sample commands:

```
aws --profile zeus s3 cp target/liten-website-1.0.1-SNAPSHOT.jar s3://truward-backup/code/liten/liten1.jar
aws --profile zeus s3 ls s3://truward-backup/code/liten/
```

Reduced Redundancy/Infrequent Access:

```
aws --profile zeus s3 --storage-class=REDUCED_REDUNDANCY cp target/liten-website-1.0.1-SNAPSHOT.jar s3://truward-backup/code/liten/liten1.jar
aws --profile zeus s3 --storage-class=STANDARD_IA cp target/liten-website-1.0.1-SNAPSHOT.jar s3://truward-backup/code/liten/liten1.jar
```