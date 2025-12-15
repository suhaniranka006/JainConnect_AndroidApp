import os
import re

BASE_DIR = r"app/src/main/java/com/mycompany/jainconnect"

# Map Identifier -> Full Package
MISSING_IMPORTS = {
    "AuthResponse": "com.mycompany.jainconnect.data.models.AuthResponse",
    "LoginRequest": "com.mycompany.jainconnect.data.models.LoginRequest",
    "SunResponse": "com.mycompany.jainconnect.data.models.SunResponse",
    "HorizonItem": "com.mycompany.jainconnect.data.models.HorizonItem",
    "DailyData": "com.mycompany.jainconnect.data.models.DailyData",
    "EventSubmissionRequest": "com.mycompany.jainconnect.data.models.EventSubmissionRequest",
    "ApiResponse": "com.mycompany.jainconnect.data.models.ApiResponse",
    "MaharajSubmissionRequest": "com.mycompany.jainconnect.data.models.MaharajSubmissionRequest",
    "RsvpResponse": "com.mycompany.jainconnect.data.models.RsvpResponse",
    "RetrofitInstance": "com.mycompany.jainconnect.data.network.RetrofitInstance",
    "NetworkResult": "com.mycompany.jainconnect.data.network.NetworkResult"
}

for root, dirs, files in os.walk(BASE_DIR):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()

            new_lines = []
            lines = content.splitlines()
            
            imports_to_add = set()
            
            # Identify which imports are needed
            for cls, full_pkg in MISSING_IMPORTS.items():
                # Check for usage
                if re.search(r'\b' + re.escape(cls) + r'\b', content):
                    # Check if already imported (basic check)
                    if f"import {full_pkg}" not in content:
                        # Check if defined in CURRENT package?
                        # I can deduce current package from path, but simplest checks:
                        # If file path package matches full_pkg package, don't import.
                        # data/models/User.kt defines AuthResponse, so it shouldn't import it.
                        
                        file_pkg_match = re.search(r'^package\s+([\w\.]+)', content)
                        file_pkg = file_pkg_match.group(1) if file_pkg_match else ""
                        target_pkg = ".".join(full_pkg.split(".")[:-1])
                        
                        if file_pkg != target_pkg:
                             imports_to_add.add(full_pkg)

            # Process lines to find insertion point
            last_import_idx = -1
            has_package = False
            
            for i, line in enumerate(lines):
                if line.strip().startswith("package "):
                    has_package = True
                    # Just keep line
                elif line.strip().startswith("import "):
                    last_import_idx = i
                
            insertion_point = last_import_idx + 1 if last_import_idx != -1 else (1 if has_package else 0)

            # Reconstruct content
            
            # Sort imports
            sorted_imports = sorted(list(imports_to_add))
            
            final_lines = lines[:]
            
            added_count = 0
            for imp in sorted_imports:
                 # Double check we aren't adding a duplicate loop
                 # (Just inserting)
                 final_lines.insert(insertion_point + added_count, f"import {imp}")
                 added_count += 1
            
            if added_count > 0:
                with open(path, "w", encoding="utf-8") as f:
                    f.write("\n".join(final_lines))
                print(f"Fixed imports for {file}")
