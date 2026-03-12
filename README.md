# Quronest Backend


## Run the project

**Step 1:** Configure localhost.quronest.com

Edit your `/etc/hosts` file to add a line:

```
127.0.0.1       localhost.quronest.com
```

**Step 2:** Run dev setup with
```
make dev
```


When initializing / updating the database schema, you need to update liquibase:

```
make liquibase
```