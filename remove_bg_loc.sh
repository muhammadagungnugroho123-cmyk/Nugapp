sed -i '/ACCESS_BACKGROUND_LOCATION/d' app/src/main/AndroidManifest.xml
sed -i '/ACCESS_BACKGROUND_LOCATION/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)/d' app/src/main/java/com/example/MainActivity.kt
