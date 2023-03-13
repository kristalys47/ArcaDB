import json

def createMetadata():
    meta = {}
    meta["files"] = []
    json_data = json.dumps(meta)

    with open('metadata.json', 'w') as f:
        f.write(json_data)
