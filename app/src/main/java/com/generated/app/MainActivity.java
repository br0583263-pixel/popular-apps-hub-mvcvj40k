package com.generated.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String PREFS_NAME = "popular_apps_hub";
    private static final String KEY_FAVORITES = "favorite_app_ids";

    private final List<AppItem> masterList = new ArrayList<>();
    private final List<AppItem> displayList = new ArrayList<>();

    private ListView lvApps;
    private AppAdapter adapter;
    private EditText etSearch;
    private View btnClearSearch;
    private TextView tvStats;
    private View layoutEmpty;
    private LinearLayout chipsContainer;

    private String currentCategory = "הכל";
    private String currentSearchQuery = "";
    private SharedPreferences prefs;

    // Categories definition
    private final String[] CATEGORIES = new String[]{
            "הכל",
            "⭐ מועדפים",
            "רשתות וחברה",
            "בידור ומדיה",
            "עבודה ופרודוקטיביות",
            "ניווט ותחבורה",
            "קניות ותשלומים",
            "מותקנות במכשיר"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        lvApps = findViewById(R.id.lv_apps);
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        tvStats = findViewById(R.id.tv_stats);
        layoutEmpty = findViewById(R.id.layout_empty);
        chipsContainer = findViewById(R.id.chips_container);

        setupCategoriesBar();
        setupSearch();

        adapter = new AppAdapter();
        lvApps.setAdapter(adapter);

        loadPopularApps();
        refreshInstalledStatus();
        applyFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshInstalledStatus();
        applyFilter();
    }

    private void setupCategoriesBar() {
        chipsContainer.removeAllViews();
        for (final String cat : CATEGORIES) {
            final TextView chip = new TextView(this);
            chip.setText(cat);
            chip.setTextSize(13);
            chip.setPadding(32, 16, 32, 16);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4);
            chip.setLayoutParams(params);

            updateChipStyle(chip, cat.equals(currentCategory));

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentCategory = cat;
                    updateAllChips();
                    applyFilter();
                }
            });

            chipsContainer.addView(chip);
        }
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_normal);
            chip.setTextColor(Color.parseColor("#475569"));
        }
    }

    private void updateAllChips() {
        int count = chipsContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = chipsContainer.getChildAt(i);
            if (v instanceof TextView) {
                TextView chip = (TextView) v;
                boolean isSelected = chip.getText().toString().equals(currentCategory);
                updateChipStyle(chip, isSelected);
            }
        }
    }

    private void setupSearch() {
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPopularApps() {
        masterList.clear();

        // 1. Social & Chat
        masterList.add(new AppItem("whatsapp", "WhatsApp", "com.whatsapp", "רשתות וחברה", "הודעות, שיחות וידאו וקבוצות", "https://whatsapp.com"));
        masterList.add(new AppItem("instagram", "Instagram", "com.instagram.android", "רשתות וחברה", "שיתוף תמונות, רילס וסטוריז", "https://instagram.com"));
        masterList.add(new AppItem("facebook", "Facebook", "com.facebook.katana", "רשתות וחברה", "הרשת החברתית המובילה בעולם", "https://facebook.com"));
        masterList.add(new AppItem("tiktok", "TikTok", "com.zhiliaoapp.musically", "רשתות וחברה", "סרטונים קצרים ומוזיקה", "https://tiktok.com"));
        masterList.add(new AppItem("telegram", "Telegram", "org.telegram.messenger", "רשתות וחברה", "הודעות מהירות ומאובטחות", "https://telegram.org"));
        masterList.add(new AppItem("twitter", "X (Twitter)", "com.twitter.android", "רשתות וחברה", "חדשות, ציוצים ועדכונים בזמן אמת", "https://twitter.com"));
        masterList.add(new AppItem("snapchat", "Snapchat", "com.snapchat.android", "רשתות וחברה", "תמונות עם פילטרים והודעות נעלמות", "https://snapchat.com"));
        masterList.add(new AppItem("linkedin", "LinkedIn", "com.linkedin.android", "רשתות וחברה", "רשת חברתית מקצועית וחיפוש עבודה", "https://linkedin.com"));
        masterList.add(new AppItem("discord", "Discord", "com.discord", "רשתות וחברה", "צ'אט קולי וקהילות גיימינג", "https://discord.com"));
        masterList.add(new AppItem("reddit", "Reddit", "com.reddit.frontpage", "רשתות וחברה", "קהילות, פורומים ודיונים", "https://reddit.com"));

        // 2. Entertainment & Media
        masterList.add(new AppItem("youtube", "YouTube", "com.google.android.youtube", "בידור ומדיה", "סרטוני וידאו, מוזיקה ושידורים חיים", "https://youtube.com"));
        masterList.add(new AppItem("spotify", "Spotify", "com.spotify.music", "בידור ומדיה", "הזרמת מוזיקה, פלייליסטים ופודקאסטים", "https://spotify.com"));
        masterList.add(new AppItem("netflix", "Netflix", "com.netflix.mediaclient", "בידור ומדיה", "סדרות, סרטים ותכנים מקוריים", "https://netflix.com"));
        masterList.add(new AppItem("disneyplus", "Disney+", "com.disney.disneyplus", "בידור ומדיה", "סרטי דיסני, מארוול ומלחמת הכוכבים", "https://disneyplus.com"));
        masterList.add(new AppItem("twitch", "Twitch", "tv.twitch.android.app", "בידור ומדיה", "שידורים חיים של משחקים ותוכן", "https://twitch.tv"));
        masterList.add(new AppItem("shazam", "Shazam", "com.shazam.android", "בידור ומדיה", "זיהוי שירים ומוזיקה בשניות", "https://shazam.com"));
        masterList.add(new AppItem("ytmusic", "YouTube Music", "com.google.android.apps.youtube.music", "בידור ומדיה", "אפליקציית המוזיקה הרשמית של יוטיוב", "https://music.youtube.com"));

        // 3. Productivity & Tools
        masterList.add(new AppItem("chrome", "Google Chrome", "com.android.chrome", "עבודה ופרודוקטיביות", "דפדפן האינטרנט הפופולרי והמהיר", "https://google.com/chrome"));
        masterList.add(new AppItem("gmail", "Gmail", "com.google.android.gm", "עבודה ופרודוקטיביות", "דואר אלקטרוני מהיר ובטוח מבית גוגל", "https://mail.google.com"));
        masterList.add(new AppItem("drive", "Google Drive", "com.google.android.apps.docs", "עבודה ופרודוקטיביות", "אחסון קבצים, מסמכים וגיבוי בענן", "https://drive.google.com"));
        masterList.add(new AppItem("zoom", "Zoom", "us.zoom.videomeetings", "עבודה ופרודוקטיביות", "פגישות וידאו, שיחות ועידה ווובינרים", "https://zoom.us"));
        masterList.add(new AppItem("teams", "Microsoft Teams", "com.microsoft.teams", "עבודה ופרודוקטיביות", "עבודת צוות, צ'אט ופגישות עבודה", "https://teams.microsoft.com"));
        masterList.add(new AppItem("keep", "Google Keep", "com.google.android.keep", "עבודה ופרודוקטיביות", "פתקים, תזכורות ורשימות קניות", "https://keep.google.com"));
        masterList.add(new AppItem("chatgpt", "ChatGPT", "com.openai.chatgpt", "עבודה ופרודוקטיביות", "עוזר בינה מלאכותית חכם מבית OpenAI", "https://chat.openai.com"));

        // 4. Navigation & Transport
        masterList.add(new AppItem("waze", "Waze", "com.waze", "ניווט ותחבורה", "ניווט חכם עם דיווחי תנועה בזמן אמת", "https://waze.com"));
        masterList.add(new AppItem("maps", "Google Maps", "com.google.android.apps.maps", "ניווט ותחבורה", "מפות עולמיות, מסלולים ומקומות מומלצים", "https://maps.google.com"));
        masterList.add(new AppItem("moovit", "Moovit", "com.tranzmate", "ניווט ותחבורה", "לוחות זמנים לתחבורה ציבורית, אוטובוסים ורכבות", "https://moovitapp.com"));
        masterList.add(new AppItem("gett", "Gett", "com.gettaxi.android", "ניווט ותחבורה", "הזמנת מוניות ושליחויות בלחיצה", "https://gett.com"));
        masterList.add(new AppItem("uber", "Uber", "com.ubercab", "ניווט ותחבורה", "נסיעות שיתופיות ושירותי הסעות", "https://uber.com"));

        // 5. Shopping & Finance
        masterList.add(new AppItem("bit", "Bit", "com.bnhp.wallet", "קניות ותשלומים", "העברת כספים ותשלום מהיר בנייד", "https://bitpay.co.il"));
        masterList.add(new AppItem("paybox", "PayBox", "com.payboxapp", "קניות ותשלומים", "איסוף כסף קבוצתי ותשלומים דיגיטליים", "https://payboxapp.com"));
        masterList.add(new AppItem("aliexpress", "AliExpress", "com.alibaba.aliexpresshd", "קניות ותשלומים", "קניות אונליין, מבצעים ומגוון מוצרים", "https://aliexpress.com"));
        masterList.add(new AppItem("amazon", "Amazon", "com.amazon.mShop.android.shopping", "קניות ותשלומים", "קניות מקוונות ומשלוחים בינלאומיים", "https://amazon.com"));
        masterList.add(new AppItem("shein", "SHEIN", "com.zzkko", "קניות ותשלומים", "אופנה, בגדים ואקססוריז במחירים נוחים", "https://shein.com"));

        // Add additional installed apps discovered on device
        scanAndAppendDeviceApps();
    }

    private void scanAndAppendDeviceApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<android.content.pm.ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        Set<String> existingPackages = new HashSet<>();
        for (AppItem item : masterList) {
            existingPackages.add(item.packageName);
        }

        for (android.content.pm.ResolveInfo ri : resolveInfos) {
            if (ri.activityInfo != null && !ri.activityInfo.packageName.equals(getPackageName())) {
                String pkg = ri.activityInfo.packageName;
                if (!existingPackages.contains(pkg)) {
                    String label = ri.loadLabel(pm).toString();
                    AppItem deviceApp = new AppItem(
                            pkg,
                            label,
                            pkg,
                            "מותקנות במכשיר",
                            "אפליקציה המותקנת במכשיר שלך",
                            ""
                    );
                    deviceApp.isSystemApp = true;
                    deviceApp.isInstalled = true;
                    deviceApp.resolvedIcon = ri.loadIcon(pm);
                    masterList.add(deviceApp);
                    existingPackages.add(pkg);
                }
            }
        }
    }

    private void refreshInstalledStatus() {
        PackageManager pm = getPackageManager();
        Set<String> favoriteIds = prefs.getStringSet(KEY_FAVORITES, new HashSet<String>());

        for (AppItem app : masterList) {
            app.isFavorite = favoriteIds.contains(app.id);

            if (!app.isSystemApp) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(app.packageName, 0);
                    app.isInstalled = (ai != null);
                    if (app.isInstalled && app.resolvedIcon == null) {
                        app.resolvedIcon = pm.getApplicationIcon(ai);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    app.isInstalled = false;
                }
            }
        }
    }

    private void applyFilter() {
        displayList.clear();

        for (AppItem app : masterList) {
            // Category filter
            boolean matchesCat = false;
            if ("הכל".equals(currentCategory)) {
                matchesCat = true;
            } else if ("⭐ מועדפים".equals(currentCategory)) {
                matchesCat = app.isFavorite;
            } else if ("מותקנות במכשיר".equals(currentCategory)) {
                matchesCat = app.isInstalled;
            } else {
                matchesCat = app.category.equals(currentCategory);
            }

            if (!matchesCat) {
                continue;
            }

            // Search filter
            if (!currentSearchQuery.isEmpty()) {
                boolean matchesSearch = app.name.toLowerCase().contains(currentSearchQuery)
                        || app.description.toLowerCase().contains(currentSearchQuery)
                        || app.category.toLowerCase().contains(currentSearchQuery);
                if (!matchesSearch) {
                    continue;
                }
            }

            displayList.add(app);
        }

        // Sort: Favorites first, then installed apps, then alphabetical
        Collections.sort(displayList, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem a, AppItem b) {
                if (a.isFavorite != b.isFavorite) {
                    return a.isFavorite ? -1 : 1;
                }
                if (a.isInstalled != b.isInstalled) {
                    return a.isInstalled ? -1 : 1;
                }
                return a.name.compareToIgnoreCase(b.name);
            }
        });

        // Update UI
        adapter.notifyDataSetChanged();
        if (displayList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            lvApps.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            lvApps.setVisibility(View.VISIBLE);
        }

        tvStats.setText(displayList.size() + " " + getString(R.string.apps_found_suffix));
    }

    private void toggleFavorite(AppItem item) {
        Set<String> currentFavs = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<String>()));
        if (item.isFavorite) {
            currentFavs.remove(item.id);
            item.isFavorite = false;
        } else {
            currentFavs.add(item.id);
            item.isFavorite = true;
        }
        prefs.edit().putStringSet(KEY_FAVORITES, currentFavs).apply();
        applyFilter();
    }

    private void launchOrInstallApp(AppItem item) {
        if (item.isInstalled) {
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(item.packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(this, "לא ניתן להפעיל אפליקציה זו", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Open in Google Play Store
            try {
                Intent playIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + item.packageName));
                playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(playIntent);
            } catch (Exception e) {
                // Fallback to Web Play Store or Web fallback url
                String webUrl = (item.webFallbackUrl != null && !item.webFallbackUrl.isEmpty())
                        ? item.webFallbackUrl
                        : "https://play.google.com/store/apps/details?id=" + item.packageName;
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                    startActivity(browserIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "שגיאה בפתיחת החנות", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class AppAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return displayList.size();
        }

        @Override
        public AppItem getItem(int position) {
            return displayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_app, parent, false);
                holder = new ViewHolder();
                holder.tvName = convertView.findViewById(R.id.tv_app_name);
                holder.tvBadge = convertView.findViewById(R.id.tv_badge);
                holder.tvCategory = convertView.findViewById(R.id.tv_category);
                holder.tvDescription = convertView.findViewById(R.id.tv_description);
                holder.ivIcon = convertView.findViewById(R.id.iv_icon);
                holder.btnAction = convertView.findViewById(R.id.btn_action);
                holder.btnFavorite = convertView.findViewById(R.id.btn_favorite);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final AppItem item = getItem(position);

            holder.tvName.setText(item.name);
            holder.tvCategory.setText(item.category);
            holder.tvDescription.setText(item.description);

            // Icon
            if (item.resolvedIcon != null) {
                holder.ivIcon.setImageDrawable(item.resolvedIcon);
            } else {
                // Generate a colored letter icon badge
                holder.ivIcon.setImageDrawable(createLetterIcon(item.name));
            }

            // Favorite state
            if (item.isFavorite) {
                holder.btnFavorite.setTextColor(Color.parseColor("#F59E0B"));
                holder.btnFavorite.setText("★");
            } else {
                holder.btnFavorite.setTextColor(Color.parseColor("#CBD5E1"));
                holder.btnFavorite.setText("☆");
            }

            holder.btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFavorite(item);
                }
            });

            // Installed vs Store state
            if (item.isInstalled) {
                holder.tvBadge.setText(R.string.status_badge_installed);
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_installed);
                holder.tvBadge.setTextColor(Color.parseColor("#059669"));

                holder.btnAction.setText(R.string.status_installed);
                holder.btnAction.setBackgroundResource(R.drawable.btn_action_installed);
            } else {
                holder.tvBadge.setText(R.string.status_badge_store);
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_store);
                holder.tvBadge.setTextColor(Color.parseColor("#4338CA"));

                holder.btnAction.setText(R.string.status_get);
                holder.btnAction.setBackgroundResource(R.drawable.btn_action_store);
            }

            holder.btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchOrInstallApp(item);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchOrInstallApp(item);
                }
            });

            return convertView;
        }

        private Drawable createLetterIcon(String name) {
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(16);

            int hash = Math.abs(name.hashCode());
            int[] palette = new int[]{
                    Color.parseColor("#3B82F6"),
                    Color.parseColor("#EC4899"),
                    Color.parseColor("#10B981"),
                    Color.parseColor("#F59E0B"),
                    Color.parseColor("#8B5CF6"),
                    Color.parseColor("#06B6D4"),
                    Color.parseColor("#EF4444")
            };
            int color = palette[hash % palette.length];
            shape.setColor(color);
            return shape;
        }
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvBadge;
        TextView tvCategory;
        TextView tvDescription;
        ImageView ivIcon;
        Button btnAction;
        TextView btnFavorite;
    }
}
