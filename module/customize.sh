#!/system/bin/sh

SKIPUNZIP=0

ui_print "- A15 AOSP Surface font bridge"
ui_print "- Checking user-supplied font payload"

FONT_DIR="$MODPATH/payload/fonts"
REGULAR="$FONT_DIR/GoogleSans-Regular.ttf"
MEDIUM="$FONT_DIR/GoogleSans-Medium.ttf"
BOLD="$FONT_DIR/GoogleSans-Bold.ttf"
ITALIC="$FONT_DIR/GoogleSans-Italic.ttf"

for required in "$REGULAR" "$MEDIUM" "$BOLD"; do
  if [ ! -s "$required" ]; then
    abort "Missing $(basename "$required"). Add the licensed font file before packaging."
  fi
done

TARGET="$MODPATH/system/fonts"
mkdir -p "$TARGET" || abort "Cannot create systemless font directory"

cp -f "$REGULAR" "$TARGET/Roboto-Regular.ttf"
cp -f "$REGULAR" "$TARGET/Roboto-Light.ttf"
cp -f "$REGULAR" "$TARGET/Roboto-Thin.ttf"
cp -f "$MEDIUM" "$TARGET/Roboto-Medium.ttf"
cp -f "$MEDIUM" "$TARGET/Roboto-Black.ttf"
cp -f "$BOLD" "$TARGET/Roboto-Bold.ttf"

if [ -s "$ITALIC" ]; then
  cp -f "$ITALIC" "$TARGET/Roboto-Italic.ttf"
fi

set_perm_recursive "$MODPATH" 0 0 0755 0644
ui_print "- Font mappings staged systemlessly"
ui_print "- Reboot after installation"
