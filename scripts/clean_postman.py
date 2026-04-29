import json

file_path = 'c:/Users/valen/Documents/messenger-backend/Messenger_API.postman_collection.json'

with open(file_path, 'r', encoding='utf-8') as f:
    collection = json.load(f)

def clean_collection(items):
    new_items = []
    for item in items:
        # Check if it's a folder
        if 'item' in item:
            item['item'] = clean_collection(item['item'])
            new_items.append(item)
            continue
        
        name = item.get('name', '')
        url_obj = item.get('request', {}).get('url', {})
        path = "/".join(url_obj.get('path', [])) if isinstance(url_obj, dict) else ""
        
        # 1. Delete redundant "Messenger Activity" (non-pageable version in Postman)
        # We identify it because the "Get Messenger Activity (Pageable)" already exists
        if name == "Messenger Activity" and "monitoring" in path:
            print(f"Removing redundant endpoint: {name}")
            continue
            
        # 2. Rename "(Pageable)" or "(Paginated)" versions
        if "(Pageable)" in name or "(Paginated)" in name:
            new_name = name.replace("(Pageable)", "").replace("(Paginated)", "").strip()
            print(f"Renaming '{name}' to '{new_name}'")
            item['name'] = new_name
            
        # 3. Specific renaming
        if name == "Get Trash (Deleted Services)":
             item['name'] = "Get Trash"
             print(f"Renaming '{name}' to 'Get Trash'")
        
        new_items.append(item)
    return new_items

collection['item'] = clean_collection(collection['item'])

# Update Version
collection['info']['description'] += "\n\nUpdated to Paginated Architecture v1.12.0"

with open(file_path, 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4, ensure_ascii=False)

print("Postman collection cleanup completed.")
