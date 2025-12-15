import os

BASE_DIR = r"app/src/main/java/com/mycompany/jainconnect"

# List of classes that moved FROM com.mycompany.jainconnect TO a subpackage
MOVED_CLASSES = [
    "User", "Event", "Maharaj", "Tithi", "sunResponse",
    "ApiService", "RetrofitInstance", "NetworkResult",
    "JainRepository", "SessionManager", "JainViewModel",
    "EventAdapter", "TithiAdapter", "MaharajAdapter", "HorizonsAdapter",
    "MyFirebaseMessagingService",
    # Activities
    "MainActivity", "LoginActivity", "SignUpActivity", "SplashActivity", 
    "ProfileActivity", "EditProfileActivity", "TithiActivity", 
    "MaharajLocationActivity", "AddMaharajActivity", "EventActivity", 
    "AddEventActivity", "HorizonsActivity", "ContactActivity"
]

for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                lines = f.readlines()
            
            new_lines = []
            file_modified = False
            
            for line in lines:
                stripped = line.strip()
                should_remove = False
                if stripped.startswith("import com.mycompany.jainconnect."):
                    # Check if it imports one of the moved classes directly from root
                    # e.g. import com.mycompany.jainconnect.User
                    parts = stripped.split(".")
                    if len(parts) > 0:
                        last_part = parts[-1] 
                        # Only cleanup if it matches exactly "import com.mycompany.jainconnect.User" 
                        # AND we know User is moved.
                        # Be careful not to remove "com.mycompany.jainconnect.data.models.User"
                        # The prefix "com.mycompany.jainconnect." is 4 parts: com, mycompany, jainconnect, ClassName
                        
                        # Correct import: com.mycompany.jainconnect.data.models.User (5+ parts)
                        # Stale import: com.mycompany.jainconnect.User (4 parts)
                        
                        # So if the import has exactly 4 parts (com, mycompany, jainconnect, ClassName)
                        # AND ClassName is in MOVED_CLASSES -> Remove it.
                        
                        import_path = stripped.split(" ")[1] # com.mycompany.jainconnect.User
                        seg = import_path.split(".")
                        if len(seg) == 4 and seg[0]=="com" and seg[1]=="mycompany" and seg[2]=="jainconnect":
                            if seg[3] in MOVED_CLASSES:
                                should_remove = True
                
                if not should_remove:
                    new_lines.append(line)
                else:
                    file_modified = True
            
            if file_modified:
                with open(path, "w", encoding="utf-8") as f:
                    f.writelines(new_lines)
                print(f"Cleaned {file}")
