
# Design Limitations

* Backup and restore should be done via REST API

# Model

```
DownloadInfo {
  originId: string,
  downloadId: string,
  downloadSize: int
}

IceItemId: IceItem {
  id: string
  type: string
  alias: string // ???
  sku: [
    {
      string id;
      title: string
      language: string

      // sku-specific
      wikipediaUrl: string

      instances: [
        {
          string id;
          created: date

          // download-specific
          downloadInfo: DownloadInfo
        } // instance
      ]
    } // sku
  ]
} // item

IceItemId: IceItemExternalId {
  externalId: string
  type: string
}
```

# API

https://github.com/JetBrains/xodus/wiki/Entity-Stores

## Get Item by ID

```
GET /catalog/v1.0/item/{id}
Takes: (string ID)
Returns: IceItem
```

## Get Name Hints

```
GET /catalog/v1.0/hints/{type}?prefix={namePrefix}
Returns: List<String>
```



