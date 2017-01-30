

# Small DB

in ``opt/db/liten/mig``:

```
cp export.mv.db small.mv.db
```

Connect:

```
rlwrap java -cp ~/.m2/repository/com/h2database/h2/1.4.183/h2-1.4.183.jar org.h2.tools.Shell -url jdbc:h2:$HOME/opt/db/liten/mig/small -user sa
```

