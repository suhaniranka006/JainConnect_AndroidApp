import os

LAYOUT_DIR = r"app/src/main/res/layout"
ACTIVITIES = [
    "AddEventActivity", "AddMaharajActivity", "ContactActivity", 
    "EditProfileActivity", "EventActivity", "HorizonsActivity", 
    "LoginActivity", "MaharajLocationActivity", "MainActivity", 
    "ProfileActivity", "SignUpActivity", "SplashActivity", "TithiActivity"
]

for root, dirs, files in os.walk(LAYOUT_DIR):
    for file in files:
        if file.endswith(".xml"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
            
            new_content = content
            for activity in ACTIVITIES:
                # Replace short relative path path
                # e.g. .MainActivity -> .ui.activities.MainActivity
                # BUT avoid double replacement if I run twice (though strict finding helps)
                
                # Check for .ActivityName"
                if f'.{activity}"' in new_content:
                     new_content = new_content.replace(f'.{activity}"', f'.ui.activities.{activity}"')
                
                # Check for full package path if used (rare but possible)
                # com.mycompany.jainconnect.MainActivity -> ...ui.activities.MainActivity
                # I'm trusting the package is com.mycompany.jainconnect
                if f'com.mycompany.jainconnect.{activity}"' in new_content:
                     new_content = new_content.replace(f'com.mycompany.jainconnect.{activity}"', f'com.mycompany.jainconnect.ui.activities.{activity}"')

            if new_content != content:
                with open(path, "w", encoding="utf-8") as f:
                    f.write(new_content)
                print(f"Updated {file}")
