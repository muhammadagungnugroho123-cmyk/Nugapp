import urllib.request
import json
import math

url = "https://raw.githubusercontent.com/johan/world.geo.json/master/countries.geo.json"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())

def simplify_polygon(points, tolerance):
    # Very simple bounding box filter or just keep every Nth point to reduce size,
    # or implement a simple Visvalingam-Whyatt or Douglas-Peucker.
    # For now, just keep every 2nd point to reduce size if large, but geo.json is already somewhat simple.
    if len(points) < 4:
        return points
    result = [points[0]]
    for i in range(1, len(points)-1, 2):
        result.append(points[i])
    result.append(points[-1])
    return result

polygons = []
for feature in data['features']:
    geom = feature.get('geometry')
    if not geom: continue
    typ = geom['type']
    coords = geom['coordinates']
    
    if typ == 'Polygon':
        for ring in coords:
            # We only care about exterior rings (the first one) for simplicity, or all rings.
            # ring is list of [lon, lat]
            polygons.append(ring)
    elif typ == 'MultiPolygon':
        for poly in coords:
            for ring in poly:
                polygons.append(ring)

# Filter out very small polygons (islands) to save memory
filtered_polygons = [p for p in polygons if len(p) > 10]

# Generate Kotlin file
kt_code = "package com.example.ui.screens.worldclock\n\n"
kt_code += "object WorldMapData {\n"
kt_code += "    // Array of polygons, each polygon is a FloatArray [lon1, lat1, lon2, lat2, ...]\n"
kt_code += "    val polygons: Array<FloatArray> = arrayOf(\n"

for i, poly in enumerate(filtered_polygons):
    poly = simplify_polygon(poly, 0.5)
    poly = simplify_polygon(poly, 0.5) # simplify again
    if len(poly) < 4: continue
    
    kt_code += "        floatArrayOf("
    # format nicely
    flat = []
    for pt in poly:
        flat.append(f"{pt[0]:.2f}f")
        flat.append(f"{pt[1]:.2f}f")
    kt_code += ", ".join(flat)
    kt_code += ")"
    if i < len(filtered_polygons) - 1:
        kt_code += ",\n"
    else:
        kt_code += "\n"

kt_code += "    )\n"
kt_code += "}\n"

with open("app/src/main/java/com/example/ui/screens/worldclock/WorldMapData.kt", "w") as f:
    f.write(kt_code)

print(f"Generated {len(filtered_polygons)} polygons")
