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
<img width="3883" height="2523" alt="image" src="https://github.com/user-attachments/assets/39c6d94a-b86e-41d7-b9ff-6f76541c295c" />
