package dev.helixoff.a15launcher;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class MainActivity extends Activity {
    private static final int REQUEST_FONT = 41;
    private final List<AppEntry> allApps = new ArrayList<>();
    private AppAdapter adapter;
    private EditText search;
    private Typeface uiTypeface;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        configureSystemBars();
        uiTypeface = loadTypeface();
        setContentView(buildHome());
        loadApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) loadApps();
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(getColor(R.color.launcher_background));
        window.setDecorFitsSystemWindows(false);
    }

    private View buildHome() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(16), pad, dp(12));
        root.setBackgroundColor(getColor(R.color.launcher_background));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(pad, bars.top + dp(12), pad, bars.bottom + dp(8));
            return insets;
        });

        TextView clock = textView(42, true);
        clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        root.addView(clock, matchWrap());

        TextView date = textView(16, false);
        date.setText(DateFormat.getDateInstance(DateFormat.FULL).format(new Date()));
        date.setAlpha(0.72f);
        root.addView(date, matchWrap());

        TextView fontAction = textView(13, true);
        fontAction.setText(R.string.import_font);
        fontAction.setGravity(Gravity.END);
        fontAction.setPadding(dp(8), dp(8), dp(4), dp(8));
        fontAction.setOnClickListener(view -> chooseFont());
        root.addView(fontAction, matchWrap());

        search = new EditText(this);
        search.setSingleLine(true);
        search.setHint(R.string.search_hint);
        search.setTextColor(getColor(R.color.launcher_on_surface));
        search.setHintTextColor(withAlpha(getColor(R.color.launcher_on_surface), 145));
        search.setTypeface(uiTypeface);
        search.setTextSize(16);
        search.setPadding(dp(20), 0, dp(20), 0);
        GradientDrawable searchBackground = new GradientDrawable();
        searchBackground.setColor(getColor(R.color.launcher_surface));
        searchBackground.setCornerRadius(dp(28));
        search.setBackground(searchBackground);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        searchParams.setMargins(0, dp(24), 0, dp(16));
        root.addView(search, searchParams);

        GridView grid = new GridView(this);
        grid.setNumColumns(4);
        grid.setGravity(Gravity.CENTER);
        grid.setHorizontalSpacing(dp(8));
        grid.setVerticalSpacing(dp(12));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setClipToPadding(false);
        adapter = new AppAdapter();
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((parent, view, position, id) -> launch(adapter.visible.get(position)));
        grid.setOnItemLongClickListener((parent, view, position, id) -> {
            openAppInfo(adapter.visible.get(position));
            return true;
        });
        root.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        search.setOnEditorActionListener((view, actionId, event) -> {
            if (!search.getText().toString().trim().isEmpty() && adapter.visible.isEmpty()) {
                webSearch(search.getText().toString().trim());
                return true;
            }
            return false;
        });
        return root;
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> matches = pm.queryIntentActivities(query, 0);
        allApps.clear();
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.equals(getPackageName())) continue;
            allApps.add(new AppEntry(
                    info.loadLabel(pm).toString(),
                    info.activityInfo.packageName,
                    info.activityInfo.name,
                    info.loadIcon(pm)));
        }
        Collections.sort(allApps, (left, right) -> left.label.compareToIgnoreCase(right.label));
        adapter.filter(search == null ? "" : search.getText().toString());
    }

    private void launch(AppEntry app) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setClassName(app.packageName, app.activityName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "App is no longer available", Toast.LENGTH_SHORT).show();
            loadApps();
        }
    }

    private void openAppInfo(AppEntry app) {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + app.packageName)));
    }

    private void webSearch(String query) {
        try {
            startActivity(new Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, query));
        } catch (ActivityNotFoundException exception) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))));
        }
    }

    private void chooseFont() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES,
                        new String[]{"font/ttf", "font/otf", "application/x-font-ttf", "application/octet-stream"});
        startActivityForResult(intent, REQUEST_FONT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_FONT || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        File target = new File(getFilesDir(), "launcher-font");
        try (InputStream input = getContentResolver().openInputStream(data.getData());
             FileOutputStream output = new FileOutputStream(target, false)) {
            if (input == null) throw new IllegalArgumentException("Font stream unavailable");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            Typeface.createFromFile(target);
            Toast.makeText(this, R.string.font_applied, Toast.LENGTH_SHORT).show();
            recreate();
        } catch (Exception exception) {
            target.delete();
            Toast.makeText(this, R.string.font_failed, Toast.LENGTH_LONG).show();
        }
    }

    private Typeface loadTypeface() {
        File custom = new File(getFilesDir(), "launcher-font");
        if (custom.isFile()) {
            try { return Typeface.createFromFile(custom); }
            catch (RuntimeException ignored) { custom.delete(); }
        }
        return Typeface.create("sans-serif", Typeface.NORMAL);
    }

    private TextView textView(int sp, boolean bold) {
        TextView view = new TextView(this);
        view.setTextColor(getColor(R.color.launcher_on_surface));
        view.setTextSize(sp);
        view.setTypeface(uiTypeface, bold ? Typeface.BOLD : Typeface.NORMAL);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static final class AppEntry {
        final String label;
        final String packageName;
        final String activityName;
        final android.graphics.drawable.Drawable icon;

        AppEntry(String label, String packageName, String activityName,
                 android.graphics.drawable.Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.activityName = activityName;
            this.icon = icon;
        }
    }

    private final class AppAdapter extends BaseAdapter {
        final List<AppEntry> visible = new ArrayList<>();

        void filter(String rawQuery) {
            String query = rawQuery.trim().toLowerCase(Locale.getDefault());
            visible.clear();
            for (AppEntry app : allApps) {
                if (query.isEmpty() || app.label.toLowerCase(Locale.getDefault()).contains(query)) {
                    visible.add(app);
                }
            }
            notifyDataSetChanged();
        }

        @Override public int getCount() { return visible.size(); }
        @Override public AppEntry getItem(int position) { return visible.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View recycled, ViewGroup parent) {
            LinearLayout cell;
            if (recycled instanceof LinearLayout) {
                cell = (LinearLayout) recycled;
            } else {
                cell = new LinearLayout(MainActivity.this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setPadding(dp(4), dp(6), dp(4), dp(6));

                ImageView icon = new ImageView(MainActivity.this);
                icon.setTag("icon");
                cell.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

                TextView label = textView(12, false);
                label.setTag("label");
                label.setGravity(Gravity.CENTER);
                label.setMaxLines(2);
                LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                labelParams.topMargin = dp(5);
                cell.addView(label, labelParams);
            }
            AppEntry app = getItem(position);
            ImageView icon = cell.findViewWithTag("icon");
            TextView label = cell.findViewWithTag("label");
            icon.setImageDrawable(app.icon);
            label.setText(app.label);
            return cell;
        }
    }
}
