from PIL import Image, ImageEnhance

# Input and Output paths
input_path = "C:/Users/suhani/.gemini/antigravity/brain/4b32224e-286b-456e-b8bf-40940ab488cd/uploaded_image_1765869002741.jpg"
output_path = "c:/Users/suhani/AndroidStudioProjects/JainConnect/app/src/main/res/drawable/app_bg.png"

try:
    # Open the image
    img = Image.open(input_path).convert("RGBA")

    # Increase brightness to make it like a watermark
    enhancer = ImageEnhance.Brightness(img)
    # Factor > 1 brightens. 2.0 is double brightness.
    # We want it very light, so maybe 1.8 brightness and reduced opacity.
    img = enhancer.enhance(1.5)

    # Reduce opacity (make it transparent/faded)
    # Create a grey layer to blend with (Light Grey: #F5F5F5 -> 245, 245, 245)
    # User asked to replace white with grey. 
    # Using (235, 235, 235) for a visible light grey.
    grey_layer = Image.new("RGBA", img.size, (235, 235, 235, 255))
    
    # Check if we can blend with alpha
    # Alternatively, just reduce alpha of the image itself but that needs a background color behind it usually.
    # The user wants "lighter". Let's blend it with white.
    # 85% grey, 15% image = very subtle watermark
    watermark = Image.blend(img, grey_layer, 0.85)
    
    # Save as PNG
    watermark.save(output_path, "PNG")
    print(f"Successfully saved watermark to {output_path}")

except Exception as e:
    print(f"Error processing image: {e}")
