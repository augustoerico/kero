## Connect

```shell
mongo "mongodb+srv://kero-skxx1.mongodb.net/test" --username <user>
```

## Import products

```shell
mongoimport <file_path> --collection products --uri mongodb+srv://<user>:<password>@kero-skxx1.mongodb.net/test \
    --jsonArray
```
