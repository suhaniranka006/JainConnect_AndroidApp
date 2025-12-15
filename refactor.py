import os
import re

BASE_DIR = r"app/src/main/java/com/mycompany/jainconnect"
BASE_PACKAGE = "com.mycompany.jainconnect"

file_package_map = {}
class_package_map = {}

# 1. First Pass: Determine packages and built existing class map
print("Scanning files...")
for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            
            # Calculate package from path
            rel_path = os.path.relpath(root, BASE_DIR)
            if rel_path == ".":
                package = BASE_PACKAGE
            else:
                # Replace path separators with dots
                suffix = rel_path.replace(os.sep, ".")
                package = f"{BASE_PACKAGE}.{suffix}"
            
            file_package_map[path] = package
            class_name = file[:-3] # Remove .kt
            class_package_map[class_name] = package
            print(f"Found {class_name} -> {package}")

# 2. Second Pass: Update Content
print("\nUpdating files...")
for path, new_package in file_package_map.items():
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    lines = content.splitlines()
    new_lines = []
    
    # Update Package Declaration
    package_updated = False
    imports_to_add = set()
    
    if new_package != BASE_PACKAGE:
        imports_to_add.add(f"{BASE_PACKAGE}.R")
        # Check for ViewBinding usage (Activity/Fragment bindings usually)
        if "Binding" in content:
             imports_to_add.add(f"{BASE_PACKAGE}.databinding.*")


    for class_name, cls_pkg in class_package_map.items():
        if class_name == os.path.basename(path)[:-3]: continue 
        if cls_pkg == new_package: continue 
        
        if re.search(r'\b' + re.escape(class_name) + r'\b', content):
            imports_to_add.add(f"{cls_pkg}.{class_name}")

    has_package = False
    last_import_idx = -1
    
    for i, line in enumerate(lines):
        if line.strip().startswith("package "):
            new_lines.append(f"package {new_package}")
            has_package = True
        elif line.strip().startswith("import "):
            new_lines.append(line)
            last_import_idx = len(new_lines) - 1
        else:
            new_lines.append(line)

    if not has_package:
        new_lines.insert(0, f"package {new_package}")
        last_import_idx = 0 

    insertion_point = last_import_idx + 1 if last_import_idx != -1 else 1
    
    existing_imports = set()
    for line in lines:
        if line.strip().startswith("import "):
             parts = line.strip().split(" ")
             if len(parts) > 1:
                existing_imports.add(parts[1])

    sorted_imports = sorted(list(imports_to_add))
    
    added_count = 0
    for imp in sorted_imports:
        if imp not in existing_imports and not imp.endswith(".*"): # Star imports are hard to check, just adding them might duplicate but harmless-ish
           new_lines.insert(insertion_point + added_count, f"import {imp}")
           added_count += 1
        elif imp.endswith(".*") and imp not in existing_imports:
             new_lines.insert(insertion_point + added_count, f"import {imp}")
             added_count += 1
           

    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(new_lines))
    
    print(f"Updated {os.path.basename(path)}")
