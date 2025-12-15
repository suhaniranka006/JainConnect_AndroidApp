import os

BASE_DIR = r"app/src/main/java/com/mycompany/jainconnect"

BAD_IMPORTS = [
    "import com.mycompany.jainconnect.HorizonItem",
    "import com.mycompany.jainconnect.SunResponse",
    "import NetworkResult",
    "import com.mycompany.jainconnect.RetrofitInstance.api"
]

for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                lines = f.readlines()
            
            new_lines = []
            modified = False
            for line in lines:
                if line.strip() in BAD_IMPORTS:
                    modified = True
                    continue # Skip this line
                new_lines.append(line)
            
            if modified:
                with open(path, "w", encoding="utf-8") as f:
                    f.writelines(new_lines)
                print(f"Cleaned {file}")
