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

### LLM Requests processing architecture using jobs

<img width="2817" height="1752" alt="image" src="https://github.com/user-attachments/assets/f0d2d979-b4ad-4149-946f-4673d04c4237" />

