import os

BASE_DIR = r"app/src/main/java/com/mycompany/jainconnect"

OLD_REF = "com.mycompany.jainconnect.utils.NetworkResult"
NEW_REF = "NetworkResult" 

for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()

            if OLD_REF in content:
                new_content = content.replace(OLD_REF, NEW_REF)
                with open(path, "w", encoding="utf-8") as f:
                    f.write(new_content)
                print(f"Updated {file}")
