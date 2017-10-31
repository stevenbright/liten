
# Sample Configs

Use sample VM properties: ``-Dbrikar.settings.path=file:/home/user/opt/config/liten.properties``.

Use ``core.properties`` as a base.

# Run Intermediate Build Steps From Command Line

```
npm run-script browserify
```

and

```
npm run-script watchify
```

# Package Lock

Package lock has not been checked in (rather renamed to copy-package-lock.json) in order to prevent back and forth changes of it due to optional fsevent dependency.

Note, that npm issues useless warnings about that.

